package com.bugsnag.android.repackaged.dslplatform.json;

/**
 * Configuration API for setting up readers/writers during library initialization.
 * DslJson will use ServiceLoader.load(Configuration.class) in default constructor.
 * This will load services registered in META-INF/services/com.bugsnag.dslplatform.json.Configuration file.
 */
@SuppressWarnings("rawtypes") // suppress pre-existing warnings
public interface Configuration {
	/**
	 * Configure library instance with appropriate readers/writers/etc...
	 *
	 * @param json library instance
	 */
	void configure(DslJson json);
}
