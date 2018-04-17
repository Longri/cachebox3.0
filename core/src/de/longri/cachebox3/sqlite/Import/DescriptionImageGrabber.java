/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
import com.badlogic.gdx.utils.StringBuilder;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NetUtils;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class DescriptionImageGrabber {
    final static Logger log = LoggerFactory.getLogger(DescriptionImageGrabber.class);

    public static class Segment {
        public int start;
        public int ende;
        public String text;
    }

    public static CB_List<Segment> Segmentize(String text, String leftSeperator, String rightSeperator) {
        CB_List<Segment> result = new CB_List<Segment>();

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

    /**
     * @param gcCode
     * @param _uri
     * @return
     */
    public static String BuildImageFilename(CharSequence gcCode, URI _uri) {
        // in der DB stehts ohne large. der Dateiname wurde aber mit large gebildet. Ev auch nur ein Handy / PC Problem.
        String path = _uri.getPath();
        String authority = _uri.getAuthority();
        if (authority != null) {
            if (authority.equals("img.geocaching.com")) {
                path = path.replace("/large/", "/");
            }
        }

        StringBuilder imagePath = getStringBuilderPathforGcCode(gcCode);

        // String uriName = url.Substring(url.LastIndexOf('/') + 1);
        // int idx = uri.AbsolutePath.LastIndexOf('.');
        // //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int idx = path.lastIndexOf('.');
        // String extension = (idx >= 0) ? uri.AbsolutePath.Substring(idx) :
        // ".";!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String extension = (idx >= 0) ? path.substring(idx) : ".";

        // return imagePath + "\\" + GcCode +
        // Global.sdbm(uri.AbsolutePath).ToString() + extension;!!!!!!!!!!!!!
        imagePath.append("/").append(gcCode).append(Utils.sdbm(path)).append(extension);
        return imagePath.toString();
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
     * @param abstractCache
     * @param html
     * @param suppressNonLocalMedia
     * @param nonLocalImages
     * @param nonLocalImagesUrl
     * @return
     */
    public static String resolveImages(AbstractCache abstractCache, String html, boolean suppressNonLocalMedia, LinkedList<String> nonLocalImages, LinkedList<String> nonLocalImagesUrl) {
        /*
         * NonLocalImages = new List<string>(); NonLocalImagesUrl = new List<string>();
         */

        URI baseUri;
        try {
            baseUri = URI.create(abstractCache.getUrl(Database.Data));
        } catch (Exception exc) {
            log.error("DescriptionImageGrabber.resolveImages: failed to resolve {}", abstractCache.getUrl(Database.Data), exc);
            baseUri = null;
        }

        if (baseUri == null) {
            abstractCache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + abstractCache.getGcCode());
            try {
                baseUri = URI.create(abstractCache.getUrl(Database.Data));
            } catch (Exception exc) {
                log.error("DescriptionImageGrabber.resolveImages: failed to resolve {}", abstractCache.getUrl(Database.Data), exc);
                return html;
            }
        }

        // String htmlNoSpaces = RemoveSpaces(html);

        CB_List<Segment> imgTags = Segmentize(html, "<img", ">");

        int delta = 0;

        for (int i = 0, n = imgTags.size; i < n; i++) {
            Segment img = imgTags.get(i);
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            int srcStart = img.text.indexOf('"', srcIdx + 4);
            int srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd/*
                 * - srcStart - 1
                 */);
                try {
                    URI imgUri = URI.create(/* baseUri, */src); // NICHT
                    // ORGINAL!!!!!!!!!
                    String localFile = BuildImageFilename(abstractCache.getGcCode().toString(), imgUri);

                    if (Utils.FileExistsNotEmpty(localFile)) {
                        int idx = 0;

                        while ((idx = html.indexOf(src, idx)) >= 0) {
                            if (idx >= (img.start + delta) && (idx <= img.ende + delta)) {
                                String head = html.substring(0, img.start + delta);
                                String tail = html.substring(img.ende + delta);
                                String uri = new File(localFile).toURI().toString();
                                String body = img.text.replace(src, uri);

                                delta += (uri.length() - src.length());
                                html = head + body + tail;
                            }
                            idx++;
                        }
                    } else {
                        nonLocalImages.add(localFile);
                        nonLocalImagesUrl.add(imgUri.toString());

                        if (suppressNonLocalMedia) {
                            // Wenn nicht-lokale Inhalte unterdrückt werden
                            // sollen,
                            // wird das <img>-Tag vollständig entfernt
                            html = html.substring(0, img.start - 4 + delta) + html.substring(img.ende + 1 + delta);
                            delta -= 5 + img.ende - img.start;
                        }

                    }
                } catch (Exception exc) {
                    /*
                     * #if DEBUG Global.AddLog( "DescriptionImageGrabber.resolveImages: failed to resolve relative uri. Base '" + baseUri +
                     * "', relative '" + src + "': " + exc.ToString()); #endif
                     */
                }
            }
        }

        return html;
    }

//    public static LinkedList<String> GetAllImages(AbstractCache AbstractCache) {
//
//        LinkedList<String> images = new LinkedList<String>();
//
//        URI baseUri;
//        try {
//            baseUri = URI.create(AbstractCache.getUrl());
//        } catch (Exception exc) {
//            baseUri = null;
//        }
//
//        if (baseUri == null) {
//            AbstractCache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + AbstractCache.getGcCode());
//            try {
//                baseUri = URI.create(AbstractCache.getUrl());
//            } catch (Exception exc) {
//                return images;
//            }
//        }
//
//        CB_List<Segment> imgTags = Segmentize(AbstractCache.getShortDescription(), "<img", ">");
//
//        imgTags.addAll(Segmentize(AbstractCache.getLongDescription(), "<img", ">"));
//
//        for (int i = 0, n = imgTags.size; i < n; i++) {
//            Segment img = imgTags.get(i);
//            int srcStart = -1;
//            int srcEnd = -1;
//            int srcIdx = img.text.toLowerCase().indexOf("src=");
//            if (srcIdx != -1)
//                srcStart = img.text.indexOf('"', srcIdx + 4);
//            if (srcStart != -1)
//                srcEnd = img.text.indexOf('"', srcStart + 1);
//
//            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
//                String src = img.text.substring(srcStart + 1, srcEnd);
//                try {
//                    URI imgUri = URI.create(src);
//
//                    images.add(imgUri.toString());
//
//                } catch (Exception exc) {
//                }
//            }
//        }
//
//        return images;
//    }

    public static LinkedList<URI> GetImageUris(String html, String baseUrl) {

        LinkedList<URI> images = new LinkedList<URI>();

        // chk baseUrl
        try {
            URI.create(baseUrl);
        } catch (Exception exc) {
            return images;
        }

        CB_List<Segment> imgTags = Segmentize(html, "<img", ">");

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

                } catch (Exception exc) {
                }
            }
        }

        return images;
    }


    public static ApiResultState GrabImagesSelectedByCache(ImporterProgress ip, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id, String gcCode, String name, String description, String url) {
        boolean imageLoadError = false;

        if (!descriptionImagesUpdated) {
            if (ip != null) ip.ProgressChangeMsg("importImages", "Importing Description Images for " + gcCode);

            LinkedList<URI> imgUris = GetImageUris(description, url);

            for (URI uri : imgUris) {
                try {// for cancel/interupt Thread
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return ApiResultState.CANCELED;
                }

                if (BreakawayImportThread.isCanceled())
                    return ApiResultState.CANCELED;

                String local = BuildImageFilename(gcCode, uri);

                if (ip != null)
                    ip.ProgressChangeMsg("importImages", "Importing Description Images for " + gcCode + " - download: " + uri);

                // build URL
                for (int j = 0; j < 1 /* && !parent.Cancel */; j++) {
                    if (NetUtils.download(uri.toString(), local)) {
                        // Next image
                        DeleteMissingImageInformation(local);
                        break;
                    } else {
                        imageLoadError = HandleMissingImages(imageLoadError, uri, local);
                    }
                }
            }

            descriptionImagesUpdated = true;

            if (!imageLoadError) {
                Database.Parameters args = new Database.Parameters();
                args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
            }
        }

        if (!additionalImagesUpdated) {
            // get additional images (Spoiler)

            // Liste aller Spoiler Images für diesen Cache erstellen
            // anhand dieser Liste kann überprüft werden, ob ein Spoiler schon geladen ist und muss nicht ein 2. mal geladen werden.
            // Außerdem können anhand dieser Liste veraltete Spoiler identifiziert werden, die gelöscht werden können / müssen
            FileHandle[] files = getFilesInDirectory(Config.SpoilerFolder.getValue(), gcCode);
            FileHandle[] filesLocal = getFilesInDirectory(Config.SpoilerFolderLocal.getValue(), gcCode);
            ArrayList<String> afiles = new ArrayList<String>();
            for (FileHandle file : files)
                afiles.add(file.name());
            for (FileHandle file : filesLocal)
                afiles.add(file.name());

            {
                if (ip != null) ip.ProgressChangeMsg("importImages", "Importing Spoiler Images for " + gcCode);
                HashMap<String, URI> allimgDict = new HashMap<String, URI>();

                ApiResultState result = ApiResultState.UNKNOWN;
                long startTs = System.currentTimeMillis();

                result = GroundspeakAPI.getAllImageLinks(gcCode, allimgDict, null);

                if (result.isErrorState()) {
                    return result;
                }

                for (String key : allimgDict.keySet()) {

                    try {// for cancel/interupt Thread
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return ApiResultState.IO;
                    }

                    if (BreakawayImportThread.isCanceled())
                        return ApiResultState.CANCELED;

                    URI uri = allimgDict.get(key);
                    if (uri.toString().contains("/cache/log/"))
                        continue; // LOG-Image

                    if (ip != null)
                        ip.ProgressChangeMsg("importImages", "Importing Spoiler Images for " + gcCode + " - download: " + uri);

                    String decodedImageName = key;

                    String local = buildAdditionalImageFilename(gcCode, decodedImageName, uri);
                    if (Gdx.files.local(local).exists()) {
                        // Spoiler ohne den Hash im Dateinamen löschen
                        Gdx.files.local(local).delete();
                    }
                    // Local Filename mit Hash erzeugen, damit Änderungen der Datei ohne Änderungen des Dateinamens erkannt werden können
                    // Hier erst die alten Version mit den Klammern als Eingrenzung des Hash
                    // Dies hier machen, damit die Namen der Spoiler ins neue System Konvertiert werden können.
                    String localOld = BuildAdditionalImageFilenameHashNew(gcCode, decodedImageName, uri);
                    // Neuen Local Filename mit Hash erzeugen, damit Änderungen der Datei ohne Änderungen des Dateinamens erkannt werden können
                    // Hier jetzt mit @ als Eingrenzung des Hashs
                    local = BuildAdditionalImageFilenameHashNew(gcCode, decodedImageName, uri);
                    String filename = local.substring(local.lastIndexOf('/') + 1);
                    FileHandle oldFile = Gdx.files.local(localOld);
                    if (oldFile.exists()) {
                        try {
                            oldFile.file().renameTo(Gdx.files.local(local).file());
                            afiles.add(filename);
                        } catch (Exception ex) {
                            log.error("Error trying to rename Spoiler with old name format", ex);
                        }
                    }

                    // überprüfen, ob dieser Spoiler bereits geladen wurde
                    if (afiles.contains(filename)) {
                        // wenn ja, dann aus der Liste der aktuell vorhandenen Spoiler entfernen und mit dem nächsten Spoiler weiter
                        // machen
                        // dieser Spoiler muss jetzt nicht mehr geladen werden da er schon vorhanden ist.
                        afiles.remove(filename);
                        continue;
                    }

                    // build URL
                    for (int j = 0; j < 1; j++) {
                        if (NetUtils.download(uri.toString(), local)) {
                            // Next image
                            DeleteMissingImageInformation(local);
                            break;
                        } else {
                            imageLoadError = HandleMissingImages(imageLoadError, uri, local);
                        }

                    }
                }

                additionalImagesUpdated = true;

                if (!imageLoadError) {
                    Database.Parameters args = new Database.Parameters();
                    args.put("ImagesUpdated", additionalImagesUpdated);
                    Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
                    // jetzt können noch alle "alten" Spoiler gelöscht werden. "alte" Spoiler sind die, die auf der SD vorhanden sind,
                    // aber
                    // nicht als Link über die API gemeldet wurden
                    // Alle Spoiler in der Liste afiles sind "alte"
                    for (String file : afiles) {
                        String fileNameWithOutExt = file.replaceFirst("[.][^.]+$", "");
                        // Testen, ob dieser Dateiname einen gültigen ACB Hash hat (eingeschlossen zwischen @....@>
                        if (fileNameWithOutExt.endsWith("@") && fileNameWithOutExt.contains("@")) {
                            // file enthält nur den Dateinamen, nicht den Pfad. Diesen Dateinamen um den Pfad erweitern, in dem hier die
                            // Spoiler gespeichert wurden
                            String path = getSpoilerPath(gcCode);
                            FileHandle f = Gdx.files.local(path + '/' + file);
                            try {
                                f.delete();
                            } catch (Exception ex) {
                                log.error("DescriptionImageGrabber - GrabImagesSelectedByCache - DeleteSpoiler", ex);
                            }
                        }
                    }
                }

            }
        }
        return ApiResultState.IO;
    }

    private static FileHandle[] getFilesInDirectory(String path, final String GcCode) {
        String imagePath = path + "/" + GcCode.substring(0, 4);

        if (Gdx.files.local(imagePath).exists()) {
            FileHandle dir = Gdx.files.local(imagePath);
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

    /**
     * @param GcCode
     * @param ImageName
     * @param uri
     * @return
     */
    public static String buildAdditionalImageFilename(String GcCode, String ImageName, URI uri) {
        String imagePath = getSpoilerPath(GcCode);

        ImageName = ImageName.replace("[/:*?\"<>|]", "");
        ImageName = ImageName.replace("\\", "");
        ImageName = ImageName.replace("\n", "");
        ImageName = ImageName.replace("\"", "");
        ImageName = ImageName.trim();

        int idx = uri.toString().lastIndexOf('.');
        String extension = (idx >= 0) ? uri.toString().substring(idx) : ".";

        return imagePath + "/" + GcCode + " - " + ImageName + extension;
    }

    /**
     * Neue Version, mit @ als Eingrenzung des Hashs, da die Klammern nicht als URL's verwendet werden dürfen
     *
     * @param GcCode
     * @param ImageName
     * @param uri
     * @return
     */
    public static String BuildAdditionalImageFilenameHashNew(String GcCode, String ImageName, URI uri) {
        String imagePath = Config.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

        if (Config.SpoilerFolderLocal.getValue().length() > 0)
            imagePath = Config.SpoilerFolderLocal.getValue() + "/" + GcCode.substring(0, 4);

        ImageName = ImageName.replace("[/:*?\"<>|]", "");
        ImageName = ImageName.replace("\\", "");
        ImageName = ImageName.replace("\n", "");
        ImageName = ImageName.replace("\"", "");
        ImageName = ImageName.trim();

        int idx = uri.toString().lastIndexOf('.');
        String extension = (idx >= 0) ? uri.toString().substring(idx) : ".";

        // Create sdbm Hash from Path of URI, not from complete URI
        return imagePath + "/" + GcCode + " - " + ImageName + " @" + Utils.sdbm(uri.getPath().toString()) + "@" + extension;
    }

    private static boolean HandleMissingImages(boolean imageLoadError, URI uri, String local) {
        try {
            FileHandle file = Gdx.files.local(local + "_broken_link.txt");
            if (!file.exists()) {
                FileHandle file2 = Gdx.files.local(local + ".1st");
                if (file2.exists()) {
                    // After first try, we can be sure that the image cannot be loaded.
                    // At this point mark the image as loaded and go ahead.
                    file2.file().renameTo(file.file());
                } else {
                    // Crate a local file for marking it that it could not load one time.
                    // Maybe the link is broken temporarely. So try it next time once again.
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
        } catch (Exception ex) {
            // Global.AddLog("HandleMissingImages (uri=" + uri + ") (local=" + local + ") - " + ex.ToString());
        }
        return imageLoadError;
    }

    private static void DeleteMissingImageInformation(String local) {
        FileHandle file = Gdx.files.local(local + "_broken_link.txt");
        if (file.exists()) {
            file.delete();
        }

        file = Gdx.files.local(local + ".1st");
        if (file.exists()) {
            file.delete();
        }
    }

}
