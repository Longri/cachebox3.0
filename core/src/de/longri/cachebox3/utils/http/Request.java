package de.longri.cachebox3.utils.http;

import de.longri.cachebox3.utils.ICancel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Request {
    public enum Method {
        GET, POST, PUT, DELETE
    }

    final Method method;
    final String uri;
    private final Webb webb;
    Boolean followRedirects;
    Integer timeout;
    Map<String, Object> headers;
    Object payload;
    Map<String, Object> params;
    boolean ensureSuccess;
    ICancel icancel;

    Request(Webb webb, Method method, String uri) {
        this.webb = webb;
        this.method = method;
        this.uri = uri;
        this.followRedirects = webb.followRedirects;
    }

    public Request readTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Request param(String name, Object value) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(name, value);
        return this;
    }

    /**
     * Set (or overwrite) a parameter with multiple values.
     * <br>
     * The parameter will be used to create a query string for GET-requests and as the body for POST-requests
     * with MIME-type <code>application/x-www-form-urlencoded</code>.
     * <br>
     *
     * @param name the name of the parameter (it's better to use only contain ASCII characters)
     * @param values the values of the parameter; will be expanded to multiple valued parameters.
     * @return <code>this</code> for method chaining (fluent API)
     * @since 1.3.0
     */
    public Request param(String name, Iterable<Object> values) {
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        params.put(name, values);
        return this;
    }

    /**
     * Set the payload for the request.
     * <br>
     * Using this method together with {#param(String, Object)} has the effect of <code>body</code> being
     * ignored without notice. The method can be called more than once: the value will be stored and converted
     * to bytes later.
     * <br>
     * Following types are supported for the body:
     * <ul>
     *     <li>
     *         <code>null</code> clears the body
     *     </li>
     *     <li>
     *         {@link org.json.JSONObject}, HTTP header 'Content-Type' will be set to JSON, if not set
     *     </li>
     *     <li>
     *         {@link org.json.JSONArray}, HTTP header 'Content-Type' will be set to JSON, if not set
     *     </li>
     *     <li>
     *         {java.lang.String }, HTTP header 'Content-Type' will be set to TEXT, if not set;
     *         Text will be converted to UTF-8 bytes.
     *     </li>
     *     <li>
     *         <code>byte[]</code> the easiest way for DavidWebb - it's just passed through.
     *         HTTP header 'Content-Type' will be set to BINARY, if not set.
     *     </li>
     *     <li>
     *         {@link java.io.File}, HTTP header 'Content-Type' will be set to BINARY, if not set;
     *         The file gets streamed to the web-server and 'Content-Length' will be set to the number
     *         of bytes of the file. There is absolutely no conversion done. So if you want to upload
     *         e.g. a text-file and convert it to another encoding than stored on disk, you have to do
     *         it by yourself.
     *     </li>
     *     <li>
     *         {@link java.io.InputStream}, HTTP header 'Content-Type' will be set to BINARY, if not set;
     *         Similar to <code>File</code>. Content-Length cannot be set (which has some drawbacks compared
     *         to knowing the size of the body in advance).<br>
     *         <strong>You have to care for closing the stream!</strong>
     *     </li>
     * </ul>
     *
     * @param body the payload
     * @return <code>this</code> for method chaining (fluent API)
     */
    public Request body(Object body) {
        if (method == Method.GET || method == Method.DELETE) {
            throw new IllegalStateException("body not allowed for request method " + method);
        }
        this.payload = body;
        return this;
    }

    /**
     * By calling this method, the HTTP status code is checked and a <code>WebbException</code> is thrown if
     * the status code is not something like 2xx.<br>
     * <br>
     * Be careful! If you request resources e.g. with { #ifModifiedSince(long) }, an exception will also be
     * thrown in the positive case of <code>304 Not Modified</code>.
     *
     * @return <code>this</code> for method chaining (fluent API)
     */
    public Request ensureSuccess() {
        this.ensureSuccess = true;
        return this;
    }

    /**
     * Execute the request and expect the result to be convertible to <code>String</code>.
     * @return the created <code>Response</code> object carrying the payload from the server as <code>String</code>
     */
    public Response<String> asString() {
        return webb.execute(this, String.class);
    }

    /**
     * Execute the request and expect the result to be convertible to <code>JSONObject</code>.
     * @return the created <code>Response</code> object carrying the payload from the server as <code>JSONObject</code>
     */
    public Response<JSONObject> asJsonObject() {
        return webb.execute(this, JSONObject.class);
    }

    /**
     * Execute the request and expect the result to be convertible to <code>JSONArray</code>.
     * @return the created <code>Response</code> object carrying the payload from the server as <code>JSONArray</code>
     */
    public Response<JSONArray> asJsonArray() {
        return webb.execute(this, JSONArray.class);
    }

    /**
     * Execute the request and expect the result to be convertible to <code>byte[]</code>.
     * @return the created <code>Response</code> object carrying the payload from the server as <code>byte[]</code>
     */
    public Response<byte[]> asBytes() {
        return (Response<byte[]>) webb.execute(this, Const.BYTE_ARRAY_CLASS);
    }

    /**
     * Execute the request and expect the result to be convertible to <code>InputStream</code>.
     * @return the created <code>Response</code> object carrying the payload from the server as <code>InputStream</code>
     */
    public Response<InputStream> asStream() {
        return webb.execute(this, InputStream.class);
    }

    /**
     * Execute the request and expect no result payload (only status-code and headers).
     * @return the created <code>Response</code> object where no payload is expected or simply will be ignored.
     */
    public Response<Void> asVoid() {
        return webb.execute(this, Void.class);
    }

}
