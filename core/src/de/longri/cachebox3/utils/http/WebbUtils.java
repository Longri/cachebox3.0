package de.longri.cachebox3.utils.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


public class WebbUtils {
    protected WebbUtils() {}
    /**
     * Convert a Map to a query string.
     * @param values the map with the values
     *               <code>null</code> will be encoded as empty string, all other objects are converted to
     *               String by calling its <code>toString()</code> method.
     * @return e.g. "key1=value&amp;key2=&amp;email=max%40example.com"
     */
    public static String queryString(Map<String, Object> values) {
        StringBuilder sbuf = new StringBuilder();
        String separator = "";

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue instanceof Object[]) {
                for (Object value : (Object[]) entryValue) {
                    appendParam(sbuf, separator, entry.getKey(), value);
                    separator = "&";
                }
            } else if (entryValue instanceof Iterable) {
                for (Object multiValue : (Iterable) entryValue) {
                    appendParam(sbuf, separator, entry.getKey(), multiValue);
                    separator = "&";
                }
            } else {
                appendParam(sbuf, separator, entry.getKey(), entryValue);
                separator = "&";
            }
        }

        return sbuf.toString();
    }

    private static void appendParam(StringBuilder sbuf, String separator, String entryKey, Object value) {
        String sValue = value == null ? "" : String.valueOf(value);
        sbuf.append(separator);
        sbuf.append(urlEncode(entryKey));
        sbuf.append('=');
        sbuf.append(urlEncode(sValue));
    }

    static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    static String getPayloadAsBytesAndSetContentType(
            Connection connection,
            Request request,
            int jsonIndentFactor) throws JSONException {

        String bodyStr = null;

        if (request.params != null) {
            connection.ensureRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_FORM);
            bodyStr = WebbUtils.queryString(request.params);
        } else if (request.payload == null) {
            return null;
        } else if (request.payload instanceof JSONObject) {
            connection.ensureRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_JSON);
            bodyStr = jsonIndentFactor >= 0
                    ? ((JSONObject) request.payload).toString(jsonIndentFactor)
                    : request.payload.toString();
        } else if (request.payload instanceof JSONArray) {
            connection.ensureRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_JSON);
            bodyStr = jsonIndentFactor >= 0
                    ? ((JSONArray) request.payload).toString(jsonIndentFactor)
                    : request.payload.toString();
        }

        return bodyStr;
    }

    static InputStream wrapStream(String contentEncoding, InputStream inputStream) throws IOException {
        if (contentEncoding == null || "identity".equalsIgnoreCase(contentEncoding)) {
            return inputStream;
        }
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            return new GZIPInputStream(inputStream);
        }
        if ("deflate".equalsIgnoreCase(contentEncoding)) {
            return new InflaterInputStream(inputStream, new Inflater(false), 512);
        }
        throw new WebbException("unsupported content-encoding: " + contentEncoding);
    }

    static <T> void parseResponseBody(Class<T> clazz, Response<T> response, InputStream responseBodyStream)
            throws IOException {

        if (responseBodyStream == null || clazz == Void.class) {
            return;
        } else if (clazz == InputStream.class) {
            response.setBody(responseBodyStream);
            return;
        }

        byte[] responseBody = WebbUtils.readBytes(responseBodyStream);
        // we are ignoring headers describing the content type of the response, instead
        // try to force the content based on the type the client is expecting it (clazz)
        if (clazz == String.class) {
            response.setBody(new String(responseBody, StandardCharsets.UTF_8));
        } else if (clazz == Const.BYTE_ARRAY_CLASS) {
            response.setBody(responseBody);
        } else if (clazz == JSONObject.class) {
            response.setBody(WebbUtils.toJsonObject(responseBody));
        } else if (clazz == org.json.JSONArray.class) {
            response.setBody(WebbUtils.toJsonArray(responseBody));
        }
    }

    /**
     * Read an <code>InputStream</code> into <code>byte[]</code> until EOF.
     * <br>
     * Does not close the InputStream!
     *
     * @param is the stream to read the bytes from
     * @return all read bytes as an array
     * @throws IOException when read or write operation fails
     */
    public static byte[] readBytes(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(is, baos);
        return baos.toByteArray();
    }

    /**
     * Copy complete content of <code>InputStream</code> to <code>OutputStream</code> until EOF.
     * <br>
     * Does not close the InputStream nor OutputStream!
     *
     * @param input the stream to read the bytes from
     * @param output the stream to write the bytes to
     * @throws IOException when read or write operation fails
     */
    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
    }

    /**
     * Convert a byte array to a JSONObject.
     * @param bytes a UTF-8 encoded string representing a JSON object.
     * @return the parsed object
     * @throws WebbException in case of error (usually a parsing error due to invalid JSON)
     */
    public static JSONObject toJsonObject(byte[] bytes) {
        String json;
        try {
            json = new String(bytes, StandardCharsets.UTF_8);
            return new JSONObject(json);
        } catch (JSONException e) {
            throw new WebbException("payload is not a valid JSON object", e);
        }
    }

    /**
     * Convert a byte array to a JSONArray.
     * @param bytes a UTF-8 encoded string representing a JSON array.
     * @return the parsed JSON array
     * @throws WebbException in case of error (usually a parsing error due to invalid JSON)
     */
    public static JSONArray toJsonArray(byte[] bytes) {
        String json;
        try {
            json = new String(bytes, StandardCharsets.UTF_8);
            return new JSONArray(json);
        } catch (JSONException e) {
            throw new WebbException("payload is not a valid JSON array", e);
        }
    }

    public abstract static class StreamHandleObject {
        public InputStream stream;

        public abstract void handled();
    }

}
