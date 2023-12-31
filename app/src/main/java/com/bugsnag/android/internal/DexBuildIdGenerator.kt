package com.bugsnag.android.internal

import android.content.pm.ApplicationInfo
import androidx.annotation.VisibleForTesting
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.experimental.xor

internal object DexBuildIdGenerator {
    private const val MAGIC_NUMBER_BYTE_COUNT = 8
    private const val CHECKSUM_BYTE_COUNT = 4
    private const val SIGNATURE_START_BYTE = MAGIC_NUMBER_BYTE_COUNT + CHECKSUM_BYTE_COUNT
    private const val SIGNATURE_BYTE_COUNT = 20

    private const val HEADER_SIZE =
        MAGIC_NUMBER_BYTE_COUNT + CHECKSUM_BYTE_COUNT + SIGNATURE_BYTE_COUNT

    fun generateBuildId(appInfo: ApplicationInfo): String? {
        @Suppress("SwallowedException") // this is deliberate
        return try {
            unsafeGenerateBuildId(appInfo)?.toHexString()
        } catch (ex: Throwable) {
            null
        }
    }

    private fun unsafeGenerateBuildId(appInfo: ApplicationInfo): ByteArray? {
        val apk = File(appInfo.sourceDir)

        // we can't read the APK
        if (!apk.canRead()) {
            return null
        }

        return generateApkBuildId(apk)
    }

    @VisibleForTesting
    internal fun generateApkBuildId(apk: File): ByteArray? {
        ZipFile(apk, ZipFile.OPEN_READ).use { zip ->
            var dexEntry = zip.getEntry("classes.dex") ?: return null
            val buildId = signatureFromZipEntry(zip, dexEntry) ?: return null

            // search for any other classes(N).dex files and merge the signatures together
            var dexFileIndex = 2

            // removing the second break would only create noise in this loop
            @Suppress("LoopWithTooManyJumpStatements")
            while (true) {
                dexEntry = zip.getEntry("classes$dexFileIndex.dex") ?: break
                val secondarySignature = signatureFromZipEntry(zip, dexEntry) ?: break
                mergeSignatureInfoBuildId(buildId, secondarySignature)

                dexFileIndex++
            }

            return buildId
        }
    }

    private fun mergeSignatureInfoBuildId(buildId: ByteArray, signature: ByteArray) {
        for (i in buildId.indices) {
            buildId[i] = buildId[i] xor signature[i]
        }
    }

    private fun signatureFromZipEntry(zip: ZipFile, dexEntry: ZipEntry): ByteArray? {
        // read the byte[20] signature from the dex file header, after validating the magic number
        // https://source.android.com/docs/core/runtime/dex-format#header-item

        return zip.getInputStream(dexEntry).use { input ->
            val header = ByteArray(HEADER_SIZE)
            if (input.read(header, 0, HEADER_SIZE) == HEADER_SIZE) {
                extractDexSignature(header)
            } else {
                null
            }
        }
    }

    @VisibleForTesting
    internal fun extractDexSignature(header: ByteArray): ByteArray? {
        return if (!validateHeader(header)) {
            null
        } else {
            return header.copyOfRange(
                SIGNATURE_START_BYTE,
                SIGNATURE_START_BYTE + SIGNATURE_BYTE_COUNT
            )
        }
    }

    @Suppress("MagicNumber", "ReturnCount")
    private fun validateHeader(header: ByteArray): Boolean {
        // https://source.android.com/docs/core/runtime/dex-format#dex-file-magic
        if (header[0].toInt() and 0xff != 0x64) return false
        if (header[1].toInt() and 0xff != 0x65) return false
        if (header[2].toInt() and 0xff != 0x78) return false
        if (header[3].toInt() and 0xff != 0x0a) return false

        // we skip the version digits
        // the magic number ends in a 0
        if (header[7].toInt() and 0xff != 0) return false

        return true
    }
}
