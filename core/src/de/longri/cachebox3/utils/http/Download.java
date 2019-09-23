package de.longri.cachebox3.utils.http;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.InputStream;

public class Download {
    private static Logger log = LoggerFactory.getLogger(Download.class);

    public static boolean Download(String remote, String local) {
        boolean err = false;
        FileHandle localFile = Gdx.files.absolute(local);
        /* create parent directories, if necessary */
        FileHandle parent = localFile.parent();
        if ((parent != null) && !parent.exists()) {
            parent.mkdirs();
        }
        InputStream inStream = null;
        BufferedOutputStream outStream = null;

        boolean redirected;
        int redirCount = 0;
        do {
            redirected = false;
            redirCount++;
            try {
                inStream = Webb.create()
                        .get(remote)
                        .ensureSuccess()
                        .asStream()
                        .getBody();
                outStream = new BufferedOutputStream(localFile.write(false));
                WebbUtils.copyStream(inStream, outStream);
            } catch (Exception ex) {
                if (ex instanceof WebbException) {
                    WebbException we = (WebbException) ex;
                    Response re = we.getResponse();
                    if (re != null) {
                        int APIError = re.getStatusCode();
                        if (APIError >= 300 && APIError < 400) {
                            if (remote.startsWith("http:")) {
                                redirected = true;
                                remote = "https:" + remote.substring(5);
                            } else {
                                // other cases should have been handled automatically
                                // log.error("Download", remote + " to " + local, ex);
                                err = true;
                            }
                        }
                    }
                } else {
                    log.error(remote + " to " + local, ex);
                    err = true;
                }
            } finally {
                try {
                    inStream.close();
                    outStream.close();
                } catch (Exception ignored) {
                }
            }
        }
        while (redirected && redirCount < 2);

        if (err) {
            try {
                localFile.delete();
            } catch (Exception e) {
                // wie egal
            }
            return false;
        } else {
            return localFile.exists();
        }
    }

}
