package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public abstract class NumberConverter {

	public final static short[] SHORT_EMPTY_ARRAY = new short[0];
	public final static int[] INT_EMPTY_ARRAY = new int[0];
	public final static long[] LONG_EMPTY_ARRAY = new long[0];
	public final static float[] FLOAT_EMPTY_ARRAY = new float[0];
	public final static double[] DOUBLE_EMPTY_ARRAY = new double[0];
	public final static Short SHORT_ZERO = 0;
	public final static Integer INT_ZERO = 0;
	public final static Long LONG_ZERO = 0L;
	public final static Float FLOAT_ZERO = 0f;
	public final static Double DOUBLE_ZERO = 0.0;

	private final static int[] DIGITS = new int[1000];
	private final static int[] DIFF = {111, 222, 444, 888, 1776};
	private final static int[] ERROR = {50, 100, 200, 400, 800};
	private final static int[] SCALE_10 = {10000, 1000, 100, 10, 1};
	private final static double[] POW_10 = {
			1e1,  1e2,  1e3,  1e4,  1e5, 1e6, 1e7, 1e8,  1e9,
			1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19,
			1e20, 1e21, 1e22, 1e23, 1e24, 1e25, 1e26, 1e27, 1e28, 1e29,
			1e30, 1e31, 1e32, 1e33, 1e34, 1e35, 1e36, 1e37, 1e38, 1e39,
			1e40, 1e41, 1e42, 1e43, 1e44, 1e45, 1e46, 1e47, 1e48, 1e49,
			1e50, 1e51, 1e52, 1e53, 1e54, 1e55, 1e56, 1e57, 1e58, 1e59,
			1e60, 1e61, 1e62, 1e63, 1e64, 1e65
	};
	public static final JsonReader.ReadObject<Double> DOUBLE_READER = new JsonReader.ReadObject<Double>() {
		@Nullable
		@Override
		public Double read(JsonReader reader) throws IOException {
			return deserializeDouble(reader);
		}
	};
	public static final JsonReader.ReadObject<Double> NULLABLE_DOUBLE_READER = new JsonReader.ReadObject<Double>() {
		@Nullable
		@Override
		public Double read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeDouble(reader);
		}
	};
	public static final JsonWriter.WriteObject<Double> DOUBLE_WRITER = new JsonWriter.WriteObject<Double>() {
		@Override
		public void write(JsonWriter writer, @Nullable Double value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<double[]> DOUBLE_ARRAY_READER = new JsonReader.ReadObject<double[]>() {
		@Nullable
		@Override
		public double[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.newParseError("Expecting '[' for double array start");
			reader.getNextToken();
			return deserializeDoubleArray(reader);
		}
	};
	public static final JsonWriter.WriteObject<double[]> DOUBLE_ARRAY_WRITER = new JsonWriter.WriteObject<double[]>() {
		@Override
		public void write(JsonWriter writer, @Nullable double[] value) {
			serialize(value, writer);
		}
	};

	public static final JsonReader.ReadObject<Float> FLOAT_READER = new JsonReader.ReadObject<Float>() {
		@Override
		public Float read(JsonReader reader) throws IOException {
			return deserializeFloat(reader);
		}
	};
	public static final JsonReader.ReadObject<Float> NULLABLE_FLOAT_READER = new JsonReader.ReadObject<Float>() {
		@Nullable
		@Override
		public Float read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeFloat(reader);
		}
	};
	public static final JsonWriter.WriteObject<Float> FLOAT_WRITER = new JsonWriter.WriteObject<Float>() {
		@Override
		public void write(JsonWriter writer, @Nullable Float value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<float[]> FLOAT_ARRAY_READER = new JsonReader.ReadObject<float[]>() {
		@Nullable
		@Override
		public float[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.newParseError("Expecting '[' for float array start");
			reader.getNextToken();
			return deserializeFloatArray(reader);
		}
	};
	public static final JsonWriter.WriteObject<float[]> FLOAT_ARRAY_WRITER = new JsonWriter.WriteObject<float[]>() {
		@Override
		public void write(JsonWriter writer, @Nullable float[] value) {
			serialize(value, writer);
		}
	};
	public static final JsonReader.ReadObject<Integer> INT_READER = new JsonReader.ReadObject<Integer>() {
		@Override
		public Integer read(JsonReader reader) throws IOException {
			return deserializeInt(reader);
		}
	};
	public static final JsonReader.ReadObject<Integer> NULLABLE_INT_READER = new JsonReader.ReadObject<Integer>() {
		@Nullable
		@Override
		public Integer read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeInt(reader);
		}
	};
	public static final JsonWriter.WriteObject<Integer> INT_WRITER = new JsonWriter.WriteObject<Integer>() {
		@Override
		public void write(JsonWriter writer, @Nullable Integer value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<int[]> INT_ARRAY_READER = new JsonReader.ReadObject<int[]>() {
		@Nullable
		@Override
		public int[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.newParseError("Expecting '[' for int array start");
			reader.getNextToken();
			return deserializeIntArray(reader);
		}
	};
	public static final JsonWriter.WriteObject<int[]> INT_ARRAY_WRITER = new JsonWriter.WriteObject<int[]>() {
		@Override
		public void write(JsonWriter writer, @Nullable int[] value) {
			serialize(value, writer);
		}
	};
	public static final JsonReader.ReadObject<Short> SHORT_READER = new JsonReader.ReadObject<Short>() {
		@Override
		public Short read(JsonReader reader) throws IOException {
			return deserializeShort(reader);
		}
	};
	public static final JsonReader.ReadObject<Short> NULLABLE_SHORT_READER = new JsonReader.ReadObject<Short>() {
		@Nullable
		@Override
		public Short read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeShort(reader);
		}
	};
	public static final JsonWriter.WriteObject<Short> SHORT_WRITER = new JsonWriter.WriteObject<Short>() {
		@Override
		public void write(JsonWriter writer, @Nullable Short value) {
			if (value == null) writer.writeNull();
			else serialize(value.intValue(), writer);
		}
	};
	public static final JsonReader.ReadObject<short[]> SHORT_ARRAY_READER = new JsonReader.ReadObject<short[]>() {
		@Nullable
		@Override
		public short[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.newParseError("Expecting '[' for short array start");
			reader.getNextToken();
			return deserializeShortArray(reader);
		}
	};
	public static final JsonWriter.WriteObject<short[]> SHORT_ARRAY_WRITER = new JsonWriter.WriteObject<short[]>() {
		@Override
		public void write(JsonWriter writer, @Nullable short[] value) {
			serialize(value, writer);
		}
	};

	public static final JsonReader.ReadObject<Long> LONG_READER = new JsonReader.ReadObject<Long>() {
		@Override
		public Long read(JsonReader reader) throws IOException {
			return deserializeLong(reader);
		}
	};
	public static final JsonReader.ReadObject<Long> NULLABLE_LONG_READER = new JsonReader.ReadObject<Long>() {
		@Nullable
		@Override
		public Long read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeLong(reader);
		}
	};
	public static final JsonWriter.WriteObject<Long> LONG_WRITER = new JsonWriter.WriteObject<Long>() {
		@Override
		public void write(JsonWriter writer, @Nullable Long value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonReader.ReadObject<long[]> LONG_ARRAY_READER = new JsonReader.ReadObject<long[]>() {
		@Nullable
		@Override
		public long[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.newParseError("Expecting '[' for long array start");
			reader.getNextToken();
			return deserializeLongArray(reader);
		}
	};
	public static final JsonWriter.WriteObject<long[]> LONG_ARRAY_WRITER = new JsonWriter.WriteObject<long[]>() {
		@Override
		public void write(JsonWriter writer, @Nullable long[] value) {
			serialize(value, writer);
		}
	};

	public static final JsonReader.ReadObject<BigDecimal> DecimalReader = new JsonReader.ReadObject<BigDecimal>() {
		@Nullable
		@Override
		public BigDecimal read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeDecimal(reader);
		}
	};
	public static final JsonWriter.WriteObject<BigDecimal> DecimalWriter = new JsonWriter.WriteObject<BigDecimal>() {
		@Override
		public void write(JsonWriter writer, @Nullable BigDecimal value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<Number> NumberReader = new JsonReader.ReadObject<Number>() {
		@Nullable
		@Override
		public Number read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeNumber(reader);
		}
	};

	static {
		for (int i = 0; i < DIGITS.length; i++) {
			DIGITS[i] = (i < 10 ? (2 << 24) : i < 100 ? (1 << 24) : 0)
					+ (((i / 100) + '0') << 16)
					+ ((((i / 10) % 10) + '0') << 8)
					+ i % 10 + '0';
		}
	}

	static void write4(final int value, final byte[] buf, final int pos) {
		if (value > 9999) {
			throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + value);
		}
		final int q = value / 1000;
		final int v = DIGITS[value - q * 1000];
		buf[pos] = (byte) (q + '0');
		buf[pos + 1] = (byte) (v >> 16);
		buf[pos + 2] = (byte) (v >> 8);
		buf[pos + 3] = (byte) v;
	}

	static void write3(final int number, final byte[] buf, int pos) {
		final int v = DIGITS[number];
		buf[pos] = (byte) (v >> 16);
		buf[pos + 1] = (byte) (v >> 8);
		buf[pos + 2] = (byte) v;
	}

	static void write2(final int value, final byte[] buf, final int pos) {
		final int v = DIGITS[value];
		buf[pos] = (byte) (v >> 8);
		buf[pos + 1] = (byte) v;
	}

	static int read2(final char[] buf, final int pos) {
		final int v1 = buf[pos] - 48;
		return (v1 << 3) + (v1 << 1) + buf[pos + 1] - 48;
	}

	static int read4(final char[] buf, final int pos) {
		final int v2 = buf[pos + 1] - 48;
		final int v3 = buf[pos + 2] - 48;
		return (buf[pos] - 48) * 1000
				+ (v2 << 6) + (v2 << 5) + (v2 << 2)
				+ (v3 << 3) + (v3 << 1)
				+ buf[pos + 3] - 48;
	}

	static void numberException(final JsonReader reader, final int start, final int end, String message) throws ParsingException {
		final int len = end - start;
		if (len > reader.maxNumberDigits) {
			throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
		}
		throw reader.newParseErrorWith("Error parsing number", len, "", message, null, ". Error parsing number");
	}

	static void numberException(final JsonReader reader, final int start, final int end, String message, Object messageArgument) throws ParsingException {
		final int len = end - start;
		if (len > reader.maxNumberDigits) {
			throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
		}
		throw reader.newParseErrorWith("Error parsing number", len, "", message, messageArgument, ". Error parsing number");
	}

	public static void serializeNullable(@Nullable final Double value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	private static BigDecimal parseNumberGeneric(final char[] buf, final int len, final JsonReader reader, final boolean withQuotes) throws ParsingException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		if (end > reader.maxNumberDigits) {
			throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
		}
		final int offset = buf[0] == '-' ? 1 : 0;
		if (buf[offset] == '0' && end > offset + 1 && buf[offset + 1] >= '0' && buf[offset + 1] <= '9') {
			throw reader.newParseErrorAt("Leading zero is not allowed. Error parsing number", len + (withQuotes ? 2 : 0));
		}
		try {
			return new BigDecimal(buf, 0, end);
		} catch (NumberFormatException nfe) {
			throw reader.newParseErrorAt("Error parsing number", len + (withQuotes ? 2 : 0), nfe);
		}
	}

	public static void serialize(final double value, final JsonWriter sw) {
		sw.writeDouble(value);
	}

	public static void serialize(@Nullable final double[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for (int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	private static class NumberInfo {
		final char[] buffer;
		final int length;

		NumberInfo(final char[] buffer, final int length) {
			this.buffer = buffer;
			this.length = length;
		}
	}

	private static NumberInfo readLongNumber(final JsonReader reader, final int start) throws IOException {
		int len = reader.length() - start;
		char[] result = reader.prepareBuffer(start, len);
		while (reader.length() == reader.getCurrentIndex()) {
			if (reader.isEndOfStream()) break;
			reader.scanNumber(); // peek, do not read
			int end = reader.getCurrentIndex();
			int oldLen = len;
			len += end;
			if (len > reader.maxNumberDigits) {
				throw reader.newParseErrorFormat("Too many digits detected in number", len, "Number of digits larger than %d. Unable to read number", reader.maxNumberDigits);
			}
			char[] tmp = result;
			result = new char[len];
			System.arraycopy(tmp, 0, result, 0, oldLen);
			System.arraycopy(reader.prepareBuffer(0, end), 0, result, oldLen, end);
		}
		return new NumberInfo(result, len);
	}

	public static double deserializeDouble(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			return parseDoubleGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true);
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return -parseDouble(buf, reader, start, end, 1);
		}
		return parseDouble(buf, reader, start, end, 0);
	}

	private static double parseDouble(final byte[] buf, final JsonReader reader, final int start, final int end, final int offset) throws IOException {
		if (end - start - offset > reader.doubleLengthLimit) {
			if (end == reader.length()) {
				final NumberInfo tmp = readLongNumber(reader, start + offset);
				return parseDoubleGeneric(tmp.buffer, tmp.length, reader, false);
			}
			return parseDoubleGeneric(reader.prepareBuffer(start + offset, end - start - offset), end - start - offset, reader, false);
		}
		long value = 0;
		byte ch = ' ';
		int i = start + offset;
		final boolean leadingZero = buf[start + offset] == 48;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + offset + 1) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (i > start + offset && reader.allWhitespace(i, end)) return value;
				numberException(reader, start, end, "Unknown digit", (char)ch);
			}
			value = (value << 3) + (value << 1) + ind;
		}
		if (i == start + offset) numberException(reader, start, end, "Digit not found");
		else if (leadingZero && ch != '.' && i > start + offset + 1) numberException(reader, start, end, "Leading zero is not allowed");
		else if (i == end) return value;
		else if (ch == '.') {
			i++;
			if (i == end) numberException(reader, start, end, "Number ends with a dot");
			final int maxLen;
			final double preciseDividor;
			final int expDiff;
			final int decPos = i;
			final int decOffset;
			if (value == 0) {
				maxLen = i + 15;
				ch = buf[i];
				if (ch == '0' && end > maxLen) {
					return parseDoubleGeneric(reader.prepareBuffer(start + offset, end - start - offset), end - start - offset, reader, false);
				} else if (ch < '8') {
					preciseDividor = 1e14;
					expDiff = -1;
					decOffset = 1;
				} else {
					preciseDividor = 1e15;
					expDiff = 0;
					decOffset = 0;
				}
			} else {
				maxLen = start + offset + 16;
				if (buf[start + offset] < '8') {
					preciseDividor = 1e14;
					expDiff = i - maxLen + 14;
					decOffset = 1;
				} else {
					preciseDividor = 1e15;
					expDiff = i - maxLen + 15;
					decOffset = 0;
				}
			}
			final int numLimit = maxLen < end ? maxLen : end;
			//TODO zeros
			for (; i < numLimit; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return value / POW_10[i - decPos - 1];
					numberException(reader, start, end, "Unknown digit", (char)buf[i]);
				}
				value = (value << 3) + (value << 1) + ind;
			}
			if (i == end) return value / POW_10[i - decPos - 1];
			else if (ch == 'e' || ch == 'E') {
				return doubleExponent(reader, value, i - decPos,0, buf, start, end, offset, i);
			}
			if (reader.doublePrecision == JsonReader.DoublePrecision.HIGH) {
				return parseDoubleGeneric(reader.prepareBuffer(start + offset, end - start - offset), end - start - offset, reader, false);
			}
			int decimals = 0;
			final int decLimit = start + offset + 18 < end ? start + offset + 18 : end;
			final int remPos = i;
			for(;i < decLimit; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) {
						return approximateDouble(decimals, value / preciseDividor, i - remPos - decOffset);
					}
					numberException(reader, start, end, "Unknown digit", (char)buf[i]);
				}
				decimals = (decimals << 3) + (decimals << 1) + ind;
			}
			final double number = approximateDouble(decimals, value / preciseDividor, i - remPos - decOffset);
			while (i < end && ch >= '0' && ch <= '9') {
				ch = buf[i++];
			}
			if (ch == 'e' || ch == 'E') {
				return doubleExponent(reader, 0, expDiff, number, buf, start, end, offset, i);
			} else if (expDiff > 0) {
				return number * POW_10[expDiff - 1];
			} else if (expDiff < 0) {
				return number / POW_10[-expDiff - 1];
			} else {
				return number;
			}
		} else if (ch == 'e' || ch == 'E') {
			return doubleExponent(reader, value, 0, 0, buf, start, end, offset, i);
		}
		return value;
	}

	private static double approximateDouble(final int decimals, final double precise, final int digits) {
		final long bits = Double.doubleToRawLongBits(precise);
		final int exp = (int)(bits >> 52) - 1022;
		final int missing = (decimals * SCALE_10[digits + 1] + ERROR[exp]) / DIFF[exp];
		return Double.longBitsToDouble(bits + missing);
	}

	private static double doubleExponent(JsonReader reader, final long whole, final int decimals, double fraction, byte[] buf, int start, int end, int offset, int i) throws IOException {
		if (reader.doublePrecision == JsonReader.DoublePrecision.EXACT) {
			return parseDoubleGeneric(reader.prepareBuffer(start + offset, end - start - offset), end - start - offset, reader, false);
		}
		byte ch;
		ch = buf[++i];
		final int exp;
		if (ch == '-') {
			exp = parseNegativeInt(buf, reader, i, end) - decimals;
		} else if (ch == '+') {
			exp = parsePositiveInt(buf, reader, i, end, 1) - decimals;
		} else {
			exp = parsePositiveInt(buf, reader, i, end, 0) - decimals;
		}
		if (fraction == 0) {
			if (exp == 0 || whole == 0) return whole;
			else if (exp > 0 && exp < POW_10.length) return whole * POW_10[exp - 1];
			else if (exp < 0 && -exp < POW_10.length) return whole / POW_10[-exp - 1];
			else if (reader.doublePrecision != JsonReader.DoublePrecision.HIGH) {
				if (exp > 0 && exp < 300) return whole * Math.pow(10, exp);
				else if (exp > -300 && exp < 0) return whole / Math.pow(10, exp);
			}
		} else {
			if (exp == 0) return whole + fraction;
			else if (exp > 0 && exp < POW_10.length) return fraction * POW_10[exp - 1] + whole * POW_10[exp - 1];
			else if (exp < 0 && -exp < POW_10.length) return fraction / POW_10[-exp - 1] + whole / POW_10[-exp - 1];
			else if (reader.doublePrecision != JsonReader.DoublePrecision.HIGH) {
				if (exp > 0 && exp < 300) return whole * Math.pow(10, exp);
				else if (exp > -300 && exp < 0) return whole / Math.pow(10, exp);
			}
		}
		return parseDoubleGeneric(reader.prepareBuffer(start + offset, end - start - offset), end - start - offset, reader, false);
	}

	private static double parseDoubleGeneric(final char[] buf, final int len, final JsonReader reader, final boolean withQuotes) throws IOException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		if (end > reader.maxNumberDigits) {
			throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
		}
		final int offset = buf[0] == '-' ? 1 : 0;
		if (buf[offset] == '0' && end > offset + 1 && buf[offset + 1] >= '0' && buf[offset + 1] <= '9') {
			throw reader.newParseErrorAt("Leading zero is not allowed. Error parsing number", len + (withQuotes ? 2 : 0));
		}
		try {
			return Double.parseDouble(new String(buf, 0, end));
		} catch (NumberFormatException nfe) {
			throw reader.newParseErrorAt("Error parsing number", len + (withQuotes ? 2 : 0), nfe);
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Double> deserializeDoubleCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DOUBLE_READER);
	}

	public static void deserializeDoubleCollection(final JsonReader reader, final Collection<Double> res) throws IOException {
		reader.deserializeCollection(DOUBLE_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Double> deserializeDoubleNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DOUBLE_READER);
	}

	public static void deserializeDoubleNullableCollection(final JsonReader reader, final Collection<Double> res) throws IOException {
		reader.deserializeNullableCollection(DOUBLE_READER, res);
	}

	public static void serializeNullable(@Nullable final Float value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final float value, final JsonWriter sw) {
		if (value == Float.POSITIVE_INFINITY) {
			sw.writeAscii("\"Infinity\"");
		} else if (value == Float.NEGATIVE_INFINITY) {
			sw.writeAscii("\"-Infinity\"");
		} else if (value != value) {
			sw.writeAscii("\"NaN\"");
		} else {
			sw.writeAscii(Float.toString(value));//TODO: better implementation required
		}
	}

	public static void serialize(@Nullable final float[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for (int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static float deserializeFloat(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			return parseFloatGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true);
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		if (end == reader.length()) {
			final NumberInfo tmp = readLongNumber(reader, start);
			return parseFloatGeneric(tmp.buffer, tmp.length, reader, false);
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return -parseFloat(buf, reader, start, end, 1);
		}
		return parseFloat(buf, reader, start, end, 0);
	}

	private static float parseFloat(byte[] buf, final JsonReader reader, final int start, int end, int offset) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start + offset;
		final int digitStart = i;
		final boolean leadingZero = buf[start + offset] == 48;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + offset + 1) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (i > start + offset && reader.allWhitespace(i, end)) return value;
				numberException(reader, start, end, "Unknown digit", (char)ch);
			}
			value = (value << 3) + (value << 1) + ind;
		}
		if (i == digitStart) numberException(reader, start, end, "Digit not found");
		else if (leadingZero && ch != '.' && i > start + offset + 1) {
			numberException(reader, start, end, "Leading zero is not allowed");
		} else if (i > 18 + digitStart) {
			return parseFloatGeneric(reader.prepareBuffer(start + offset, end - start - offset), end - start - offset, reader, false);
		} else if (i == end) {
			return value;
		} else if (ch == '.') {
			i++;
			if (i == end) numberException(reader, start, end, "Number ends with a dot");
			final int decPos;
			final int maxLen;
			final int pointOffset;
			if (value == 0) {
				pointOffset = 0;
				decPos = i + 1;
				while (i < end && buf[i] == '0') {
					i++;
				}
				maxLen = i + 17;
			} else {
				pointOffset = 1;
				maxLen = digitStart + 17;
				decPos = i;
			}
			final int numLimit = maxLen < end ? maxLen : end;
			boolean foundE = false;
			for (; i < numLimit; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') {
					foundE = true;
					++i;
					break;
				}
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return (float) (value / POW_10[i - decPos - pointOffset]);
					numberException(reader, start, end, "Unknown digit", (char) ch);
				}
				value = (value << 3) + (value << 1) + ind;
			}
			final int endPos;
			if (i == numLimit && !foundE) {
				endPos = i + 1 - pointOffset;
				while (i < end && ch >= '0' && ch <= '9') {
					ch = buf[i++];
				}
			} else endPos = i - pointOffset;
			while (i == end && reader.length() == end) {
				i = reader.scanNumber();
				end = reader.getCurrentIndex();
				buf = reader.buffer;
				while (i < end && ch >= '0' && ch <= '9') {
					ch = buf[i++];
				}
			}
			if (ch == 'e' || ch == 'E') {
				return floatExponent(reader, value, endPos - decPos, buf, end, i);
			}
			final int expDiff = endPos - decPos;
			if (expDiff > 0) {
				return (float)(value / POW_10[expDiff - 1]);
			} else if (expDiff < 0) {
				return (float)(value * POW_10[-expDiff - 1]);
			} else {
				return value;
			}
		} else if (ch == 'e' || ch == 'E') {
			return floatExponent(reader, value, 0, buf, end, i + 1);
		}
		return value;
	}

	private static float floatExponent(JsonReader reader, final long whole, final int decimals, byte[] buf, int end, int i) throws IOException {
		byte ch;
		ch = buf[i];
		final int exp;
		if (ch == '-') {
			exp = parseNegativeInt(buf, reader, i, end) - decimals;
		} else if (ch == '+') {
			exp = parsePositiveInt(buf, reader, i, end, 1) - decimals;
		} else {
			exp = parsePositiveInt(buf, reader, i, end, 0) - decimals;
		}
		if (exp == 0 || whole == 0) return whole;
		else if (exp > 0 && exp < POW_10.length) return (float) (whole * POW_10[exp - 1]);
		else if (exp < 0 && -exp < POW_10.length) return (float) (whole / POW_10[-exp - 1]);
		else return exp > 0 ? Float.POSITIVE_INFINITY : 0f;
	}

	private static float parseFloatGeneric(final char[] buf, final int len, final JsonReader reader, final boolean withQuotes) throws ParsingException {
		int end = len;
		while (end > 0 && Character.isWhitespace(buf[end - 1])) {
			end--;
		}
		if (end > reader.maxNumberDigits) {
			throw reader.newParseErrorWith("Too many digits detected in number", len, "", "Too many digits detected in number", end, "");
		}
		final int offset = buf[0] == '-' ? 1 : 0;
		if (buf[offset] == '0' && end > offset + 1 && buf[offset + 1] >= '0' && buf[offset + 1] <= '9') {
			throw reader.newParseErrorAt("Leading zero is not allowed. Error parsing number", len + (withQuotes ? 2 : 0));
		}
		try {
			return Float.parseFloat(new String(buf, 0, end));
		} catch (NumberFormatException nfe) {
			throw reader.newParseErrorAt("Error parsing number", len + (withQuotes ? 2 : 0), nfe);
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Float> deserializeFloatCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(FLOAT_READER);
	}

	public static void deserializeFloatCollection(final JsonReader reader, Collection<Float> res) throws IOException {
		reader.deserializeCollection(FLOAT_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Float> deserializeFloatNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(FLOAT_READER);
	}

	public static void deserializeFloatNullableCollection(final JsonReader reader, final Collection<Float> res) throws IOException {
		reader.deserializeNullableCollection(FLOAT_READER, res);
	}

	public static void serializeNullable(@Nullable final Integer value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	private static final byte MINUS = '-';
	private static final byte[] MIN_INT = "-2147483648".getBytes();

	public static void serialize(final int value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(11);
		final int position = sw.size();
		int current = serialize(buf, position, value);
		sw.advance(current - position);
	}

	private static int serialize(final byte[] buf, int pos, final int value) {
		int i;
		if (value < 0) {
			if (value == Integer.MIN_VALUE) {
				for (int x = 0; x < MIN_INT.length; x++) {
					buf[pos + x] = MIN_INT[x];
				}
				return pos + MIN_INT.length;
			}
			i = -value;
			buf[pos++] = MINUS;
		} else {
			i = value;
		}
		final int q1 = i / 1000;
		if (q1 == 0) {
			pos += writeFirstBuf(buf, DIGITS[i], pos);
			return pos;
		}
		final int r1 = i - q1 * 1000;
		final int q2 = q1 / 1000;
		if (q2 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[q1];
			int off = writeFirstBuf(buf, v2, pos);
			writeBuf(buf, v1, pos + off);
			return pos + 3 + off;
		}
		final int r2 = q1 - q2 * 1000;
		final int q3 = q2 / 1000;
		final int v1 = DIGITS[r1];
		final int v2 = DIGITS[r2];
		if (q3 == 0) {
			pos += writeFirstBuf(buf, DIGITS[q2], pos);
		} else {
			final int r3 = q2 - q3 * 1000;
			buf[pos++] = (byte) (q3 + '0');
			writeBuf(buf, DIGITS[r3], pos);
			pos += 3;
		}
		writeBuf(buf, v2, pos);
		writeBuf(buf, v1, pos + 3);
		return pos + 6;
	}

	public static void serialize(@Nullable final int[] values, final JsonWriter sw) {
		if (values == null) {
			sw.writeNull();
		} else if (values.length == 0) {
			sw.writeAscii("[]");
		} else {
			final byte[] buf = sw.ensureCapacity(values.length * 11 + 2);
			int position = sw.size();
			buf[position++] = '[';
			position = serialize(buf, position, values[0]);
			for (int i = 1; i < values.length; i++) {
				buf[position++] = ',';
				position = serialize(buf, position, values[i]);
			}
			buf[position++] = ']';
			sw.advance(position - sw.size());
		}
	}

	public static void serialize(@Nullable final short[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			serialize(value[0], sw);
			for (int i = 1; i < value.length; i++) {
				sw.writeByte(JsonWriter.COMMA);
				serialize(value[i], sw);
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static short deserializeShort(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			try {
				return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true).shortValueExact();
			} catch (ArithmeticException ignore) {
				throw reader.newParseErrorAt("Short overflow detected", reader.getCurrentIndex() - position);
			}
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		final int value = ch == '-'
				? parseNegativeInt(buf, reader, start, end)
				: parsePositiveInt(buf, reader, start, end, 0);
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
			throw reader.newParseErrorAt("Short overflow detected", reader.getCurrentIndex());
		}
		return (short)value;
	}

	public static int deserializeInt(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			try {
				return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true).intValueExact();
			} catch (ArithmeticException ignore) {
				throw reader.newParseErrorAt("Integer overflow detected", reader.getCurrentIndex() - position);
			}
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			if (end > start + 2 && buf[start + 1] == '0' && buf[start + 2] >= '0' && buf[start + 2] <= '9') {
				numberException(reader, start, end, "Leading zero is not allowed");
			}
			return parseNegativeInt(buf, reader, start, end);
		} else {
			if (ch == '0' && end > start + 1 && buf[start + 1] >= '0' && buf[start + 1] <= '9') {
				numberException(reader, start, end, "Leading zero is not allowed");
			}
			return parsePositiveInt(buf, reader, start, end, 0);
		}
	}

	private static int parsePositiveInt(final byte[] buf, final JsonReader reader, final int start, final int end, final int offset) throws IOException {
		int value = 0;
		int i = start + offset;
		if (i == end) numberException(reader, start, end, "Digit not found");
		for (; i < end; i++) {
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (i > start + offset && reader.allWhitespace(i, end)) return value;
				else if (i == end - 1 && buf[i] == '.') numberException(reader, start, end, "Number ends with a dot");
				final BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start, end - start), end - start, reader, false);
				if (v.scale() > 0) numberException(reader, start, end, "Expecting int but found decimal value", v);
				return v.intValue();

			}
			value = (value << 3) + (value << 1) + ind;
			if (value < 0) {
				numberException(reader, start, end, "Integer overflow detected");
			}
		}
		return value;
	}

	private static int parseNegativeInt(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		int value = 0;
		int i = start + 1;
		if (i == end) numberException(reader, start, end, "Digit not found");
		for (; i < end; i++) {
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (i > start + 1 && reader.allWhitespace(i, end)) return value;
				else if (i == end - 1 && buf[i] == '.') numberException(reader, start, end, "Number ends with a dot");
				final BigDecimal v = parseNumberGeneric(reader.prepareBuffer(start, end - start), end - start, reader, false);
				if (v.scale() > 0) numberException(reader, start, end, "Expecting int but found decimal value", v);
				return v.intValue();
			}
			value = (value << 3) + (value << 1) - ind;
			if (value > 0) {
				numberException(reader, start, end, "Integer overflow detected");
			}
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Integer> deserializeIntCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(INT_READER);
	}

	public static int[] deserializeIntArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return INT_EMPTY_ARRAY;
		}
		int[] buffer = new int[4];
		buffer[0] = deserializeInt(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeInt(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static short[] deserializeShortArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return SHORT_EMPTY_ARRAY;
		}
		short[] buffer = new short[4];
		buffer[0] = (short)deserializeInt(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = (short)deserializeInt(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static long[] deserializeLongArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return LONG_EMPTY_ARRAY;
		}
		long[] buffer = new long[4];
		buffer[0] = deserializeLong(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeLong(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static float[] deserializeFloatArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return FLOAT_EMPTY_ARRAY;
		}
		float[] buffer = new float[4];
		buffer[0] = deserializeFloat(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeFloat(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static double[] deserializeDoubleArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return DOUBLE_EMPTY_ARRAY;
		}
		double[] buffer = new double[4];
		buffer[0] = deserializeDouble(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserializeDouble(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	public static void deserializeShortCollection(final JsonReader reader, final Collection<Short> res) throws IOException {
		reader.deserializeCollection(SHORT_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Short> deserializeShortNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(SHORT_READER);
	}

	public static void deserializeShortNullableCollection(final JsonReader reader, final Collection<Short> res) throws IOException {
		reader.deserializeNullableCollection(SHORT_READER, res);
	}

	public static void deserializeIntCollection(final JsonReader reader, final Collection<Integer> res) throws IOException {
		reader.deserializeCollection(INT_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Integer> deserializeIntNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(INT_READER);
	}

	public static void deserializeIntNullableCollection(final JsonReader reader, final Collection<Integer> res) throws IOException {
		reader.deserializeNullableCollection(INT_READER, res);
	}

	public static void serializeNullable(@Nullable final Long value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	private static int writeFirstBuf(final byte[] buf, final int v, int pos) {
		final int start = v >> 24;
		if (start == 0) {
			buf[pos++] = (byte) (v >> 16);
			buf[pos++] = (byte) (v >> 8);
		} else if (start == 1) {
			buf[pos++] = (byte) (v >> 8);
		}
		buf[pos] = (byte) v;
		return 3 - start;
	}

	private static void writeBuf(final byte[] buf, final int v, int pos) {
		buf[pos] = (byte) (v >> 16);
		buf[pos + 1] = (byte) (v >> 8);
		buf[pos + 2] = (byte) v;
	}

	private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();

	public static void serialize(final long value, final JsonWriter sw) {
		final byte[] buf = sw.ensureCapacity(21);
		final int position = sw.size();
		int current = serialize(buf, position, value);
		sw.advance(current - position);
	}

	private static int serialize(final byte[] buf, int pos, final long value) {
		long i;
		if (value < 0) {
			if (value == Long.MIN_VALUE) {
				for (int x = 0; x < MIN_LONG.length; x++) {
					buf[pos + x] = MIN_LONG[x];
				}
				return pos + MIN_LONG.length;
			}
			i = -value;
			buf[pos++] = MINUS;
		} else {
			i = value;
		}
		final long q1 = i / 1000;
		if (q1 == 0) {
			pos += writeFirstBuf(buf, DIGITS[(int) i], pos);
			return pos;
		}
		final int r1 = (int) (i - q1 * 1000);
		final long q2 = q1 / 1000;
		if (q2 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[(int) q1];
			int off = writeFirstBuf(buf, v2, pos);
			writeBuf(buf, v1, pos + off);
			return pos + 3 + off;
		}
		final int r2 = (int) (q1 - q2 * 1000);
		final long q3 = q2 / 1000;
		if (q3 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[r2];
			final int v3 = DIGITS[(int) q2];
			pos += writeFirstBuf(buf, v3, pos);
			writeBuf(buf, v2, pos);
			writeBuf(buf, v1, pos + 3);
			return pos + 6;
		}
		final int r3 = (int) (q2 - q3 * 1000);
		final int q4 = (int) (q3 / 1000);
		if (q4 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[r2];
			final int v3 = DIGITS[r3];
			final int v4 = DIGITS[(int) q3];
			pos += writeFirstBuf(buf, v4, pos);
			writeBuf(buf, v3, pos);
			writeBuf(buf, v2, pos + 3);
			writeBuf(buf, v1, pos + 6);
			return pos + 9;
		}
		final int r4 = (int) (q3 - q4 * 1000);
		final int q5 = q4 / 1000;
		if (q5 == 0) {
			final int v1 = DIGITS[r1];
			final int v2 = DIGITS[r2];
			final int v3 = DIGITS[r3];
			final int v4 = DIGITS[r4];
			final int v5 = DIGITS[q4];
			pos += writeFirstBuf(buf, v5, pos);
			writeBuf(buf, v4, pos);
			writeBuf(buf, v3, pos + 3);
			writeBuf(buf, v2, pos + 6);
			writeBuf(buf, v1, pos + 9);
			return pos + 12;
		}
		final int r5 = q4 - q5 * 1000;
		final int q6 = q5 / 1000;
		final int v1 = DIGITS[r1];
		final int v2 = DIGITS[r2];
		final int v3 = DIGITS[r3];
		final int v4 = DIGITS[r4];
		final int v5 = DIGITS[r5];
		if (q6 == 0) {
			pos += writeFirstBuf(buf, DIGITS[q5], pos);
		} else {
			final int r6 = q5 - q6 * 1000;
			buf[pos++] = (byte) (q6 + '0');
			writeBuf(buf, DIGITS[r6], pos);
			pos += 3;
		}
		writeBuf(buf, v5, pos);
		writeBuf(buf, v4, pos + 3);
		writeBuf(buf, v3, pos + 6);
		writeBuf(buf, v2, pos + 9);
		writeBuf(buf, v1, pos + 12);
		return pos + 15;
	}

	public static void serialize(@Nullable final long[] values, final JsonWriter sw) {
		if (values == null) {
			sw.writeNull();
		} else if (values.length == 0) {
			sw.writeAscii("[]");
		} else {
			final byte[] buf = sw.ensureCapacity(values.length * 21 + 2);
			int position = sw.size();
			buf[position++] = '[';
			position = serialize(buf, position, values[0]);
			for (int i = 1; i < values.length; i++) {
				buf[position++] = ',';
				position = serialize(buf, position, values[i]);
			}
			buf[position++] = ']';
			sw.advance(position - sw.size());
		}
	}

	public static long deserializeLong(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int position = reader.getCurrentIndex();
			final char[] buf = reader.readSimpleQuote();
			try {
				return parseNumberGeneric(buf, reader.getCurrentIndex() - position - 1, reader, true).longValueExact();
			} catch (ArithmeticException ignore) {
				throw reader.newParseErrorAt("Long overflow detected", reader.getCurrentIndex() - position);
			}
		}
		final int start = reader.scanNumber();
		final int end = reader.getCurrentIndex();
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		int i = start;
		long value = 0;
		if (ch == '-') {
			i = start + 1;
			if (i == end) numberException(reader, start, end, "Digit not found");
			final boolean leadingZero = buf[i] == 48;
			for (; i < end; i++) {
				final int ind = buf[i] - 48;
				if (ind < 0 || ind > 9) {
					if (leadingZero && i > start + 2) {
						numberException(reader, start, end, "Leading zero is not allowed");
					}
					if (i > start + 1 && reader.allWhitespace(i, end)) return value;
					return parseLongGeneric(reader, start, end);
				}
				value = (value << 3) + (value << 1) - ind;
				if (value > 0) {
					numberException(reader, start, end, "Long overflow detected");
				}
			}
			if (leadingZero && i > start + 2) {
				numberException(reader, start, end, "Leading zero is not allowed");
			}
			return value;
		}
		if (i == end) numberException(reader, start, end, "Digit not found");
		final boolean leadingZero = buf[i] == 48;
		for (; i < end; i++) {
			final int ind = buf[i] - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + 1) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (ch == '+' && i > start + 1 && reader.allWhitespace(i, end)) return value;
				else if (ch != '+' && i > start && reader.allWhitespace(i, end)) return value;
				return parseLongGeneric(reader, start, end);
			}
			value = (value << 3) + (value << 1) + ind;
			if (value < 0) {
				numberException(reader, start, end, "Long overflow detected");
			}
		}
		if (leadingZero && i > start + 1) {
			numberException(reader, start, end, "Leading zero is not allowed");
		}
		return value;
	}

	private static long parseLongGeneric(final JsonReader reader, final int start, final int end) throws IOException {
		final int len = end - start;
		final char[] buf = reader.prepareBuffer(start, len);
		if (len > 0 && buf[len - 1] == '.') numberException(reader, start, end, "Number ends with a dot");
		final BigDecimal v = parseNumberGeneric(buf, len, reader, false);
		if (v.scale() > 0) numberException(reader, start, end, "Expecting long, but found decimal value ", v);
		return v.longValue();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Long> deserializeLongCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(LONG_READER);
	}

	public static void deserializeLongCollection(final JsonReader reader, final Collection<Long> res) throws IOException {
		reader.deserializeCollection(LONG_READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Long> deserializeLongNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(LONG_READER);
	}

	public static void deserializeLongNullableCollection(final JsonReader reader, final Collection<Long> res) throws IOException {
		reader.deserializeNullableCollection(LONG_READER, res);
	}

	public static void serializeNullable(@Nullable final BigDecimal value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			sw.writeAscii(value.toString());
		}
	}

	public static void serialize(final BigDecimal value, final JsonWriter sw) {
		sw.writeAscii(value.toString());
	}

	public static BigDecimal deserializeDecimal(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			final int len = reader.parseString();
			return parseNumberGeneric(reader.chars, len, reader, true);
		}
		final int start = reader.scanNumber();
		int end = reader.getCurrentIndex();
		if (end == reader.length()) {
			NumberInfo info = readLongNumber(reader, start);
			return parseNumberGeneric(info.buffer, info.length, reader, false);
		}
		int len = end - start;
		if (len > 18) {
			return parseNumberGeneric(reader.prepareBuffer(start, len), len, reader, false);
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeDecimal(buf, reader, start, end);
		}
		return parsePositiveDecimal(buf, reader, start, end);
	}

	private static BigDecimal parsePositiveDecimal(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start;
		final boolean leadingZero = buf[start] == 48;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + 1) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (i > start && reader.allWhitespace(i, end)) return BigDecimal.valueOf(value);
				numberException(reader, start, end, "Unknown digit", (char)ch);
			}
			value = (value << 3) + (value << 1) + ind;
		}
		if (i == start) numberException(reader, start, end, "Digit not found");
		else if (leadingZero && ch != '.' && i > start + 1) numberException(reader, start, end, "Leading zero is not allowed");
		else if (i == end) return BigDecimal.valueOf(value);
		else if (ch == '.') {
			i++;
			if (i == end) numberException(reader, start, end, "Number ends with a dot");
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return BigDecimal.valueOf(value, i - dp);
					numberException(reader, start, end, "Unknown digit", (char)ch);
				}
				value = (value << 3) + (value << 1) + ind;
			}
			if (i == end) return BigDecimal.valueOf(value, end - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, i, end);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, i, end, 1);
				} else {
					exp = parsePositiveInt(buf, reader, i, end, 0);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, i, end);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, i, end, 1);
			} else {
				exp = parsePositiveInt(buf, reader, i, end, 0);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static BigDecimal parseNegativeDecimal(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start + 1;
		final boolean leadingZero = buf[start + 1] == 48;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + 2) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (i > start + 1 && reader.allWhitespace(i, end)) return BigDecimal.valueOf(value);
				numberException(reader, start, end, "Unknown digit", (char)ch);
			}
			value = (value << 3) + (value << 1) - ind;
		}
		if (i == start + 1) numberException(reader, start, end, "Digit not found");
		else if (leadingZero && ch != '.' && i > start + 2) numberException(reader, start, end, "Leading zero is not allowed");
		else if (i == end) return BigDecimal.valueOf(value);
		else if (ch == '.') {
			i++;
			if (i == end) numberException(reader, start, end, "Number ends with a dot");
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return BigDecimal.valueOf(value, i - dp);
					numberException(reader, start, end, "Unknown digit", (char)ch);
				}
				value = (value << 3) + (value << 1) - ind;
			}
			if (i == end) return BigDecimal.valueOf(value, end - dp);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, i, end);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, i, end, 1);
				} else {
					exp = parsePositiveInt(buf, reader, i, end, 0);
				}
				return BigDecimal.valueOf(value, ep - dp - exp);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, i, end);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, i, end, 1);
			} else {
				exp = parsePositiveInt(buf, reader, i, end, 0);
			}
			return BigDecimal.valueOf(value, -exp);
		}
		return BigDecimal.valueOf(value);
	}

	private static final BigDecimal BD_MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);
	private static final BigDecimal BD_MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);

	private static Number bigDecimalOrDouble(BigDecimal num, JsonReader.UnknownNumberParsing unknownNumbers) {
		return unknownNumbers == JsonReader.UnknownNumberParsing.LONG_AND_BIGDECIMAL
				? num
				: num.doubleValue();
	}

	private static Number tryLongFromBigDecimal(final char[] buf, final int len, JsonReader reader) throws IOException {
		final BigDecimal num = parseNumberGeneric(buf, len, reader, false);
		if (num.scale() == 0 && num.precision() <= 19) {
			if (num.signum() == 1) {
				if (num.compareTo(BD_MAX_LONG) <= 0) {
					return num.longValue();
				}
			} else if (num.compareTo(BD_MIN_LONG) >= 0) {
				return num.longValue();
			}
		}
		return bigDecimalOrDouble(num, reader.unknownNumbers);
	}

	public static Number deserializeNumber(final JsonReader reader) throws IOException {
		if (reader.unknownNumbers == JsonReader.UnknownNumberParsing.BIGDECIMAL) return deserializeDecimal(reader);
		else if (reader.unknownNumbers == JsonReader.UnknownNumberParsing.DOUBLE) return deserializeDouble(reader);
		final int start = reader.scanNumber();
		int end = reader.getCurrentIndex();
		if (end == reader.length()) {
			NumberInfo info = readLongNumber(reader, start);
			return tryLongFromBigDecimal(info.buffer, info.length, reader);
		}
		int len = end - start;
		if (len > 18) {
			return tryLongFromBigDecimal(reader.prepareBuffer(start, len), len, reader);
		}
		final byte[] buf = reader.buffer;
		final byte ch = buf[start];
		if (ch == '-') {
			return parseNegativeNumber(buf, reader, start, end);
		}
		return parsePositiveNumber(buf, reader, start, end);
	}

	private static Number parsePositiveNumber(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start;
		final boolean leadingZero = buf[start] == 48;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + 1) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (i > start && reader.allWhitespace(i, end)) return value;
				return tryLongFromBigDecimal(reader.prepareBuffer(start, end - start), end - start, reader);
			}
			value = (value << 3) + (value << 1) + ind;
		}
		if (i == start) numberException(reader, start, end, "Digit not found");
		else if (leadingZero && ch != '.' && i > start + 1) numberException(reader, start, end, "Leading zero is not allowed");
		else if (i == end) return value;
		else if (ch == '.') {
			i++;
			if (i == end) numberException(reader, start, end, "Number ends with a dot");
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return BigDecimal.valueOf(value, i - dp);
					return tryLongFromBigDecimal(reader.prepareBuffer(start, end - start), end - start, reader);
				}
				value = (value << 3) + (value << 1) + ind;
			}
			if (i == end) return bigDecimalOrDouble(BigDecimal.valueOf(value, end - dp), reader.unknownNumbers);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, i, end);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, i, end, 1);
				} else {
					exp = parsePositiveInt(buf, reader, i, end, 0);
				}
				return bigDecimalOrDouble(BigDecimal.valueOf(value, ep - dp - exp), reader.unknownNumbers);
			}
			return BigDecimal.valueOf(value, end - dp);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, i, end);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, i, end, 1);
			} else {
				exp = parsePositiveInt(buf, reader, i, end, 0);
			}
			return bigDecimalOrDouble(BigDecimal.valueOf(value, -exp), reader.unknownNumbers);
		}
		return bigDecimalOrDouble(BigDecimal.valueOf(value), reader.unknownNumbers);
	}

	private static Number parseNegativeNumber(final byte[] buf, final JsonReader reader, final int start, final int end) throws IOException {
		long value = 0;
		byte ch = ' ';
		int i = start + 1;
		final boolean leadingZero = buf[start + 1] == 48;
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > start + 2) {
					numberException(reader, start, end, "Leading zero is not allowed");
				}
				if (i > start + 1 && reader.allWhitespace(i, end)) return value;
				return tryLongFromBigDecimal(reader.prepareBuffer(start, end - start), end - start, reader);
			}
			value = (value << 3) + (value << 1) - ind;
		}
		if (i == start + 1) numberException(reader, start, end, "Digit not found");
		else if (leadingZero && ch != '.' && i > start + 2) numberException(reader, start, end, "Leading zero is not allowed");
		else if (i == end) return value;
		else if (ch == '.') {
			i++;
			if (i == end) numberException(reader, start, end, "Number ends with a dot");
			int dp = i;
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (reader.allWhitespace(i, end)) return BigDecimal.valueOf(value, i - dp);
					return tryLongFromBigDecimal(reader.prepareBuffer(start, end - start), end - start, reader);
				}
				value = (value << 3) + (value << 1) - ind;
			}
			if (i == end) return bigDecimalOrDouble(BigDecimal.valueOf(value, end - dp), reader.unknownNumbers);
			else if (ch == 'e' || ch == 'E') {
				final int ep = i;
				i++;
				ch = buf[i];
				final int exp;
				if (ch == '-') {
					exp = parseNegativeInt(buf, reader, i, end);
				} else if (ch == '+') {
					exp = parsePositiveInt(buf, reader, i, end, 1);
				} else {
					exp = parsePositiveInt(buf, reader, i, end, 0);
				}
				return bigDecimalOrDouble(BigDecimal.valueOf(value, ep - dp - exp), reader.unknownNumbers);
			}
			return bigDecimalOrDouble(BigDecimal.valueOf(value, end - dp), reader.unknownNumbers);
		} else if (ch == 'e' || ch == 'E') {
			i++;
			ch = buf[i];
			final int exp;
			if (ch == '-') {
				exp = parseNegativeInt(buf, reader, i, end);
			} else if (ch == '+') {
				exp = parsePositiveInt(buf, reader, i, end, 1);
			} else {
				exp = parsePositiveInt(buf, reader, i, end, 0);
			}
			return bigDecimalOrDouble(BigDecimal.valueOf(value, -exp), reader.unknownNumbers);
		}
		return bigDecimalOrDouble(BigDecimal.valueOf(value), reader.unknownNumbers);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<BigDecimal> deserializeDecimalCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(DecimalReader);
	}

	public static void deserializeDecimalCollection(final JsonReader reader, final Collection<BigDecimal> res) throws IOException {
		reader.deserializeCollection(DecimalReader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<BigDecimal> deserializeDecimalNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(DecimalReader);
	}

	public static void deserializeDecimalNullableCollection(final JsonReader reader, final Collection<BigDecimal> res) throws IOException {
		reader.deserializeNullableCollection(DecimalReader, res);
	}
}