package biweekly.parameter;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.validate.AllowedCharacters;
import com.github.mangstadt.vinnie.validate.VObjectValidator;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Messages;
import biweekly.ValidationWarning;
import biweekly.property.Attendee;
import biweekly.property.Conference;
import biweekly.property.FreeBusy;
import biweekly.property.Image;
import biweekly.property.Organizer;
import biweekly.property.RecurrenceId;
import biweekly.property.RelatedTo;
import biweekly.property.Trigger;
import biweekly.util.ListMultimap;

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
 * Stores the parameters that belong to a property.
 * @author Michael Angstadt
 */
public class ICalParameters extends ListMultimap<String, String> {
	/**
	 * Contains a URI that points to additional information about the entity
	 * represented by the property.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	public static final String ALTREP = "ALTREP";

	/**
	 * Defines the character set that the property value is encoded in (for
	 * example, "UTF-8"). It is only used in the vCal 1.0 standard, and is
	 * typically used when a property value is encoded in quoted-printable
	 * encoding.
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16</a>
	 */
	public static final String CHARSET = "CHARSET";

	/**
	 * Contains a human-readable, display name of the entity represented by this
	 * property (for example, "John Doe"). It is used by the {@link Attendee}
	 * and {@link Organizer} properties.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	public static final String CN = "CN";

	/**
	 * Used by the {@link Attendee} property. It defines the type of object that
	 * the attendee is (for example, an "individual" or a "room").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public static final String CUTYPE = "CUTYPE";

	/**
	 * Used by the {@link Attendee} property. It stores a list of people who
	 * have delegated their responsibility to the attendee. The values must be
	 * URIs. They are typically email URIs (for example,
	 * "mailto:janedoe@example.com").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public static final String DELEGATED_FROM = "DELEGATED-FROM";

	/**
	 * Used by the {@link Attendee} property. It stores a list of people to
	 * which the attendee has delegated his or her responsibility. The values
	 * must be URIs. They are typically email URIs (for example,
	 * "mailto:janedoe@example.com").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public static final String DELEGATED_TO = "DELEGATED-TO";

	/**
	 * Contains a URI (such as an LDAP URI) which points to additional
	 * information about the person that the property represents. It is used by
	 * the {@link Attendee} and {@link Organizer} properties.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	public static final String DIR = "DIR";

	/**
	 * Used by the {@link Image} property. It defines the ways in which the
	 * client application should display the image (for example, as a
	 * thumbnail-sized image).
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-13" >
	 * draft-ietf-calext-extensions p.13</a>
	 */
	public static final String DISPLAY = "DISPLAY";

	/**
	 * Used by the {@link Attendee} property. Normally, this property's value
	 * contains the email address of the attendee. But if the property value
	 * must hold something else, this parameter can be used to store the
	 * attendee's email address.
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-14" >
	 * draft-ietf-calext-extensions p.14</a>
	 */
	public static final String EMAIL = "EMAIL";

	/**
	 * Defines how the property value is encoded (for example, "base64" for a
	 * binary value).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18-9</a>
	 */
	public static final String ENCODING = "ENCODING";

	/**
	 * Used by the {@link Attendee} property. It defines whether the event
	 * organizer expects the attendee to attend or not. It is only used in the
	 * vCal 1.0 standard.
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public static final String EXPECT = "EXPECT";

	/**
	 * Used by the {@link Conference} property. It defines the features that the
	 * conference supports (for example, audio and video).
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-15" >
	 * draft-ietf-calext-extensions p.15</a>
	 */
	public static final String FEATURE = "FEATURE";

	/**
	 * Defines the content type of the property value (for example, "image/jpg"
	 * if the property value is a JPEG image).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	public static final String FMTTYPE = "FMTTYPE";

	/**
	 * Used by the {@link FreeBusy} property. It defines whether the person is
	 * "free" or "busy" over the time periods that are specified in the property
	 * value. If this parameter is not set, the user should be considered "busy"
	 * during these times.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public static final String FBTYPE = "FBTYPE";

	/**
	 * Defines a human-readable label for the property.
	 * @see <a href=
	 * "http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-16">
	 * draft-ietf-calext-extensions-01 p.16</a>
	 */
	public static final String LABEL = "LABEL";

	/**
	 * Defines the language that the property value is written in (for example,
	 * "en" for English).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	public static final String LANGUAGE = "LANGUAGE";

	/**
	 * Used by the {@link Attendee} property. It defines the groups that the
	 * attendee is a member of in the form of URIs. Typically, these are email
	 * URIs (for example, "mailto:mailinglist@example.com").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public static final String MEMBER = "MEMBER";

	/**
	 * Used by the {@link Attendee} property. It defines the participation
	 * status of the attendee (for example, "ACCEPTED"). If none is defined,
	 * then the property should be treated as if this parameter was set to
	 * "NEEDS-ACTION".
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22</a>
	 */
	public static final String PARTSTAT = "PARTSTAT";

	/**
	 * Used by the {@link RecurrenceId} property. It defines the effective range
	 * of recurrence instances that the property references.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public static final String RANGE = "RANGE";

	/**
	 * Used by the {@link Trigger} property. It defines the date-time field that
	 * the property's duration (if specified) is relative to (for example, the
	 * start date or the end date).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public static final String RELATED = "RELATED";

	/**
	 * Used by the {@link RelatedTo} property. It defines the kind of
	 * relationship the property is describing (for example, a "child"
	 * relationship).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public static final String RELTYPE = "RELTYPE";

	/**
	 * Used by the {@link Attendee} property. It defines the attendee's role
	 * and/or whether they must attend or not (for example, "OPT-PARTICIPANT"
	 * for "optional participant"). If none is defined, then the property should
	 * be treated as if this parameter was set to "REQ-PARTICIPANT" (required
	 * participant).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public static final String ROLE = "ROLE";

	/**
	 * Used by the {@link Attendee} property. It defines whether the event
	 * organizer would like the attendee to reply with his or her intention of
	 * attending ("true" if the organizer would like a reply, "false" if not).
	 * If this parameter is not defined, then the property should be treated as
	 * if this parameter was set to "false".
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public static final String RSVP = "RSVP";

	/**
	 * Defines a URI which represents a person who is acting on behalf of the
	 * person that is defined in the property. Typically, the URI is an email
	 * URI (for example, "mailto:janedoe@example.com"). It is used by the
	 * {@link Attendee} and {@link Organizer} properties.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	public static final String SENT_BY = "SENT-BY";

	/**
	 * Used by the {@link Attendee} property. It defines the status of the
	 * person's event invitation (for example, "TENTATIVE" if the person may or
	 * may not attend). It is only used in the vCal 1.0 standard.
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public static final String STATUS = "STATUS";

	/**
	 * Defines the content type of the property value (for example, "WAVE" for
	 * an audio file). It is only used in the vCal 1.0 standard.
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.27</a>
	 */
	public static final String TYPE = "TYPE";

	/**
	 * Used by properties that contain date-time values. It defines the timezone
	 * that the property value is formatted in. It either references a timezone
	 * defined in a VTIMEZONE component, or contains an Olson timezone ID. To
	 * use an Olson timezone ID, the parameter value must be prepended with a
	 * "/" (for example, "/America/New_York").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27-8</a>
	 */
	public static final String TZID = "TZID";

	/**
	 * Defines the data type of the property value (for example, "date" if the
	 * property value is a date without a time component). It is used if the
	 * property accepts multiple values that have different data types.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
	 * p.29-50</a>
	 */
	public static final String VALUE = "VALUE";

	/**
	 * Creates a parameters list.
	 */
	public ICalParameters() {
		/*
		 * Initialize map size to 0 because most properties don't use any
		 * parameters.
		 */
		super(0);
	}

	/**
	 * Copies an existing parameters list.
	 * @param parameters the list to copy
	 */
	public ICalParameters(ICalParameters parameters) {
		super(parameters);
	}

	/**
	 * <p>
	 * Creates a parameter list that is backed by the given map. Any changes
	 * made to the given map will effect the parameter list and vice versa.
	 * </p>
	 * <p>
	 * Care must be taken to ensure that the given map's keys are all in
	 * uppercase.
	 * </p>
	 * <p>
	 * To avoid problems, it is highly recommended that the given map NOT be
	 * modified by anything other than this {@link ICalParameters} class after
	 * being passed into this constructor.
	 * </p>
	 * @param map the map
	 */
	public ICalParameters(Map<String, List<String>> map) {
		super(map);
	}

	/**
	 * <p>
	 * Gets the ALTREP (alternate representation) parameter value.
	 * </p>
	 * <p>
	 * This parameter contains a URI that points to additional information about
	 * the entity represented by the property.
	 * </p>
	 * @return the URI or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	public String getAltRepresentation() {
		return first(ALTREP);
	}

	/**
	 * <p>
	 * Sets the ALTREP (alternate representation) parameter value.
	 * </p>
	 * <p>
	 * This parameter contains a URI that points to additional information about
	 * the entity represented by the property.
	 * </p>
	 * @param uri the URI or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	public void setAltRepresentation(String uri) {
		replace(ALTREP, uri);
	}

	/**
	 * <p>
	 * Gets the CHARSET parameter value.
	 * </p>
	 * <p>
	 * This parameter contains the character set that the property value is
	 * encoded in (for example, "UTF-8"). It is only used in the vCal 1.0
	 * standard, and is typically used when a property value is encoded in
	 * quoted-printable encoding.
	 * </p>
	 * @return the character set or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16</a>
	 */
	public String getCharset() {
		return first(CHARSET);
	}

	/**
	 * <p>
	 * Sets the CHARSET parameter value.
	 * </p>
	 * <p>
	 * This parameter contains the character set that the property value is
	 * encoded in (for example, "UTF-8"). It is only used in the vCal 1.0
	 * standard, and is typically used when a property value is encoded in
	 * quoted-printable encoding.
	 * </p>
	 * @param charset the character set or null to remove
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16</a>
	 */
	public void setCharset(String charset) {
		replace(CHARSET, charset);
	}

	/**
	 * <p>
	 * Gets the CN (common name) parameter value.
	 * </p>
	 * <p>
	 * This parameter contains a human-readable, display name of the entity
	 * represented by this property (for example, "John Doe"). It is used by the
	 * {@link Attendee} and {@link Organizer} properties.
	 * </p>
	 * @return the common name or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	public String getCommonName() {
		return first(CN);
	}

	/**
	 * <p>
	 * Sets the CN (common name) parameter value.
	 * </p>
	 * <p>
	 * This parameter contains a human-readable, display name of the entity
	 * represented by this property (for example, "John Doe"). It is used by the
	 * {@link Attendee} and {@link Organizer} properties.
	 * </p>
	 * @param cn the common name or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	public void setCommonName(String cn) {
		replace(CN, cn);
	}

	/**
	 * <p>
	 * Gets the CUTYPE (calendar user type) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * type of object that the attendee is (for example, an "individual" or a
	 * "room").
	 * </p>
	 * @return the calendar user type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public CalendarUserType getCalendarUserType() {
		String value = first(CUTYPE);
		return (value == null) ? null : CalendarUserType.get(value);
	}

	/**
	 * <p>
	 * Sets the CUTYPE (calendar user type) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * type of object that the attendee is (for example, an "individual" or a
	 * "room").
	 * </p>
	 * @param calendarUserType the calendar user type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public void setCalendarUserType(CalendarUserType calendarUserType) {
		replace(CUTYPE, (calendarUserType == null) ? null : calendarUserType.getValue());
	}

	/**
	 * <p>
	 * Gets the DELEGATED-FROM parameter values.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It stores a list
	 * of people who have delegated their responsibility to the attendee. The
	 * values must be URIs. They are typically email URIs (for example,
	 * "mailto:janedoe@example.com").
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link ICalParameters}
	 * object, and vice versa.
	 * </p>
	 * @return the URIs or an empty list if none are set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public List<String> getDelegatedFrom() {
		return get(DELEGATED_FROM);
	}

	/**
	 * <p>
	 * Gets the DELEGATED-TO parameter values.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It stores a list
	 * of people to which the attendee has delegated his or her responsibility.
	 * The values must be URIs. They are typically email URIs (for example,
	 * "mailto:janedoe@example.com").
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link ICalParameters}
	 * object, and vice versa.
	 * </p>
	 * @return the URIs or an empty list if none are set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public List<String> getDelegatedTo() {
		return get(DELEGATED_TO);
	}

	/**
	 * <p>
	 * Gets the DIR (directory entry) parameter value.
	 * </p>
	 * <p>
	 * This parameter contains a URI (such as an LDAP URI) which points to
	 * additional information about the person that the property represents. It
	 * is used by the {@link Attendee} and {@link Organizer} properties.
	 * </p>
	 * @return the URI or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	public String getDirectoryEntry() {
		return first(DIR);
	}

	/**
	 * <p>
	 * Sets the DIR (directory entry) parameter value.
	 * </p>
	 * <p>
	 * This parameter contains a URI (such as an LDAP URI) which points to
	 * additional information about the person that the property represents. It
	 * is used by the {@link Attendee} and {@link Organizer} properties.
	 * </p>
	 * @param uri the URI or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	public void setDirectoryEntry(String uri) {
		replace(DIR, uri);
	}

	/**
	 * <p>
	 * Gets the DISPLAY parameter values.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Image} property. It defines the ways
	 * in which the client application should display the image (for example, as
	 * a thumbnail-sized image).
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link ICalParameters}
	 * object, and vice versa.
	 * </p>
	 * @return the display suggestions or empty list if none are defined
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-13" >
	 * draft-ietf-calext-extensions p.13</a>
	 */
	public List<Display> getDisplays() {
		return new EnumParameterList<Display>(DISPLAY) {
			@Override
			protected Display _asObject(String value) {
				return Display.get(value);
			}
		};
	}

	/**
	 * <p>
	 * Gets the EMAIL parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. Normally, this
	 * property's value contains the email address of the attendee. But if the
	 * property value must hold something else, this parameter can be used to
	 * store the attendee's email address.
	 * </p>
	 * @return the email or null if not set
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-14" >
	 * draft-ietf-calext-extensions p.14</a>
	 */
	public String getEmail() {
		return first(EMAIL);
	}

	/**
	 * <p>
	 * Sets the EMAIL parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. Normally, this
	 * property's value contains the email address of the attendee. But if the
	 * property value must hold something else, this parameter can be used to
	 * store the attendee's email address.
	 * </p>
	 * @param email the email or null to remove
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-14" >
	 * draft-ietf-calext-extensions p.14</a>
	 */
	public void setEmail(String email) {
		replace(EMAIL, email);
	}

	/**
	 * <p>
	 * Gets the ENCODING parameter value.
	 * </p>
	 * <p>
	 * This parameter defines how the property value is encoded (for example,
	 * "base64" for a binary value).
	 * </p>
	 * @return the encoding or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18-9</a>
	 */
	public Encoding getEncoding() {
		String value = first(ENCODING);
		return (value == null) ? null : Encoding.get(value);
	}

	/**
	 * <p>
	 * Sets the ENCODING parameter value.
	 * </p>
	 * <p>
	 * This parameter defines how the property value is encoded (for example,
	 * "base64" for a binary value).
	 * </p>
	 * @param encoding the encoding or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18-9</a>
	 */
	public void setEncoding(Encoding encoding) {
		replace(ENCODING, (encoding == null) ? null : encoding.getValue());
	}

	/**
	 * <p>
	 * Gets the EXPECT parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines
	 * whether the event organizer expects the attendee to attend or not. It is
	 * only used in the vCal 1.0 standard.
	 * </p>
	 * @return the attendance expectation or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public String getExpect() {
		return first(EXPECT);
	}

	/**
	 * <p>
	 * Sets the EXPECT parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines
	 * whether the event organizer expects the attendee to attend or not. It is
	 * only used in the vCal 1.0 standard.
	 * </p>
	 * @param expect the attendance expectation or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public void setExpect(String expect) {
		replace(EXPECT, expect);
	}

	/**
	 * <p>
	 * Gets the FEATURE parameter values.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Conference} property. It defines the
	 * features that the conference supports (for example, audio and video).
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link ICalParameters}
	 * object, and vice versa.
	 * </p>
	 * @return the features or empty list if none are set
	 * @see <a href=
	 * "https://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-15" >
	 * draft-ietf-calext-extensions p.15</a>
	 */
	public List<Feature> getFeatures() {
		return new EnumParameterList<Feature>(FEATURE) {
			@Override
			protected Feature _asObject(String value) {
				return Feature.get(value);
			}
		};
	}

	/**
	 * <p>
	 * Gets the FMTTYPE (format type) parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the content type of the property value (for
	 * example, "image/jpg" if the property value is a JPEG image).
	 * </p>
	 * @return the format type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	public String getFormatType() {
		return first(FMTTYPE);
	}

	/**
	 * <p>
	 * Sets the FMTTYPE (format type) parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the content type of the property value (for
	 * example, "image/jpg" if the property value is a JPEG image).
	 * </p>
	 * @param formatType the format type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	public void setFormatType(String formatType) {
		replace(FMTTYPE, formatType);
	}

	/**
	 * <p>
	 * Gets the FBTYPE (free busy type) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link FreeBusy} property. It defines
	 * whether the person is "free" or "busy" over the time periods that are
	 * specified in the property value. If this parameter is not set, the user
	 * should be considered "busy" during these times.
	 * </p>
	 * @return the free busy type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public FreeBusyType getFreeBusyType() {
		String value = first(FBTYPE);
		return (value == null) ? null : FreeBusyType.get(value);
	}

	/**
	 * <p>
	 * Sets the FBTYPE (free busy type) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link FreeBusy} property. It defines
	 * whether the person is "free" or "busy" over the time periods that are
	 * specified in the property value. If this parameter is not set, the user
	 * should be considered "busy" during these times.
	 * </p>
	 * @param freeBusyType the free busy type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public void setFreeBusyType(FreeBusyType freeBusyType) {
		replace(FBTYPE, (freeBusyType == null) ? null : freeBusyType.getValue());
	}

	/**
	 * <p>
	 * Gets the LABEL parameter value.
	 * </p>
	 * <p>
	 * This parameter defines a human-readable label for the property.
	 * </p>
	 * @return the label or null if not set
	 * @see <a href=
	 * "http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-16">
	 * draft-ietf-calext-extensions-01 p.16</a>
	 */
	public String getLabel() {
		return first(LABEL);
	}

	/**
	 * <p>
	 * Sets the LABEL parameter value.
	 * </p>
	 * <p>
	 * This parameter defines a human-readable label for the property.
	 * </p>
	 * @param label the label or null to remove
	 * @see <a href=
	 * "http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-16">
	 * draft-ietf-calext-extensions-01 p.16</a>
	 */
	public void setLabel(String label) {
		replace(LABEL, label);
	}

	/**
	 * <p>
	 * Gets the LANGUAGE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the language that the property value is written in
	 * (for example, "en" for English).
	 * </p>
	 * @return the language or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	public String getLanguage() {
		return first(LANGUAGE);
	}

	/**
	 * <p>
	 * Sets the LANGUAGE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the language that the property value is written in
	 * (for example, "en" for English).
	 * </p>
	 * @param language the language or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	public void setLanguage(String language) {
		replace(LANGUAGE, language);
	}

	/**
	 * <p>
	 * Gets the MEMBER property values.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * groups that the attendee is a member of in the form of URIs. Typically,
	 * these are email URIs (for example, "mailto:mailinglist@example.com").
	 * </p>
	 * <p>
	 * Changes to the returned list will update the {@link ICalParameters}
	 * object, and vice versa.
	 * </p>
	 * @return the groups or empty list if none are set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public List<String> getMembers() {
		return get(MEMBER);
	}

	/**
	 * <p>
	 * Gets the PARTSTAT (participation status) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * participation status of the attendee (for example, "ACCEPTED"). If none
	 * is defined, then the property should be treated as if this parameter was
	 * set to "NEEDS-ACTION".
	 * </p>
	 * @return the participation status or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22</a>
	 */
	public String getParticipationStatus() {
		return first(PARTSTAT);
	}

	/**
	 * <p>
	 * Gets the PARTSTAT (participation status) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * participation status of the attendee (for example, "ACCEPTED"). If none
	 * is defined, then the property should be treated as if this parameter was
	 * set to "NEEDS-ACTION".
	 * </p>
	 * @param participationStatus the participation status or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22</a>
	 */
	public void setParticipationStatus(String participationStatus) {
		replace(PARTSTAT, participationStatus);
	}

	/**
	 * <p>
	 * Gets the RANGE parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link RecurrenceId} property. It defines
	 * the effective range of recurrence instances that the property references.
	 * </p>
	 * @return the range or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public Range getRange() {
		String value = first(RANGE);
		return (value == null) ? null : Range.get(value);
	}

	/**
	 * <p>
	 * Sets the RANGE parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link RecurrenceId} property. It defines
	 * the effective range of recurrence instances that the property references.
	 * </p>
	 * @param range the range or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public void setRange(Range range) {
		replace(RANGE, (range == null) ? null : range.getValue());
	}

	/**
	 * <p>
	 * Gets the RELATED parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Trigger} property. It defines the
	 * date-time field that the property's duration (if specified) is relative
	 * to (for example, the start date or the end date).
	 * </p>
	 * @return the related field or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public Related getRelated() {
		String value = first(RELATED);
		return (value == null) ? null : Related.get(value);
	}

	/**
	 * <p>
	 * Sets the RELATED parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Trigger} property. It defines the
	 * date-time field that the property's duration (if specified) is relative
	 * to (for example, the start date or the end date).
	 * </p>
	 * @param related the related field or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public void setRelated(Related related) {
		replace(RELATED, (related == null) ? null : related.getValue());
	}

	/**
	 * <p>
	 * Gets the RELTYPE (relationship type) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link RelatedTo} property. It defines the
	 * kind of relationship the property is describing (for example, a "child"
	 * relationship).
	 * </p>
	 * @return the relationship type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public RelationshipType getRelationshipType() {
		String value = first(RELTYPE);
		return (value == null) ? null : RelationshipType.get(value);
	}

	/**
	 * <p>
	 * Sets the RELTYPE (relationship type) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link RelatedTo} property. It defines the
	 * kind of relationship the property is describing (for example, a "child"
	 * relationship).
	 * </p>
	 * @param relationshipType the relationship type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public void setRelationshipType(RelationshipType relationshipType) {
		replace(RELTYPE, (relationshipType == null) ? null : relationshipType.getValue());
	}

	/**
	 * <p>
	 * Gets the ROLE parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * attendee's role and/or whether they must attend or not (for example,
	 * "OPT-PARTICIPANT" for "optional participant"). If none is defined, then
	 * the property should be treated as if this parameter was set to
	 * "REQ-PARTICIPANT" (required participant).
	 * </p>
	 * @return the role or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public String getRole() {
		/*
		 * Note: The acceptable values for this parameter differs in vCal 1.0,
		 * which is why this method does not return an enum.
		 */
		return first(ROLE);
	}

	/**
	 * <p>
	 * Sets the ROLE parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * attendee's role and/or whether they must attend or not (for example,
	 * "OPT-PARTICIPANT" for "optional participant"). If none is defined, then
	 * the property should be treated as if this parameter was set to
	 * "REQ-PARTICIPANT" (required participant).
	 * </p>
	 * @param role the role or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public void setRole(String role) {
		replace(ROLE, role);
	}

	/**
	 * <p>
	 * Gets the RSVP parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines
	 * whether the event organizer would like the attendee to reply with his or
	 * her intention of attending ("true" if the organizer would like a reply,
	 * "false" if not). If this parameter is not defined, then the property
	 * should be treated as if this parameter was set to "false".
	 * </p>
	 * @return the value or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public String getRsvp() {
		/*
		 * Note: The acceptable values for this parameter differs in vCal 1.0,
		 * which is why this method does not return a boolean.
		 */
		return first(RSVP);
	}

	/**
	 * <p>
	 * Sets the RSVP parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines
	 * whether the event organizer would like the attendee to reply with his or
	 * her intention of attending ("true" if the organizer would like a reply,
	 * "false" if not). If this parameter is not defined, then the property
	 * should be treated as if this parameter was set to "false".
	 * </p>
	 * @param rsvp the value or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public void setRsvp(String rsvp) {
		replace(RSVP, rsvp);
	}

	/**
	 * <p>
	 * Gets the SENT-BY parameter value.
	 * </p>
	 * <p>
	 * This parameter defines a URI which represents a person who is acting on
	 * behalf of the person that is defined in the property. Typically, the URI
	 * is an email URI (for example, "mailto:janedoe@example.com"). It is used
	 * by the {@link Attendee} and {@link Organizer} properties.
	 * </p>
	 * @return the URI or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	public String getSentBy() {
		return first(SENT_BY);
	}

	/**
	 * <p>
	 * Sets the SENT-BY parameter value.
	 * </p>
	 * <p>
	 * This parameter defines a URI which represents a person who is acting on
	 * behalf of the person that is defined in the property. Typically, the URI
	 * is an email URI (for example, "mailto:janedoe@example.com"). It is used
	 * by the {@link Attendee} and {@link Organizer} properties.
	 * </p>
	 * @param uri the URI or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	public void setSentBy(String uri) {
		replace(SENT_BY, uri);
	}

	/**
	 * <p>
	 * Gets the STATUS parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * status of the person's event invitation (for example, "TENTATIVE" if the
	 * person may or may not attend). It is only used in the vCal 1.0 standard.
	 * </p>
	 * @return the status or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public String getStatus() {
		return first(STATUS);
	}

	/**
	 * <p>
	 * Sets the STATUS parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by the {@link Attendee} property. It defines the
	 * status of the person's event invitation (for example, "TENTATIVE" if the
	 * person may or may not attend). It is only used in the vCal 1.0 standard.
	 * </p>
	 * @param status the status or null to remove
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public void setStatus(String status) {
		replace(STATUS, status);
	}

	/**
	 * <p>
	 * Gets the TZID (timezone ID) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by properties that contain date-time values. It
	 * defines the timezone that the property value is formatted in. It either
	 * references a timezone defined in a VTIMEZONE component, or contains an
	 * Olson timezone ID. To use an Olson timezone ID, the parameter value must
	 * be prepended with a "/" (for example, "/America/New_York").
	 * </p>
	 * @return the timezone ID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27-8</a>
	 */
	public String getTimezoneId() {
		return first(TZID);
	}

	/**
	 * <p>
	 * Sets the TZID (timezone ID) parameter value.
	 * </p>
	 * <p>
	 * This parameter is used by properties that contain date-time values. It
	 * defines the timezone that the property value is formatted in. It either
	 * references a timezone defined in a VTIMEZONE component, or contains an
	 * Olson timezone ID. To use an Olson timezone ID, the parameter value must
	 * be prepended with a "/" (for example, "/America/New_York").
	 * </p>
	 * @param timezoneId the timezone ID or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27-8</a>
	 */
	public void setTimezoneId(String timezoneId) {
		replace(TZID, timezoneId);
	}

	/**
	 * <p>
	 * Gets the TYPE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the content type of the property value (for
	 * example, "WAVE" for an audio file). It is only used in the vCal 1.0
	 * standard.
	 * </p>
	 * @return the type or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.27</a>
	 */
	public String getType() {
		return first(TYPE);
	}

	/**
	 * <p>
	 * Sets the TYPE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the content type of the property value (for
	 * example, "WAVE" for an audio file). It is only used in the vCal 1.0
	 * standard.
	 * </p>
	 * @param type the type or null to remove
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.27</a>
	 */
	public void setType(String type) {
		replace(TYPE, type);
	}

	/**
	 * <p>
	 * Gets the VALUE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the data type of the property value (for example,
	 * "date" if the property value is a date without a time component). It is
	 * used if the property accepts multiple values that have different data
	 * types.
	 * </p>
	 * @return the data type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
	 * p.29-50</a>
	 */
	public ICalDataType getValue() {
		String value = first(VALUE);
		return (value == null) ? null : ICalDataType.get(value);
	}

	/**
	 * <p>
	 * Sets the VALUE parameter value.
	 * </p>
	 * <p>
	 * This parameter defines the data type of the property value (for example,
	 * "date" if the property value is a date without a time component). It is
	 * used if the property accepts multiple values that have different data
	 * types.
	 * </p>
	 * @param dataType the data type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
	 * p.29-50</a>
	 */
	public void setValue(ICalDataType dataType) {
		replace(VALUE, (dataType == null) ? null : dataType.getName());
	}

	/**
	 * <p>
	 * Checks the parameters for data consistency problems or deviations from
	 * the specification.
	 * </p>
	 * <p>
	 * These problems will not prevent the iCalendar object from being written
	 * to a data stream*, but may prevent it from being parsed correctly by the
	 * consuming application.
	 * </p>
	 * <p>
	 * *With a few exceptions: One thing this method does is check for illegal
	 * characters. There are certain characters that will break the iCalendar
	 * syntax if written (such as a newline character in a parameter name). If
	 * one of these characters is present, it WILL prevent the iCalendar object
	 * from being written.
	 * </p>
	 * @param version the version to validate against
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public List<ValidationWarning> validate(ICalVersion version) {
		List<ValidationWarning> warnings = new ArrayList<ValidationWarning>(0);

		SyntaxStyle syntax;
		switch (version) {
		case V1_0:
			syntax = SyntaxStyle.OLD;
			break;
		default:
			syntax = SyntaxStyle.NEW;
			break;
		}

		/*
		 * Check for invalid characters in names and values.
		 */
		for (Map.Entry<String, List<String>> entry : this) {
			String name = entry.getKey();

			//check the parameter name
			if (!VObjectValidator.validateParameterName(name, syntax, true)) {
				if (syntax == SyntaxStyle.OLD) {
					AllowedCharacters notAllowed = VObjectValidator.allowedCharactersParameterName(syntax, true).flip();
					warnings.add(new ValidationWarning(57, name, notAllowed.toString(true)));
				} else {
					warnings.add(new ValidationWarning(54, name));
				}
			}

			//check the parameter value(s)
			List<String> values = entry.getValue();
			for (String value : values) {
				if (!VObjectValidator.validateParameterValue(value, syntax, false, true)) {
					AllowedCharacters notAllowed = VObjectValidator.allowedCharactersParameterValue(syntax, false, true).flip();
					int code = (syntax == SyntaxStyle.OLD) ? 58 : 53;
					warnings.add(new ValidationWarning(code, name, value, notAllowed.toString(true)));
				}
			}
		}

		final int nonStandardCode = 1, deprecated = 47;

		String value = first(RSVP);
		if (value != null) {
			value = value.toLowerCase();
			List<String> validValues = Arrays.asList("true", "false", "yes", "no");
			if (!validValues.contains(value)) {
				warnings.add(new ValidationWarning(nonStandardCode, RSVP, value, validValues));
			}
		}

		value = first(CUTYPE);
		if (value != null && CalendarUserType.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, CUTYPE, value, CalendarUserType.all()));
		}

		value = first(ENCODING);
		if (value != null && Encoding.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, ENCODING, value, Encoding.all()));
		}

		value = first(FBTYPE);
		if (value != null && FreeBusyType.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, FBTYPE, value, FreeBusyType.all()));
		}

		value = first(PARTSTAT);
		if (value != null && ParticipationStatus.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, PARTSTAT, value, ParticipationStatus.all()));
		}

		value = first(RANGE);
		if (value != null) {
			Range range = Range.find(value);

			if (range == null) {
				warnings.add(new ValidationWarning(nonStandardCode, RANGE, value, Range.all()));
			}

			if (range == Range.THIS_AND_PRIOR && version == ICalVersion.V2_0) {
				warnings.add(new ValidationWarning(deprecated, RANGE, value));
			}
		}

		value = first(RELATED);
		if (value != null && Related.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, RELATED, value, Related.all()));
		}

		value = first(RELTYPE);
		if (value != null && RelationshipType.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, RELTYPE, value, RelationshipType.all()));
		}

		value = first(ROLE);
		if (value != null && Role.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, ROLE, value, Role.all()));
		}

		value = first(VALUE);
		if (value != null && ICalDataType.find(value) == null) {
			warnings.add(new ValidationWarning(nonStandardCode, VALUE, value, ICalDataType.all()));
		}

		return warnings;
	}

	@Override
	protected String sanitizeKey(String key) {
		return (key == null) ? null : key.toUpperCase();
	}

	@Override
	public int hashCode() {
		/*
		 * Remember: Keys are case-insensitive, key order does not matter, and
		 * value order does not matter
		 */
		final int prime = 31;
		int result = 1;

		for (Map.Entry<String, List<String>> entry : this) {
			String key = entry.getKey();
			List<String> value = entry.getValue();

			int valueHash = 1;
			for (String v : value) {
				valueHash += v.toLowerCase().hashCode();
			}

			int entryHash = 1;
			entryHash += prime * entryHash + ((key == null) ? 0 : key.toLowerCase().hashCode());
			entryHash += prime * entryHash + valueHash;

			result += entryHash;
		}

		return result;
	}

	/**
	 * <p>
	 * Determines whether the given object is logically equivalent to this list
	 * of parameters.
	 * </p>
	 * <p>
	 * Note that iCalendar parameter names are case-insensitive. Also, note that
	 * the order in which they are defined does not matter.
	 * </p>
	 * @param obj the object to compare to
	 * @return true if the objects are equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		/*
		 * Remember: Keys are case-insensitive, key order does not matter, and
		 * value order does not matter
		 */
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		ICalParameters other = (ICalParameters) obj;
		if (size() != other.size()) return false;

		for (Map.Entry<String, List<String>> entry : this) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			List<String> otherValue = other.get(key);

			if (value.size() != otherValue.size()) {
				return false;
			}

			List<String> valueLower = new ArrayList<String>(value.size());
			for (String v : value) {
				valueLower.add(v.toLowerCase());
			}
			Collections.sort(valueLower);

			List<String> otherValueLower = new ArrayList<String>(otherValue.size());
			for (String v : otherValue) {
				otherValueLower.add(v.toLowerCase());
			}
			Collections.sort(otherValueLower);

			if (!valueLower.equals(otherValueLower)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>
	 * A list that converts the raw string values of a parameter to the
	 * appropriate {@link EnumParameterValue} object that some parameters use.
	 * </p>
	 * <p>
	 * This list is backed by the {@link ICalParameters} object. Any changes
	 * made to the list will affect the {@link ICalParameters} object and vice
	 * versa.
	 * </p>
	 * @param <T> the enum parameter class
	 */
	public abstract class EnumParameterList<T extends EnumParameterValue> extends ICalParameterList<T> {
		public EnumParameterList(String parameterName) {
			super(parameterName);
		}

		@Override
		protected String _asString(T value) {
			return value.getValue();
		}
	}

	/**
	 * <p>
	 * A list that converts the raw string values of a parameter to another kind
	 * of value (for example, Integers).
	 * </p>
	 * <p>
	 * This list is backed by the {@link ICalParameters} object. Any changes
	 * made to the list will affect the {@link ICalParameters} object and vice
	 * versa.
	 * </p>
	 * <p>
	 * If a String value cannot be converted to the appropriate data type, an
	 * {@link IllegalStateException} is thrown.
	 * </p>
	 */
	public abstract class ICalParameterList<T> extends AbstractList<T> {
		protected final String parameterName;
		protected final List<String> parameterValues;

		/**
		 * @param parameterName the name of the parameter (case insensitive)
		 */
		public ICalParameterList(String parameterName) {
			this.parameterName = parameterName;
			parameterValues = ICalParameters.this.get(parameterName);
		}

		@Override
		public void add(int index, T value) {
			String valueStr = _asString(value);
			parameterValues.add(index, valueStr);
		}

		@Override
		public T remove(int index) {
			String removed = parameterValues.remove(index);
			return asObject(removed);
		}

		@Override
		public T get(int index) {
			String value = parameterValues.get(index);
			return asObject(value);
		}

		@Override
		public T set(int index, T value) {
			String valueStr = _asString(value);
			String replaced = parameterValues.set(index, valueStr);
			return asObject(replaced);
		}

		@Override
		public int size() {
			return parameterValues.size();
		}

		private T asObject(String value) {
			try {
				return _asObject(value);
			} catch (Exception e) {
				throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(26, parameterName), e);
			}
		}

		/**
		 * Converts the object to a String value for storing in the
		 * {@link ICalParameters} object.
		 * @param value the value
		 * @return the string value
		 */
		protected abstract String _asString(T value);

		/**
		 * Converts a String value to its object form.
		 * @param value the string value
		 * @return the object
		 * @throws Exception if there is a problem parsing the string
		 */
		protected abstract T _asObject(String value) throws Exception;
	}
}
