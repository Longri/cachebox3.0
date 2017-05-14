package com.badlogic.gdx.utils;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by longri on 13.05.17.
 */
public class GdxJsonReader extends JsonReader implements JsonParser {

    @Override
    public JsonValue parse(InputStream input, long length) {
        InputStreamReader reader = new InputStreamReader(input);
        return parse(reader);
    }

}
