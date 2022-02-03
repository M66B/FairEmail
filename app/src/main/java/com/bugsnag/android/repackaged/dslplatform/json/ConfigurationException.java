package com.bugsnag.android.repackaged.dslplatform.json;

@SuppressWarnings("serial") // suppress pre-existing warnings
public class ConfigurationException extends RuntimeException {
	public ConfigurationException(String reason) {
		super(reason);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
