package biweekly.io.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * Holds the value of a jCal property.
 * @author Michael Angstadt
 */
public class JCalValue {
	private final List<JsonValue> values;

	/**
	 * Creates a new jCal value.
	 * @param values the values
	 */
	public JCalValue(List<JsonValue> values) {
		this.values = Collections.unmodifiableList(values);
	}

	/**
	 * Creates a new jCal value.
	 * @param values the values
	 */
	public JCalValue(JsonValue... values) {
		this.values = Arrays.asList(values); //unmodifiable
	}

	/**
	 * Creates a single-valued value.
	 * @param value the value
	 * @return the jCal value
	 */
	public static JCalValue single(Object value) {
		return new JCalValue(new JsonValue(value));
	}

	/**
	 * Creates a multi-valued value.
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue multi(Object... values) {
		return multi(Arrays.asList(values));
	}

	/**
	 * Creates a multi-valued value.
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue multi(List<?> values) {
		List<JsonValue> multiValues = new ArrayList<JsonValue>(values.size());
		for (Object value : values) {
			multiValues.add(new JsonValue(value));
		}
		return new JCalValue(multiValues);
	}

	/**
	 * <p>
	 * Creates a structured value.
	 * </p>
	 * <p>
	 * This method accepts a vararg of {@link Object} instances. {@link List}
	 * objects will be treated as multi-valued components. All other objects.
	 * Null values will be treated as empty components.
	 * </p>
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue structured(Object... values) {
		List<List<?>> valuesList = new ArrayList<List<?>>(values.length);
		for (Object value : values) {
			List<?> list = (value instanceof List) ? (List<?>) value : Collections.singletonList(value);
			valuesList.add(list);
		}
		return structured(valuesList);
	}

	/**
	 * Creates a structured value.
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue structured(List<List<?>> values) {
		List<JsonValue> array = new ArrayList<JsonValue>(values.size());

		for (List<?> list : values) {
			if (list.isEmpty()) {
				array.add(new JsonValue(""));
				continue;
			}

			if (list.size() == 1) {
				Object value = list.get(0);
				if (value == null) {
					value = "";
				}
				array.add(new JsonValue(value));
				continue;
			}

			List<JsonValue> subArray = new ArrayList<JsonValue>(list.size());
			for (Object value : list) {
				if (value == null) {
					value = "";
				}
				subArray.add(new JsonValue(value));
			}
			array.add(new JsonValue(subArray));
		}

		return new JCalValue(new JsonValue(array));
	}

	/**
	 * Creates an object value.
	 * @param value the object
	 * @return the jCal value
	 */
	public static JCalValue object(ListMultimap<String, Object> value) {
		Map<String, JsonValue> object = new LinkedHashMap<String, JsonValue>();
		for (Map.Entry<String, List<Object>> entry : value) {
			String key = entry.getKey();
			List<Object> list = entry.getValue();

			JsonValue v;
			if (list.size() == 1) {
				v = new JsonValue(list.get(0));
			} else {
				List<JsonValue> array = new ArrayList<JsonValue>(list.size());
				for (Object element : list) {
					array.add(new JsonValue(element));
				}
				v = new JsonValue(array);
			}
			object.put(key, v);
		}
		return new JCalValue(new JsonValue(object));
	}

	/**
	 * Gets the raw JSON values. Use one of the "{@code as*}" methods to parse
	 * the values as one of the standard jCal values.
	 * @return the JSON values
	 */
	public List<JsonValue> getValues() {
		return values;
	}

	/**
	 * Parses this jCal value as a single-valued property value.
	 * @return the value or empty string if not found
	 */
	public String asSingle() {
		if (values.isEmpty()) {
			return "";
		}

		JsonValue first = values.get(0);
		if (first.isNull()) {
			return "";
		}

		Object obj = first.getValue();
		if (obj != null) {
			return obj.toString();
		}

		//get the first element of the array
		List<JsonValue> array = first.getArray();
		if (array != null && !array.isEmpty()) {
			obj = array.get(0).getValue();
			if (obj != null) {
				return obj.toString();
			}
		}

		return "";
	}

	/**
	 * Parses this jCal value as a structured property value.
	 * @return the structured values or empty list if not found
	 */
	public List<List<String>> asStructured() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		JsonValue first = values.get(0);

		//["request-status", {}, "text", ["2.0", "Success"] ]
		List<JsonValue> array = first.getArray();
		if (array != null) {
			List<List<String>> components = new ArrayList<List<String>>(array.size());
			for (JsonValue value : array) {
				if (value.isNull()) {
					components.add(Collections.<String>emptyList());
					continue;
				}

				Object obj = value.getValue();
				if (obj != null) {
					String s = obj.toString();
					List<String> component = s.isEmpty() ? Collections.<String>emptyList() : Collections.singletonList(s);
					components.add(component);
					continue;
				}

				List<JsonValue> subArray = value.getArray();
				if (subArray != null) {
					List<String> component = new ArrayList<String>(subArray.size());
					for (JsonValue subArrayValue : subArray) {
						if (subArrayValue.isNull()) {
							component.add("");
							continue;
						}

						obj = subArrayValue.getValue();
						if (obj != null) {
							component.add(obj.toString());
							continue;
						}
					}
					if (component.size() == 1 && component.get(0).isEmpty()) {
						component.clear();
					}
					components.add(component);
				}
			}
			return components;
		}

		//get the first value if it's not enclosed in an array
		//["request-status", {}, "text", "2.0"]
		Object obj = first.getValue();
		if (obj != null) {
			List<List<String>> components = new ArrayList<List<String>>(1);
			String s = obj.toString();
			List<String> component = s.isEmpty() ? Collections.<String>emptyList() : Collections.singletonList(s);
			components.add(component);
			return components;
		}

		//["request-status", {}, "text", null]
		if (first.isNull()) {
			List<List<String>> components = new ArrayList<List<String>>(1);
			components.add(Collections.<String>emptyList());
			return components;
		}

		return Collections.emptyList();
	}

	/**
	 * Parses this jCal value as a multi-valued property value.
	 * @return the values or empty list if not found
	 */
	public List<String> asMulti() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> multi = new ArrayList<String>(values.size());
		for (JsonValue value : values) {
			if (value.isNull()) {
				multi.add("");
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				multi.add(obj.toString());
				continue;
			}
		}
		return multi;
	}

	/**
	 * Parses this jCal value as an object property value.
	 * @return the object or an empty map if not found
	 */
	public ListMultimap<String, String> asObject() {
		if (values.isEmpty()) {
			return new ListMultimap<String, String>(0);
		}

		Map<String, JsonValue> map = values.get(0).getObject();
		if (map == null) {
			return new ListMultimap<String, String>(0);
		}

		ListMultimap<String, String> values = new ListMultimap<String, String>();
		for (Map.Entry<String, JsonValue> entry : map.entrySet()) {
			String key = entry.getKey();
			JsonValue value = entry.getValue();

			if (value.isNull()) {
				values.put(key, "");
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				values.put(key, obj.toString());
				continue;
			}

			List<JsonValue> array = value.getArray();
			if (array != null) {
				for (JsonValue element : array) {
					if (element.isNull()) {
						values.put(key, "");
						continue;
					}

					obj = element.getValue();
					if (obj != null) {
						values.put(key, obj.toString());
					}
				}
			}
		}
		return values;
	}
}
