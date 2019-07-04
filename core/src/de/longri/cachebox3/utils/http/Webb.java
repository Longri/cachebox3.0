package de.longri.cachebox3.utils.http;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class Webb {
    public static final String DEFAULT_USER_AGENT = Const.DEFAULT_USER_AGENT;
    public static final String APP_FORM = Const.APP_FORM;
    public static final String APP_JSON = Const.APP_JSON;
    public static final String APP_BINARY = Const.APP_BINARY;
    public static final String TEXT_PLAIN = Const.TEXT_PLAIN;
    public static final String HDR_CONTENT_TYPE = Const.HDR_CONTENT_TYPE;
    public static final String HDR_CONTENT_ENCODING = Const.HDR_CONTENT_ENCODING;
    public static final String HDR_ACCEPT = Const.HDR_ACCEPT;
    public static final String HDR_ACCEPT_ENCODING = Const.HDR_ACCEPT_ENCODING;
    public static final String HDR_USER_AGENT = Const.HDR_USER_AGENT;
    public static final String HDR_AUTHORIZATION = "Authorization";
    static final Map<String, Object> globalHeaders = new LinkedHashMap<String, Object>();
    private static final Logger log = LoggerFactory.getLogger(Webb.class);
    static String globalBaseUri;

    static Integer timeout = 3 * 60000; // 3 minutes, must be object Integer, cause set to null
    static int jsonIndentFactor = -1;

    Boolean followRedirects;
    String baseUri;
    Map<String, Object> defaultHeaders;

    protected Webb() {
        setDefaultHeader("User-Agent", DEFAULT_USER_AGENT);
        setDefaultHeader(HDR_ACCEPT_ENCODING, "gzip");
    }

    /**
     * Create an instance which can be reused for multiple requests in the same Thread.
     *
     * @return the created instance.
     */
    public static Webb create() {
        return new Webb();
    }

    public static void setGlobalBaseUri(String globalBaseUri) {
        Webb.globalBaseUri = globalBaseUri;
    }

    public static void setTimeout(int globalTimeout) {
        timeout = globalTimeout > 0 ? globalTimeout : null;
    }

    public Request get(String pathOrUri) {
        return new Request(this, Request.Method.GET, buildPath(pathOrUri));
    }

    public Request post(String pathOrUri) {
        return new Request(this, Request.Method.POST, buildPath(pathOrUri));
    }

    public Request put(String pathOrUri) {
        return new Request(this, Request.Method.PUT, buildPath(pathOrUri));
    }

    public Request delete(String pathOrUri) {
        return new Request(this, Request.Method.DELETE, buildPath(pathOrUri));
    }

    <T> Response<T> execute(Request request, Class<T> clazz) {
        Response<T> response;

        response = _execute(request, clazz);

        if (response == null) {
            throw new IllegalStateException(); // should never reach this line
        }
        if (request.ensureSuccess) {
            response.ensureSuccess();
        }

        return response;
    }

    private <T> Response<T> _execute(Request request, Class<T> clazz) {
        Response<T> response = new Response<>(request);
        Connection connection = new Connection(request.method);

        InputStream is = null;
        boolean closeStream = true;

        try {
            String uri = request.uri;
            if (request.method == Request.Method.GET &&
                    !uri.contains("?") &&
                    request.params != null &&
                    !request.params.isEmpty()) {
                uri += "?" + WebbUtils.queryString(request.params);
            }
            log.debug("url " + URLDecoder.decode(uri, "UTF-8"));
            connection.setUrl(uri);
            if (request.followRedirects != null) {
                connection.setFollowRedirects(request.followRedirects.booleanValue());
            }
            connection.setTimeOut(request.timeout != null ? request.timeout : timeout);

            connection.addRequestProperties(mergeHeaders(request.headers));
            if (clazz == JSONObject.class || clazz == JSONArray.class) {
                connection.ensureRequestProperty(HDR_ACCEPT, APP_JSON);
            }

            if (request.method == Request.Method.POST || request.method == Request.Method.PUT) {
                String requestBody = WebbUtils.getPayloadAsBytesAndSetContentType(connection, request, jsonIndentFactor);

                if (requestBody != null) {
                    connection.setContent(requestBody);
                }
            }
            connection.connect(request.icancel);

            response.statusCode = connection.statusCode;
            response.responseMessage = connection.getResponseMessage();

            // get the response body (if any)
            is = response.isSuccess() ? connection.getResultAsStream() : connection.getResultAsStream();
            is = WebbUtils.wrapStream(connection.getContentEncoding(), is);
            if (clazz == InputStream.class) {
                is = new AutoDisconnectInputStream(connection, is);
                closeStream = false;
            }

            if (response.isSuccess()) {
                WebbUtils.parseResponseBody(clazz, response, is);
            } else {
                connection.parseErrorResponse(clazz, response, is);
            }
            return response;

        } catch (WebbException e) {

            throw e;

        } catch (Exception e) {

            throw new WebbException(e);

        } finally {
            if (closeStream) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ignored) {
                    }
                }
                connection.waitForStreamHandled.set(false);
                if (connection.httpRequest != null) {
                    try {
                        connection.httpRequest.reset();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public void setFollowRedirects(boolean auto) {
        followRedirects = auto;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setDefaultHeader(String name, Object value) {
        if (defaultHeaders == null) {
            defaultHeaders = new HashMap<>();
        }
        if (value == null) {
            defaultHeaders.remove(name);
        } else {
            defaultHeaders.put(name, value);
        }
    }

    private String buildPath(String pathOrUri) {
        if (pathOrUri == null) {
            throw new IllegalArgumentException("pathOrUri must not be null");
        }
        if (pathOrUri.startsWith("http://") || pathOrUri.startsWith("https://")) {
            return pathOrUri;
        }
        String myBaseUri = baseUri != null ? baseUri : globalBaseUri;
        return myBaseUri == null ? pathOrUri : myBaseUri + pathOrUri;
    }

    Map<String, Object> mergeHeaders(Map<String, Object> requestHeaders) {
        Map<String, Object> headers = null;
        if (!globalHeaders.isEmpty()) {
            headers = new LinkedHashMap<>();
            headers.putAll(globalHeaders);
        }
        if (defaultHeaders != null) {
            if (headers == null) {
                headers = new LinkedHashMap<>();
            }
            headers.putAll(defaultHeaders);
        }
        if (requestHeaders != null) {
            if (headers == null) {
                headers = requestHeaders;
            } else {
                headers.putAll(requestHeaders);
            }
        }
        return headers;
    }

    /**
     * Disconnect the underlying <code>Connection</code> on close.
     */
    private static class AutoDisconnectInputStream extends FilterInputStream {

        /**
         * The underlying <code>Connection</code>.
         */
        private final Connection connection;

        /**
         * Creates an <code>AutoDisconnectInputStream</code>
         * by assigning the  argument <code>in</code>
         * to the field <code>this.in</code> so as
         * to remember it for later use.
         *
         * @param connection the underlying connection to disconnect on close.
         * @param in         the underlying input stream, or <code>null</code> if
         *                   this instance is to be created without an underlying stream.
         */
        protected AutoDisconnectInputStream(final Connection connection, final InputStream in) {
            super(in);
            this.connection = connection;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                connection.waitForStreamHandled.set(false);
            }
        }
    }

}
