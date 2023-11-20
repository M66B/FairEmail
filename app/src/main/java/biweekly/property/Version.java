package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.util.VersionNumber;

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
 * Defines the min/max iCalendar versions a consumer must support in order to
 * successfully parse the iCalendar object.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * //all ICalendar objects are given a VERSION property on creation
 * ical.getVersion(); //"2.0"
 * 
 * //get the default iCal version
 * Version version = Version.v2_0();
 * ical.setVersion(version);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-79">RFC 5545
 * p.79-80</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-76">RFC 2445 p.76-7</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
 */
public class Version extends ICalProperty {
	public static final VersionNumber VCAL = new VersionNumber(ICalVersion.V1_0.getVersion());
	public static final VersionNumber ICAL = new VersionNumber(ICalVersion.V2_0.getVersion());

	private VersionNumber minVersion, maxVersion;

	/**
	 * Creates a new version property.
	 * @param version the version that a consumer must support in order to
	 * successfully parse the iCalendar object
	 */
	public Version(ICalVersion version) {
		this((version == null) ? null : version.getVersion());
	}

	/**
	 * Creates a new version property.
	 * @param version the version that a consumer must support in order to
	 * successfully parse the iCalendar object
	 * @throws IllegalArgumentException if the version string is invalid
	 */
	public Version(String version) {
		this(null, version);
	}

	/**
	 * Creates a new version property.
	 * @param minVersion the minimum version that a consumer must support in
	 * order to successfully parse the iCalendar object
	 * @param maxVersion the maximum version that a consumer must support in
	 * order to successfully parse the iCalendar object
	 * @throws IllegalArgumentException if one of the versions strings are
	 * invalid
	 */
	public Version(String minVersion, String maxVersion) {
		this((minVersion == null) ? null : new VersionNumber(minVersion), (maxVersion == null) ? null : new VersionNumber(maxVersion));
	}

	private Version(VersionNumber minVersion, VersionNumber maxVersion) {
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Version(Version original) {
		super(original);
		minVersion = original.minVersion;
		maxVersion = original.maxVersion;
	}

	/**
	 * Creates a version property that is set to the older vCalendar version
	 * (1.0).
	 * @return the property instance
	 */
	public static Version v1_0() {
		return new Version(ICalVersion.V1_0);
	}

	/**
	 * Creates a version property that is set to the latest iCalendar version
	 * (2.0).
	 * @return the property instance
	 */
	public static Version v2_0() {
		return new Version(ICalVersion.V2_0);
	}

	/**
	 * Determines if this property is set to the older vCalendar version.
	 * @return true if the version is "1.0", false if not
	 */
	public boolean isV1_0() {
		return VCAL.equals(maxVersion);
	}

	/**
	 * Determines if this property is set to the latest iCalendar version.
	 * @return true if the version is "2.0", false if not
	 */
	public boolean isV2_0() {
		return ICAL.equals(maxVersion);
	}

	/**
	 * Gets the minimum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @return the minimum version or null if not set
	 */
	public VersionNumber getMinVersion() {
		return minVersion;
	}

	/**
	 * Sets the minimum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @param minVersion the minimum version or null to remove
	 */
	public void setMinVersion(VersionNumber minVersion) {
		this.minVersion = minVersion;
	}

	/**
	 * Gets the maximum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @return the maximum version or null if not set
	 */
	public VersionNumber getMaxVersion() {
		return maxVersion;
	}

	/**
	 * Sets the maximum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @param maxVersion the maximum version (this field is <b>required</b>)
	 */
	public void setMaxVersion(VersionNumber maxVersion) {
		this.maxVersion = maxVersion;
	}

	/**
	 * Converts this property's value to an {@link ICalVersion} enum.
	 * @return the {@link ICalVersion} enum or null if it couldn't be converted
	 */
	public ICalVersion toICalVersion() {
		if (minVersion == null && maxVersion != null) {
			return ICalVersion.get(maxVersion.toString());
		}
		return null;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (maxVersion == null) {
			warnings.add(new ValidationWarning(35));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("minVersion", minVersion);
		values.put("maxVersion", maxVersion);
		return values;
	}

	@Override
	public Version copy() {
		return new Version(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((maxVersion == null) ? 0 : maxVersion.hashCode());
		result = prime * result + ((minVersion == null) ? 0 : minVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Version other = (Version) obj;
		if (maxVersion == null) {
			if (other.maxVersion != null) return false;
		} else if (!maxVersion.equals(other.maxVersion)) return false;
		if (minVersion == null) {
			if (other.minVersion != null) return false;
		} else if (!minVersion.equals(other.minVersion)) return false;
		return true;
	}
}
