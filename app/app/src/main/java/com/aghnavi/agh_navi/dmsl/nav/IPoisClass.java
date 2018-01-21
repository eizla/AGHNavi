package com.aghnavi.agh_navi.dmsl.nav;


import java.io.Serializable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public interface IPoisClass extends Serializable {

    enum Type {
        AnyPlacePOI, GooglePlace;
    }

    public final class MyInterfaceAdapter implements JsonDeserializer<IPoisClass>, JsonSerializer<IPoisClass> {
        private static final String PROP_NAME = "myClass";

        @Override
        public JsonElement serialize(IPoisClass src, java.lang.reflect.Type arg1, JsonSerializationContext context) {
            // note : won't work, you must delegate this
            JsonObject jo = context.serialize(src).getAsJsonObject();

            String classPath = src.getClass().getName();
            jo.add(PROP_NAME, new JsonPrimitive(classPath));

            return jo;
        }

        @Override
        public IPoisClass deserialize(JsonElement json, java.lang.reflect.Type arg1, JsonDeserializationContext context) throws JsonParseException {
            try {
                String classPath = json.getAsJsonObject().getAsJsonPrimitive(PROP_NAME).getAsString();
                Class<IPoisClass> cls = (Class<IPoisClass>) Class.forName(classPath);

                return (IPoisClass) context.deserialize(json, cls);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    String id();

    double lat();

    double lng();

    String name();

    String description();

    Type type();

    boolean source();
}

