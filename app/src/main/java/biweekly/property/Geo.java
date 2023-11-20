package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;

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
 * <p>
 * Defines a set of geographical coordinates.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Geo geo = new Geo(40.714623, -74.006605);
 * event.setGeo(geo);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-85">RFC 5545 p.85-7</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-82">RFC 2445 p.82-3</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.23</a>
 */
public class Geo extends ICalProperty {
	private Double latitude;
	private Double longitude;

	/**
	 * Creates a new geo property.
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public Geo(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Geo(Geo original) {
		super(original);
		latitude = original.latitude;
		longitude = original.longitude;
	}

	/**
	 * Gets the latitude.
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude.
	 * @param latitude the latitude
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Gets the longitude.
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude.
	 * @param longitude the longitude
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Converts a coordinate in the degrees-minutes-seconds format into its
	 * decimal equivalent.
	 * @param degrees the degrees
	 * @param minutes the minutes
	 * @param seconds the seconds
	 * @return the decimal value
	 */
	public static double toDecimal(int degrees, int minutes, int seconds) {
		return degrees + (minutes / 60.0) + (seconds / 3600.0);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (latitude == null) {
			warnings.add(new ValidationWarning(41));
		}
		if (longitude == null) {
			warnings.add(new ValidationWarning(42));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("latitude", latitude);
		values.put("longitude", longitude);
		return values;
	}

	@Override
	public Geo copy() {
		return new Geo(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Geo other = (Geo) obj;
		if (latitude == null) {
			if (other.latitude != null) return false;
		} else if (!latitude.equals(other.latitude)) return false;
		if (longitude == null) {
			if (other.longitude != null) return false;
		} else if (!longitude.equals(other.longitude)) return false;
		return true;
	}
}
