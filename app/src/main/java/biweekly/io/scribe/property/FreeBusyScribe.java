package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.FreeBusy;
import biweekly.util.Duration;
import biweekly.util.ICalDate;
import biweekly.util.Period;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

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
 * Marshals {@link FreeBusy} properties.
 * @author Michael Angstadt
 */
public class FreeBusyScribe extends ICalPropertyScribe<FreeBusy> {
	public FreeBusyScribe() {
		super(FreeBusy.class, "FREEBUSY");
	}

	@Override
	protected ICalDataType _defaultDataType(ICalVersion version) {
		return ICalDataType.PERIOD;
	}

	@Override
	protected String _writeText(final FreeBusy property, final WriteContext context) {
		List<Period> values = property.getValues();
		List<String> strValues = new ArrayList<String>(values.size());
		for (Period period : values) {
			StringBuilder sb = new StringBuilder();

			Date start = period.getStartDate();
			if (start != null) {
				String dateStr = date(start, property, context).extended(false).write();
				sb.append(dateStr);
			}

			sb.append('/');

			Date end = period.getEndDate();
			if (end != null) {
				String dateStr = date(end, property, context).extended(false).write();
				sb.append(dateStr);
			} else if (period.getDuration() != null) {
				sb.append(period.getDuration());
			}

			strValues.add(sb.toString());
		}

		return VObjectPropertyValues.writeList(strValues);
	}

	@Override
	protected FreeBusy _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(VObjectPropertyValues.parseList(value), parameters, context);
	}

	@Override
	protected void _writeXml(FreeBusy property, XCalElement element, WriteContext context) {
		for (Period period : property.getValues()) {
			XCalElement periodElement = element.append(ICalDataType.PERIOD);

			Date start = period.getStartDate();
			if (start != null) {
				String dateStr = date(start, property, context).extended(true).write();
				periodElement.append("start", dateStr);
			}

			Date end = period.getEndDate();
			if (end != null) {
				String dateStr = date(end, property, context).extended(true).write();
				periodElement.append("end", dateStr);
			}

			Duration duration = period.getDuration();
			if (duration != null) {
				periodElement.append("duration", duration.toString());
			}
		}
	}

	@Override
	protected FreeBusy _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		List<XCalElement> periodElements = element.children(ICalDataType.PERIOD);
		if (periodElements.isEmpty()) {
			throw missingXmlElements(ICalDataType.PERIOD);
		}

		FreeBusy property = new FreeBusy();
		for (XCalElement periodElement : periodElements) {
			String startStr = periodElement.first("start");
			if (startStr == null) {
				throw new CannotParseException(9);
			}

			ICalDate start;
			try {
				start = date(startStr).parse();
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(10, startStr);
			}

			String endStr = periodElement.first("end");
			if (endStr != null) {
				try {
					ICalDate end = date(endStr).parse();
					property.getValues().add(new Period(start, end));
					context.addDate(start, property, parameters);
					context.addDate(end, property, parameters);
				} catch (IllegalArgumentException e) {
					throw new CannotParseException(11, endStr);
				}
				continue;
			}

			String durationStr = periodElement.first("duration");
			if (durationStr != null) {
				try {
					Duration duration = Duration.parse(durationStr);
					property.getValues().add(new Period(start, duration));
					context.addDate(start, property, parameters);
				} catch (IllegalArgumentException e) {
					throw new CannotParseException(12, durationStr);
				}
				continue;
			}

			throw new CannotParseException(13);
		}

		return property;
	}

	@Override
	protected JCalValue _writeJson(FreeBusy property, WriteContext context) {
		List<Period> values = property.getValues();
		if (values.isEmpty()) {
			return JCalValue.single("");
		}

		List<String> valuesStr = new ArrayList<String>();
		for (Period period : values) {
			StringBuilder sb = new StringBuilder();
			Date start = period.getStartDate();
			if (start != null) {
				String dateStr = date(start, property, context).extended(true).write();
				sb.append(dateStr);
			}

			sb.append('/');

			Date end = period.getEndDate();
			if (end != null) {
				String dateStr = date(end, property, context).extended(true).write();
				sb.append(dateStr);
			} else if (period.getDuration() != null) {
				sb.append(period.getDuration());
			}

			valuesStr.add(sb.toString());
		}

		return JCalValue.multi(valuesStr);
	}

	@Override
	protected FreeBusy _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(value.asMulti(), parameters, context);
	}

	private FreeBusy parse(List<String> periods, ICalParameters parameters, ParseContext context) {
		FreeBusy property = new FreeBusy();

		for (String period : periods) {
			int slash = period.indexOf('/');
			if (slash < 0) {
				throw new CannotParseException(13);
			}

			String startStr = period.substring(0, slash);
			ICalDate start;
			try {
				start = date(startStr).parse();
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(10, startStr);
			}

			String endStr = period.substring(slash + 1);
			ICalDate end;
			try {
				end = date(endStr).parse();
				property.getValues().add(new Period(start, end));
				context.addDate(start, property, parameters);
				context.addDate(end, property, parameters);
			} catch (IllegalArgumentException e) {
				//must be a duration
				try {
					Duration duration = Duration.parse(endStr);
					property.getValues().add(new Period(start, duration));
					context.addDate(start, property, parameters);
				} catch (IllegalArgumentException e2) {
					throw new CannotParseException(14, endStr);
				}
			}
		}

		return property;
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}
}
