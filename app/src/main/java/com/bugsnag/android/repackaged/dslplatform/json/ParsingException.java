package com.bugsnag.android.repackaged.dslplatform.json;

import java.io.IOException;

@SuppressWarnings("serial") // suppress pre-existing warnings
public class ParsingException extends IOException {

	private ParsingException(String reason) {
		super(reason);
	}

	private ParsingException(String reason, Throwable cause) {
		super(reason, cause);
	}

	public static ParsingException create(String reason, boolean withStackTrace) {
		return withStackTrace
				? new ParsingException(reason)
				: new ParsingStacklessException(reason);
	}


	public static ParsingException create(String reason, Throwable cause, boolean withStackTrace) {
		return withStackTrace
				? new ParsingException(reason, cause)
				: new ParsingStacklessException(reason, cause);
	}

	private static class ParsingStacklessException extends ParsingException {

		private ParsingStacklessException(String reason) {
			super(reason);
		}

		private ParsingStacklessException(String reason, Throwable cause) {
			super(reason, cause);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return this;
		}
	}
}
