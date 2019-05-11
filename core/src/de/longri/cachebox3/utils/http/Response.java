package de.longri.cachebox3.utils.http;

public class Response<T> {
    final Request request;
    int statusCode;
    String responseMessage;
    T body;
    Object errorBody;

    Response(Request request) {
        this.request = request;
    }

    public void ensureSuccess() {
        if (!isSuccess()) {
            throw new WebbException("Request failed: " + statusCode + " " + responseMessage, this);
        }
    }

    public boolean isSuccess() {
        return (statusCode / 100) == 2; // 200, 201, 204, ...
    }

    /**
     * Returns the payload of the response converted to the given type.
     *
     * @return the converted payload (can be null).
     */
    public T getBody() {
        return body;
    }

    void setBody(Object body) {
        this.body = (T) body;
    }

    /**
     * Get the body which was returned in case of error (HTTP-Code &gt;= 400).
     * <br>
     * The type of the error body depends on following factors:
     * <ul>
     * <li>
     * <code>Content-Type</code> header (overrules the expected return type of the response)
     * </li>
     * <li>
     * The expected type (see <code>asXyz()</code>). We try to coerce the error body to this type.
     * In case of REST services, where often a JSONObject is the normal response body, the error body
     * will be converted to JSONObject if possible. <code>JSONArray</code> is not expected to be the
     * error body.
     * </li>
     * </ul>
     * If converting the error body is not successful, <code>String</code> and <code>byte[]</code> is used as
     * a fallback. You have to check the type with <code>instanceof</code> or try/catch the cast.
     *
     * @return the error body converted to an object (see above) or <code>null</code> if there is no body or
     * no error.
     */
    public Object getErrorBody() {
        return errorBody;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
