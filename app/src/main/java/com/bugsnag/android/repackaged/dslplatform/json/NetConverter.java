package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public abstract class NetConverter {

	static final JsonReader.ReadObject<URI> UriReader = new JsonReader.ReadObject<URI>() {
		@Nullable
		@Override
		public URI read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeUri(reader);
		}
	};
	static final JsonWriter.WriteObject<URI> UriWriter = new JsonWriter.WriteObject<URI>() {
		@Override
		public void write(JsonWriter writer, @Nullable URI value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<InetAddress> AddressReader = new JsonReader.ReadObject<InetAddress>() {
		@Nullable
		@Override
		public InetAddress read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserializeIp(reader);
		}
	};
	static final JsonWriter.WriteObject<InetAddress> AddressWriter = new JsonWriter.WriteObject<InetAddress>() {
		@Override
		public void write(JsonWriter writer, @Nullable InetAddress value) {
			serializeNullable(value, writer);
		}
	};

	public static void serializeNullable(@Nullable final URI value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final URI value, final JsonWriter sw) {
		StringConverter.serializeShort(value.toString(), sw);
	}

	public static URI deserializeUri(final JsonReader reader) throws IOException {
		return URI.create(reader.readString());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<URI> deserializeUriCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(UriReader);
	}

	public static void deserializeUriCollection(final JsonReader reader, final Collection<URI> res) throws IOException {
		reader.deserializeCollection(UriReader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<URI> deserializeUriNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(UriReader);
	}

	public static void deserializeUriNullableCollection(final JsonReader reader, final Collection<URI> res) throws IOException {
		reader.deserializeNullableCollection(UriReader, res);
	}

	public static void serializeNullable(@Nullable final InetAddress value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final InetAddress value, final JsonWriter sw) {
		sw.writeByte(JsonWriter.QUOTE);
		sw.writeAscii(value.getHostAddress());
		sw.writeByte(JsonWriter.QUOTE);
	}

	public static InetAddress deserializeIp(final JsonReader reader) throws IOException {
		return InetAddress.getByName(reader.readSimpleString());
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<InetAddress> deserializeIpCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(AddressReader);
	}

	public static void deserializeIpCollection(final JsonReader reader, final Collection<InetAddress> res) throws IOException {
		reader.deserializeCollection(AddressReader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<InetAddress> deserializeIpNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(AddressReader);
	}

	public static void deserializeIpNullableCollection(final JsonReader reader, final Collection<InetAddress> res) throws IOException {
		reader.deserializeNullableCollection(AddressReader, res);
	}
}
