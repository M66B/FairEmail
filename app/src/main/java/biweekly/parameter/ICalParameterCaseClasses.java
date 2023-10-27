package biweekly.parameter;

import java.lang.reflect.Constructor;

import biweekly.ICalVersion;
import biweekly.util.CaseClasses;

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
 * Manages the list of pre-defined values for a parameter (such as VALUE or
 * ENCODING).
 * @author Michael Angstadt
 * @param <T> the parameter class
 */
public class ICalParameterCaseClasses<T extends EnumParameterValue> extends CaseClasses<T, String> {
	public ICalParameterCaseClasses(Class<T> clazz) {
		super(clazz);
	}

	@Override
	protected T create(String value) {
		//reflection: return new ClassName(value);
		try {
			//try (String) constructor
			Constructor<T> constructor = clazz.getDeclaredConstructor(String.class);
			constructor.setAccessible(true);
			return constructor.newInstance(value);
		} catch (Exception e) {
			try {
				//try (String, ICalVersion...) constructor
				Constructor<T> constructor = clazz.getDeclaredConstructor(String.class, ICalVersion[].class);
				constructor.setAccessible(true);
				return constructor.newInstance(value, new ICalVersion[] {});
			} catch (Exception e2) {
				throw new RuntimeException(e2);
			}
		}
	}

	@Override
	protected boolean matches(T object, String value) {
		return object.value.equalsIgnoreCase(value);
	}
}
