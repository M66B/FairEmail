package biweekly.io.scribe.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalVersion;
import biweekly.component.VFreeBusy;
import biweekly.property.FreeBusy;
import biweekly.property.ICalProperty;
import biweekly.util.Period;

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
 * @author Michael Angstadt
 */
public class VFreeBusyScribe extends ICalComponentScribe<VFreeBusy> {
	public VFreeBusyScribe() {
		super(VFreeBusy.class, "VFREEBUSY");
	}

	@Override
	public List<ICalProperty> getProperties(VFreeBusy component) {
		List<ICalProperty> properties = super.getProperties(component);

		List<FreeBusy> fb = new ArrayList<FreeBusy>(component.getFreeBusy());
		if (fb.isEmpty()) {
			return properties;
		}

		//sort FREEBUSY properties by start date (p.100)
		Collections.sort(fb, new Comparator<FreeBusy>() {
			public int compare(FreeBusy one, FreeBusy two) {
				Date oneStart = getEarliestStartDate(one);
				Date twoStart = getEarliestStartDate(two);
				if (oneStart == null && twoStart == null) {
					return 0;
				}
				if (oneStart == null) {
					return 1;
				}
				if (twoStart == null) {
					return -1;
				}
				return oneStart.compareTo(twoStart);
			}

			private Date getEarliestStartDate(FreeBusy fb) {
				Date date = null;
				for (Period tp : fb.getValues()) {
					if (tp.getStartDate() == null) {
						continue;
					}
					if (date == null || date.compareTo(tp.getStartDate()) > 0) {
						date = tp.getStartDate();
					}
				}
				return date;
			}
		});

		//find index of first FREEBUSY instance
		int index = 0;
		for (ICalProperty prop : properties) {
			if (prop instanceof FreeBusy) {
				break;
			}
			index++;
		}

		//remove and re-add the FREEBUSY instances in sorted order
		properties = new ArrayList<ICalProperty>(properties);
		for (FreeBusy f : fb) {
			properties.remove(f);
			properties.add(index++, f);
		}

		return properties;
	}

	@Override
	protected VFreeBusy _newInstance() {
		return new VFreeBusy();
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}
}
