package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * DslJson writes JSON into JsonWriter which has two primary modes of operation:
 *
 *  * targeting specific output stream
 *  * buffering the entire response in memory
 *
 * In both cases JsonWriter writes into an byte[] buffer.
 * If stream is used as target, it will copy buffer into the stream whenever there is no more room in buffer for new data.
 * If stream is not used as target, it will grow the buffer to hold the encoded result.
 * To use stream as target reset(OutputStream) must be called before processing.
 * This class provides low level methods for JSON serialization.
 * <p>
 * After the processing is done,
 * in case then stream was used as target, flush() must be called to copy the remaining of the buffer into stream.
 * When entire response was buffered in memory, buffer can be copied to stream or resulting byte[] can be used directly.
 * <p>
 * For maximum performance JsonWriter instances should be reused (to avoid allocation of new byte[] buffer instances).
 * They should not be shared across threads (concurrently) so for Thread reuse it's best to use patterns such as ThreadLocal.
 */
@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public final class JsonWriter {

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	final byte[] ensureCapacity(final int free) {
		if (position + free >= buffer.length) {
			enlargeOrFlush(position, free);
		}
		return buffer;
	}

	void advance(int size) {
		position += size;
	}

	private int position;
	private long flushed;
	private OutputStream target;
	private byte[] buffer;

	private final UnknownSerializer unknownSerializer;
	private final Grisu3.FastDtoaBuilder doubleBuilder = new Grisu3.FastDtoaBuilder();

	/**
	 * Prefer creating JsonWriter through DslJson#newWriter
	 * This instance is safe to use when all type information is known and lookups to custom writers is not required.
	 */
	@Deprecated
	public JsonWriter() {
		this(512, null);
	}

	JsonWriter(@Nullable final UnknownSerializer unknownSerializer) {
		this(512, unknownSerializer);
	}

	JsonWriter(final int size, @Nullable final UnknownSerializer unknownSerializer) {
		this(new byte[size], unknownSerializer);
	}

	JsonWriter(final byte[] buffer, @Nullable final UnknownSerializer unknownSerializer) {
		this.buffer = buffer;
		this.unknownSerializer = unknownSerializer;
	}

	/**
	 * Helper for writing JSON object start: {
	 */
	public static final byte OBJECT_START = '{';
	/**
	 * Helper for writing JSON object end: }
	 */
	public static final byte OBJECT_END = '}';
	/**
	 * Helper for writing JSON array start: [
	 */
	public static final byte ARRAY_START = '[';
	/**
	 * Helper for writing JSON array end: ]
	 */
	public static final byte ARRAY_END = ']';
	/**
	 * Helper for writing comma separator: ,
	 */
	public static final byte COMMA = ',';
	/**
	 * Helper for writing semicolon: :
	 */
	public static final byte SEMI = ':';
	/**
	 * Helper for writing JSON quote: "
	 */
	public static final byte QUOTE = '"';
	/**
	 * Helper for writing JSON escape: \\
	 */
	public static final byte ESCAPE = '\\';

	private void enlargeOrFlush(final int size, final int padding) {
		if (target != null) {
			try {
				target.write(buffer, 0, size);
			} catch (IOException ex) {
				throw new SerializationException("Unable to write to target stream.", ex);
			}
			position = 0;
			flushed += size;
			if (padding > buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length + buffer.length / 2 + padding);
			}
		} else {
			buffer = Arrays.copyOf(buffer, buffer.length + buffer.length / 2 + padding);
		}
	}

	/**
	 * Optimized method for writing 'null' into the JSON.
	 */
	public final void writeNull() {
		if ((position + 4) >= buffer.length) {
			enlargeOrFlush(position, 0);
		}
		final int s = position;
		final byte[] _result = buffer;
		_result[s] = 'n';
		_result[s + 1] = 'u';
		_result[s + 2] = 'l';
		_result[s + 3] = 'l';
		position += 4;
	}

	/**
	 * Write a single byte into the JSON.
	 *
	 * @param value byte to write into the JSON
	 */
	public final void writeByte(final byte value) {
		if (position == buffer.length) {
			enlargeOrFlush(position, 0);
		}
		buffer[position++] = value;
	}

	/**
	 * Write a quoted string into the JSON.
	 * String will be appropriately escaped according to JSON escaping rules.
	 *
	 * @param value string to write
	 */
	public final void writeString(final String value) {
		final int len = value.length();
		if (position + (len << 2) + (len << 1) + 2 >= buffer.length) {
			enlargeOrFlush(position, (len << 2) + (len << 1) + 2);
		}
		final byte[] _result = buffer;
		_result[position] = QUOTE;
		int cur = position + 1;
		for (int i = 0; i < len; i++) {
			final char c = value.charAt(i);
			if (c > 31 && c != '"' && c != '\\' && c < 126) {
				_result[cur++] = (byte) c;
			} else {
				writeQuotedString(value, i, cur, len);
				return;
			}
		}
		_result[cur] = QUOTE;
		position = cur + 1;
	}

	/**
	 * Write a quoted string into the JSON.
	 * Char sequence will be appropriately escaped according to JSON escaping rules.
	 *
	 * @param value char sequence to write
	 */
	public final void writeString(final CharSequence value) {
		final int len = value.length();
		if (position + (len << 2) + (len << 1) + 2 >= buffer.length) {
			enlargeOrFlush(position, (len << 2) + (len << 1) + 2);
		}
		final byte[] _result = buffer;
		_result[position] = QUOTE;
		int cur = position + 1;
		for (int i = 0; i < len; i++) {
			final char c = value.charAt(i);
			if (c > 31 && c != '"' && c != '\\' && c < 126) {
				_result[cur++] = (byte) c;
			} else {
				writeQuotedString(value, i, cur, len);
				return;
			}
		}
		_result[cur] = QUOTE;
		position = cur + 1;
	}

	private void writeQuotedString(final CharSequence str, int i, int cur, final int len) {
		final byte[] _result = this.buffer;
		for (; i < len; i++) {
			final char c = str.charAt(i);
			if (c == '"') {
				_result[cur++] = ESCAPE;
				_result[cur++] = QUOTE;
			} else if (c == '\\') {
				_result[cur++] = ESCAPE;
				_result[cur++] = ESCAPE;
			} else if (c < 32) {
				if (c == 8) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'b';
				} else if (c == 9) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 't';
				} else if (c == 10) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'n';
				} else if (c == 12) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'f';
				} else if (c == 13) {
					_result[cur++] = ESCAPE;
					_result[cur++] = 'r';
				} else {
					_result[cur] = ESCAPE;
					_result[cur + 1] = 'u';
					_result[cur + 2] = '0';
					_result[cur + 3] = '0';
					switch (c) {
						case 0:
							_result[cur + 4] = '0';
							_result[cur + 5] = '0';
							break;
						case 1:
							_result[cur + 4] = '0';
							_result[cur + 5] = '1';
							break;
						case 2:
							_result[cur + 4] = '0';
							_result[cur + 5] = '2';
							break;
						case 3:
							_result[cur + 4] = '0';
							_result[cur + 5] = '3';
							break;
						case 4:
							_result[cur + 4] = '0';
							_result[cur + 5] = '4';
							break;
						case 5:
							_result[cur + 4] = '0';
							_result[cur + 5] = '5';
							break;
						case 6:
							_result[cur + 4] = '0';
							_result[cur + 5] = '6';
							break;
						case 7:
							_result[cur + 4] = '0';
							_result[cur + 5] = '7';
							break;
						case 11:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'B';
							break;
						case 14:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'E';
							break;
						case 15:
							_result[cur + 4] = '0';
							_result[cur + 5] = 'F';
							break;
						case 16:
							_result[cur + 4] = '1';
							_result[cur + 5] = '0';
							break;
						case 17:
							_result[cur + 4] = '1';
							_result[cur + 5] = '1';
							break;
						case 18:
							_result[cur + 4] = '1';
							_result[cur + 5] = '2';
							break;
						case 19:
							_result[cur + 4] = '1';
							_result[cur + 5] = '3';
							break;
						case 20:
							_result[cur + 4] = '1';
							_result[cur + 5] = '4';
							break;
						case 21:
							_result[cur + 4] = '1';
							_result[cur + 5] = '5';
							break;
						case 22:
							_result[cur + 4] = '1';
							_result[cur + 5] = '6';
							break;
						case 23:
							_result[cur + 4] = '1';
							_result[cur + 5] = '7';
							break;
						case 24:
							_result[cur + 4] = '1';
							_result[cur + 5] = '8';
							break;
						case 25:
							_result[cur + 4] = '1';
							_result[cur + 5] = '9';
							break;
						case 26:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'A';
							break;
						case 27:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'B';
							break;
						case 28:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'C';
							break;
						case 29:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'D';
							break;
						case 30:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'E';
							break;
						default:
							_result[cur + 4] = '1';
							_result[cur + 5] = 'F';
							break;
					}
					cur += 6;
				}
			} else if (c < 0x007F) {
				_result[cur++] = (byte) c;
			} else {
				final int cp = Character.codePointAt(str, i);
				if (Character.isSupplementaryCodePoint(cp)) {
					i++;
				}
				if (cp == 0x007F) {
					_result[cur++] = (byte) cp;
				} else if (cp <= 0x7FF) {
					_result[cur++] = (byte) (0xC0 | ((cp >> 6) & 0x1F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFF)) {
					_result[cur++] = (byte) (0xE0 | ((cp >> 12) & 0x0F));
					_result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else if (cp >= 0x10000 && cp <= 0x10FFFF) {
					_result[cur++] = (byte) (0xF0 | ((cp >> 18) & 0x07));
					_result[cur++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
					_result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
					_result[cur++] = (byte) (0x80 | (cp & 0x3F));
				} else {
					throw new SerializationException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
				}
			}
		}
		_result[cur] = QUOTE;
		position = cur + 1;
	}

	/**
	 * Write string consisting of only ascii characters.
	 * String will not be escaped according to JSON escaping rules.
	 *
	 * @param value ascii string
	 */
	@SuppressWarnings("deprecation")
	public final void writeAscii(final String value) {
		final int len = value.length();
		if (position + len >= buffer.length) {
			enlargeOrFlush(position, len);
		}
		value.getBytes(0, len, buffer, position);
		position += len;
	}

	/**
	 * Write part of string consisting of only ascii characters.
	 * String will not be escaped according to JSON escaping rules.
	 *
	 * @param value ascii string
	 * @param len   part of the provided string to use
	 */
	@SuppressWarnings("deprecation")
	public final void writeAscii(final String value, final int len) {
		if (position + len >= buffer.length) {
			enlargeOrFlush(position, len);
		}
		value.getBytes(0, len, buffer, position);
		position += len;
	}

	/**
	 * Copy bytes into JSON as is.
	 * Provided buffer can't be null.
	 *
	 * @param buf byte buffer to copy
	 */
	public final void writeAscii(final byte[] buf) {
		final int len = buf.length;
		if (position + len >= buffer.length) {
			enlargeOrFlush(position, len);
		}
		final int p = position;
		final byte[] _result = buffer;
		for (int i = 0; i < buf.length; i++) {
			_result[p + i] = buf[i];
		}
		position += len;
	}

	/**
	 * Copy part of byte buffer into JSON as is.
	 * Provided buffer can't be null.
	 *
	 * @param buf byte buffer to copy
	 * @param len part of buffer to copy
	 */
	public final void writeAscii(final byte[] buf, final int len) {
		if (position + len >= buffer.length) {
			enlargeOrFlush(position, len);
		}
		final int p = position;
		final byte[] _result = buffer;
		for (int i = 0; i < len; i++) {
			_result[p + i] = buf[i];
		}
		position += len;
	}

	/**
	 * Copy part of byte buffer into JSON as is.
	 * Provided buffer can't be null.
	 *
	 * @param buf    byte buffer to copy
	 * @param offset in buffer to start from
	 * @param len    part of buffer to copy
	 */
	public final void writeRaw(final byte[] buf, final int offset, final int len) {
		if (position + len >= buffer.length) {
			enlargeOrFlush(position, len);
		}
		System.arraycopy(buf, offset, buffer, position, len);
		position += len;
	}

	/**
	 * Encode bytes as Base 64.
	 * Provided value can't be null.
	 *
	 * @param value bytes to encode
	 */
	public final void writeBinary(final byte[] value) {
		if (position + (value.length << 1) + 2 >= buffer.length) {
			enlargeOrFlush(position, (value.length << 1) + 2);
		}
		buffer[position++] = '"';
		position += Base64.encodeToBytes(value, buffer, position);
		buffer[position++] = '"';
	}

	final void writeDouble(final double value) {
		if (value == Double.POSITIVE_INFINITY) {
			writeAscii("\"Infinity\"");
		} else if (value == Double.NEGATIVE_INFINITY) {
			writeAscii("\"-Infinity\"");
		} else if (value != value) {
			writeAscii("\"NaN\"");
		} else if (value == 0.0) {
			writeAscii("0.0");
		} else {
			if (Grisu3.tryConvert(value, doubleBuilder)) {
				if (position + 24 >= buffer.length) {
					enlargeOrFlush(position, 24);
				}
				final int len = doubleBuilder.copyTo(buffer, position);
				position += len;
			} else {
				writeAscii(Double.toString(value));
			}
		}
	}

	@Override
	public String toString() {
		return new String(buffer, 0, position, UTF_8);
	}

	/**
	 * Content of buffer can be copied to another array of appropriate size.
	 * This method can't be used when targeting output stream.
	 * Ideally it should be avoided if possible, since it will create an array copy.
	 * It's better to use getByteBuffer and size instead.
	 *
	 * @return copy of the buffer up to the current position
	 */
	public final byte[] toByteArray() {
		if (target != null) {
			throw new ConfigurationException("Method is not available when targeting stream");
		}
		return Arrays.copyOf(buffer, position);
	}

	/**
	 * When JsonWriter does not target stream, this method should be used to copy content of the buffer into target stream.
	 * It will also reset the buffer position to 0 so writer can be continued to be used even without a call to reset().
	 *
	 * @param stream target stream
	 * @throws IOException propagates from stream.write
	 */
	public final void toStream(final OutputStream stream) throws IOException {
		if (target != null) {
			throw new ConfigurationException("Method should not be used when targeting streams. Instead use flush() to copy what's remaining in the buffer");
		}
		stream.write(buffer, 0, position);
		flushed += position;
		position = 0;
	}

	/**
	 * Current buffer.
	 * If buffer grows, a new instance will be created and old one will not be used anymore.
	 *
	 * @return current buffer
	 */
	public final byte[] getByteBuffer() {
		return buffer;
	}

	/**
	 * Current position in the buffer. When stream is not used, this is also equivalent
	 * to the size of the resulting JSON in bytes
	 *
	 * @return position in the populated buffer
	 */
	public final int size() {
		return position;
	}

	/**
	 * Total bytes currently flushed to stream
	 *
	 * @return bytes flushed
	 */
	public final long flushed() {
		return flushed;
	}

	/**
	 * Resets the writer - same as calling reset(OutputStream = null)
	 */
	public final void reset() {
		reset(null);
	}

	/**
	 * Resets the writer - specifies the target stream and sets the position in buffer to 0.
	 * If stream is set to null, JsonWriter will work in growing byte[] buffer mode (entire response will be buffered in memory).
	 *
	 * @param stream sets/clears the target stream
	 */
	public final void reset(@Nullable OutputStream stream) {
		position = 0;
		target = stream;
		flushed = 0;
	}

	/**
	 * If stream was used, copies the buffer to stream and resets the position in buffer to 0.
	 * It will not reset the stream as target,
	 * meaning new usages of the JsonWriter will try to use the already provided stream.
	 * It will not do anything if stream was not used
	 * <p>
	 * To reset the stream to null use reset() or reset(OutputStream) methods.
	 */
	public final void flush() {
		if (target != null && position != 0) {
			try {
				target.write(buffer, 0, position);
			} catch (IOException ex) {
				throw new SerializationException("Unable to write to target stream.", ex);
			}
			flushed += position;
			position = 0;
		}
	}

	/**
	 * This is deprecated method which exists only for backward compatibility
	 *
	 * @throws java.io.IOException unable to write to target stream
	 */
	@Deprecated
	public void close() throws IOException {
		if (target != null && position != 0) {
			target.write(buffer, 0, position);
			position = 0;
			flushed = 0;
		}
	}

	/**
	 * Custom objects can be serialized based on the implementation specified through this interface.
	 * Annotation processor creates custom deserializers at compile time and registers them into DslJson.
	 *
	 * @param <T> type
	 */
	public interface WriteObject<T> {
		void write(JsonWriter writer, @Nullable T value);
	}

	/**
	 * Convenience method for serializing array of JsonObject's.
	 * Array can't be null nor can't contain null values (it will result in NullPointerException).
	 *
	 * @param array input objects
	 * @param <T>   type of objects
	 */
	public <T extends JsonObject> void serialize(final T[] array) {
		writeByte(ARRAY_START);
		if (array.length != 0) {
			array[0].serialize(this, false);
			for (int i = 1; i < array.length; i++) {
				writeByte(COMMA);
				array[i].serialize(this, false);
			}
		}
		writeByte(ARRAY_END);
	}

	/**
	 * Convenience method for serializing only part of JsonObject's array.
	 * Useful when array is reused and only part of it needs to be serialized.
	 * Array can't be null nor can't contain null values (it will result in NullPointerException).
	 *
	 * @param array input objects
	 * @param len   size of array which should be serialized
	 * @param <T>   type of objects
	 */
	public <T extends JsonObject> void serialize(final T[] array, final int len) {
		writeByte(ARRAY_START);
		if (array.length != 0 && len != 0) {
			array[0].serialize(this, false);
			for (int i = 1; i < len; i++) {
				writeByte(COMMA);
				array[i].serialize(this, false);
			}
		}
		writeByte(ARRAY_END);
	}

	/**
	 * Convenience method for serializing list of JsonObject's.
	 * List can't be null nor can't contain null values (it will result in NullPointerException).
	 * It will use list .get(index) method to access the object.
	 * When using .get(index) is not appropriate,
	 * it's better to call the serialize(Collection&lt;JsonObject&gt;) method instead.
	 *
	 * @param list input objects
	 * @param <T>  type of objects
	 */
	public <T extends JsonObject> void serialize(final List<T> list) {
		writeByte(ARRAY_START);
		if (list.size() != 0) {
			list.get(0).serialize(this, false);
			for (int i = 1; i < list.size(); i++) {
				writeByte(COMMA);
				list.get(i).serialize(this, false);
			}
		}
		writeByte(ARRAY_END);
	}

	/**
	 * Convenience method for serializing array through instance serializer (WriteObject).
	 * Array can be null and can contain null values.
	 * Instance serializer will not be invoked for null values
	 *
	 * @param array   array to serialize
	 * @param encoder instance serializer
	 * @param <T>     type of object
	 */
	public <T> void serialize(@Nullable final T[] array, final WriteObject<T> encoder) {
		if (array == null) {
			writeNull();
			return;
		}
		writeByte(ARRAY_START);
		if (array.length != 0) {
			T item = array[0];
			if (item != null) {
				encoder.write(this, item);
			} else {
				writeNull();
			}
			for (int i = 1; i < array.length; i++) {
				writeByte(COMMA);
				item = array[i];
				if (item != null) {
					encoder.write(this, item);
				} else {
					writeNull();
				}
			}
		}
		writeByte(ARRAY_END);
	}

	/**
	 * Convenience method for serializing list through instance serializer (WriteObject).
	 * List can be null and can contain null values.
	 * Instance serializer will not be invoked for null values
	 * It will use list .get(index) method to access the object.
	 * When using .get(index) is not appropriate,
	 * it's better to call the serialize(Collection&lt;JsonObject&gt;, WriteObject) method instead.
	 *
	 * @param list    list to serialize
	 * @param encoder instance serializer
	 * @param <T>     type of object
	 */
	public <T> void serialize(@Nullable final List<T> list, final WriteObject<T> encoder) {
		if (list == null) {
			writeNull();
			return;
		}
		writeByte(ARRAY_START);
		if (!list.isEmpty()) {
			if (list instanceof RandomAccess) {
				T item = list.get(0);
				if (item != null) {
					encoder.write(this, item);
				} else {
					writeNull();
				}
				for (int i = 1; i < list.size(); i++) {
					writeByte(COMMA);
					item = list.get(i);
					if (item != null) {
						encoder.write(this, item);
					} else {
						writeNull();
					}
				}
			} else {
				Iterator<T> iter = list.iterator();
				T item = iter.next();
				if (item != null) {
					encoder.write(this, item);
				} else {
					writeNull();
				}
				while (iter.hasNext()) {
					writeByte(COMMA);
					item = iter.next();
					if (item != null) {
						encoder.write(this, item);
					} else {
						writeNull();
					}
				}
			}
		}
		writeByte(ARRAY_END);
	}

	public void serializeRaw(@Nullable final List list, final WriteObject encoder) {
		serialize(list, encoder);
	}

	/**
	 * Convenience method for serializing collection through instance serializer (WriteObject).
	 * Collection can be null and can contain null values.
	 * Instance serializer will not be invoked for null values
	 *
	 * @param collection collection to serialize
	 * @param encoder instance serializer
	 * @param <T> type of object
	 */
	public <T> void serialize(@Nullable final Collection<T> collection, final WriteObject<T> encoder) {
		if (collection == null) {
			writeNull();
			return;
		}
		writeByte(ARRAY_START);
		if (!collection.isEmpty()) {
			final Iterator<T> it = collection.iterator();
			T item = it.next();
			if (item != null) {
				encoder.write(this, item);
			} else {
				writeNull();
			}
			while (it.hasNext()) {
				writeByte(COMMA);
				item = it.next();
				if (item != null) {
					encoder.write(this, item);
				} else {
					writeNull();
				}
			}
		}
		writeByte(ARRAY_END);
	}

	public void serializeRaw(@Nullable final Collection collection, final WriteObject encoder) {
		serialize(collection, encoder);
	}

	public <K, V> void serialize(@Nullable final Map<K, V> map, final WriteObject<K> keyEncoder, final WriteObject<V> valueEncoder) {
		if (map == null) {
			writeNull();
			return;
		}
		writeByte(OBJECT_START);
		final int size = map.size();
		if (size > 0) {
			final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
			Map.Entry<K, V> kv = iterator.next();
			writeQuoted(keyEncoder, kv.getKey());
			writeByte(SEMI);
			valueEncoder.write(this, kv.getValue());
			for (int i = 1; i < size; i++) {
				writeByte(COMMA);
				kv = iterator.next();
				writeQuoted(keyEncoder, kv.getKey());
				writeByte(SEMI);
				valueEncoder.write(this, kv.getValue());
			}
		}
		writeByte(OBJECT_END);
	}

	public void serializeRaw(@Nullable final Map map, final WriteObject keyEncoder, final WriteObject valueEncoder) {
		serialize(map, keyEncoder, valueEncoder);
	}

	public <T> void writeQuoted(final JsonWriter.WriteObject<T> keyWriter, final T key) {
		if (key instanceof Double) {
			final double value = (Double) key;
			if (Double.isNaN(value)) writeAscii("\"NaN\"");
			else if (value == Double.POSITIVE_INFINITY) writeAscii("\"Infinity\"");
			else if (value == Double.NEGATIVE_INFINITY) writeAscii("\"-Infinity\"");
			else {
				writeByte(QUOTE);
				NumberConverter.serialize(value, this);
				writeByte(QUOTE);
			}
		} else if (key instanceof Float) {
			final float value = (Float) key;
			if (Float.isNaN(value)) writeAscii("\"NaN\"");
			else if (value == Float.POSITIVE_INFINITY) writeAscii("\"Infinity\"");
			else if (value == Float.NEGATIVE_INFINITY) writeAscii("\"-Infinity\"");
			else {
				writeByte(QUOTE);
				NumberConverter.serialize(value, this);
				writeByte(QUOTE);
			}
		} else if (key instanceof Number) {
			writeByte(QUOTE);
			keyWriter.write(this, key);
			writeByte(QUOTE);
		} else {
			keyWriter.write(this, key);
		}
	}

	/**
	 * Generic object serializer which is used for "unknown schema" objects.
	 * It will throw SerializationException in case if it doesn't know how to serialize provided instance.
	 * Will delegate the serialization to UnknownSerializer, which in most cases is the DslJson instance from which the writer was created.
	 * This enables it to use DslJson configuration and serialize using custom serializers (when they are provided).
	 *
	 * @param value instance to serialize
	 */
	public void serializeObject(@Nullable final Object value) {
		if (value == null) {
			writeNull();
		} else if (unknownSerializer != null) {
			try {
				unknownSerializer.serialize(this, value);
			} catch (IOException ex) { //serializing unknown stuff can fail in various ways ;(
				throw new SerializationException(ex);
			}
		} else {
			throw new ConfigurationException("Unable to serialize: " + value.getClass() + ".\n" +
					"Check that JsonWriter was created through DslJson#newWriter.");
		}
	}
}
