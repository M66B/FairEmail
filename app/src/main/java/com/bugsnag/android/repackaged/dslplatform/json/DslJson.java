package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import org.w3c.dom.Element;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main DSL-JSON class.
 * Easiest way to use the library is to create an DslJson&lt;Object&gt; instance and reuse it within application.
 * DslJson has optional constructor for specifying default readers/writers.
 * <p>
 * During initialization DslJson will use ServiceLoader API to load registered services.
 * This is done through `META-INF/services/com.dslplatform.json.CompiledJson` file.
 * <p>
 * DslJson can fallback to another serializer in case when it doesn't know how to handle specific type.
 * This can be specified by Fallback interface during initialization.
 * <p>
 * If you wish to use compile time databinding @CompiledJson annotation must be specified on the target class
 * or implicit reference to target class must exists from a class with @CompiledJson annotation.
 * <p>
 * Usage example:
 * <pre>
 *     DslJson&lt;Object&gt; dsl = new DslJson&lt;&gt;();
 *     dsl.serialize(instance, OutputStream);
 *     POJO pojo = dsl.deserialize(POJO.class, InputStream);
 * </pre>
 * <p>
 * For best performance use serialization API with JsonWriter and byte[] as target.
 * JsonWriter is reused via thread local variable. When custom JsonWriter's are used, reusing them will yield maximum performance.
 * JsonWriter can be reused via reset methods.
 * For best deserialization performance prefer byte[] API instead of InputStream API.
 * JsonReader is reused via thread local variable. When custom JsonReaders are used, reusing them will yield maximum performance.
 * JsonReader can be reused via process methods.
 * <p>
 * During deserialization TContext can be used to pass data into deserialized classes.
 * This is useful when deserializing domain objects which require state or service provider.
 * For example DSL Platform entities require service locator to be able to perform lazy load.
 * <p>
 * DslJson doesn't have a String or Reader API since it's optimized for processing bytes and streams.
 * If you wish to process String, use String.getBytes("UTF-8") as argument for DslJson.
 * Only UTF-8 is supported for encoding and decoding JSON.
 * <pre>
 *     DslJson&lt;Object&gt; dsl = new DslJson&lt;&gt;();
 *     JsonWriter writer = dsl.newWriter();
 *     dsl.serialize(writer, instance);
 *     String json = writer.toString(); //JSON as string - avoid using JSON as Strings whenever possible
 *     byte[] input = json.getBytes("UTF-8");
 *     POJO pojo = dsl.deserialize(POJO.class, input, input.length);
 * </pre>
 *
 * @param <TContext> used for library specialization. If unsure, use Object
 */
@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public class DslJson<TContext> implements UnknownSerializer, TypeLookup {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Object unknownValue = new Object();

	/**
	 * The context of this instance.
	 * Can be used for library specialization
	 */
	@Nullable
	public final TContext context;
	@Nullable
	protected final Fallback<TContext> fallback;
	/**
	 * Should properties with default values be omitted from the resulting JSON?
	 * This will leave out nulls, empty collections, zeros and other attributes with default values
	 * which can be reconstructed from schema information
	 */
	public final boolean omitDefaults;
	/**
	 * When object supports array format, eg. [prop1, prop2, prop3] this value must be enabled before
	 * object will be serialized in such a way. Regardless of this value deserialization will support all formats.
	 */
	public final boolean allowArrayFormat;

	protected final StringCache keyCache;
	protected final StringCache valuesCache;
	protected final List<ConverterFactory<JsonWriter.WriteObject>> writerFactories = new CopyOnWriteArrayList<ConverterFactory<JsonWriter.WriteObject>>();
	private final int settingsWriters;
	protected final List<ConverterFactory<JsonReader.ReadObject>> readerFactories = new CopyOnWriteArrayList<ConverterFactory<JsonReader.ReadObject>>();
	private final int settingsReaders;
	protected final List<ConverterFactory<JsonReader.BindObject>> binderFactories = new CopyOnWriteArrayList<ConverterFactory<JsonReader.BindObject>>();
	private final int settingsBinders;
	private final JsonReader.ErrorInfo errorInfo;
	private final JsonReader.DoublePrecision doublePrecision;
	private final JsonReader.UnknownNumberParsing unknownNumbers;
	private final int maxNumberDigits;
	private final int maxStringSize;
	protected final ThreadLocal<JsonWriter> localWriter;
	protected final ThreadLocal<JsonReader> localReader;
	private final ExternalConverterAnalyzer externalConverterAnalyzer;
	private final Map<Class<? extends Annotation>, Boolean> creatorMarkers;

	public interface Fallback<TContext> {
		void serialize(@Nullable Object instance, OutputStream stream) throws IOException;

		@Nullable
		Object deserialize(@Nullable TContext context, Type manifest, byte[] body, int size) throws IOException;

		@Nullable
		Object deserialize(@Nullable TContext context, Type manifest, InputStream stream) throws IOException;
	}

	public interface ConverterFactory<T> {
		@Nullable
		T tryCreate(Type manifest, DslJson dslJson);
	}

	/**
	 * Configuration for DslJson options.
	 * By default key cache is enabled. Everything else is not configured.
	 * To load `META-INF/services` call `includeServiceLoader()`
	 *
	 * @param <TContext> DslJson context
	 */
	public static class Settings<TContext> {
		private TContext context;
		private boolean javaSpecifics;
		private Fallback<TContext> fallback;
		private boolean omitDefaults;
		private boolean allowArrayFormat;
		private StringCache keyCache = new SimpleStringCache();
		private StringCache valuesCache;
		private int fromServiceLoader;
		private JsonReader.ErrorInfo errorInfo = JsonReader.ErrorInfo.WITH_STACK_TRACE;
		private JsonReader.DoublePrecision doublePrecision = JsonReader.DoublePrecision.DEFAULT;
		private JsonReader.UnknownNumberParsing unknownNumbers = JsonReader.UnknownNumberParsing.LONG_AND_BIGDECIMAL;
		private int maxNumberDigits = 512;
		private int maxStringBuffer = 128 * 1024 * 1024;
		private final List<Configuration> configurations = new ArrayList<Configuration>();
		private final List<ConverterFactory<JsonWriter.WriteObject>> writerFactories = new ArrayList<ConverterFactory<JsonWriter.WriteObject>>();
		private final List<ConverterFactory<JsonReader.ReadObject>> readerFactories = new ArrayList<ConverterFactory<JsonReader.ReadObject>>();
		private final List<ConverterFactory<JsonReader.BindObject>> binderFactories = new ArrayList<ConverterFactory<JsonReader.BindObject>>();
		private final Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
		private final Map<Class<? extends Annotation>, Boolean> creatorMarkers = new HashMap<Class<? extends Annotation>, Boolean>();

		/**
		 * Pass in context for DslJson.
		 * Context will be available in JsonReader for objects which needs it.
		 *
		 * @param context context propagated to JsonReaders
		 * @return itself
		 */
		public Settings<TContext> withContext(@Nullable TContext context) {
			this.context = context;
			return this;
		}

		/**
		 * Enable converters for Java specific types (Graphics API) not available on Android.
		 *
		 * @param javaSpecifics should register Java specific converters
		 * @return itself
		 */
		public Settings<TContext> withJavaConverters(boolean javaSpecifics) {
			this.javaSpecifics = javaSpecifics;
			return this;
		}

		/**
		 * Will be eventually replaced with writer/reader factories.
		 * Used by DslJson to call into when trying to serialize/deserialize object which is not supported.
		 *
		 * @param fallback how to handle unsupported type
		 * @return which fallback to use in case of unsupported type
		 */
		@Deprecated
		public Settings<TContext> fallbackTo(@Nullable Fallback<TContext> fallback) {
			this.fallback = fallback;
			return this;
		}

		/**
		 * DslJson can exclude some properties from resulting JSON which it can reconstruct fully from schema information.
		 * Eg. int with value 0 can be omitted since that is default value for the type.
		 * Null values can be excluded since they are handled the same way as missing property.
		 *
		 * @param omitDefaults should exclude default values from resulting JSON
		 * @return itself
		 */
		public Settings<TContext> skipDefaultValues(boolean omitDefaults) {
			this.omitDefaults = omitDefaults;
			return this;
		}

		/**
		 * Some encoders/decoders support writing objects in array format.
		 * For encoder to write objects in such format, Array format must be defined before the Default and minified formats
		 * and array format must be allowed via this setting.
		 * If objects support multiple formats decoding will work regardless of this setting.
		 *
		 * @param allowArrayFormat allow serialization via array format
		 * @return itself
		 */
		public Settings<TContext> allowArrayFormat(boolean allowArrayFormat) {
			this.allowArrayFormat = allowArrayFormat;
			return this;
		}

		/**
		 * Use specific key cache implementation.
		 * Key cache is enabled by default and it's used when deserializing unstructured objects such as Map&lt;String, Object&gt;
		 * to avoid allocating new String key instance. Instead StringCache will provide a new or an old instance.
		 * This improves memory usage and performance since there is usually small number of keys.
		 * It does have some performance overhead, but this is dependant on the implementation.
		 * <p>
		 * To disable key cache, provide null for it.
		 *
		 * @param keyCache which key cache to use
		 * @return itself
		 */
		public Settings<TContext> useKeyCache(@Nullable StringCache keyCache) {
			this.keyCache = keyCache;
			return this;
		}

		/**
		 * Use specific string values cache implementation.
		 * By default string values cache is disabled.
		 * <p>
		 * To support memory restricted scenarios where there is limited number of string values,
		 * values cache can be used.
		 * <p>
		 * Not every "JSON string" will use this cache... eg UUID, LocalDate don't create an instance of string
		 * and therefore don't use this cache.
		 *
		 * @param valuesCache which values cache to use
		 * @return itself
		 */
		public Settings<TContext> useStringValuesCache(@Nullable StringCache valuesCache) {
			this.valuesCache = valuesCache;
			return this;
		}

		/**
		 * DslJson will iterate over converter factories when requested type is unknown.
		 * Registering writer converter factory allows for constructing JSON converter lazily.
		 *
		 * @param writer registered writer factory
		 * @return itself
		 */
		@SuppressWarnings("unchecked")
		public Settings<TContext> resolveWriter(ConverterFactory<? extends JsonWriter.WriteObject> writer) {
			if (writer == null) throw new IllegalArgumentException("writer can't be null");
			if (writerFactories.contains(writer)) {
				throw new IllegalArgumentException("writer already registered");
			}
			writerFactories.add((ConverterFactory<JsonWriter.WriteObject>) writer);
			return this;
		}

		/**
		 * DslJson will iterate over converter factories when requested type is unknown.
		 * Registering reader converter factory allows for constructing JSON converter lazily.
		 *
		 * @param reader registered reader factory
		 * @return itself
		 */
		@SuppressWarnings("unchecked")
		public Settings<TContext> resolveReader(ConverterFactory<? extends JsonReader.ReadObject> reader) {
			if (reader == null) throw new IllegalArgumentException("reader can't be null");
			if (readerFactories.contains(reader)) {
				throw new IllegalArgumentException("reader already registered");
			}
			readerFactories.add((ConverterFactory<JsonReader.ReadObject>) reader);
			return this;
		}

		/**
		 * DslJson will iterate over converter factories when requested type is unknown.
		 * Registering binder converter factory allows for constructing JSON converter lazily.
		 *
		 * @param binder registered binder factory
		 * @return itself
		 */
		@SuppressWarnings("unchecked")
		public Settings<TContext> resolveBinder(ConverterFactory<? extends JsonReader.BindObject> binder) {
			if (binder == null) throw new IllegalArgumentException("binder can't be null");
			if (binderFactories.contains(binder)) {
				throw new IllegalArgumentException("binder already registered");
			}
			binderFactories.add((ConverterFactory<JsonReader.BindObject>) binder);
			return this;
		}

		/**
		 * Load converters using thread local ClassLoader.
		 * Will scan through `META-INF/services/com.bugsnag.dslplatform.json.Configuration` file and register implementation during startup.
		 * This will pick up compile time databindings if they are available in specific folder.
		 * <p>
		 * Note that gradle on Android has issues with preserving that file, in which case it can be provided manually.
		 * DslJson will fall back to "expected" class name if it doesn't find anything during scanning.
		 *
		 * @return itself
		 */
		public Settings<TContext> includeServiceLoader() {
			return includeServiceLoader(Thread.currentThread().getContextClassLoader());
		}

		/**
		 * Load converters using provided `ClassLoader` instance
		 * Will scan through `META-INF/services/com.bugsnag.dslplatform.json.Configuration` file and register implementation during startup.
		 * This will pick up compile time databindings if they are available in specific folder.
		 * <p>
		 * Note that gradle on Android has issues with preserving that file, in which case it can be provided manually.
		 * DslJson will fall back to "expected" class name if it doesn't find anything during scanning.
		 *
		 * @param loader ClassLoader to use
		 * @return itself
		 */
		public Settings<TContext> includeServiceLoader(ClassLoader loader) {
			if (loader == null) throw new IllegalArgumentException("loader can't be null");
			classLoaders.add(loader);
			for (Configuration c : ServiceLoader.load(Configuration.class, loader)) {
				boolean hasConfiguration = false;
				Class<?> manifest = c.getClass();
				for (Configuration cur : configurations) {
					if (cur.getClass() == manifest) {
						hasConfiguration = true;
						break;
					}
				}
				if (!hasConfiguration) {
					fromServiceLoader++;
					configurations.add(c);
				}
			}
			return this;
		}

		/**
		 * By default doubles are not deserialized into an exact value in some rare edge cases.
		 *
		 * @param errorInfo information about error in parsing exception
		 * @return itself
		 */
		public Settings<TContext> errorInfo(JsonReader.ErrorInfo errorInfo) {
			if (errorInfo == null) throw new IllegalArgumentException("errorInfo can't be null");
			this.errorInfo = errorInfo;
			return this;
		}

		/**
		 * By default doubles are not deserialized into an exact value in some rare edge cases.
		 *
		 * @param precision type of double deserialization
		 * @return itself
		 */
		public Settings<TContext> doublePrecision(JsonReader.DoublePrecision precision) {
			if (precision == null) throw new IllegalArgumentException("precision can't be null");
			this.doublePrecision = precision;
			return this;
		}

		/**
		 * When processing JSON without a schema numbers can be deserialized in various ways:
		 *
		 *  - as longs and decimals
		 *  - as longs and doubles
		 *  - as decimals only
		 *  - as doubles only
		 *
		 *  Default is as long and BigDecimal
		 *
		 * @param unknownNumbers how to deserialize numbers without a schema
		 * @return itself
		 */
		public Settings<TContext> unknownNumbers(JsonReader.UnknownNumberParsing unknownNumbers) {
			if (unknownNumbers == null) throw new IllegalArgumentException("unknownNumbers can't be null");
			this.unknownNumbers = unknownNumbers;
			return this;
		}

		/**
		 * Specify maximum allowed size for digits buffer. Default is 512.
		 * Digits buffer is used when processing strange/large input numbers.
		 *
		 * @param size maximum allowed size for digit buffer
		 * @return itself
		 */
		public Settings<TContext> limitDigitsBuffer(int size) {
			if (size < 1) throw new IllegalArgumentException("size can't be smaller than 1");
			this.maxNumberDigits = size;
			return this;
		}

		/**
		 * Specify maximum allowed size for string buffer. Default is 128MB
		 * To protect against malicious inputs, maximum allowed string buffer can be reduced.
		 *
		 * @param size maximum size of buffer in bytes
		 * @return itself
		 */
		public Settings<TContext> limitStringBuffer(int size) {
			if (size < 1) throw new IllegalArgumentException("size can't be smaller than 1");
			this.maxStringBuffer = size;
			return this;
		}

		/**
		 * When there are multiple constructors, pick the one marked with annotation.
		 * When markers is allowed on non public targets, attempt at visibility change will be done in runtime.
		 *
		 * @param marker           annotation used for marking constructor or static method factory
		 * @param expandVisibility should consider annotation declared on non public accessor
		 * @return itself
		 */
		public Settings<TContext> creatorMarker(Class<? extends Annotation> marker, boolean expandVisibility) {
			if (marker == null) throw new IllegalArgumentException("marker can't be null");
			this.creatorMarkers.put(marker, expandVisibility);
			return this;
		}

		/**
		 * Configure DslJson with custom Configuration during startup.
		 * Configurations are extension points for setting up readers/writers during DslJson initialization.
		 *
		 * @param conf custom extensibility point
		 * @return itself
		 */
		public Settings<TContext> with(Configuration conf) {
			if (conf == null) throw new IllegalArgumentException("conf can't be null");
			configurations.add(conf);
			return this;
		}

		private Settings<TContext> with(Iterable<Configuration> confs) {
			if (confs != null) {
				for (Configuration c : confs)
					configurations.add(c);
			}
			return this;
		}
	}

	/**
	 * Simple initialization entry point.
	 * Will provide null for TContext
	 * Java graphics readers/writers will not be registered.
	 * Fallback will not be configured.
	 * Key cache will be enables, values cache will be disabled.
	 * Default ServiceLoader.load method will be used to setup services from META-INF
	 */
	public DslJson() {
		this(new Settings<TContext>().includeServiceLoader());
	}

	/**
	 * Will be removed. Use DslJson(Settings) instead.
	 * Fully configurable entry point.
	 *
	 * @param context       context instance which can be provided to deserialized objects. Use null if not sure
	 * @param javaSpecifics register Java graphics specific classes such as java.awt.Point, Image, ...
	 * @param fallback      in case of unsupported type, try serialization/deserialization through external API
	 * @param omitDefaults  should serialization produce minified JSON (omit nulls and default values)
	 * @param keyCache      parsed keys can be cached (this is only used in small subset of parsing)
	 * @param serializers   additional serializers/deserializers which will be immediately registered into readers/writers
	 */
	@Deprecated
	public DslJson(
			@Nullable final TContext context,
			final boolean javaSpecifics,
			@Nullable final Fallback<TContext> fallback,
			final boolean omitDefaults,
			@Nullable final StringCache keyCache,
			final Iterable<Configuration> serializers) {
		this(new Settings<TContext>()
				.withContext(context)
				.withJavaConverters(javaSpecifics)
				.fallbackTo(fallback)
				.skipDefaultValues(omitDefaults)
				.useKeyCache(keyCache)
				.with(serializers)
		);
	}

	/**
	 * Fully configurable entry point.
	 * Provide settings for DSL-JSON initialization.
	 *
	 * @param settings DSL-JSON configuration
	 */
	public DslJson(final Settings<TContext> settings) {
		if (settings == null) throw new IllegalArgumentException("settings can't be null");
		final DslJson<TContext> self = this;
		this.localWriter = new ThreadLocal<JsonWriter>() {
			@Override
			protected JsonWriter initialValue() {
				return new JsonWriter(4096, self);
			}
		};
		this.localReader = new ThreadLocal<JsonReader>() {
			@Override
			protected JsonReader initialValue() {
				return new JsonReader<TContext>(new byte[4096], 4096, self.context, new char[64], self.keyCache, self.valuesCache, self, self.errorInfo, self.doublePrecision, self.unknownNumbers, self.maxNumberDigits, self.maxStringSize);
			}
		};
		this.context = settings.context;
		this.fallback = settings.fallback;
		this.omitDefaults = settings.omitDefaults;
		this.allowArrayFormat = settings.allowArrayFormat;
		this.keyCache = settings.keyCache;
		this.valuesCache = settings.valuesCache;
		this.unknownNumbers = settings.unknownNumbers;
		this.errorInfo = settings.errorInfo;
		this.doublePrecision = settings.doublePrecision;
		this.maxNumberDigits = settings.maxNumberDigits;
		this.maxStringSize = settings.maxStringBuffer;
		this.writerFactories.addAll(settings.writerFactories);
		this.settingsWriters = settings.writerFactories.size();
		this.readerFactories.addAll(settings.readerFactories);
		this.settingsReaders = settings.readerFactories.size();
		this.binderFactories.addAll(settings.binderFactories);
		this.settingsBinders = settings.binderFactories.size();
		this.externalConverterAnalyzer = new ExternalConverterAnalyzer(settings.classLoaders);
		this.creatorMarkers = new HashMap<Class<? extends Annotation>, Boolean>(settings.creatorMarkers);

		registerReader(byte[].class, BinaryConverter.Base64Reader);
		registerWriter(byte[].class, BinaryConverter.Base64Writer);
		registerReader(boolean.class, BoolConverter.READER);
		registerWriter(boolean.class, BoolConverter.WRITER);
		registerDefault(boolean.class, false);
		registerReader(boolean[].class, BoolConverter.ARRAY_READER);
		registerWriter(boolean[].class, BoolConverter.ARRAY_WRITER);
		registerReader(Boolean.class, BoolConverter.NULLABLE_READER);
		registerWriter(Boolean.class, BoolConverter.WRITER);
		if (settings.javaSpecifics) {
			registerJavaSpecifics(this);
		}
		registerReader(LinkedHashMap.class, ObjectConverter.MapReader);
		registerReader(HashMap.class, ObjectConverter.MapReader);
		registerReader(Map.class, ObjectConverter.MapReader);
		registerWriter(Map.class, new JsonWriter.WriteObject<Map>() {
			@Override
			public void write(JsonWriter writer, @Nullable Map value) {
				if (value == null) {
					writer.writeNull();
				} else {
					try {
						serializeMap(value, writer);
					} catch (IOException ex) {
						throw new SerializationException(ex);
					}
				}
			}
		});
		registerReader(URI.class, NetConverter.UriReader);
		registerWriter(URI.class, NetConverter.UriWriter);
		registerReader(InetAddress.class, NetConverter.AddressReader);
		registerWriter(InetAddress.class, NetConverter.AddressWriter);
		registerReader(double.class, NumberConverter.DOUBLE_READER);
		registerWriter(double.class, NumberConverter.DOUBLE_WRITER);
		registerDefault(double.class, 0.0);
		registerReader(double[].class, NumberConverter.DOUBLE_ARRAY_READER);
		registerWriter(double[].class, NumberConverter.DOUBLE_ARRAY_WRITER);
		registerReader(Double.class, NumberConverter.NULLABLE_DOUBLE_READER);
		registerWriter(Double.class, NumberConverter.DOUBLE_WRITER);
		registerReader(float.class, NumberConverter.FLOAT_READER);
		registerWriter(float.class, NumberConverter.FLOAT_WRITER);
		registerDefault(float.class, 0.0f);
		registerReader(float[].class, NumberConverter.FLOAT_ARRAY_READER);
		registerWriter(float[].class, NumberConverter.FLOAT_ARRAY_WRITER);
		registerReader(Float.class, NumberConverter.NULLABLE_FLOAT_READER);
		registerWriter(Float.class, NumberConverter.FLOAT_WRITER);
		registerReader(int.class, NumberConverter.INT_READER);
		registerWriter(int.class, NumberConverter.INT_WRITER);
		registerDefault(int.class, 0);
		registerReader(int[].class, NumberConverter.INT_ARRAY_READER);
		registerWriter(int[].class, NumberConverter.INT_ARRAY_WRITER);
		registerReader(Integer.class, NumberConverter.NULLABLE_INT_READER);
		registerWriter(Integer.class, NumberConverter.INT_WRITER);
		registerReader(short.class, NumberConverter.SHORT_READER);
		registerWriter(short.class, NumberConverter.SHORT_WRITER);
		registerDefault(short.class, (short)0);
		registerReader(short[].class, NumberConverter.SHORT_ARRAY_READER);
		registerWriter(short[].class, NumberConverter.SHORT_ARRAY_WRITER);
		registerReader(Short.class, NumberConverter.NULLABLE_SHORT_READER);
		registerWriter(Short.class, NumberConverter.SHORT_WRITER);
		registerReader(long.class, NumberConverter.LONG_READER);
		registerWriter(long.class, NumberConverter.LONG_WRITER);
		registerDefault(long.class, 0L);
		registerReader(long[].class, NumberConverter.LONG_ARRAY_READER);
		registerWriter(long[].class, NumberConverter.LONG_ARRAY_WRITER);
		registerReader(Long.class, NumberConverter.NULLABLE_LONG_READER);
		registerWriter(Long.class, NumberConverter.LONG_WRITER);
		registerReader(BigDecimal.class, NumberConverter.DecimalReader);
		registerWriter(BigDecimal.class, NumberConverter.DecimalWriter);
		registerReader(String.class, StringConverter.READER);
		registerWriter(String.class, StringConverter.WRITER);
		registerReader(UUID.class, UUIDConverter.READER);
		registerWriter(UUID.class, UUIDConverter.WRITER);
		registerReader(Number.class, NumberConverter.NumberReader);
		registerWriter(CharSequence.class, StringConverter.WRITER_CHARS);
		registerReader(StringBuilder.class, StringConverter.READER_BUILDER);
		registerReader(StringBuffer.class, StringConverter.READER_BUFFER);

		for (Configuration serializer : settings.configurations) {
			serializer.configure(this);
		}
		if (!settings.classLoaders.isEmpty() && settings.fromServiceLoader == 0) {
			//TODO: workaround common issue with failed services registration. try to load common external name if exists
			loadDefaultConverters(this, settings.classLoaders, "dsl_json_Annotation_Processor_External_Serialization");
			loadDefaultConverters(this, settings.classLoaders, "dsl_json.json.ExternalSerialization");
			loadDefaultConverters(this, settings.classLoaders, "dsl_json_ExternalSerialization");
		}
	}

	/**
	 * Simplistic string cache implementation.
	 * It uses a fixed String[] structure in which it caches string value based on it's hash.
	 * Eg, hash &amp; mask provide index into the structure. Different string with same hash will overwrite the previous one.
	 */
	public static class SimpleStringCache implements StringCache {

		private final int mask;
		private final String[] cache;

		/**
		 * Will use String[] with 1024 elements.
		 */
		public SimpleStringCache() {
			this(10);
		}

		public SimpleStringCache(int log2Size) {
			int size = 2;
			for (int i = 1; i < log2Size; i++) {
				size *= 2;
			}
			mask = size - 1;
			cache = new String[size];
		}

		/**
		 * Calculates hash of the provided "string" and looks it up from the String[]
		 * It it doesn't exists of a different string is already there a new String instance is created
		 * and saved into the String[]
		 *
		 * @param chars buffer into which string was parsed
		 * @param len the string length inside the buffer
		 * @return String instance matching the char[]/int pair
		 */
		@Override
		public String get(char[] chars, int len) {
			long hash = 0x811c9dc5;
			for (int i = 0; i < len; i++) {
				hash ^= (byte) chars[i];
				hash *= 0x1000193;
			}
			final int index = (int) hash & mask;
			final String value = cache[index];
			if (value == null) return createAndPut(index, chars, len);
			if (value.length() != len) return createAndPut(index, chars, len);
			for (int i = 0; i < value.length(); i++) {
				if (value.charAt(i) != chars[i]) return createAndPut(index, chars, len);
			}
			return value;
		}

		private String createAndPut(int index, char[] chars, int len) {
			final String value = new String(chars, 0, len);
			cache[index] = value;
			return value;
		}
	}

	/**
	 * Create a writer bound to this DSL-JSON.
	 * Ideally it should be reused.
	 * Bound writer can use lookups to find custom writers.
	 * This can be used to serialize unknown types such as Object.class
	 *
	 * @return bound writer
	 */
	public JsonWriter newWriter() {
		return new JsonWriter(this);
	}

	/**
	 * Create a writer bound to this DSL-JSON.
	 * Ideally it should be reused.
	 * Bound writer can use lookups to find custom writers.
	 * This can be used to serialize unknown types such as Object.class
	 *
	 * @param size initial buffer size
	 * @return bound writer
	 */
	public JsonWriter newWriter(int size) {
		return new JsonWriter(size, this);
	}

	/**
	 * Create a writer bound to this DSL-JSON.
	 * Ideally it should be reused.
	 * Bound writer can use lookups to find custom writers.
	 * This can be used to serialize unknown types such as Object.class
	 *
	 * @param buffer initial buffer
	 * @return bound writer
	 */
	public JsonWriter newWriter(byte[] buffer) {
		if (buffer == null) throw new IllegalArgumentException("null value provided for buffer");
		return new JsonWriter(buffer, this);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * This reader can be reused via process method.
	 *
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader() {
		return new JsonReader<TContext>(new byte[4096], 4096, context, new char[64], keyCache, valuesCache, this, errorInfo, doublePrecision, unknownNumbers, maxNumberDigits, maxStringSize);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * This reader can be reused via process method.
	 *
	 * @param bytes input bytes
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader(byte[] bytes) {
		return new JsonReader<TContext>(bytes, bytes.length, context, new char[64], keyCache, valuesCache, this, errorInfo, doublePrecision, unknownNumbers, maxNumberDigits, maxStringSize);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * This reader can be reused via process method.
	 *
	 * @param bytes  input bytes
	 * @param length use input bytes up to specified length
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader(byte[] bytes, int length) {
		return new JsonReader<TContext>(bytes, length, context, new char[64], keyCache, valuesCache, this, errorInfo, doublePrecision, unknownNumbers, maxNumberDigits, maxStringSize);
	}


	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * Pass in initial string buffer.
	 * This reader can be reused via process method.
	 *
	 * @param bytes  input bytes
	 * @param length use input bytes up to specified length
	 * @param tmp string parsing buffer
	 * @return bound reader
	 */
	public JsonReader<TContext> newReader(byte[] bytes, int length, char[] tmp) {
		return new JsonReader<TContext>(bytes, length, context, tmp, keyCache, valuesCache, this, errorInfo, doublePrecision, unknownNumbers, maxNumberDigits, maxStringSize);
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * Created reader can be reused (using process method).
	 * This is convenience method for creating a new reader and binding it to stream.
	 *
	 * @param stream input stream
	 * @param buffer temporary buffer
	 * @return bound reader
	 * @throws java.io.IOException unable to read from stream
	 */
	public JsonReader<TContext> newReader(InputStream stream, byte[] buffer) throws IOException {
		final JsonReader<TContext> reader = newReader(buffer);
		reader.process(stream);
		return reader;
	}

	/**
	 * Create a reader bound to this DSL-JSON.
	 * Bound reader can reuse key cache (which is used during Map deserialization)
	 * This method id Deprecated since it should be avoided.
	 * It's better to use byte[] or InputStream based readers
	 *
	 * @param input JSON string
	 * @return bound reader
	 */
	@Deprecated
	public JsonReader<TContext> newReader(String input) {
		final byte[] bytes = input.getBytes(UTF8);
		return new JsonReader<TContext>(bytes, bytes.length, context, new char[64], keyCache, valuesCache, this, errorInfo, doublePrecision, unknownNumbers, maxNumberDigits, maxStringSize);
	}

	private static void loadDefaultConverters(final DslJson json, Set<ClassLoader> loaders, final String name) {
		for (ClassLoader loader : loaders) {
			try {
				Class<?> external = loader.loadClass(name);
				Configuration instance = (Configuration) external.newInstance();
				instance.configure(json);
			} catch (NoClassDefFoundError ignore) {
			} catch (Exception ignore) {
			}
		}
	}

	static void registerJavaSpecifics(final DslJson json) {
		json.registerReader(Element.class, XmlConverter.Reader);
		json.registerWriter(Element.class, XmlConverter.Writer);
	}

	private final Map<Type, Object> defaults = new ConcurrentHashMap<Type, Object>();

	public <T> void registerDefault(Class<T> manifest, T instance) {
		defaults.put(manifest, instance);
	}

	@SuppressWarnings("unchecked")
	public boolean registerWriterFactory(ConverterFactory<? extends JsonWriter.WriteObject> factory) {
		if (factory == null) throw new IllegalArgumentException("factory can't be null");
		if (writerFactories.contains(factory)) return false;
		writerFactories.add(writerFactories.size() - settingsWriters, (ConverterFactory<JsonWriter.WriteObject>) factory);
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean registerReaderFactory(ConverterFactory<? extends JsonReader.ReadObject> factory) {
		if (factory == null) throw new IllegalArgumentException("factory can't be null");
		if (readerFactories.contains(factory)) return false;
		readerFactories.add(readerFactories.size() - settingsReaders, (ConverterFactory<JsonReader.ReadObject>) factory);
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean registerBinderFactory(ConverterFactory<? extends JsonReader.BindObject> factory) {
		if (factory == null) throw new IllegalArgumentException("factory can't be null");
		if (binderFactories.contains(factory)) return false;
		binderFactories.add(binderFactories.size() - settingsBinders, (ConverterFactory<JsonReader.BindObject>) factory);
		return true;
	}

	@Nullable
	public final Object getDefault(@Nullable Type manifest) {
		if (manifest == null) return null;
		Object instance = defaults.get(manifest);
		if (instance != null) return instance;
		final Class<?> rawType;
		if (manifest instanceof Class<?>) {
			rawType = (Class<?>) manifest;
		} else if (manifest instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) manifest;
			rawType = (Class<?>) pt.getRawType();
		} else return null;
		if (rawType.isPrimitive()) {
			return Array.get(Array.newInstance(rawType, 1), 0);
		}
		return defaults.get(rawType);
	}

	private final ConcurrentMap<Class<?>, JsonReader.ReadJsonObject<JsonObject>> objectReaders =
			new ConcurrentHashMap<Class<?>, JsonReader.ReadJsonObject<JsonObject>>();

	private final ConcurrentMap<Type, JsonReader.ReadObject> readers = new ConcurrentHashMap<Type, JsonReader.ReadObject>();
	private final ConcurrentMap<Type, JsonReader.BindObject> binders = new ConcurrentHashMap<Type, JsonReader.BindObject>();
	private final ConcurrentMap<Type, JsonWriter.WriteObject> writers = new ConcurrentHashMap<Type, JsonWriter.WriteObject>();


	public final Set<Type> getRegisteredDecoders() {
		return readers.keySet();
	}

	public final Set<Type> getRegisteredBinders() {
		return binders.keySet();
	}

	public final Set<Type> getRegisteredEncoders() {
		return writers.keySet();
	}

	public final Map<Class<? extends Annotation>, Boolean> getRegisteredCreatorMarkers() {
		return creatorMarkers;
	}

	/**
	 * Register custom reader for specific type (JSON -&gt; instance conversion).
	 * Reader is used for conversion from input byte[] -&gt; target object instance
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a reader this will disable deserialization of specified type
	 *
	 * @param manifest specified type
	 * @param reader   provide custom implementation for reading JSON into an object instance
	 * @param <T>      type
	 * @param <S>      type or subtype
	 */
	public <T, S extends T> void registerReader(final Class<T> manifest, @Nullable final JsonReader.ReadObject<S> reader) {
		if (reader == null) readers.remove(manifest);
		else readers.put(manifest, reader);
	}

	/**
	 * Register custom reader for specific type (JSON -&gt; instance conversion).
	 * Reader is used for conversion from input byte[] -&gt; target object instance
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a reader this will disable deserialization of specified type
	 *
	 * @param manifest specified type
	 * @param reader   provide custom implementation for reading JSON into an object instance
	 * @return old registered value
	 */
	@Nullable
	public JsonReader.ReadObject registerReader(final Type manifest, @Nullable final JsonReader.ReadObject<?> reader) {
		if (reader == null) return readers.remove(manifest);
		try {
			return readers.get(manifest);
		} finally {
			readers.put(manifest, reader);
		}
	}

	/**
	 * Register custom binder for specific type (JSON -&gt; instance conversion).
	 * Binder is used for conversion from input byte[] -&gt; existing target object instance.
	 * It's similar to reader, with the difference that it accepts target instance.
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a binder this will disable binding of specified type
	 *
	 * @param manifest specified type
	 * @param binder   provide custom implementation for binding JSON to an object instance
	 * @param <T>      type
	 * @param <S>      type or subtype
	 */
	public <T, S extends T> void registerBinder(final Class<T> manifest, @Nullable final JsonReader.BindObject<S> binder) {
		if (binder == null) binders.remove(manifest);
		else binders.put(manifest, binder);
	}

	/**
	 * Register custom binder for specific type (JSON -&gt; instance conversion).
	 * Binder is used for conversion from input byte[] -&gt; existing target object instance.
	 * It's similar to reader, with the difference that it accepts target instance.
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a binder this will disable binding of specified type
	 *
	 * @param manifest specified type
	 * @param binder   provide custom implementation for binding JSON to an object instance
	 */
	public void registerBinder(final Type manifest, @Nullable final JsonReader.BindObject<?> binder) {
		if (binder == null) binders.remove(manifest);
		else binders.put(manifest, binder);
	}

	/**
	 * Register custom writer for specific type (instance -&gt; JSON conversion).
	 * Writer is used for conversion from object instance -&gt; output byte[]
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a writer this will disable serialization of specified type
	 *
	 * @param manifest specified type
	 * @param writer   provide custom implementation for writing JSON from object instance
	 * @param <T>      type
	 */
	public <T> void registerWriter(final Class<T> manifest, @Nullable final JsonWriter.WriteObject<T> writer) {
		if (writer == null) {
			writerMap.remove(manifest);
			writers.remove(manifest);
		} else {
			writerMap.put(manifest, manifest);
			writers.put(manifest, writer);
		}
	}

	/**
	 * Register custom writer for specific type (instance -&gt; JSON conversion).
	 * Writer is used for conversion from object instance -&gt; output byte[]
	 * <p>
	 * Types registered through @CompiledJson annotation should be registered automatically through
	 * ServiceLoader.load method and you should not be registering them manually.
	 * <p>
	 * If null is registered for a writer this will disable serialization of specified type
	 *
	 * @param manifest specified type
	 * @param writer   provide custom implementation for writing JSON from object instance
	 * @return old registered value
	 */
	@Nullable
	public JsonWriter.WriteObject registerWriter(final Type manifest, @Nullable final JsonWriter.WriteObject<?> writer) {
		if (writer == null) return writers.remove(manifest);
		try {
			return writers.get(manifest);
		} finally {
			writers.put(manifest, writer);
		}
	}

	private final ConcurrentMap<Class<?>, Class<?>> writerMap = new ConcurrentHashMap<Class<?>, Class<?>>();

	/**
	 * Try to find registered writer for provided type.
	 * If writer is not found, null will be returned.
	 * If writer for exact type is not found, type hierarchy will be scanned for base writer.
	 * <p>
	 * Writer is used for conversion from object instance into JSON representation.
	 *
	 * @param manifest specified type
	 * @return writer for specified type if found
	 */
	@Nullable
	public JsonWriter.WriteObject<?> tryFindWriter(final Type manifest) {
		JsonWriter.WriteObject writer = writers.get(manifest);
		if (writer != null) return writer;
		final Type actualType = extractActualType(manifest);
		if (actualType != manifest) {
			writer = writers.get(actualType);
			if (writer != null) {
				writers.putIfAbsent(manifest, writer);
				return writer;
			}
		}
		if (actualType instanceof Class<?>) {
			final Class<?> signature = (Class<?>) actualType;
			if (JsonObject.class.isAssignableFrom(signature)) {
				writers.putIfAbsent(manifest, OBJECT_WRITER);
				return OBJECT_WRITER;
			}
		}
		writer = lookupFromFactories(manifest, actualType, writerFactories, writers);
		if (writer != null) return writer;
		if (!(actualType instanceof Class<?>)) return null;
		Class<?> found = writerMap.get(actualType);
		if (found != null) {
			return writers.get(found);
		}
		Class<?> container = (Class<?>) actualType;
		final ArrayList<Class<?>> signatures = new ArrayList<Class<?>>();
		findAllSignatures(container, signatures);
		for (final Class<?> sig : signatures) {
			writer = writers.get(sig);
			if (writer == null) {
				writer = lookupFromFactories(manifest, sig, writerFactories, writers);
			}
			if (writer != null) {
				writerMap.putIfAbsent(container, sig);
				return writer;
			}
		}
		return null;
	}

	private static Type extractActualType(final Type manifest) {
		if (manifest instanceof WildcardType) {
			WildcardType wt = (WildcardType) manifest;
			if (wt.getUpperBounds().length == 1 && wt.getLowerBounds().length == 0) {
				return wt.getUpperBounds()[0];
			}
		}
		return manifest;
	}

	private <T> void checkExternal(final Type manifest, final ConcurrentMap<Type, T> cache) {
		if (manifest instanceof Class<?>) {
			externalConverterAnalyzer.tryFindConverter((Class<?>) manifest, this);
		} else if (manifest instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) manifest;
			Type container = pt.getRawType();
			externalConverterAnalyzer.tryFindConverter((Class<?>) container, this);
			for (Type arg : pt.getActualTypeArguments()) {
				if (!cache.containsKey(arg)) {
					Type actualType = extractActualType(arg);
					if (actualType != arg && !cache.containsKey(actualType)) {
						checkExternal(actualType, cache);
					}
				}
			}
		}
	}

	@Nullable
	private <T> T lookupFromFactories(
			final Type signature,
			final Type manifest,
			final List<ConverterFactory<T>> factories,
			final ConcurrentMap<Type, T> cache) {
		if (manifest instanceof Class<?>) {
			externalConverterAnalyzer.tryFindConverter((Class<?>) manifest, this);
			T found = cache.get(manifest);
			if (found != null) return found;
		} else if (manifest instanceof ParameterizedType) {
			checkExternal(manifest, cache);
		}

		for (ConverterFactory<T> wrt : factories) {
			final T converter = wrt.tryCreate(manifest, this);
			if (converter != null) {
				cache.putIfAbsent(signature, converter);
				return converter;
			}
		}
		return null;
	}

	/**
	 * Try to find registered reader for provided type.
	 * If reader is not found, null will be returned.
	 * Exact match must be found, type hierarchy will not be scanned for alternative readers.
	 * <p>
	 * If you wish to use alternative reader for specific type, register it manually with something along the lines of
	 * <pre>
	 *     DslJson dslJson = ...
	 *     dslJson.registerReader(Interface.class, dslJson.tryFindReader(Implementation.class));
	 * </pre>
	 *
	 * @param manifest specified type
	 * @return found reader for specified type
	 */
	@Nullable
	public JsonReader.ReadObject<?> tryFindReader(final Type manifest) {
		JsonReader.ReadObject found = readers.get(manifest);
		if (found != null) return found;
		final Type actualType = extractActualType(manifest);
		if (actualType != manifest) {
			found = readers.get(actualType);
			if (found != null) {
				readers.putIfAbsent(manifest, found);
				return found;
			}
		}
		if (actualType instanceof Class<?>) {
			final Class<?> signature = (Class<?>) actualType;
			if (JsonObject.class.isAssignableFrom(signature)) {
				final JsonReader.ReadJsonObject<?> decoder = getObjectReader(signature);
				if (decoder != null) {
					found = convertToReader(decoder);
					readers.putIfAbsent(manifest, found);
					return found;
				}
			}
		}
		return lookupFromFactories(manifest, actualType, readerFactories, readers);
	}

	/**
	 * Try to find registered binder for provided type.
	 * If binder is not found, null will be returned.
	 * Exact match must be found, type hierarchy will not be scanned for alternative binders.
	 * <p>
	 * If you wish to use alternative binder for specific type, register it manually with something along the lines of
	 * <pre>
	 *     DslJson dslJson = ...
	 *     dslJson.registerBinder(Interface.class, dslJson.tryFindBinder(Implementation.class));
	 * </pre>
	 *
	 * @param manifest specified type
	 * @return found reader for specified type
	 */
	@Nullable
	public JsonReader.BindObject<?> tryFindBinder(final Type manifest) {
		JsonReader.BindObject found = binders.get(manifest);
		if (found != null) return found;
		final Type actualType = extractActualType(manifest);
		if (actualType != manifest) {
			found = binders.get(actualType);
			if (found != null) {
				binders.putIfAbsent(manifest, found);
				return found;
			}
		}
		return lookupFromFactories(manifest, actualType, binderFactories, binders);
	}

	/**
	 * Try to find registered writer for provided type.
	 * If writer is not found, null will be returned.
	 * If writer for exact type is not found, type hierarchy will be scanned for base writer.
	 * <p>
	 * Writer is used for conversion from object instance into JSON representation.
	 *
	 * @param manifest specified class
	 * @param <T> specified type
	 * @return found writer for specified class or null
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> JsonWriter.WriteObject<T> tryFindWriter(final Class<T> manifest) {
		return (JsonWriter.WriteObject<T>) tryFindWriter((Type) manifest);
	}

	/**
	 * Try to find registered reader for provided type.
	 * If reader is not found, null will be returned.
	 * Exact match must be found, type hierarchy will not be scanned for alternative reader.
	 * <p>
	 * If you wish to use alternative reader for specific type, register it manually with something along the lines of
	 * <pre>
	 *     DslJson dslJson = ...
	 *     dslJson.registerReader(Interface.class, dslJson.tryFindReader(Implementation.class));
	 * </pre>
	 *
	 * @param manifest specified class
	 * @param <T> specified type
	 * @return found reader for specified class or null
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> JsonReader.ReadObject<T> tryFindReader(final Class<T> manifest) {
		return (JsonReader.ReadObject<T>) tryFindReader((Type) manifest);
	}

	/**
	 * Try to find registered binder for provided type.
	 * If binder is not found, null will be returned.
	 * Exact match must be found, type hierarchy will not be scanned for alternative binder.
	 * <p>
	 * If you wish to use alternative binder for specific type, register it manually with something along the lines of
	 * <pre>
	 *     DslJson dslJson = ...
	 *     dslJson.registerBinder(Interface.class, dslJson.tryFindBinder(Implementation.class));
	 * </pre>
	 *
	 * @param manifest specified class
	 * @param <T> specified type
	 * @return found reader for specified class or null
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> JsonReader.BindObject<T> tryFindBinder(final Class<T> manifest) {
		return (JsonReader.BindObject<T>) tryFindBinder((Type) manifest);
	}

	private static void findAllSignatures(final Class<?> manifest, final ArrayList<Class<?>> found) {
		if (found.contains(manifest)) {
			return;
		}
		found.add(manifest);
		final Class<?> superClass = manifest.getSuperclass();
		if (superClass != null && superClass != Object.class) {
			findAllSignatures(superClass, found);
		}
		for (final Class<?> iface : manifest.getInterfaces()) {
			findAllSignatures(iface, found);
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private JsonReader.ReadJsonObject<JsonObject> probeForObjectReader(Class<?> manifest, Object instance) {
		Object found;
		try {
			found = manifest.getField("JSON_READER").get(instance);
		} catch (Exception ignore) {
			try {
				found = manifest.getMethod("JSON_READER").invoke(instance);
			} catch (Exception ignore2) {
				try {
					found = manifest.getMethod("getJSON_READER").invoke(instance);
				} catch (Exception ignore3) {
					return null;
				}
			}
		}
		return found instanceof JsonReader.ReadJsonObject
				? (JsonReader.ReadJsonObject<JsonObject>)found
				: null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected final JsonReader.ReadJsonObject<JsonObject> getObjectReader(final Class<?> manifest) {
		try {
			JsonReader.ReadJsonObject<JsonObject> reader = objectReaders.get(manifest);
			if (reader == null) {
				reader = probeForObjectReader(manifest, null);
				if (reader == null) {
					//probe in few special places
					try {
						Object companion = manifest.getField("Companion").get(null);
						reader = probeForObjectReader(companion.getClass(), companion);
					} catch (Exception ignore) {
						return null;
					}
				}
				if (reader != null) {
					objectReaders.putIfAbsent(manifest, reader);
				}
			}
			return reader;
		} catch (final Exception ignore) {
			return null;
		}
	}

	public void serializeMap(final Map<String, Object> value, final JsonWriter sw) throws IOException {
		sw.writeByte(JsonWriter.OBJECT_START);
		final int size = value.size();
		if (size > 0) {
			final Iterator<Map.Entry<String, Object>> iterator = value.entrySet().iterator();
			Map.Entry<String, Object> kv = iterator.next();
			sw.writeString(kv.getKey());
			sw.writeByte(JsonWriter.SEMI);
			serialize(sw, kv.getValue());
			for (int i = 1; i < size; i++) {
				sw.writeByte(JsonWriter.COMMA);
				kv = iterator.next();
				sw.writeString(kv.getKey());
				sw.writeByte(JsonWriter.SEMI);
				serialize(sw, kv.getValue());
			}
		}
		sw.writeByte(JsonWriter.OBJECT_END);
	}

	@Deprecated
	@Nullable
	public static Object deserializeObject(final JsonReader reader) throws IOException {
		return ObjectConverter.deserializeObject(reader);
	}

	/**
	 * Will be removed
	 * @param reader JSON reader
	 * @return deseralized list
	 * @throws IOException error during parsing
	 */
	@Deprecated
	public static ArrayList<Object> deserializeList(final JsonReader reader) throws IOException {
		return ObjectConverter.deserializeList(reader);
	}

	/**
	 * Will be removed
	 * @param reader JSON reader
	 * @return deserialized map
	 * @throws IOException error during parsing
	 */
	@Deprecated
	public static LinkedHashMap<String, Object> deserializeMap(final JsonReader reader) throws IOException {
		return ObjectConverter.deserializeMap(reader);
	}

	private static Object convertResultToArray(Class<?> elementType, List<?> result) {
		if (elementType.isPrimitive()) {
			if (boolean.class.equals(elementType)) {
				boolean[] array = new boolean[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Boolean) result.get(i);
				}
				return array;
			} else if (int.class.equals(elementType)) {
				int[] array = new int[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Integer) result.get(i);
				}
				return array;
			} else if (long.class.equals(elementType)) {
				long[] array = new long[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Long) result.get(i);
				}
				return array;
			} else if (short.class.equals(elementType)) {
				short[] array = new short[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Short) result.get(i);
				}
				return array;
			} else if (byte.class.equals(elementType)) {
				byte[] array = new byte[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Byte) result.get(i);
				}
				return array;
			} else if (float.class.equals(elementType)) {
				float[] array = new float[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Float) result.get(i);
				}
				return array;
			} else if (double.class.equals(elementType)) {
				double[] array = new double[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Double) result.get(i);
				}
				return array;
			} else if (char.class.equals(elementType)) {
				char[] array = new char[result.size()];
				for (int i = 0; i < result.size(); i++) {
					array[i] = (Character) result.get(i);
				}
				return array;
			}
		}
		return result.toArray((Object[]) Array.newInstance(elementType, 0));
	}

	/**
	 * Check if DslJson knows how to serialize a type.
	 * It will check if a writer for such type exists or can be used.
	 *
	 * @param manifest type to check
	 * @return can serialize this type into JSON
	 */
	public final boolean canSerialize(final Type manifest) {
		JsonWriter.WriteObject writer = writers.get(manifest);
		if (writer != null) return true;
		if (manifest instanceof Class<?>) {
			final Class<?> content = (Class<?>) manifest;
			if (JsonObject.class.isAssignableFrom(content)) {
				return true;
			}
			if (JsonObject[].class.isAssignableFrom(content)) {
				return true;
			}
			if (tryFindWriter(manifest) != null) {
				return true;
			}
			if (content.isArray()) {
				return !content.getComponentType().isArray()
						&& !Collection.class.isAssignableFrom(content.getComponentType())
						&& canSerialize(content.getComponentType());
			}
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1) {
				final Class<?> container = (Class<?>) pt.getRawType();
				if (container.isArray() || Collection.class.isAssignableFrom(container)) {
					final Type content = pt.getActualTypeArguments()[0];
					return content instanceof Class<?> && JsonObject.class.isAssignableFrom((Class<?>) content)
							|| tryFindWriter(content) != null;
				}
			}
		} else if (manifest instanceof GenericArrayType) {
			final GenericArrayType gat = (GenericArrayType) manifest;
			return gat.getGenericComponentType() instanceof Class<?>
					&& JsonObject.class.isAssignableFrom((Class<?>) gat.getGenericComponentType())
					|| tryFindWriter(gat.getGenericComponentType()) != null;
		}
		for (ConverterFactory<JsonWriter.WriteObject> wrt : writerFactories) {
			if (wrt.tryCreate(manifest, this) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if DslJson knows how to deserialize a type.
	 * It will check if a reader for such type exists or can be used.
	 *
	 * @param manifest type to check
	 * @return can read this type from JSON
	 */
	public final boolean canDeserialize(final Type manifest) {
		if (tryFindReader(manifest) != null) {
			return true;
		}
		if (manifest instanceof Class<?>) {
			final Class<?> objectType = (Class<?>) manifest;
			if (objectType.isArray()) {
				return !objectType.getComponentType().isArray()
						&& !Collection.class.isAssignableFrom(objectType.getComponentType())
						&& canDeserialize(objectType.getComponentType());
			}
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1) {
				final Class<?> container = (Class<?>) pt.getRawType();
				if (container.isArray() || Collection.class.isAssignableFrom(container)) {
					final Type content = pt.getActualTypeArguments()[0];
					if (tryFindReader(content) != null) {
						return true;
					}
				}
			}
		} else if (manifest instanceof GenericArrayType) {
			final Type content = ((GenericArrayType) manifest).getGenericComponentType();
			return tryFindReader(content) != null;
		}
		return false;
	}

	/**
	 * Reusable deserialize API.
	 * For maximum performance `JsonReader` should be reused (otherwise small buffer will be allocated for processing)
	 * and `JsonReader.ReadObject` should be prepared (otherwise a lookup will be required).
	 * <p>
	 * This is mostly convenience API since it starts the processing of the JSON by calling getNextToken on JsonReader,
	 * checks for null and calls converter.read(input).
	 *
	 * @param <T> specified type
	 * @param converter target reader
	 * @param input     input JSON
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@Nullable
	public <T> T deserialize(
			final JsonReader.ReadObject<T> converter,
			final JsonReader<TContext> input) throws IOException {
		if (converter == null) {
			throw new IllegalArgumentException("converter can't be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("input can't be null");
		}
		input.getNextToken();
		return converter.read(input);
	}

	/**
	 * Convenient deserialize API for working with bytes.
	 * Deserialize provided byte input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 *
	 * @param manifest  target type
	 * @param body      input JSON
	 * @param size      length
	 * @param <TResult> target type
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> TResult deserialize(
			final Class<TResult> manifest,
			final byte[] body,
			final int size) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (body == null) {
			throw new IllegalArgumentException("body can't be null");
		}
		final JsonReader json = localReader.get().process(body, size);
		try {
			json.getNextToken();
			final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
			if (simpleReader != null) {
				return (TResult) simpleReader.read(json);
			}
			if (manifest.isArray()) {
				if (json.wasNull()) {
					return null;
				} else if (json.last() != '[') {
					throw json.newParseError("Expecting '[' for array start");
				}
				final Class<?> elementManifest = manifest.getComponentType();
				final List<?> list = deserializeList(elementManifest, body, size);
				if (list == null) {
					return null;
				}
				return (TResult) convertResultToArray(elementManifest, list);
			}
			if (fallback != null) {
				return (TResult) fallback.deserialize(context, manifest, body, size);
			}
			throw createErrorMessage(manifest);
		} finally {
			json.reset();
		}
	}

	/**
	 * Deserialize API for working with bytes.
	 * Deserialize provided byte input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 *
	 * @param manifest target type
	 * @param body     input JSON
	 * @param size     length
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@Nullable
	public Object deserialize(
			final Type manifest,
			final byte[] body,
			final int size) throws IOException {
		if (manifest instanceof Class<?>) {
			return deserialize((Class<?>) manifest, body, size);
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (body == null) {
			throw new IllegalArgumentException("body can't be null");
		}
		final JsonReader json = localReader.get().process(body, size);
		try {
			json.getNextToken();
			final Object result = deserializeWith(manifest, json);
			if (result != unknownValue) return result;
			if (fallback != null) {
				return fallback.deserialize(context, manifest, body, size);
			}
			throw new ConfigurationException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
					"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
		} finally {
			json.reset();
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected Object deserializeWith(Type manifest, JsonReader json) throws IOException {
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return simpleReader.read(json);
		}
		if (manifest instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) manifest;
			if (pt.getActualTypeArguments().length == 1) {
				final Type content = pt.getActualTypeArguments()[0];
				final Class<?> container = (Class<?>) pt.getRawType();
				if (container.isArray() || Collection.class.isAssignableFrom(container)) {
					if (json.wasNull()) {
						return null;
					} else if (json.last() != '[') {
						throw json.newParseError("Expecting '[' for array start");
					}
					if (json.getNextToken() == ']') {
						if (container.isArray()) {
							returnEmptyArray(content);
						}
						return new ArrayList<Object>(0);
					}
					final JsonReader.ReadObject<?> contentReader = tryFindReader(content);
					if (contentReader != null) {
						final ArrayList<?> result = json.deserializeNullableCollection(contentReader);
						if (container.isArray()) {
							return returnAsArray(content, result);
						}
						return result;
					}
				}
			}
		} else if (manifest instanceof GenericArrayType) {
			if (json.wasNull()) {
				return null;
			} else if (json.last() != '[') {
				throw json.newParseError("Expecting '[' for array start");
			}
			final Type content = ((GenericArrayType) manifest).getGenericComponentType();
			if (json.getNextToken() == ']') {
				return returnEmptyArray(content);
			}
			final JsonReader.ReadObject<?> contentReader = tryFindReader(content);
			if (contentReader != null) {
				final ArrayList<?> result = json.deserializeNullableCollection(contentReader);
				return returnAsArray(content, result);
			}
		}
		return unknownValue;
	}

	private static Object returnAsArray(final Type content, final ArrayList<?> result) {
		if (content instanceof Class<?>) {
			return convertResultToArray((Class<?>) content, result);
		}
		if (content instanceof ParameterizedType) {
			final ParameterizedType cpt = (ParameterizedType) content;
			return result.toArray((Object[]) Array.newInstance((Class<?>) cpt.getRawType(), 0));
		}
		return result.toArray();
	}

	private static Object returnEmptyArray(Type content) {
		if (content instanceof Class<?>) {
			return Array.newInstance((Class<?>) content, 0);
		}
		if (content instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) content;
			return Array.newInstance((Class<?>) pt.getRawType(), 0);
		}
		return new Object[0];
	}

	protected IOException createErrorMessage(final Class<?> manifest) {
		final ArrayList<Class<?>> signatures = new ArrayList<Class<?>>();
		findAllSignatures(manifest, signatures);
		for (final Class<?> sig : signatures) {
			if (readers.containsKey(sig)) {
				if (sig.equals(manifest)) {
					return new IOException("Reader for provided type: " + manifest + " is disabled and fallback serialization is not registered (converter is registered as null).\n" +
							"Try initializing system with custom fallback or don't register null for " + manifest);
				}
				return new IOException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
						"Found reader for: " + sig + " so try deserializing into that instead?\n" +
						"Alternatively, try initializing system with custom fallback or register specified type using registerReader into " + getClass());
			}
		}
		return new IOException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
				"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
	}

	/**
	 * Convenient deserialize list API for working with bytes.
	 * Deserialize provided byte input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 *
	 * @param manifest  target type
	 * @param body      input JSON
	 * @param size      length
	 * @param <TResult> target element type
	 * @return deserialized list instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> List<TResult> deserializeList(
			final Class<TResult> manifest,
			final byte[] body,
			final int size) throws IOException {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (body == null) throw new IllegalArgumentException("body can't be null");
		if (size == 4 && body[0] == 'n' && body[1] == 'u' && body[2] == 'l' && body[3] == 'l') {
			return null;
		} else if (size == 2 && body[0] == '[' && body[1] == ']') {
			return new ArrayList<TResult>(0);
		}
		final JsonReader json = localReader.get().process(body, size);
		try {
			if (json.getNextToken() != '[') {
				if (json.wasNull()) {
					return null;
				}
				throw json.newParseError("Expecting '[' for list start");
			}
			if (json.getNextToken() == ']') {
				return new ArrayList<TResult>(0);
			}
			//leave for now in to avoid overhead of going through redirection via generic tryFindReader
			if (JsonObject.class.isAssignableFrom(manifest)) {
				final JsonReader.ReadJsonObject<JsonObject> reader = getObjectReader(manifest);
				if (reader != null) {
					return (List<TResult>) json.deserializeNullableCollection(reader);
				}
			}
			final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
			if (simpleReader != null) {
				return json.deserializeNullableCollection(simpleReader);
			}
			if (fallback != null) {
				final Object array = Array.newInstance(manifest, 0);
				final TResult[] result = (TResult[]) fallback.deserialize(context, array.getClass(), body, size);
				if (result == null) {
					return null;
				}
				final ArrayList<TResult> list = new ArrayList<TResult>(result.length);
				for (TResult aResult : result) {
					list.add(aResult);
				}
				return list;
			}
			throw createErrorMessage(manifest);
		} finally {
			json.reset();
		}
	}

	/**
	 * This is deprecated to avoid using it.
	 * Use deserializeList method without the buffer argument instead.
	 *
	 * Convenient deserialize list API for working with streams.
	 * Deserialize provided stream input into target object.
	 * Use buffer for internal conversion from stream into byte[] for partial processing.
	 * This method creates a new instance of JsonReader.
	 * There is also deserializeList without the buffer which reuses thread local reader.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * Provided buffer will be used as input for partial processing.
	 * <p>
	 * For best performance buffer should be reused.
	 *
	 * @param manifest  target type
	 * @param stream    input JSON
	 * @param buffer    buffer used for InputStream -&gt; byte[] conversion
	 * @param <TResult> target element type
	 * @return deserialized list
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> List<TResult> deserializeList(
			final Class<TResult> manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (stream == null) throw new IllegalArgumentException("stream can't be null");
		if (buffer == null) throw new IllegalArgumentException("buffer can't be null");
		return deserializeList(manifest, newReader(stream, buffer), stream);
	}

	/**
	 * Convenient deserialize list API for working with streams.
	 * Deserialize provided stream input into target object.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * <p>
	 *
	 * @param manifest  target type
	 * @param stream    input JSON
	 * @param <TResult> target element type
	 * @return deserialized list
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> List<TResult> deserializeList(
			final Class<TResult> manifest,
			final InputStream stream) throws IOException {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (stream == null) throw new IllegalArgumentException("stream can't be null");

		final JsonReader json = localReader.get().process(stream);
		try {
			return deserializeList(manifest, json, stream);
		} finally {
			json.reset();
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected <TResult> List<TResult> deserializeList(
			final Class<TResult> manifest,
			JsonReader<TContext> json,
			InputStream stream) throws IOException {
		if (json.getNextToken() != '[') {
			if (json.wasNull()) {
				return null;
			}
			throw json.newParseError("Expecting '[' for list start");
		}
		if (json.getNextToken() == ']') {
			return new ArrayList<TResult>(0);
		}
		//leave for now in to avoid overhead of going through redirection via generic tryFindReader
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> reader = getObjectReader(manifest);
			if (reader != null) {
				return (List<TResult>) json.deserializeNullableCollection(reader);
			}
		}
		final JsonReader.ReadObject simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return json.deserializeNullableCollection(simpleReader);
		}
		if (fallback != null) {
			final Object array = Array.newInstance(manifest, 0);
			final TResult[] result = (TResult[]) fallback.deserialize(context, array.getClass(), new RereadStream(json.buffer, stream));
			if (result == null) {
				return null;
			}
			final ArrayList<TResult> list = new ArrayList<TResult>(result.length);
			for (TResult aResult : result) {
				list.add(aResult);
			}
			return list;
		}
		throw createErrorMessage(manifest);
	}

	/**
	 * Convenient deserialize API for working with streams.
	 * Deserialize provided stream input into target object.
	 * This method accepts a buffer and will create a new reader using provided buffer.
	 * This buffer is used for internal conversion from stream into byte[] for partial processing.
	 * There is also method without the buffer which reuses local thread reader for processing.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * Provided buffer will be used as input for partial processing.
	 * <p>
	 * For best performance buffer should be reused.
	 *
	 * @param manifest  target type
	 * @param stream    input JSON
	 * @param buffer    buffer used for InputStream -&gt; byte[] conversion
	 * @param <TResult> target type
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> TResult deserialize(
			final Class<TResult> manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		return deserialize(manifest, newReader(stream, buffer), stream);
	}

	/**
	 * Convenient deserialize API for working with streams.
	 * Deserialize provided stream input into target object.
	 * This method reuses thread local reader for processing input stream.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * <p>
	 *
	 * @param manifest  target type
	 * @param stream    input JSON
	 * @param <TResult> target type
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> TResult deserialize(
			final Class<TResult> manifest,
			final InputStream stream) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		final JsonReader json = localReader.get().process(stream);
		try {
			return deserialize(manifest, json, stream);
		} finally {
			json.reset();
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected <TResult> TResult deserialize(
			final Class<TResult> manifest,
			final JsonReader json,
			final InputStream stream) throws IOException {
		json.getNextToken();
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return (TResult) simpleReader.read(json);
		}
		if (manifest.isArray()) {
			if (json.wasNull()) {
				return null;
			} else if (json.last() != '[') {
				throw json.newParseError("Expecting '[' for array start");
			}
			final Class<?> elementManifest = manifest.getComponentType();
			if (json.getNextToken() == ']') {
				return (TResult) Array.newInstance(elementManifest, 0);
			}
			//leave for now in to avoid overhead of going through redirection via generic tryFindReader
			if (JsonObject.class.isAssignableFrom(elementManifest)) {
				final JsonReader.ReadJsonObject<JsonObject> objectReader = getObjectReader(elementManifest);
				if (objectReader != null) {
					List<?> list = json.deserializeNullableCollection(objectReader);
					return (TResult) convertResultToArray(elementManifest, list);
				}
			}
			final JsonReader.ReadObject<?> simpleElementReader = tryFindReader(elementManifest);
			if (simpleElementReader != null) {
				List<?> list = json.deserializeNullableCollection(simpleElementReader);
				return (TResult) convertResultToArray(elementManifest, list);
			}
		}
		if (fallback != null) {
			return (TResult) fallback.deserialize(context, manifest, new RereadStream(json.buffer, stream));
		}
		throw createErrorMessage(manifest);
	}

	/**
	 * Deserialize API for working with streams.
	 * Deserialize provided stream input into target object.
	 * Use buffer for internal conversion from stream into byte[] for partial processing.
	 * This method creates a new instance of JsonReader for processing the stream.
	 * There is also a method without the byte[] buffer which reuses thread local reader.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * DslJson will treat input as a sequence of bytes which allows for various optimizations.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * Provided buffer will be used as input for partial processing.
	 * <p>
	 * For best performance buffer should be reused.
	 *
	 * @param manifest target type
	 * @param stream   input JSON
	 * @param buffer   buffer used for InputStream -&gt; byte[] conversion
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@Nullable
	public Object deserialize(
			final Type manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest instanceof Class<?>) {
			return deserialize((Class<?>) manifest, stream, buffer);
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		final JsonReader json = newReader(stream, buffer);
		json.getNextToken();
		final Object result = deserializeWith(manifest, json);
		if (result != unknownValue) return result;
		if (fallback != null) {
			return fallback.deserialize(context, manifest, new RereadStream(buffer, stream));
		}
		throw new ConfigurationException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
				"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
	}

	/**
	 * Deserialize API for working with streams.
	 * Deserialize provided stream input into target object.
	 * This method reuses thread local reader for processing JSON input.
	 * <p>
	 * Since JSON is often though of as a series of char,
	 * most libraries will convert inputs into a sequence of chars and do processing on them.
	 * <p>
	 * When working on InputStream DslJson will process JSON in chunks of byte[] inputs.
	 * <p>
	 *
	 * @param manifest target type
	 * @param stream   input JSON
	 * @return deserialized instance
	 * @throws IOException error during deserialization
	 */
	@Nullable
	public Object deserialize(
			final Type manifest,
			final InputStream stream) throws IOException {
		if (manifest instanceof Class<?>) {
			return deserialize((Class<?>) manifest, stream);
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		final JsonReader json = localReader.get().process(stream);
		try {
			json.getNextToken();
			final Object result = deserializeWith(manifest, json);
			if (result != unknownValue) return result;
			if (fallback != null) {
				return fallback.deserialize(context, manifest, new RereadStream(json.buffer, stream));
			}
			throw new ConfigurationException("Unable to find reader for provided type: " + manifest + " and fallback serialization is not registered.\n" +
					"Try initializing DslJson with custom fallback in case of unsupported objects or register specified type using registerReader into " + getClass());
		} finally {
			json.reset();
		}
	}

	static class RereadStream extends InputStream {
		private final byte[] buffer;
		private final InputStream stream;
		private boolean usingBuffer;
		private int position;

		RereadStream(byte[] buffer, InputStream stream) {
			this.buffer = buffer;
			this.stream = stream;
			usingBuffer = true;
		}

		@Override
		public int read() throws IOException {
			if (usingBuffer) {
				if (position < buffer.length) {
					return buffer[position++];
				} else usingBuffer = false;
			}
			return stream.read();
		}

		@Override
		public int read(byte[] buf) throws IOException {
			if (usingBuffer) {
				return super.read(buf);
			}
			return stream.read(buf);
		}

		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			if (usingBuffer) {
				return super.read(buf, off, len);
			}
			return stream.read(buf, off, len);
		}
	}

	private static final Iterator EMPTY_ITERATOR = new Iterator() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public void remove() {
		}

		@Nullable
		@Override
		public Object next() {
			return null;
		}
	};

	/**
	 * Streaming API for collection deserialization.
	 * DslJson will create iterator based on provided manifest info.
	 * It will attempt to deserialize from stream on each next() invocation.
	 * This method requires buffer instance for partial stream processing.
	 * It will create a new instance of JsonReader.
	 * There is also a method without the buffer which will reuse thread local reader.
	 * <p>
	 * Useful for processing very large streams if only one instance from collection is required at once.
	 * <p>
	 * Stream will be processed in chunks of specified buffer byte[].
	 * It will block on reading until buffer is full or end of stream is detected.
	 *
	 * @param manifest  type info
	 * @param stream    JSON data stream
	 * @param <TResult> type info
	 * @return Iterator to instances deserialized from input JSON
	 * @throws IOException if reader is not found or there is an error processing input stream
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> Iterator<TResult> iterateOver(
			final Class<TResult> manifest,
			final InputStream stream) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		final JsonReader json = localReader.get();
		json.process(stream);
		return iterateOver(manifest, json, stream);
	}


	/**
	 * Streaming API for collection deserialization.
	 * DslJson will create iterator based on provided manifest info.
	 * It will attempt to deserialize from stream on each next() invocation.
	 * This method requires buffer instance for partial stream processing.
	 * It will create a new instance of JsonReader.
	 * There is also a method without the buffer which will reuse thread local reader.
	 * <p>
	 * Useful for processing very large streams if only one instance from collection is required at once.
	 * <p>
	 * Stream will be processed in chunks of specified buffer byte[].
	 * It will block on reading until buffer is full or end of stream is detected.
	 *
	 * @param manifest  type info
	 * @param stream    JSON data stream
	 * @param buffer    size of processing chunk
	 * @param <TResult> type info
	 * @return Iterator to instances deserialized from input JSON
	 * @throws IOException if reader is not found or there is an error processing input stream
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <TResult> Iterator<TResult> iterateOver(
			final Class<TResult> manifest,
			final InputStream stream,
			final byte[] buffer) throws IOException {
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("buffer can't be null");
		}
		return iterateOver(manifest, newReader(stream, buffer), stream);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected <TResult> Iterator<TResult> iterateOver(
			final Class<TResult> manifest,
			final JsonReader json,
			final InputStream stream) throws IOException {
		if (json.getNextToken() != '[') {
			if (json.wasNull()) {
				return null;
			}
			throw json.newParseError("Expecting '[' for iterator start");
		}
		if (json.getNextToken() == ']') {
			return EMPTY_ITERATOR;
		}
		//leave for now in to avoid overhead of going through redirection via generic tryFindReader
		if (JsonObject.class.isAssignableFrom(manifest)) {
			final JsonReader.ReadJsonObject<JsonObject> reader = getObjectReader(manifest);
			if (reader != null) {
				return json.iterateOver(reader);
			}
		}
		final JsonReader.ReadObject<?> simpleReader = tryFindReader(manifest);
		if (simpleReader != null) {
			return json.iterateOver(simpleReader);
		}
		if (fallback != null) {
			final Object array = Array.newInstance(manifest, 0);
			final TResult[] result = (TResult[]) fallback.deserialize(context, array.getClass(), new RereadStream(json.buffer, stream));
			if (result == null) {
				return null;
			}
			final ArrayList<TResult> list = new ArrayList<TResult>(result.length);
			for (TResult aResult : result) {
				list.add(aResult);
			}
			return list.iterator();
		}
		throw createErrorMessage(manifest);
	}

	private final JsonWriter.WriteObject<JsonObject> OBJECT_WRITER = new JsonWriter.WriteObject<JsonObject>() {
		@Override
		public void write(JsonWriter writer, @Nullable JsonObject value) {
			if (value == null) writer.writeNull();
			else value.serialize(writer, omitDefaults);
		}
	};
	private <T extends JsonObject> JsonReader.ReadObject<T> convertToReader(final JsonReader.ReadJsonObject<T> decoder) {
		return new JsonReader.ReadObject<T>() {
			@Override
			public T read(JsonReader reader) throws IOException {
				if (reader.wasNull()) return null;
				else if (reader.last() != '{') throw reader.newParseError("Expecting '{' for object start");
				reader.getNextToken();
				return decoder.deserialize(reader);
			}
		};
	}

	private final JsonWriter.WriteObject OBJECT_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, @Nullable Object value) {
			serialize(writer, (JsonObject[]) value);
		}
	};

	private static final JsonWriter.WriteObject CHAR_ARRAY_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, @Nullable Object value) {
			StringConverter.serialize(new String((char[]) value), writer);
		}
	};

	private final JsonWriter.WriteObject NULL_WRITER = new JsonWriter.WriteObject() {
		@Override
		public void write(JsonWriter writer, @Nullable Object value) {
			writer.writeNull();
		}
	};

	@SuppressWarnings("unchecked")
	private JsonWriter.WriteObject getOrCreateWriter(@Nullable final Object instance, final Class<?> instanceManifest) throws IOException {
		if (instance instanceof JsonObject) {
			return OBJECT_WRITER;
		}
		if (instance instanceof JsonObject[]) {
			return OBJECT_ARRAY_WRITER;
		}
		final Class<?> manifest = instanceManifest != null ? instanceManifest : instance.getClass();
		if (instanceManifest != null) {
			if (JsonObject.class.isAssignableFrom(manifest)) {
				return OBJECT_WRITER;
			}
		}
		final JsonWriter.WriteObject simpleWriter = tryFindWriter(manifest);
		if (simpleWriter != null) {
			return simpleWriter;
		}
		if (manifest.isArray()) {
			final Class<?> elementManifest = manifest.getComponentType();
			if (char.class == elementManifest) {
				return CHAR_ARRAY_WRITER;
			} else {
				final JsonWriter.WriteObject elementWriter = tryFindWriter(elementManifest);
				if (elementWriter != null) {
					//TODO: cache writer for next lookup
					return new JsonWriter.WriteObject() {
						@Override
						public void write(JsonWriter writer, @Nullable Object value) {
							writer.serialize((Object[]) value, elementWriter);
						}
					};
				}
			}
		}
		if (instance instanceof Collection || Collection.class.isAssignableFrom(manifest)) {
			return new JsonWriter.WriteObject() {
				@Override
				public void write(JsonWriter writer, @Nullable final Object value) {
					final Collection items = (Collection) value;
					Class<?> baseType = null;
					final Iterator iterator = items.iterator();
					//TODO: pick lowest common denominator!?
					do {
						final Object item = iterator.next();
						if (item != null) {
							Class<?> elementType = item.getClass();
							if (elementType != baseType) {
								if (baseType == null || elementType.isAssignableFrom(baseType)) {
									baseType = elementType;
								}
							}
						}
					} while (iterator.hasNext());
					if (baseType == null) {
						writer.writeByte(JsonWriter.ARRAY_START);
						writer.writeNull();
						for (int i = 1; i < items.size(); i++) {
							writer.writeAscii(",null");
						}
						writer.writeByte(JsonWriter.ARRAY_END);
					} else if (JsonObject.class.isAssignableFrom(baseType)) {
						serialize(writer, (Collection<JsonObject>) items);
					} else {
						final JsonWriter.WriteObject elementWriter = tryFindWriter(baseType);
						if (elementWriter != null) {
							writer.serialize(items, elementWriter);
						} else if (fallback != null) {
							final ByteArrayOutputStream stream = new ByteArrayOutputStream();
							stream.reset();
							try {
								fallback.serialize(value, stream);
							} catch (IOException ex) {
								throw new SerializationException(ex);
							}
							writer.writeAscii(stream.toByteArray());
						} else {
							throw new ConfigurationException("Unable to serialize provided object. Failed to find serializer for: " + items.getClass());
						}
					}
				}
			};
		}
		throw new ConfigurationException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
	}

	/**
	 * Streaming API for collection serialization.
	 * <p>
	 * It will iterate over entire iterator and serialize each instance into target output stream.
	 * After each instance serialization it will copy JSON into target output stream.
	 * During each serialization reader will be looked up based on next() instance which allows
	 * serializing collection with different types.
	 * If collection contains all instances of the same type, prefer the other streaming API.
	 * <p>
	 * If reader is not found an IOException will be thrown
	 * <p>
	 * If JsonWriter is provided it will be used, otherwise a new instance will be internally created.
	 *
	 * @param iterator input data
	 * @param stream   target JSON stream
	 * @param writer   temporary buffer for serializing a single item. Can be null
	 * @param <T>      input data type
	 * @throws IOException reader is not found, there is an error during serialization or problem with writing to target stream
	 */
	@SuppressWarnings("unchecked")
	public <T> void iterateOver(
			final Iterator<T> iterator,
			final OutputStream stream,
			@Nullable final JsonWriter writer) throws IOException {
		if (iterator == null) {
			throw new IllegalArgumentException("iterator can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		stream.write(JsonWriter.ARRAY_START);
		if (!iterator.hasNext()) {
			stream.write(JsonWriter.ARRAY_END);
			return;
		}
		final JsonWriter buffer = writer == null ? new JsonWriter(this) : writer;
		T item = iterator.next();
		Class<?> lastManifest = null;
		JsonWriter.WriteObject lastWriter = null;
		if (item != null) {
			lastManifest = item.getClass();
			lastWriter = getOrCreateWriter(item, lastManifest);
			buffer.reset();
			try {
				lastWriter.write(buffer, item);
			} catch (ConfigurationException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
			buffer.toStream(stream);
		} else {
			stream.write(NULL);
		}
		while (iterator.hasNext()) {
			stream.write(JsonWriter.COMMA);
			item = iterator.next();
			if (item != null) {
				final Class<?> currentManifest = item.getClass();
				if (lastWriter == null || lastManifest == null || !lastManifest.equals(currentManifest)) {
					lastManifest = currentManifest;
					lastWriter = getOrCreateWriter(item, lastManifest);
				}
				buffer.reset();
				try {
					lastWriter.write(buffer, item);
				} catch (ConfigurationException e) {
					throw e;
				} catch (Exception e) {
					throw new IOException(e);
				}
				buffer.toStream(stream);
			} else {
				stream.write(NULL);
			}
		}
		stream.write(JsonWriter.ARRAY_END);
	}

	/**
	 * Streaming API for collection serialization.
	 * <p>
	 * It will iterate over entire iterator and serialize each instance into target output stream.
	 * After each instance serialization it will copy JSON into target output stream.
	 * <p>
	 * If reader is not found an IOException will be thrown
	 * <p>
	 * If JsonWriter is provided it will be used, otherwise a new instance will be internally created.
	 *
	 * @param iterator input data
	 * @param manifest type of elements in collection
	 * @param stream   target JSON stream
	 * @param writer   temporary buffer for serializing a single item. Can be null
	 * @param <T>      input data type
	 * @throws IOException reader is not found, there is an error during serialization or problem with writing to target stream
	 */
	@SuppressWarnings("unchecked")
	public <T> void iterateOver(
			final Iterator<T> iterator,
			final Class<T> manifest,
			final OutputStream stream,
			@Nullable final JsonWriter writer) throws IOException {
		if (iterator == null) {
			throw new IllegalArgumentException("iterator can't be null");
		}
		if (manifest == null) {
			throw new IllegalArgumentException("manifest can't be null");
		}
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		final JsonWriter buffer = writer == null ? new JsonWriter(this) : writer;
		final JsonWriter.WriteObject instanceWriter = getOrCreateWriter(null, manifest);
		stream.write(JsonWriter.ARRAY_START);
		T item = iterator.next();
		if (item != null) {
			buffer.reset();
			try {
				instanceWriter.write(buffer, item);
			} catch (ConfigurationException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
			buffer.toStream(stream);
		} else {
			stream.write(NULL);
		}
		while (iterator.hasNext()) {
			stream.write(JsonWriter.COMMA);
			item = iterator.next();
			if (item != null) {
				buffer.reset();
				try {
					instanceWriter.write(buffer, item);
				} catch (ConfigurationException e) {
					throw e;
				} catch (Exception e) {
					throw new IOException(e);
				}
				buffer.toStream(stream);
			} else {
				stream.write(NULL);
			}
		}
		stream.write(JsonWriter.ARRAY_END);
	}

	/**
	 * Use writer.serialize instead
	 *
	 * @param writer writer
	 * @param array items
	 * @param <T> type
	 */
	@Deprecated
	public <T extends JsonObject> void serialize(final JsonWriter writer, @Nullable final T[] array) {
		if (array == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (array.length != 0) {
			T item = array[0];
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			for (int i = 1; i < array.length; i++) {
				writer.writeByte(JsonWriter.COMMA);
				item = array[i];
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	/**
	 * Use writer.serialize instead
	 *
	 * @param writer writer
	 * @param array items
	 * @param len part of array
	 * @param <T> type
	 */
	@Deprecated
	public <T extends JsonObject> void serialize(final JsonWriter writer, final T[] array, final int len) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (array == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (len != 0) {
			T item = array[0];
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			for (int i = 1; i < len; i++) {
				writer.writeByte(JsonWriter.COMMA);
				item = array[i];
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	/**
	 * Use writer.serialize instead
	 *
	 * @param writer writer
	 * @param list items
	 * @param <T> type
	 */
	@Deprecated
	public <T extends JsonObject> void serialize(final JsonWriter writer, @Nullable final List<T> list) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (list == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (list.size() != 0) {
			T item = list.get(0);
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			for (int i = 1; i < list.size(); i++) {
				writer.writeByte(JsonWriter.COMMA);
				item = list.get(i);
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	/**
	 * Use writer.serialize instead
	 *
	 * @param writer writer
	 * @param collection items
	 * @param <T> type
	 */
	@Deprecated
	public <T extends JsonObject> void serialize(final JsonWriter writer, @Nullable final Collection<T> collection) {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (collection == null) {
			writer.writeNull();
			return;
		}
		writer.writeByte(JsonWriter.ARRAY_START);
		if (!collection.isEmpty()) {
			final Iterator<T> it = collection.iterator();
			T item = it.next();
			if (item != null) {
				item.serialize(writer, omitDefaults);
			} else {
				writer.writeNull();
			}
			while (it.hasNext()) {
				writer.writeByte(JsonWriter.COMMA);
				item = it.next();
				if (item != null) {
					item.serialize(writer, omitDefaults);
				} else {
					writer.writeNull();
				}
			}
		}
		writer.writeByte(JsonWriter.ARRAY_END);
	}

	/**
	 * Generic serialize API.
	 * Based on provided type manifest converter will be chosen.
	 * If converter is not found method will return false.
	 * <p>
	 * Resulting JSON will be written into provided writer argument.
	 * In case of successful serialization true will be returned.
	 * <p>
	 * For best performance writer argument should be reused.
	 *
	 * @param writer   where to write resulting JSON
	 * @param manifest type manifest
	 * @param value    instance to serialize
	 * @return successful serialization
	 */
	@SuppressWarnings("unchecked")
	public boolean serialize(final JsonWriter writer, final Type manifest, @Nullable final Object value) {
		try {
			if (writer == null) {
				throw new IllegalArgumentException("writer can't be null");
			}
			if (value == null) {
				writer.writeNull();
				return true;
			} else if (value instanceof JsonObject) {
				((JsonObject) value).serialize(writer, omitDefaults);
				return true;
			} else if (value instanceof JsonObject[]) {
				serialize(writer, (JsonObject[]) value);
				return true;
			}
			final JsonWriter.WriteObject simpleWriter = tryFindWriter(manifest);
			if (simpleWriter != null) {
				simpleWriter.write(writer, value);
				return true;
			}
			Class<?> container = null;
			if (manifest instanceof Class<?>) {
				container = (Class<?>) manifest;
			}
			if (container != null && container.isArray()) {
				if (Array.getLength(value) == 0) {
					writer.writeAscii("[]");
					return true;
				}
				final Class<?> elementManifest = container.getComponentType();
				if (char.class == elementManifest) {
					//TODO? char[] !?
					StringConverter.serialize(new String((char[]) value), writer);
					return true;
				} else {
					final JsonWriter.WriteObject<Object> elementWriter = (JsonWriter.WriteObject<Object>) tryFindWriter(elementManifest);
					if (elementWriter != null) {
						writer.serialize((Object[]) value, elementWriter);
						return true;
					}
				}
			}
			if (value instanceof Collection) {
				final Collection items = (Collection) value;
				if (items.isEmpty()) {
					writer.writeAscii("[]");
					return true;
				}
				Class<?> baseType = null;
				final Iterator iterator = items.iterator();
				final boolean isList = items instanceof List;
				final List<Object> values = isList ? (List) items : new ArrayList<Object>();
				final ArrayList<JsonWriter.WriteObject> itemWriters = new ArrayList<JsonWriter.WriteObject>();
				Class<?> lastElementType = null;
				JsonWriter.WriteObject lastWriter = null;
				boolean hasUnknownWriter = false;
				//TODO: pick lowest common denominator!?
				do {
					final Object item = iterator.next();
					if (!isList) {
						values.add(item);
					}
					if (item != null) {
						final Class<?> elementType = item.getClass();
						if (elementType != baseType) {
							if (baseType == null || elementType.isAssignableFrom(baseType)) {
								baseType = elementType;
							}
						}
						if (lastElementType != elementType) {
							lastElementType = elementType;
							lastWriter = tryFindWriter(elementType);
						}
						itemWriters.add(lastWriter);
						hasUnknownWriter = hasUnknownWriter || lastWriter == null;
					} else {
						itemWriters.add(NULL_WRITER);
					}
				} while (iterator.hasNext());
				if (baseType != null && JsonObject.class.isAssignableFrom(baseType)) {
					writer.writeByte(JsonWriter.ARRAY_START);
					final Iterator iter = values.iterator();
					final JsonObject first = (JsonObject) iter.next();
					if (first != null) first.serialize(writer, omitDefaults);
					else writer.writeNull();
					while (iter.hasNext()) {
						writer.writeByte(JsonWriter.COMMA);
						final JsonObject next = (JsonObject) iter.next();
						if (next != null) next.serialize(writer, omitDefaults);
						else writer.writeNull();
					}
					writer.writeByte(JsonWriter.ARRAY_END);
					return true;
				}
				if (!hasUnknownWriter) {
					writer.writeByte(JsonWriter.ARRAY_START);
					final Iterator iter = values.iterator();
					int cur = 1;
					itemWriters.get(0).write(writer, iter.next());
					while (iter.hasNext()) {
						writer.writeByte(JsonWriter.COMMA);
						itemWriters.get(cur++).write(writer, iter.next());
					}
					writer.writeByte(JsonWriter.ARRAY_END);
					return true;
				}
				final JsonWriter.WriteObject<Object> elementWriter = (JsonWriter.WriteObject<Object>) tryFindWriter(baseType);
				if (elementWriter != null) {
					writer.serialize(items, elementWriter);
					return true;
				}
			}
			return false;
		} catch (ClassCastException exc) { // workaround for mixed primitive arrays (PLAT-7551)
			return false;
		}
	}

	private static final byte[] NULL = new byte[]{'n', 'u', 'l', 'l'};

	/**
	 * Convenient serialize API.
	 * In most cases JSON is serialized into target `OutputStream`.
	 * This method will reuse thread local instance of `JsonWriter` and serialize JSON into it.
	 *
	 * @param value    		instance to serialize
	 * @param stream 		where to write resulting JSON
	 * @throws IOException error when unable to serialize instance
	 */
	public final void serialize(@Nullable final Object value, final OutputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("stream can't be null");
		}
		if (value == null) {
			stream.write(NULL);
			return;
		}
		final JsonWriter jw = localWriter.get();
		jw.reset(stream);
		final Class<?> manifest = value.getClass();
		if (!serialize(jw, manifest, value)) {
			if (fallback == null) {
				throw new ConfigurationException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
			}
			fallback.serialize(value, stream);
		} else {
			jw.flush();
			jw.reset(null);
		}
	}

	/**
	 * Main serialization API.
	 * Convert object instance into JSON.
	 * <p>
	 * JsonWriter contains a growable byte[] where JSON will be serialized.
	 * After serialization JsonWriter can be copied into OutputStream or it's byte[] can be obtained
	 * <p>
	 * For best performance reuse `JsonWriter` or even better call `JsonWriter.WriteObject` directly
	 *
	 * @param writer where to write resulting JSON
	 * @param value  object instance to serialize
	 * @throws IOException error when unable to serialize instance
	 */
	public final void serialize(final JsonWriter writer, @Nullable final Object value) throws IOException {
		if (writer == null) {
			throw new IllegalArgumentException("writer can't be null");
		}
		if (value == null) {
			writer.writeNull();
			return;
		}
		final Class<?> manifest = value.getClass();
		if (!serialize(writer, manifest, value)) {
			if (fallback == null) {
				throw new ConfigurationException("Unable to serialize provided object. Failed to find serializer for: " + manifest);
			}
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			fallback.serialize(value, stream);
			writer.writeAscii(stream.toByteArray());
		}
	}
}
