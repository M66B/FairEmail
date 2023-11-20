package biweekly.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import biweekly.ICalendar;
import biweekly.component.VTimezone;
import biweekly.io.text.ICalReader;
import biweekly.property.TimezoneId;
import biweekly.property.ValuedProperty;
import biweekly.util.IOUtils;

/*
 Copyright (c) 2013-2023, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Downloads {@link VTimezone} components from <a
 * href="http://www.tzurl.org">tzurl.org</a>. This class is thread-safe.
 * @author Michael Angstadt
 */
public class TzUrlDotOrgGenerator {
	private static final Map<URI, VTimezone> cache = Collections.synchronizedMap(new HashMap<URI, VTimezone>());
	private final String baseUrl;

	/**
	 * Creates a new tzurl.org generator.
	 * @param outlookCompatible true to download {@link VTimezone} components
	 * that are tailored for Microsoft Outlook email clients, false to download
	 * standards-based ones
	 */
	public TzUrlDotOrgGenerator(boolean outlookCompatible) {
		baseUrl = "http://www.tzurl.org/zoneinfo" + (outlookCompatible ? "-outlook" : "") + "/";
	}

	/**
	 * Generates an iCalendar {@link VTimezone} components from a Java
	 * {@link TimeZone} object.
	 * @param timezone the timezone object
	 * @return the timezone component
	 * @throws IllegalArgumentException if a timezone definition cannot be found
	 */
	public VTimezone generate(TimeZone timezone) throws IllegalArgumentException {
		URI uri;
		try {
			uri = new URI(buildUrl(timezone));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		VTimezone component = cache.get(uri);
		if (component != null) {
			return component.copy();
		}

		ICalendar ical;
		ICalReader reader = null;
		try {
			reader = new ICalReader(getInputStream(uri));
			ical = reader.readNext();
		} catch (FileNotFoundException e) {
			throw notFound(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}

		/*
		 * There should always be exactly one iCalendar object in the file, but
		 * check to be sure.
		 */
		if (ical == null) {
			throw notFound(null);
		}

		/*
		 * There should always be exactly one VTIMEZONE component, but check to
		 * be sure.
		 */
		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		Collection<VTimezone> components = tzinfo.getComponents();
		if (components.isEmpty()) {
			components = ical.getComponents(VTimezone.class); //VTIMEZONE components without TZID properties are treated as ordinary components
			if (components.isEmpty()) {
				throw notFound(null);
			}
		}

		component = components.iterator().next();

		/*
		 * There should always be a TZID property, but just in case there there
		 * isn't one, create one.
		 */
		TimezoneId id = component.getTimezoneId();
		if (id == null) {
			component.setTimezoneId(timezone.getID());
		} else {
			String value = ValuedProperty.getValue(id);
			if (value == null || value.trim().isEmpty()) {
				id.setValue(timezone.getID());
			}
		}

		cache.put(uri, component);
		return component.copy();
	}

	private String buildUrl(TimeZone timezone) {
		return baseUrl + timezone.getID();
	}

	//for unit testing
	InputStream getInputStream(URI uri) throws IOException {
		return uri.toURL().openStream();
	}

	/**
	 * Clears the internal cache of downloaded timezone definitions.
	 */
	public static void clearCache() {
		cache.clear();
	}

	private static IllegalArgumentException notFound(Exception e) {
		return new IllegalArgumentException("Timezone ID not recognized.", e);
	}
}
