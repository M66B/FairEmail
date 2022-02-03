package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public abstract class BinaryConverter {

	static final JsonReader.ReadObject<byte[]> Base64Reader = new JsonReader.ReadObject<byte[]>() {
		@Nullable
		@Override
		public byte[] read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserialize(reader);
		}
	};
	static final JsonWriter.WriteObject<byte[]> Base64Writer = new JsonWriter.WriteObject<byte[]>() {
		@Override
		public void write(JsonWriter writer, @Nullable byte[] value) {
			serialize(value, writer);
		}
	};

	public static void serialize(@Nullable final byte[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("\"\"");
		} else {
			sw.writeBinary(value);
		}
	}

	public static byte[] deserialize(final JsonReader reader) throws IOException {
		return reader.readBase64();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<byte[]> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(Base64Reader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<byte[]> res) throws IOException {
		reader.deserializeCollection(Base64Reader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<byte[]> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(Base64Reader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<byte[]> res) throws IOException {
		reader.deserializeNullableCollection(Base64Reader, res);
	}
}
