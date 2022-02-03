package com.bugsnag.android.repackaged.dslplatform.json;

/**
 * Objects which implement this interface are supported for serialization in DslJson.
 * This is used by DSL Platform POJO objects.
 * Annotation processor uses a different method, since it can't modify existing objects to add such signature into them.
 *
 * Objects which implement JsonObject support convention based deserialization in form of public static JSON_READER
 * An example:
 *
 * <pre>
 *     public class MyCustomPojo implements JsonObject {
 *       public void serialize(JsonWriter writer, boolean minimal) {
 *         //implement serialization logic, eg: writer.writeAscii("{\"my\":\"object\"}");
 *       }
 *       public static final JsonReader.ReadJsonObject&lt;MyCustomPojo&gt; JSON_READER = new JsonReader.ReadJsonObject&lt;MyCustomPojo&gt;() {
 *         public MyCustomPojo deserialize(JsonReader reader) throws IOException {
 *           //implement deserialization logic, eg: return new MyCustomPojo();
 *         }
 *       }
 *     }
 * </pre>
 *
 */
public interface JsonObject {
	/**
	 * Serialize object instance into JsonWriter.
	 * In DslJson minimal serialization stands for serialization which omits unnecessary information from JSON.
	 * An example of such data is false for boolean or null for Integer which can be reconstructed from type definition.
	 *
	 * @param writer write JSON into target writer
	 * @param minimal is minimal serialization requested
	 */
	void serialize(JsonWriter writer, boolean minimal);
}
