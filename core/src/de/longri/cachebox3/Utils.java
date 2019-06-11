/*
 * Copyright (C) 2016 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.utils.converter.Base64;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Longri on 18.07.16.
 */
public class Utils {
    static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static final String THUMB = "thumb_";
    public static final String THUMB_OVERVIEW = "overview";

    /**
     * Returns a @Pixmap from given Bitmap
     *
     * @param bitmap
     * @return
     */
    public static Pixmap getPixmapFromBitmap(Bitmap bitmap) {
        byte[] encodedData = bitmap.getPngEncodedData();
        Pixmap ret = new Pixmap(encodedData, 0, encodedData.length);
        encodedData = new byte[0];
        System.gc();
        return ret;
    }

    public static TextureRegion getTextureRegion(InputStream inputStream) {
        try {
            Bitmap svgBitmap = PlatformConnector.getSvg("", inputStream, PlatformConnector.SvgScaleType.DPI_SCALED, 1f);
            TextureRegion ret = new TextureRegion(new Texture(getPixmapFromBitmap(svgBitmap)));
            svgBitmap.recycle();
            return ret;
        } catch (IOException e) {
            log.error("getTextureRegion", e);
        }
        return null;
    }

    /**
     * List all Files inside a FileHandle (Directory)
     *
     * @param begin
     * @param handles
     */
    public static void listFileHandels(FileHandle begin, ArrayList<FileHandle> handles) {
        FileHandle[] newHandles = begin.list();
        for (FileHandle f : newHandles) {
            if (f.isDirectory()) {
                listFileHandels(f, handles);
            } else {
                handles.add(f);
            }
        }
    }

    static final int[] Key = {128, 56, 20, 78, 33, 225};

    public static String decrypt(String value) {
        int[] b = null;
        try {
            b = byte2intArray(Base64.decode(value));
        } catch (IOException e) {

            e.printStackTrace();
        }

        rc4(b, Key);
        String decrypted = "";

        char[] c = new char[b.length];
        for (int x = 0; x < b.length; x++) {
            c[x] = (char) b[x];
        }

        decrypted = String.copyValueOf(c);

        return decrypted;

    }

    private static int[] byte2intArray(byte[] b) {
        int[] i = new int[b.length];

        for (int x = 0; x < b.length; x++) {
            int t = b[x];
            if (t < 0) {
                t += 256;
            }
            i[x] = t;
        }

        return i;
    }

    private static byte[] int2byteArray(int[] i) {
        byte[] b = new byte[i.length];

        for (int x = 0; x < i.length; x++) {

            int t = i[x];
            if (t > 128) {
                t -= 256;
            }

            b[x] = (byte) t;
        }

        return b;
    }

    public static String encrypt(String value) {
        String encrypted = "";
        try {
            int[] b = byte2intArray(value.getBytes());
            rc4(b, Key);
            encrypted = Base64.encodeBytes(int2byteArray(b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static void rc4(int[] bytes, int[] key) {
        int[] s = new int[256];
        int[] k = new int[256];
        int temp;
        int i, j;

        for (i = 0; i < 256; i++) {
            s[i] = (int) i;
            k[i] = (int) key[i % key.length];
        }

        j = 0;
        for (i = 0; i < 256; i++) {
            j = (j + s[i] + k[i]) % 256;
            temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }

        i = j = 0;
        for (int x = 0; x < bytes.length; x++) {
            i = (i + 1) % 256;
            j = (j + s[i]) % 256;
            temp = s[i];
            s[i] = s[j];
            s[j] = temp;
            int t = (s[i] + s[j]) % 256;
            bytes[x] = (int) (bytes[x] ^ s[t]);
        }
    }

    public static String getFileExtension(String filename) {
        int dotposition = filename.lastIndexOf(".");
        String ext = "";
        if (dotposition > -1) {
            ext = filename.substring(dotposition + 1, filename.length());
        }

        return ext;
    }

    public static String getFileNameWithoutExtension(String filename) {
        int dotposition = filename.lastIndexOf(".");
        if (dotposition >= 0)
            filename = filename.substring(0, dotposition);
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(slashposition + 1, filename.length());
        return filename;

    }

    public static String getFileName(String filename) {
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(slashposition + 1, filename.length());
        return filename;

    }

    public static String getDirectoryName(String filename) {
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(0, slashposition);
        return filename;
    }

    /**
     * überprüft ob ein File existiert! Und nicht leer ist (0 Bytes)
     *
     * @param filename
     * @return true, wenn das File existiert, ansonsten false.
     */
    public static boolean fileExistsNotEmpty(String filename) {
        File file = new File(filename);
        if (!file.exists())
            return false;
        if (file.length() <= 0)
            return false;

        return true;
    }


    /**
     * Returns the MD5 hash from given fileHandle, or an empty String with any Exception
     *
     * @param fileHandle
     * @return
     */
    public static String getMd5(FileHandle fileHandle) {
        try {
            InputStream fin = fileHandle.read();
            java.security.MessageDigest md5er =
                    MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int read;
            do {
                read = fin.read(buffer);
                if (read > 0)
                    md5er.update(buffer, 0, read);
            } while (read != -1);
            fin.close();
            byte[] digest = md5er.digest();
            if (digest == null)
                return null;
            String strDigest = "0x";
            for (int i = 0; i < digest.length; i++) {
                strDigest += Integer.toString((digest[i] & 0xff)
                        + 0x100, 16).substring(1).toUpperCase();
            }
            return strDigest;
        } catch (Exception e) {
            log.error("create md5 hash", e);
        }
        return "";
    }

    /**
     * SDBM-Hash algorithm for storing hash values into the database. This is neccessary to be compatible to the CacheBox@Home project.
     * Because the standard .net Hash algorithm differs from compact edition to the normal edition.
     *
     * @param str
     * @return
     */
    public static long sdbm(String str) {
        if (str == null || str.equals(""))
            return 0;

        long hash = 0;
        // set mask to 2^32!!!???!!!
        long mask = 42949672;
        mask = mask * 100 + 95;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            hash = (c + (hash << 6) + (hash << 16) - hash) & mask;
        }

        return hash;
    }

    /**
     * @param folder Path as String
     * @return true, if folder exist! false otherwise
     */
    public static boolean directoryExists(String folder) {
        FileHandle fh = new FileHandle(folder);
        boolean exist = fh.exists();
        fh = null;
        return exist;
    }

    /**
     * überprüft ob ein Ordner existiert und legt ihn an, wenn er nicht existiert.
     *
     * @param folder Pfad des Ordners
     * @return true, wenn er existiert oder angelegt wurde. false, wenn das Anlegen nicht funktioniert hat.
     */
    public static boolean createDirectory(String folder) {

        // remove extention
        int extPos = folder.lastIndexOf("/");
        String ext = "";
        if (extPos > -1)
            ext = folder.substring(extPos);

        if (ext.length() > 0 && ext.contains(".")) {
            folder = folder.replace(ext, "");
        }

        if (!checkWritePermission(folder)) {
            return false;
        }

        File f = new File(folder);

        if (f.isDirectory())
            return true;
        else {
            // have the object build the directory structure, if needed.
            return f.mkdirs();
        }
    }

    /**
     * Returns TRUE has the given PATH write permission!
     *
     * @param Path
     * @return
     */
    public static boolean checkWritePermission(String Path) {
        boolean result = true;
        try {
            String testFolderName = Path + "/Test/";

            File testFolder = new File(testFolderName);
            if (testFolder.mkdirs()) {
                File test = new File(testFolderName + "Test.txt");
                test.createNewFile();
                if (!test.exists()) {
                    result = false;
                } else {
                    test.delete();
                    testFolder.delete();
                }
            } else {
                result = false;
            }
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static String getFileName(FileHandle fileHandle) {
        return getFileName(fileHandle.name());
    }


    public static void logRunningTime(final String name, Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        long runningTime = System.currentTimeMillis() - start;
        log.info(("Time for " + name + ": " + Long.toString(runningTime)));
    }

    public static void triggerButtonClicked(Button button) {

        log.debug("Perform click event on {}", button);

        Array<EventListener> listeners = button.getListeners();
        for (int i = 0; i < listeners.size; i++) {
            if (listeners.get(i) instanceof ClickListener) {
                ((ClickListener) listeners.get(i)).clicked(null, 0, 0);
            }
        }
    }

    /**
     * Return TRUE if both Date are equals on Year, Day, Month, Hour, Minute and Second
     *
     * @param timestamp
     * @param timestamp1
     * @return
     */
    public static boolean equalsDate(Date timestamp, Date timestamp1) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return df.format(timestamp).equals(df.format(timestamp1));
    }

}
