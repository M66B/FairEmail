/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.mail.internet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;

import com.sun.mail.util.MailLogger;

/**
 * Formats and parses date specification based on
 * <a href="http://www.ietf.org/rfc/rfc2822.txt" target="_top">RFC 2822</a>. <p>
 *
 * This class does not support methods that influence the format. It always
 * formats the date based on the specification below.<p>
 *
 * 3.3. Date and Time Specification
 * <p>
 * Date and time occur in several header fields.  This section specifies
 * the syntax for a full date and time specification.  Though folding
 * white space is permitted throughout the date-time specification, it is
 * RECOMMENDED that a single space be used in each place that FWS appears
 * (whether it is required or optional); some older implementations may
 * not interpret other occurrences of folding white space correctly.
 * <pre>
 * date-time       =       [ day-of-week "," ] date FWS time [CFWS]
 *
 * day-of-week     =       ([FWS] day-name) / obs-day-of-week
 *
 * day-name        =       "Mon" / "Tue" / "Wed" / "Thu" /
 *                         "Fri" / "Sat" / "Sun"
 *
 * date            =       day month year
 *
 * year            =       4*DIGIT / obs-year
 *
 * month           =       (FWS month-name FWS) / obs-month
 *
 * month-name      =       "Jan" / "Feb" / "Mar" / "Apr" /
 *                         "May" / "Jun" / "Jul" / "Aug" /
 *                         "Sep" / "Oct" / "Nov" / "Dec"
 *
 * day             =       ([FWS] 1*2DIGIT) / obs-day
 *
 * time            =       time-of-day FWS zone
 *
 * time-of-day     =       hour ":" minute [ ":" second ]
 *
 * hour            =       2DIGIT / obs-hour
 *
 * minute          =       2DIGIT / obs-minute
 *
 * second          =       2DIGIT / obs-second
 *
 * zone            =       (( "+" / "-" ) 4DIGIT) / obs-zone
 * </pre>
 * The day is the numeric day of the month.  The year is any numeric year
 * 1900 or later.
 * <p>
 * The time-of-day specifies the number of hours, minutes, and optionally
 * seconds since midnight of the date indicated.
 * <p>
 * The date and time-of-day SHOULD express local time.
 * <p>
 * The zone specifies the offset from Coordinated Universal Time (UTC,
 * formerly referred to as "Greenwich Mean Time") that the date and
 * time-of-day represent.  The "+" or "-" indicates whether the
 * time-of-day is ahead of (i.e., east of) or behind (i.e., west of)
 * Universal Time.  The first two digits indicate the number of hours
 * difference from Universal Time, and the last two digits indicate the
 * number of minutes difference from Universal Time.  (Hence, +hhmm means
 * +(hh * 60 + mm) minutes, and -hhmm means -(hh * 60 + mm) minutes).  The
 * form "+0000" SHOULD be used to indicate a time zone at Universal Time.
 * Though "-0000" also indicates Universal Time, it is used to indicate
 * that the time was generated on a system that may be in a local time
 * zone other than Universal Time and therefore indicates that the
 * date-time contains no information about the local time zone.
 * <p>
 * A date-time specification MUST be semantically valid.  That is, the
 * day-of-the-week (if included) MUST be the day implied by the date, the
 * numeric day-of-month MUST be between 1 and the number of days allowed
 * for the specified month (in the specified year), the time-of-day MUST
 * be in the range 00:00:00 through 23:59:60 (the number of seconds
 * allowing for a leap second; see [STD12]), and the zone MUST be within
 * the range -9959 through +9959.
 *
 * <h3><a id="synchronization">Synchronization</a></h3>
 * 
 * <p>
 * Date formats are not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
 *
 * @author	Anthony Vanelverdinghe
 * @author	Max Spivak
 * @since	JavaMail 1.2
 */
public class MailDateFormat extends SimpleDateFormat {

    private static final long serialVersionUID = -8148227605210628779L;
    private static final String PATTERN = "EEE, d MMM yyyy HH:mm:ss Z (z)";

    private static final MailLogger LOGGER = new MailLogger(
            MailDateFormat.class, "DEBUG", false, System.out);

    private static final int UNKNOWN_DAY_NAME = -1;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final int LEAP_SECOND = 60;

    /**
     * Create a new date format for the RFC2822 specification with lenient
     * parsing.
     */
    public MailDateFormat() {
        super(PATTERN, Locale.US);
    }

    /**
     * Allows to serialize instances such that they are deserializable with the
     * previous implementation.
     *
     * @return the object to be serialized
     * @throws ObjectStreamException	never
     */
    private Object writeReplace() throws ObjectStreamException {
        MailDateFormat fmt = new MailDateFormat();
        fmt.superApplyPattern("EEE, d MMM yyyy HH:mm:ss 'XXXXX' (z)");
        fmt.setTimeZone(getTimeZone());
        return fmt;
    }

    /**
     * Allows to deserialize instances that were serialized with the previous
     * implementation.
     *
     * @param in the stream containing the serialized object
     * @throws IOException	on read failures
     * @throws ClassNotFoundException	never
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        super.applyPattern(PATTERN);
    }

    /**
     * Overrides Cloneable.
     *
     * @return a clone of this instance
     * @since JavaMail 1.6
     */
    @Override
    public MailDateFormat clone() {
        return (MailDateFormat) super.clone();
    }

    /**
     * Formats the given date in the format specified by 
     * RFC 2822 in the current TimeZone.
     *
     * @param   date            the Date object
     * @param   dateStrBuf      the formatted string
     * @param   fieldPosition   the current field position
     * @return	StringBuffer    the formatted String
     * @since			JavaMail 1.2
     */
    @Override
    public StringBuffer format(Date date, StringBuffer dateStrBuf,
            FieldPosition fieldPosition) {
        return super.format(date, dateStrBuf, fieldPosition);
    }

    /**
     * Parses the given date in the format specified by
     * RFC 2822.
     * <ul>
     * <li>With strict parsing, obs-* tokens are unsupported. Lenient parsing
     * supports obs-year and obs-zone, with the exception of the 1-character
     * military time zones.
     * <li>The optional CFWS token at the end is not parsed.
     * <li>RFC 2822 specifies that a zone of "-0000" indicates that the
     * date-time contains no information about the local time zone. This class
     * uses the UTC time zone in this case.
     * </ul>
     *
     * @param   text    the formatted date to be parsed
     * @param   pos     the current parse position
     * @return	Date    the parsed date. In case of error, returns null.
     * @since		JavaMail 1.2
     */
    @Override
    public Date parse(String text, ParsePosition pos) {
        if (text == null || pos == null) {
            throw new NullPointerException();
        } else if (0 > pos.getIndex() || pos.getIndex() >= text.length()) {
            return null;
        }

        return isLenient()
                ? new Rfc2822LenientParser(text, pos).parse()
                : new Rfc2822StrictParser(text, pos).parse();
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates a specific calendar.
     *
     * @throws UnsupportedOperationException if this method is invoked
     */
    @Override
    public void setCalendar(Calendar newCalendar) {
        throw new UnsupportedOperationException("Method "
                + "setCalendar() shouldn't be called");
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates a specific number format.
     *
     * @throws UnsupportedOperationException if this method is invoked
     */
    @Override
    public void setNumberFormat(NumberFormat newNumberFormat) {
        throw new UnsupportedOperationException("Method "
                + "setNumberFormat() shouldn't be called");
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates a specific pattern.
     *
     * @throws UnsupportedOperationException if this method is invoked
     * @since JavaMail 1.6
     */
    @Override
    public void applyLocalizedPattern(String pattern) {
        throw new UnsupportedOperationException("Method "
                + "applyLocalizedPattern() shouldn't be called");
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates a specific pattern.
     *
     * @throws UnsupportedOperationException if this method is invoked
     * @since JavaMail 1.6
     */
    @Override
    public void applyPattern(String pattern) {
        throw new UnsupportedOperationException("Method "
                + "applyPattern() shouldn't be called");
    }

    /**
     * This method allows serialization to change the pattern.
     */
    private void superApplyPattern(String pattern) {
        super.applyPattern(pattern);
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates another strategy for interpreting
     * 2-digits years.
     *
     * @return the start of the 100-year period into which two digit years are
     * parsed
     * @throws UnsupportedOperationException if this method is invoked
     * @since JavaMail 1.6
     */
    @Override
    public Date get2DigitYearStart() {
        throw new UnsupportedOperationException("Method "
                + "get2DigitYearStart() shouldn't be called");
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates another strategy for interpreting
     * 2-digits years.
     *
     * @throws UnsupportedOperationException if this method is invoked
     * @since JavaMail 1.6
     */
    @Override
    public void set2DigitYearStart(Date startDate) {
        throw new UnsupportedOperationException("Method "
                + "set2DigitYearStart() shouldn't be called");
    }

    /**
     * This method always throws an UnsupportedOperationException and should not
     * be used because RFC 2822 mandates specific date format symbols.
     *
     * @throws UnsupportedOperationException if this method is invoked
     * @since JavaMail 1.6
     */
    @Override
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        throw new UnsupportedOperationException("Method "
                + "setDateFormatSymbols() shouldn't be called");
    }

    /**
     * Returns the date, as specified by the parameters.
     *
     * @param dayName
     * @param day
     * @param month
     * @param year
     * @param hour
     * @param minute
     * @param second
     * @param zone
     * @return the date, as specified by the parameters
     * @throws IllegalArgumentException if this instance's Calendar is
     * non-lenient and any of the parameters have invalid values, or if dayName
     * is not consistent with day-month-year
     */
    private Date toDate(int dayName, int day, int month, int year,
            int hour, int minute, int second, int zone) {
        if (second == LEAP_SECOND) {
            second = 59;
        }

        TimeZone tz = calendar.getTimeZone();
        try {
            calendar.setTimeZone(UTC);
            calendar.clear();
            calendar.set(year, month, day, hour, minute, second);

            if (dayName == UNKNOWN_DAY_NAME
                    || dayName == calendar.get(Calendar.DAY_OF_WEEK)) {
                calendar.add(Calendar.MINUTE, zone);
                return calendar.getTime();
            } else {
                throw new IllegalArgumentException("Inconsistent day-name");
            }
        } finally {
            calendar.setTimeZone(tz);
        }
    }

    /**
     * This class provides the building blocks for date parsing.
     * <p>
     * It has the following invariants:
     * <ul>
     * <li>no exceptions are thrown, except for java.text.ParseException from
     * parse* methods
     * <li>when parse* throws ParseException OR get* returns INVALID_CHAR OR
     * skip* returns false OR peek* is invoked, then pos.getIndex() on method
     * exit is the same as it was on method entry
     * </ul>
     */
    private static abstract class AbstractDateParser {

        static final int INVALID_CHAR = -1;
        static final int MAX_YEAR_DIGITS = 8; // guarantees that:
        // year < new GregorianCalendar().getMaximum(Calendar.YEAR)

        final String text;
        final ParsePosition pos;

        AbstractDateParser(String text, ParsePosition pos) {
            this.text = text;
            this.pos = pos;
        }

        final Date parse() {
            int startPosition = pos.getIndex();
            try {
                return tryParse();
            } catch (Exception e) { // == ParseException | RuntimeException e
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Bad date: '" + text + "'", e);
                }
                pos.setErrorIndex(pos.getIndex());
                pos.setIndex(startPosition);
                return null;
            }
        }

        abstract Date tryParse() throws ParseException;

        /**
         * @return the java.util.Calendar constant for the parsed day name
         */
        final int parseDayName() throws ParseException {
            switch (getChar()) {
                case 'S':
                    if (skipPair('u', 'n')) {
                        return Calendar.SUNDAY;
                    } else if (skipPair('a', 't')) {
                        return Calendar.SATURDAY;
                    }
                    break;
                case 'T':
                    if (skipPair('u', 'e')) {
                        return Calendar.TUESDAY;
                    } else if (skipPair('h', 'u')) {
                        return Calendar.THURSDAY;
                    }
                    break;
                case 'M':
                    if (skipPair('o', 'n')) {
                        return Calendar.MONDAY;
                    }
                    break;
                case 'W':
                    if (skipPair('e', 'd')) {
                        return Calendar.WEDNESDAY;
                    }
                    break;
                case 'F':
                    if (skipPair('r', 'i')) {
                        return Calendar.FRIDAY;
                    }
                    break;
                case INVALID_CHAR:
                    throw new ParseException("Invalid day-name",
                            pos.getIndex());
            }
            pos.setIndex(pos.getIndex() - 1);
            throw new ParseException("Invalid day-name", pos.getIndex());
        }

        /**
         * @return the java.util.Calendar constant for the parsed month name
         */
        @SuppressWarnings("fallthrough")
        final int parseMonthName(boolean caseSensitive) throws ParseException {
            switch (getChar()) {
                case 'j':
                    if (caseSensitive) {
                        break;
                    }
                case 'J':
                    if (skipChar('u') || (!caseSensitive && skipChar('U'))) {
                        if (skipChar('l') || (!caseSensitive
                                && skipChar('L'))) {
                            return Calendar.JULY;
                        } else if (skipChar('n') || (!caseSensitive
                                && skipChar('N'))) {
                            return Calendar.JUNE;
                        } else {
                            pos.setIndex(pos.getIndex() - 1);
                        }
                    } else if (skipPair('a', 'n') || (!caseSensitive
                            && skipAlternativePair('a', 'A', 'n', 'N'))) {
                        return Calendar.JANUARY;
                    }
                    break;
                case 'm':
                    if (caseSensitive) {
                        break;
                    }
                case 'M':
                    if (skipChar('a') || (!caseSensitive && skipChar('A'))) {
                        if (skipChar('r') || (!caseSensitive
                                && skipChar('R'))) {
                            return Calendar.MARCH;
                        } else if (skipChar('y') || (!caseSensitive
                                && skipChar('Y'))) {
                            return Calendar.MAY;
                        } else {
                            pos.setIndex(pos.getIndex() - 1);
                        }
                    }
                    break;
                case 'a':
                    if (caseSensitive) {
                        break;
                    }
                case 'A':
                    if (skipPair('u', 'g') || (!caseSensitive
                            && skipAlternativePair('u', 'U', 'g', 'G'))) {
                        return Calendar.AUGUST;
                    } else if (skipPair('p', 'r') || (!caseSensitive
                            && skipAlternativePair('p', 'P', 'r', 'R'))) {
                        return Calendar.APRIL;
                    }
                    break;
                case 'd':
                    if (caseSensitive) {
                        break;
                    }
                case 'D':
                    if (skipPair('e', 'c') || (!caseSensitive
                            && skipAlternativePair('e', 'E', 'c', 'C'))) {
                        return Calendar.DECEMBER;
                    }
                    break;
                case 'o':
                    if (caseSensitive) {
                        break;
                    }
                case 'O':
                    if (skipPair('c', 't') || (!caseSensitive
                            && skipAlternativePair('c', 'C', 't', 'T'))) {
                        return Calendar.OCTOBER;
                    }
                    break;
                case 's':
                    if (caseSensitive) {
                        break;
                    }
                case 'S':
                    if (skipPair('e', 'p') || (!caseSensitive
                            && skipAlternativePair('e', 'E', 'p', 'P'))) {
                        return Calendar.SEPTEMBER;
                    }
                    break;
                case 'n':
                    if (caseSensitive) {
                        break;
                    }
                case 'N':
                    if (skipPair('o', 'v') || (!caseSensitive
                            && skipAlternativePair('o', 'O', 'v', 'V'))) {
                        return Calendar.NOVEMBER;
                    }
                    break;
                case 'f':
                    if (caseSensitive) {
                        break;
                    }
                case 'F':
                    if (skipPair('e', 'b') || (!caseSensitive
                            && skipAlternativePair('e', 'E', 'b', 'B'))) {
                        return Calendar.FEBRUARY;
                    }
                    break;
                case INVALID_CHAR:
                    throw new ParseException("Invalid month", pos.getIndex());
            }
            pos.setIndex(pos.getIndex() - 1);
            throw new ParseException("Invalid month", pos.getIndex());
        }

        /**
         * @return the number of minutes to be added to the time in the local
         * time zone, in order to obtain the equivalent time in the UTC time
         * zone. Returns 0 if the date-time contains no information about the
         * local time zone.
         */
        final int parseZoneOffset() throws ParseException {
            int sign = getChar();
            if (sign == '+' || sign == '-') {
                int offset = parseAsciiDigits(4, 4, true);
                if (!isValidZoneOffset(offset)) {
                    pos.setIndex(pos.getIndex() - 5);
                    throw new ParseException("Invalid zone", pos.getIndex());
                }

                return ((sign == '+') ? -1 : 1)
                        * (offset / 100 * 60 + offset % 100);
            } else if (sign != INVALID_CHAR) {
                pos.setIndex(pos.getIndex() - 1);
            }
            throw new ParseException("Invalid zone", pos.getIndex());
        }

        boolean isValidZoneOffset(int offset) {
            return (offset % 100) < 60;
        }

        final int parseAsciiDigits(int count) throws ParseException {
            return parseAsciiDigits(count, count);
        }

        final int parseAsciiDigits(int min, int max) throws ParseException {
            return parseAsciiDigits(min, max, false);
        }

        final int parseAsciiDigits(int min, int max, boolean isEOF)
                throws ParseException {
            int result = 0;
            int nbDigitsParsed = 0;
            while (nbDigitsParsed < max && peekAsciiDigit()) {
                result = result * 10 + getAsciiDigit();
                nbDigitsParsed++;
            }

            if ((nbDigitsParsed < min)
                    || (nbDigitsParsed == max && !isEOF && peekAsciiDigit())) {
                pos.setIndex(pos.getIndex() - nbDigitsParsed);
            } else {
                return result;
            }

            String range = (min == max)
                    ? Integer.toString(min)
                    : "between " + min + " and " + max;
            throw new ParseException("Invalid input: expected "
                    + range + " ASCII digits", pos.getIndex());
        }

        final void parseFoldingWhiteSpace() throws ParseException {
            if (!skipFoldingWhiteSpace()) {
                throw new ParseException("Invalid input: expected FWS",
                        pos.getIndex());
            }
        }

        final void parseChar(char ch) throws ParseException {
            if (!skipChar(ch)) {
                throw new ParseException("Invalid input: expected '" + ch + "'",
                        pos.getIndex());
            }
        }

        final int getAsciiDigit() {
            int ch = getChar();
            if ('0' <= ch && ch <= '9') {
                return Character.digit((char) ch, 10);
            } else {
                if (ch != INVALID_CHAR) {
                    pos.setIndex(pos.getIndex() - 1);
                }
                return INVALID_CHAR;
            }
        }

        final int getChar() {
            if (pos.getIndex() < text.length()) {
                char ch = text.charAt(pos.getIndex());
                pos.setIndex(pos.getIndex() + 1);
                return ch;
            } else {
                return INVALID_CHAR;
            }
        }

        boolean skipFoldingWhiteSpace() {
            // fast paths: a single ASCII space or no FWS
            if (skipChar(' ')) {
                if (!peekFoldingWhiteSpace()) {
                    return true;
                } else {
                    pos.setIndex(pos.getIndex() - 1);
                }
            } else if (!peekFoldingWhiteSpace()) {
                return false;
            }

            // normal path
            int startIndex = pos.getIndex();
            if (skipWhiteSpace()) {
                while (skipNewline()) {
                    if (!skipWhiteSpace()) {
                        pos.setIndex(startIndex);
                        return false;
                    }
                }
                return true;
            } else if (skipNewline() && skipWhiteSpace()) {
                return true;
            } else {
                pos.setIndex(startIndex);
                return false;
            }
        }

        final boolean skipWhiteSpace() {
            int startIndex = pos.getIndex();
            while (skipAlternative(' ', '\t')) { /* empty */ }
            return pos.getIndex() > startIndex;
        }

        final boolean skipNewline() {
            return skipPair('\r', '\n');
        }

        final boolean skipAlternativeTriple(
                char firstStandard, char firstAlternative,
                char secondStandard, char secondAlternative,
                char thirdStandard, char thirdAlternative
        ) {
            if (skipAlternativePair(firstStandard, firstAlternative,
                    secondStandard, secondAlternative)) {
                if (skipAlternative(thirdStandard, thirdAlternative)) {
                    return true;
                } else {
                    pos.setIndex(pos.getIndex() - 2);
                }
            }
            return false;
        }

        final boolean skipAlternativePair(
                char firstStandard, char firstAlternative,
                char secondStandard, char secondAlternative
        ) {
            if (skipAlternative(firstStandard, firstAlternative)) {
                if (skipAlternative(secondStandard, secondAlternative)) {
                    return true;
                } else {
                    pos.setIndex(pos.getIndex() - 1);
                }
            }
            return false;
        }

        final boolean skipAlternative(char standard, char alternative) {
            return skipChar(standard) || skipChar(alternative);
        }

        final boolean skipPair(char first, char second) {
            if (skipChar(first)) {
                if (skipChar(second)) {
                    return true;
                } else {
                    pos.setIndex(pos.getIndex() - 1);
                }
            }
            return false;
        }

        final boolean skipChar(char ch) {
            if (pos.getIndex() < text.length()
                    && text.charAt(pos.getIndex()) == ch) {
                pos.setIndex(pos.getIndex() + 1);
                return true;
            } else {
                return false;
            }
        }

        final boolean peekAsciiDigit() {
            return (pos.getIndex() < text.length()
                    && '0' <= text.charAt(pos.getIndex())
                    && text.charAt(pos.getIndex()) <= '9');
        }

        boolean peekFoldingWhiteSpace() {
            return (pos.getIndex() < text.length()
                    && (text.charAt(pos.getIndex()) == ' '
                    || text.charAt(pos.getIndex()) == '\t'
                    || text.charAt(pos.getIndex()) == '\r'));
        }

        final boolean peekChar(char ch) {
            return (pos.getIndex() < text.length()
                    && text.charAt(pos.getIndex()) == ch);
        }

    }

    private class Rfc2822StrictParser extends AbstractDateParser {

        Rfc2822StrictParser(String text, ParsePosition pos) {
            super(text, pos);
        }

        @Override
        Date tryParse() throws ParseException {
            int dayName = parseOptionalBegin();

            int day = parseDay();
            int month = parseMonth();
            int year = parseYear();

            parseFoldingWhiteSpace();

            int hour = parseHour();
            parseChar(':');
            int minute = parseMinute();
            int second = (skipChar(':')) ? parseSecond() : 0;

            parseFwsBetweenTimeOfDayAndZone();

            int zone = parseZone();

            try {
                return MailDateFormat.this.toDate(dayName, day, month, year,
                        hour, minute, second, zone);
            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid input: some of the calendar "
                        + "fields have invalid values, or day-name is "
                        + "inconsistent with date", pos.getIndex());
            }
        }

        /**
         * @return the java.util.Calendar constant for the parsed day name, or
         * UNKNOWN_DAY_NAME iff the begin is missing
         */
        int parseOptionalBegin() throws ParseException {
            int dayName;
            if (!peekAsciiDigit()) {
                skipFoldingWhiteSpace();
                dayName = parseDayName();
                parseChar(',');
            } else {
                dayName = UNKNOWN_DAY_NAME;
            }
            return dayName;
        }

        int parseDay() throws ParseException {
            skipFoldingWhiteSpace();
            return parseAsciiDigits(1, 2);
        }

        /**
         * @return the java.util.Calendar constant for the parsed month name
         */
        int parseMonth() throws ParseException {
            parseFwsInMonth();
            int month = parseMonthName(isMonthNameCaseSensitive());
            parseFwsInMonth();
            return month;
        }

        void parseFwsInMonth() throws ParseException {
            parseFoldingWhiteSpace();
        }

        boolean isMonthNameCaseSensitive() {
            return true;
        }

        int parseYear() throws ParseException {
            int year = parseAsciiDigits(4, MAX_YEAR_DIGITS);
            if (year >= 1900) {
                return year;
            } else {
                pos.setIndex(pos.getIndex() - 4);
                while (text.charAt(pos.getIndex() - 1) == '0') {
                    pos.setIndex(pos.getIndex() - 1);
                }
                throw new ParseException("Invalid year", pos.getIndex());
            }
        }

        int parseHour() throws ParseException {
            return parseAsciiDigits(2);
        }

        int parseMinute() throws ParseException {
            return parseAsciiDigits(2);
        }

        int parseSecond() throws ParseException {
            return parseAsciiDigits(2);
        }

        void parseFwsBetweenTimeOfDayAndZone() throws ParseException {
            parseFoldingWhiteSpace();
        }

        int parseZone() throws ParseException {
            return parseZoneOffset();
        }

    }

    private class Rfc2822LenientParser extends Rfc2822StrictParser {

        private Boolean hasDefaultFws;

        Rfc2822LenientParser(String text, ParsePosition pos) {
            super(text, pos);
        }

        @Override
        int parseOptionalBegin() {
            while (pos.getIndex() < text.length() && !peekAsciiDigit()) {
                pos.setIndex(pos.getIndex() + 1);
            }

            return UNKNOWN_DAY_NAME;
        }

        @Override
        int parseDay() throws ParseException {
            skipFoldingWhiteSpace();
            return parseAsciiDigits(1, 3);
        }

        @Override
        void parseFwsInMonth() throws ParseException {
            // '-' is allowed to accomodate for the date format as specified in
            // <a href="http://www.ietf.org/rfc/rfc3501.txt">RFC 3501</a>
            if (hasDefaultFws == null) {
                hasDefaultFws = !skipChar('-');
                skipFoldingWhiteSpace();
            } else if (hasDefaultFws) {
                skipFoldingWhiteSpace();
            } else {
                parseChar('-');
            }
        }

        @Override
        boolean isMonthNameCaseSensitive() {
            return false;
        }

        @Override
        int parseYear() throws ParseException {
            int year = parseAsciiDigits(1, MAX_YEAR_DIGITS);
            if (year >= 1000) {
                return year;
            } else if (year >= 50) {
                return year + 1900;
            } else {
                return year + 2000;
            }
        }

        @Override
        int parseHour() throws ParseException {
            return parseAsciiDigits(1, 2);
        }

        @Override
        int parseMinute() throws ParseException {
            return parseAsciiDigits(1, 2);
        }

        @Override
        int parseSecond() throws ParseException {
            return parseAsciiDigits(1, 2);
        }

        @Override
        void parseFwsBetweenTimeOfDayAndZone() throws ParseException {
            skipFoldingWhiteSpace();
        }

        @Override
        int parseZone() throws ParseException {
            try {
                if (pos.getIndex() >= text.length()) {
                    throw new ParseException("Missing zone", pos.getIndex());
                }

                if (peekChar('+') || peekChar('-')) {
                    return parseZoneOffset();
                } else if (skipAlternativePair('U', 'u', 'T', 't')) {
                    return 0;
                } else if (skipAlternativeTriple('G', 'g', 'M', 'm',
                        'T', 't')) {
                    return 0;
                } else {
                    int hoursOffset;
                    if (skipAlternative('E', 'e')) {
                        hoursOffset = 4;
                    } else if (skipAlternative('C', 'c')) {
                        hoursOffset = 5;
                    } else if (skipAlternative('M', 'm')) {
                        hoursOffset = 6;
                    } else if (skipAlternative('P', 'p')) {
                        hoursOffset = 7;
                    } else {
                        throw new ParseException("Invalid zone",
                                pos.getIndex());
                    }
                    if (skipAlternativePair('S', 's', 'T', 't')) {
                        hoursOffset += 1;
                    } else if (skipAlternativePair('D', 'd', 'T', 't')) {
                    } else {
                        pos.setIndex(pos.getIndex() - 1);
                        throw new ParseException("Invalid zone",
                                pos.getIndex());
                    }
                    return hoursOffset * 60;
                }
            } catch (ParseException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "No timezone? : '" + text + "'", e);
                }

                return 0;
            }
        }

        @Override
        boolean isValidZoneOffset(int offset) {
            return true;
        }

        @Override
        boolean skipFoldingWhiteSpace() {
            boolean result = peekFoldingWhiteSpace();

            skipLoop:
            while (pos.getIndex() < text.length()) {
                switch (text.charAt(pos.getIndex())) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        pos.setIndex(pos.getIndex() + 1);
                        break;
                    default:
                        break skipLoop;
                }
            }

            return result;
        }

        @Override
        boolean peekFoldingWhiteSpace() {
            return super.peekFoldingWhiteSpace()
                    || (pos.getIndex() < text.length()
                    && text.charAt(pos.getIndex()) == '\n');
        }

    }

}
