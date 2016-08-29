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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import de.longri.cachebox3.utils.converter.Base64;
import org.apache.commons.codec.binary.Hex;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Longri on 18.07.16.
 */
public class Utils {
    static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Returns a @Pixmap from given Bitmap
     *
     * @param bitmap
     * @return
     */
    public static Pixmap getPixmapFromBitmap(Bitmap bitmap) {
        byte[] encodedData = bitmap.getPngEncodedData();
        return new Pixmap(encodedData, 0, encodedData.length);
    }


    public static Drawable get9PatchFromSvg(InputStream inputStream, int left, int right, int top, int bottom) {
        try {
            Bitmap svgBitmap = PlatformConnector.getSvg(inputStream, PlatformConnector.SvgScaleType.DPI_SCALED, 1f);

            //scale nine patch regions
            float scale = CB.getScaledFloat(1);
            left *= scale;
            right *= scale;
            top *= scale;
            bottom *= scale;

            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(new NinePatch(new Texture(getPixmapFromBitmap(svgBitmap)), left, right, top, bottom));
            return ninePatchDrawable;

        } catch (IOException e) {
            log.error("get9PatchFromSvg", "IOE", e);
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

        RC4(b, Key);
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
            RC4(b, Key);
            encrypted = Base64.encodeBytes(int2byteArray(b));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static void RC4(int[] bytes, int[] key) {
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

    public static String GetFileExtension(String filename) {
        int dotposition = filename.lastIndexOf(".");
        String ext = "";
        if (dotposition > -1) {
            ext = filename.substring(dotposition + 1, filename.length());
        }

        return ext;
    }

    public static String GetFileNameWithoutExtension(String filename) {
        int dotposition = filename.lastIndexOf(".");
        if (dotposition >= 0)
            filename = filename.substring(0, dotposition);
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(slashposition + 1, filename.length());
        return filename;

    }

    public static String GetFileName(String filename) {
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(slashposition + 1, filename.length());
        return filename;

    }

    public static String GetDirectoryName(String filename) {
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
    public static boolean FileExistsNotEmpty(String filename) {
        File file = new File(filename);
        if (!file.exists())
            return false;
        if (file.length() <= 0)
            return false;

        return true;
    }


    /**
     * Returns the MD5 hash from given fileHandle, or an empty String with any Exception
     * @param fileHandle
     * @return
     */
    public static String getMd5(FileHandle fileHandle) {

        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] bytes = new byte[2048];
            int numBytes;
            InputStream inputStream = fileHandle.read();
            while ((numBytes = inputStream.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            inputStream.close();
            return new String(Hex.encodeHex(md.digest()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
}
