package de.longri.cachebox3.utils.http;

public class WebbException extends RuntimeException {

    private Response response;

    public WebbException(String message) {
        super(message);
    }

    public WebbException(String message, Response response) {
        super(message);
        this.response = response;
    }

    public WebbException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebbException(Throwable cause) {
        super(cause);
    }

    /**
     * Get the Response object
     * (only available if exception has been raised by #ensureSuccess().
     *
     * @return the <code>Response</code> object filled with error information like statusCode and errorBody.
     */
    public Response getResponse() {
        return response;
    }
}
