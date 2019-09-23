package com.badlogic.gdx.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by longri on 13.05.17.
 */
public class GdxJsonReader extends JsonReaderCpy {


    public JsonValue parse(InputStream input, long length) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return parse(reader);
    }

    @Override
    public void startArray(String name) {
        super.startArray(name);
    }

    @Override
    public void startObject(String name) {
        super.startObject(name);
    }

    @Override
    public void pop() {
        super.pop();
    }

    @Override
    public void string(String name, String value) {
        super.string(name, value);
    }

    @Override
    public void number(String name, double value, String stringValue) {
        super.number(name, value, stringValue);
    }

    @Override
    public void number(String name, long value, String stringValue) {
        super.number(name, value, stringValue);
    }

    @Override
    public void bool(String name, boolean value) {
        super.bool(name, value);
    }
}
