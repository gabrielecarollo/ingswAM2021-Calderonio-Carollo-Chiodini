package it.polimi.ingsw.controller.gsonManager;

import com.google.gson.*;
import it.polimi.ingsw.model.Marble.Marble;

import java.lang.reflect.Type;

/**
 * this class is used to deserialize with gson the interface marble
 */
@SuppressWarnings("rawtypes")
public class MarbleInterfaceAdapter implements JsonSerializer<Marble>, JsonDeserializer<Marble> {

    /**
     * this is the name of the class
     */
    private static final String CLASSNAME = "Marble";

    /**
     * this is the attribute of the class
     */
    private static final String DATA = "MarbleColor";

    /**
     * this method deserialize the marble
     * @param jsonElement this is the json element
     * @param type this is the type
     * @param jsonDeserializationContext this is the jsonDeserializationContext
     * @return the marble deserialized
     * @throws JsonParseException when a json parser exception occurs
     */
    @SuppressWarnings("rawtypes")
    public Marble deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();
        Class klass = getObjectClass(className);
        return jsonDeserializationContext.deserialize(jsonObject.get(DATA), klass);
    }

    /**
     * this method serialize the marble
     * @param jsonElement this is the marble to serialize
     * @param type this is the type
     * @param jsonSerializationContext this is the jsonSerializationContext
     * @return the json element associated with the marble
     */
    public JsonElement serialize(Marble jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
        jsonObject.add(DATA, jsonSerializationContext.serialize(jsonElement));
        return jsonObject;
    }

    /**
     * this method is a helper method to get the class name
     * @param className this is the string representing the name of the class
     * @return the class with the name specified in input
     */
    @SuppressWarnings("rawtypes")
    public Class getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            throw new JsonParseException(e.getMessage());
        }
    }
}