package de.longri.cachebox3.utils.http;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.utils.http.Webb.HDR_CONTENT_ENCODING;
import static de.longri.cachebox3.utils.http.Webb.HDR_CONTENT_TYPE;

public class Connection {
    private static final Logger log = LoggerFactory.getLogger(Connection.class);
    public Net.HttpRequest httpRequest;
    public Net.HttpResponse httpResponse;
    public int statusCode;
    public String responseMessage;
    public AtomicBoolean waitForStreamHandled;

    public Connection(Request.Method method) {
        switch (method) {
            case GET:
                httpRequest = new Net.HttpRequest(Net.HttpMethods.GET);
                break;
            case PUT:
                httpRequest = new Net.HttpRequest(Net.HttpMethods.PUT);
                break;
            case POST:
                httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
                break;
            default:
                httpRequest = new Net.HttpRequest(Net.HttpMethods.DELETE);
        }
        statusCode = -1;
        responseMessage = "";
    }

    public void setUrl(String uri) {
        httpRequest.setUrl(uri);
    }

    public void setContent(String requestBody) {
        httpRequest.setContent(requestBody);
    }

    public void setFollowRedirects(boolean booleanValue) {
        httpRequest.setFollowRedirects(booleanValue);
    }

    public void setTimeOut(Integer integer) {
        httpRequest.setTimeOut(integer);
    }

    public void addRequestProperties(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            addRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    public void addRequestProperty(String name, Object value) {
        if (name == null || name.length() == 0 || value == null) {
            throw new IllegalArgumentException("name and value must not be empty");
        }

        String valueAsString;
        if (value instanceof Date) {
            valueAsString = getRfc1123DateFormat().format((Date) value);
        } else if (value instanceof Calendar) {
            valueAsString = getRfc1123DateFormat().format(((Calendar) value).getTime());
        } else {
            valueAsString = value.toString();
        }

        httpRequest.setHeader(name, valueAsString);
    }

    public void ensureRequestProperty(String name, Object value) {
        if (!httpRequest.getHeaders().containsKey(name)) {
            addRequestProperty(name, value);
        }
    }

    /**
     * Creates a new instance of a <code>DateFormat</code> for RFC1123 compliant dates.
     * <br>
     * Should be stored for later use but be aware that this DateFormat is not Thread-safe!
     * <br>
     * If you have to deal with dates in this format with JavaScript, it's easy, because the JavaScript
     * Date object has a constructor for strings formatted this way.
     * @return a new instance
     */
    private DateFormat getRfc1123DateFormat() {
        DateFormat format = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

    void connect(ICancel icancel) {
        log.debug("Send httpRequest");
        final AtomicBoolean WAIT = new AtomicBoolean(true);
        waitForStreamHandled = new AtomicBoolean(true);

        if (icancel != null) {
            CB.postAsync(new NamedRunnable("connect1") {
                @Override
                public void run() {
                    while (WAIT.get()) {
                        if (icancel.cancel()) {
                            Gdx.net.cancelHttpRequest(httpRequest);
                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse r) {
                httpResponse = r;
                statusCode = r.getStatus().getStatusCode();
                waitForStreamHandled.set(true);
                WAIT.set(false);
                CB.wait(waitForStreamHandled);
                log.debug("httpResponse handled");
            }

            @Override
            public void failed(Throwable t) {
                log.error("Request failed", t);
                responseMessage = t.toString(); // + t.getMessage();
                WAIT.set(false);
            }

            @Override
            public void cancelled() {
                log.debug("Request cancelled");
                WAIT.set(false);
            }
        });

        CB.wait(WAIT);
        return;
    }

    public String getContentType() {
        return httpResponse.getHeader(HDR_CONTENT_TYPE);
    }

    public String getContentEncoding() {
        return httpResponse.getHeader(HDR_CONTENT_ENCODING);
    }

    public InputStream getResultAsStream() {
        return httpResponse.getResultAsStream();
    }

    public String getResponseMessage() {
        if (statusCode == -1) {
            return responseMessage;
        }
        else {
            return httpResponse.getHeader(null);
        }
    }

    public  <T> void parseErrorResponse(Class<T> clazz, Response<T> response, InputStream responseBodyStream)
            throws IOException {

        if (responseBodyStream == null) {
            return;
        } else if (clazz == InputStream.class) {
            response.errorBody = responseBodyStream;
            return;
        }

        byte[] responseBody = WebbUtils.readBytes(responseBodyStream);
        String contentType = getContentType();
        if (contentType == null || contentType.startsWith(Const.APP_BINARY) || clazz == Const.BYTE_ARRAY_CLASS) {
            response.errorBody = responseBody;
            return;
        }

        if (contentType.startsWith(Const.APP_JSON) && clazz == JSONObject.class) {
            try {
                response.errorBody = WebbUtils.toJsonObject(responseBody);
                return;
            } catch (Exception ignored) {
                // ignored - was just a try!
            }
        }

        // fallback to String if bytes are valid UTF-8 characters ...
        try {
            response.errorBody = new String(responseBody, StandardCharsets.UTF_8);
            return;
        } catch (Exception ignored) {
            // ignored - was just a try!
        }

        // last fallback - return error object as byte[]
        response.errorBody = responseBody;
    }
}
