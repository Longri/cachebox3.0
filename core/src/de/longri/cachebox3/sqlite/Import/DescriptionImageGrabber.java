/*
 * Copyright (C) 2014 team-cachebox.de
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
package de.longri.cachebox3.sqlite.Import;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.utils.Downloader;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NetUtils;
import de.longri.cachebox3.utils.http.ProgressCancelDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static de.longri.cachebox3.Utils.sdbm;
import static de.longri.cachebox3.apis.GroundspeakAPI.*;

public class DescriptionImageGrabber {
    final static Logger log = LoggerFactory.getLogger(DescriptionImageGrabber.class);

    public static Array<Segment> Segmentize(String text, String leftSeperator, String rightSeperator) {
        Array<Segment> result = new Array<Segment>();

        if (text == null) {
            return result;
        }

        int idx = 0;

        while (true) {
            int leftIndex = text.toLowerCase().indexOf(leftSeperator, idx);

            if (leftIndex == -1)
                break;

            leftIndex += leftSeperator.length();

            int rightIndex = text.toLowerCase().indexOf(rightSeperator, leftIndex);

            if (rightIndex == -1)
                break;

            // ignoriere URLs, die zu lang sind
            // if (text.length() > 1024)
            if ((rightIndex - leftIndex) > 1024) {
                idx = rightIndex;
                continue;
            }

            int forward = leftIndex + 50;

            if (forward > text.length()) {
                forward = text.length();
            }

            // Test, ob es sich um ein eingebettetes Bild handelt
            if (text.substring(leftIndex, forward).toLowerCase().contains("data:image/")) {
                idx = rightIndex;
                continue;
            }

            // Abschnitt gefunden
            Segment curSegment = new Segment();
            curSegment.start = leftIndex;
            curSegment.ende = rightIndex;
            curSegment.text = text.substring(leftIndex, rightIndex/* - leftIndex */);
            result.add(curSegment);

            idx = rightIndex;
        }

        return result;
    }

    public static String RemoveSpaces(String line) {
        String dummy = line.replace("\n", "");
        dummy = dummy.replace("\r", "");
        dummy = dummy.replace(" ", "");
        return dummy;
    }

    /**
     * @param GcCode
     * @param _uri
     * @return
     */
    public static String BuildDescriptionImageFilename(String GcCode, URI _uri) {
        // in der DB stehts ohne large. der Dateiname wurde aber mit large gebildet. Ev auch nur ein Handy / PC Problem.
        String path = _uri.getPath();
        String authority = _uri.getAuthority();
        if (authority != null) {
            if (authority.equals("img.geocaching.com")) {
                path = path.replace("/large/", "/");
            }
        }
        String imagePath = Config.DescriptionImageFolder.getValue() + "/" + GcCode.substring(0, 4);
        if (Config.DescriptionImageFolderLocal.getValue().length() > 0)
            imagePath = Config.DescriptionImageFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

        // String uriName = url.Substring(url.LastIndexOf('/') + 1);
        // int idx = uri.AbsolutePath.LastIndexOf('.');
        // //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int idx = path.lastIndexOf('.');
        // String extension = (idx >= 0) ? uri.AbsolutePath.Substring(idx) :
        // ".";!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String extension = (idx >= 0) ? path.substring(idx) : ".";

        // return imagePath + "\\" + GcCode +
        // Global.sdbm(uri.AbsolutePath).ToString() + extension;!!!!!!!!!!!!!
        return imagePath + "/" + GcCode + sdbm(path) + extension;
    }

    /**
     * @param Cache
     * @param html
     * @param suppressNonLocalMedia
     * @param NonLocalImages
     * @param NonLocalImagesUrl
     * @return
     */
    public static String ResolveImages(AbstractCache Cache, String html, boolean suppressNonLocalMedia, Array<String> NonLocalImages, Array<String> NonLocalImagesUrl) {
        /*
         * NonLocalImages = new List<string>(); NonLocalImagesUrl = new List<string>();
         */

        URI baseUri;
        try {
            baseUri = URI.create(Cache.getUrl().toString());
        } catch (Exception exc) {
            /*
             * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " + exc.ToString());
             * #endif
             */
            baseUri = null;
        }

        if (baseUri == null) {
            Cache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + Cache.getGcCode());
            try {
                baseUri = URI.create(Cache.getUrl().toString());
            } catch (Exception exc) {
                /*
                 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " +
                 * exc.ToString()); #endif
                 */
                return html;
            }
        }

        // String htmlNoSpaces = RemoveSpaces(html);

        Array<Segment> imgTags = Segmentize(html, "<img", ">");

        int delta = 0;

        for (int i = 0, n = imgTags.size; i < n; i++) {
            Segment img = imgTags.get(i);
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            int srcStart = img.text.indexOf('"', srcIdx + 4);
            int srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd);
                try {
                    URI imgUri = URI.create(/* baseUri, */src);
                    String localFile = BuildDescriptionImageFilename(Cache.getGcCode().toString(), imgUri);

                    if (Utils.fileExistsNotEmpty(localFile)) {
                        int idx = 0;

                        while ((idx = html.indexOf(src, idx)) >= 0) {
                            if (idx >= (img.start + delta) && (idx <= img.ende + delta)) {
                                String head = html.substring(0, img.start + delta);
                                String tail = html.substring(img.ende + delta);
                                String uri = "file://" + localFile;
                                String body = img.text.replace(src, uri);

                                delta += (uri.length() - src.length());
                                html = head + body + tail;
                            }
                            idx++;
                        }
                    } else {
                        NonLocalImages.add(localFile);
                        NonLocalImagesUrl.add(imgUri.toString());

                        if (suppressNonLocalMedia) {
                            // Wenn nicht-lokale Inhalte unterdrückt werden sollen, wird das <img>-Tag vollständig entfernt
                            html = html.substring(0, img.start - 4 + delta) + html.substring(img.ende + 1 + delta);
                            delta -= 5 + img.ende - img.start;
                        }

                    }
                } catch (Exception exc) {
                    /*
                     * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve relative uri. Base '" + baseUri +
                     * "', relative '" + src + "': " + exc.ToString()); #endif
                     */
                }
            }
        }

        return html;
    }

    public static Array<URI> GetImageUris(String html, String baseUrl) {

        Array<URI> images = new Array<>();

        // chk baseUrl
        try {
            URI.create(baseUrl);
        } catch (Exception exc) {
            return images;
        }

        Array<Segment> imgTags = Segmentize(html, "<img", ">");

        for (int i = 0, n = imgTags.size; i < n; i++) {
            Segment img = imgTags.get(i);
            int srcStart = -1;
            int srcEnd = -1;
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            if (srcIdx != -1)
                srcStart = img.text.indexOf('"', srcIdx + 4);
            if (srcStart != -1)
                srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd);
                try {
                    URI imgUri = URI.create(src);

                    images.add(imgUri);

                } catch (Exception ignored) {
                }
            }
        }

        return images;
    }

    public static void GrabImagesSelectedByCache(ProgressCancelDownloader ip, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id, String gcCode, String description, String url, boolean withLogImages) {
        boolean imageLoadError = false;

        if (!descriptionImagesUpdated) {
            log.debug("GrabImagesSelectedByCache -> grab description images for GC:{}", gcCode);
            ip.setProgress(-1, Translation.get("DescriptionImageImportForGC") + gcCode);

            Array<URI> imgUris = GetImageUris(description, url);

            for (URI uri : imgUris) {
                String local = BuildDescriptionImageFilename(gcCode, uri);
                FileHandle localFile = Gdx.files.absolute(local);
                try {
                    ip.add(new Downloader(uri.toURL(), localFile));
                } catch (MalformedURLException e) {
                    log.error("download", e);
                }
            }

            descriptionImagesUpdated = true;

            if (!imageLoadError) {
                Database.Parameters args = new Database.Parameters();
                args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
            }
            log.debug("GrabImagesSelectedByCache done");
        }

        if (!additionalImagesUpdated) {
            log.debug("GrabImagesSelectedByCache -> grab spoiler images");
            // Get additional images (Spoiler)

            FileHandle[] files = getFilesInDirectory(Config.SpoilerFolder.getValue(), gcCode);
            Array<String> allSpoilers = new Array<>();
            for (FileHandle file : files)
                allSpoilers.add(file.name());
            FileHandle[] filesLocal = getFilesInDirectory(Config.SpoilerFolderLocal.getValue(), gcCode);


            for (FileHandle file : filesLocal)
                allSpoilers.add(file.name());


            ip.setProgress(-1, Translation.get("SpoilerImageImportForGC") + gcCode);

            // todo always take from database. They are not downloaded yet
            // todo else don't write them to database on fetch/update cache
            Array<ImageEntry> imageEntries = downloadImageListForGeocache(gcCode, withLogImages);
            if (APIError != OK) {
                return;
            }

            for (ImageEntry imageEntry : imageEntries) {

                String uri = imageEntry.ImageUrl;
                imageEntry = BuildAdditionalImageFilenameHashNew(gcCode, imageEntry);
                if (imageEntry != null) {
                    // todo ? should write or update database
                    String filename = imageEntry.LocalPath.substring(imageEntry.LocalPath.lastIndexOf('/') + 1);

                    // todo to test allSpoilers content
                    if (allSpoilers.contains(filename, false)) {
                        // wenn ja, dann aus der Liste der aktuell vorhandenen Spoiler entfernen und mit dem nächsten Spoiler weitermachen
                        allSpoilers.removeValue(filename, false);
                        continue; // dieser Spoiler muss jetzt nicht mehr geladen werden da er schon vorhanden ist.
                    }

                    for (int j = 0; j < 1; j++) {

                        try {
                            ip.add(new Downloader(new URL(imageEntry.ImageUrl), Gdx.files.absolute(imageEntry.LocalPath)));
                        } catch (MalformedURLException e) {
                            log.error("download", e);
                        }
                    }
                }
            }
            log.debug("images download done");

            additionalImagesUpdated = true;

//            if (!imageLoadError) {
//                Database.Parameters args = new Database.Parameters();
//                args.put("ImagesUpdated", additionalImagesUpdated);
//                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
//                // jetzt können noch alle "alten" Spoiler gelöscht werden.
//                // "alte" Spoiler sind die, die auf der SD vorhanden sind, aber nicht als Link über die API gemeldet wurden.
//                // Alle Spoiler in der Liste allSpoilers sind "alte"
//                log.debug("Delete old spoilers.");
//                for (String file : allSpoilers) {
//                    String fileNameWithOutExt = file.replaceFirst("[.][^.]+$", "");
//                    // Testen, ob dieser Dateiname einen gültigen ACB Hash hat (eingeschlossen zwischen @....@>
//                    if (fileNameWithOutExt.endsWith("@") && fileNameWithOutExt.contains("@")) {
//                        // file enthält nur den Dateinamen, nicht den Pfad. Diesen Dateinamen um den Pfad erweitern, in dem hier die
//                        // Spoiler gespeichert wurden
//                        String path = getSpoilerPath(gcCode);
//                        FileHandle f = Gdx.files.absolute(path + '/' + file);
//                        try {
//                            f.delete();
//                        } catch (Exception ex) {
//                            log.error("DescriptionImageGrabber - GrabImagesSelectedByCache - DeleteSpoiler", ex);
//                        }
//                    }
//                }
//            }
            log.debug("GrabImagesSelectedByCache done");
        }
        return;
    }

    private static FileHandle[] getFilesInDirectory(String path, final String GcCode) {
        String imagePath = path + "/" + GcCode.substring(0, 4);

        if (Gdx.files.absolute(imagePath).exists()) {
            FileHandle dir = Gdx.files.absolute(imagePath);
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String filename = pathname.getName().toLowerCase();
                    if (filename.indexOf(GcCode.toLowerCase()) == 0) {
                        return true;
                    }
                    return false;
                }
            };
            FileHandle[] files = dir.list(filter);
            return files;
        }
        return new FileHandle[0];
    }

    public static String getSpoilerPath(String GcCode) {
        String imagePath = Config.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

        if (Config.SpoilerFolderLocal.getValue().length() > 0)
            imagePath = Config.SpoilerFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

        return imagePath;
    }

    public static String getImageFolderPath(CharSequence gcCode) {
        return getStringBuilderPathforGcCode(gcCode).toString();
    }

    private static StringBuilder getStringBuilderPathforGcCode(CharSequence gcCode) {
        StringBuilder imagePath;
        if (Config.DescriptionImageFolderLocal.getValue().length() > 0) {
            imagePath = new StringBuilder(Config.DescriptionImageFolderLocal.getValue());
        } else {
            imagePath = new StringBuilder(Config.DescriptionImageFolder.getValue());
        }

        imagePath.append("/");
        for (int i = 0; i <= 4; i++)
            imagePath.append(gcCode.charAt(i));

        imagePath.append("/");
        imagePath.append(gcCode);
        return imagePath;
    }

    /**
     * Neue Version, mit @ als Eingrenzung des Hashs, da die Klammern nicht als URL's verwendet werden dürfen
     */
    public static ImageEntry BuildAdditionalImageFilenameHashNew(String GcCode, ImageEntry imageEntry) {
        try {
            String uriPath = new URI(imageEntry.ImageUrl).getPath();
            String imagePath = Config.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

            if (Config.SpoilerFolderLocal.getValue().length() > 0)
                imagePath = Config.SpoilerFolderLocal.getValue() + "/" + GcCode.substring(0, 4);
            imageEntry.Name = imageEntry.Description.trim();
            imageEntry.Name = imageEntry.Name.replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");

            int idx = imageEntry.ImageUrl.lastIndexOf('.');
            String extension = (idx >= 0) ? imageEntry.ImageUrl.substring(idx) : ".";

            // Create sdbm Hash from Path of URI, not from complete URI
            imageEntry.LocalPath = imagePath + "/" + GcCode + " - " + imageEntry.Name + " @" + sdbm(uriPath) + "@" + extension;
            return imageEntry;
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean HandleMissingImages(boolean imageLoadError, String uri, String local) {
        try {
            FileHandle file = Gdx.files.absolute(local + "_broken_link.txt");
            if (!file.exists()) {
                FileHandle file2 = Gdx.files.absolute(local + ".1st");
                if (file2.exists()) {
                    // After first try, we can be sure that the image cannot be loaded.
                    // At this point mark the image as loaded and go ahead.
                    file2.file().renameTo(file.file());
                } else {
                    // Create a local file for marking it that it could not loaded one time.
                    // Maybe the link is broken temporarily. So try it next time once again.
                    try {
                        String text = "Could not load image from:" + uri;
                        BufferedWriter out = new BufferedWriter(new FileWriter(local + ".1st"));
                        out.write(text);
                        out.close();
                        imageLoadError = true;
                    } catch (IOException e) {
                        System.out.println("Exception ");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return imageLoadError;
    }

    private static void DeleteMissingImageInformation(String local) {
        FileHandle file = Gdx.files.absolute(local + "_broken_link.txt");
        if (file.exists()) {
            file.delete();
        }

        file = Gdx.files.absolute(local + ".1st");
        if (file.exists()) {
            file.delete();
        }
    }

    public static class Segment {
        public int start;
        public int ende;
        public String text;
    }

}
