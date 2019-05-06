package de.longri.cachebox3.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 26.06.2017.
 */
public class NetUtils {
    private final static Logger log = LoggerFactory.getLogger(NetUtils.class);

    public static Object postAndWait(final ResultType type, final Net.HttpRequest request, final ICancel icancel) {
        log.debug("Send Post request");
        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final Object[] result = new Object[1];
        if (icancel != null) {
            CB.postAsync(new NamedRunnable("NetUtils:postAndWait1") {
                @Override
                public void run() {
                    while (WAIT.get()) {
                        if (icancel.cancel()) {
                            Gdx.net.cancelHttpRequest(request);
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

        final AtomicBoolean isRedirection = new AtomicBoolean(false);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                if (httpResponse.getStatus().getStatusCode() >= 301 && httpResponse.getStatus().getStatusCode() <= 302) {
                    log.debug("redirection");
                    final String redirection = httpResponse.getHeader("Location");
                    if (redirection != null) {
                        CB.postAsync(new NamedRunnable("NetUtils:postAndWait2") {
                            @Override
                            public void run() {
                                isRedirection.set(true);
                                Gdx.net.cancelHttpRequest(request);
                                Net.HttpRequest httpGet = new Net.HttpRequest(request.getMethod());
                                httpGet.setUrl(redirection);
                                result[0] = postAndWait(type, httpGet, icancel);
                                WAIT.set(false);
                            }
                        });
                    }
                    return;
                }

                log.debug("Handle Response");
                final AtomicBoolean HANDEL_WAIT = new AtomicBoolean(true);
                switch (type) {

                    case STRING:
                        result[0] = httpResponse.getResultAsString();
                        HANDEL_WAIT.set(false);
                        break;
                    case STREAM:
                        result[0] = new StreamHandleObject() {
                            @Override
                            public void handled() {
                                HANDEL_WAIT.set(false);
                            }
                        };
                        ((StreamHandleObject) result[0]).stream = httpResponse.getResultAsStream();
                        break;
                }
                WAIT.set(false);
                CB.wait(HANDEL_WAIT);
                log.debug("returned stream handled, close connection");
            }

            @Override
            public void failed(Throwable t) {
                log.error("Request failed", t);
                WAIT.set(false);
            }

            @Override
            public void cancelled() {
                log.debug("Request cancelled");
                if (!isRedirection.get()) WAIT.set(false);
            }
        });

        CB.wait(WAIT);
        return result[0];
    }

    public static Boolean download(String uri, String local) {
        FileHandle localFile = Gdx.files.absolute(local);
        try {
            new Downloader(new URL(uri), localFile).run();
        } catch (MalformedURLException e) {
            log.error("download: " + uri + " to " + local, e);
        }
        return localFile.exists();
    }

    public enum ResultType {
        STRING, STREAM
    }

    public abstract static class StreamHandleObject {
        public InputStream stream;

        public abstract void handled();
    }
}
