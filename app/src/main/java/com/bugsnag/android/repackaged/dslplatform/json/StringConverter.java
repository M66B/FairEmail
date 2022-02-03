package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public abstract class StringConverter {

	public static final JsonReader.ReadObject<String> READER = new JsonReader.ReadObject<String>() {
		@Nullable
		@Override
		public String read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			return reader.readString();
		}
	};
	public static final JsonWriter.WriteObject<String> WRITER = new JsonWriter.WriteObject<String>() {
		@Override
		public void write(JsonWriter writer, @Nullable String value) {
			serializeNullable(value, writer);
		}
	};
	public static final JsonWriter.WriteObject<CharSequence> WRITER_CHARS = new JsonWriter.WriteObject<CharSequence>() {
		@Override
		public void write(JsonWriter writer, @Nullable CharSequence value) {
			if (value == null) writer.writeNull();
			else writer.writeString(value);
		}
	};
	public static final JsonReader.ReadObject<StringBuilder> READER_BUILDER = new JsonReader.ReadObject<StringBuilder>() {
		@Nullable
		@Override
		public StringBuilder read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			StringBuilder builder = new StringBuilder();
			return reader.appendString(builder);
		}
	};
	public static final JsonReader.ReadObject<StringBuffer> READER_BUFFER = new JsonReader.ReadObject<StringBuffer>() {
		@Nullable
		@Override
		public StringBuffer read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			StringBuffer builder = new StringBuffer();
			return reader.appendString(builder);
		}
	};

	public static void serializeShortNullable(@Nullable final String value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			sw.writeString(value);
		}
	}

	public static void serializeShort(final String value, final JsonWriter sw) {
		sw.writeString(value);
	}

	public static void serializeNullable(@Nullable final String value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			sw.writeString(value);
		}
	}

	public static void serialize(final String value, final JsonWriter sw) {
		sw.writeString(value);
	}

	public static String deserialize(final JsonReader reader) throws IOException {
		return reader.readString();
	}

	@Nullable
	public static String deserializeNullable(final JsonReader reader) throws IOException {
		if (reader.last() == 'n') {
			if (!reader.wasNull()) throw reader.newParseErrorAt("Expecting 'null' for null constant", 0);
			return null;
		}
		return reader.readString();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(READER);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<String> res) throws IOException {
		reader.deserializeCollection(READER, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(READER);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<String> res) throws IOException {
		reader.deserializeNullableCollection(READER, res);
	}

	public static void serialize(final List<String> list, final JsonWriter writer) {
		writer.writeByte(JsonWriter.ARRAY_START);
		if (list.size() != 0) {
			writer.writeString(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				writer.writeByte(JsonWriter.COMMA);
				writer.writeString(list.get(i));
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}
}
