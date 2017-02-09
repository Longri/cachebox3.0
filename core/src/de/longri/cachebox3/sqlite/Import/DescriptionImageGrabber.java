/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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

import de.longri.cachebox3.Utils;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.utils.Downloader;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
    public static String BuildImageFilename(String GcCode, URI _uri) {
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
        return imagePath + "/" + GcCode + Utils.sdbm(path) + extension;
    }

    /**
     * @param Cache
     * @param html
     * @param suppressNonLocalMedia
     * @param NonLocalImages
     * @param NonLocalImagesUrl
     * @return
     */
    public static String ResolveImages(Cache Cache, String html, boolean suppressNonLocalMedia, LinkedList<String> NonLocalImages, LinkedList<String> NonLocalImagesUrl) {
        /*
         * NonLocalImages = new List<string>(); NonLocalImagesUrl = new List<string>();
		 */

        URI baseUri;
        try {
            baseUri = URI.create(Cache.getUrl());
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
                baseUri = URI.create(Cache.getUrl());
            } catch (Exception exc) {
				/*
				 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " +
				 * exc.ToString()); #endif
				 */
                return html;
            }
        }

        // String htmlNoSpaces = RemoveSpaces(html);

        CB_List<Segment> imgTags = Segmentize(html, "<img", ">");

        int delta = 0;

        for (int i = 0, n = imgTags.size(); i < n; i++) {
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
                    String localFile = BuildImageFilename(Cache.getGcCode(), imgUri);

                    if (Utils.FileExistsNotEmpty(localFile)) {
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
                            // Wenn nicht-lokale Inhalte unterdrückt werden
                            // sollen,
                            // wird das <img>-Tag vollständig entfernt
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

    public static Boolean Download(String uri, String local) {

        File localFile = new File(local);

        try {
            new Downloader(new URL(uri), localFile).run();
        } catch (MalformedURLException e) {
            log.error("Download: " + uri + " to " + local, e);
        }

        return localFile.exists();

        // try
        // {
        // String localDir = local.substring(0, local.lastIndexOf("/"));
        // if (!FileIO.createDirectory(localDir)) return false;
        //
        // URL aURL = null;
        // try
        // {
        // // ungültige URL -> nicht importieren
        // aURL = new URL(uri.replace("&amp;", "&"));
        // }
        // catch (Exception ex)
        // {
        // return true;
        // }
        // File file = FileFactory.createFile(local);
        // URLConnection con = aURL.openConnection();
        // con.setConnectTimeout(5000);
        // con.setReadTimeout(10000);
        // con.setRequestProperty("Accept-Charset", "UTF-8");
        //
        // InputStream is = con.getInputStream();
        // FileOutputStream fos = new FileOutputStream(file);
        // BufferedInputStream bis = new BufferedInputStream(is);
        // ByteArrayBuffer baf = new ByteArrayBuffer(10024);
        // int current = 0;
        // int count = 0;
        // while ((current = bis.read()) != -1)
        // {
        // baf.append((byte) current);
        // count++;
        // if (count > 10000)
        // {
        // fos.write(baf.toByteArray());
        // count = 0;
        // baf.clear();
        // baf.setLength(0);
        // }
        // }
        //
        // fos.write(baf.toByteArray());
        // /*
        // * try { int d; while ((d = is.read()) != -1) { fos.write(d); } } catch (IOException ex) { // TODO make a callback on exception.
        // * }
        // */
        // fos.close();
        //
        // return true;
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // return false;
        // }
    }

    public static LinkedList<String> GetAllImages(Cache Cache) {

        LinkedList<String> images = new LinkedList<String>();

        URI baseUri;
        try {
            baseUri = URI.create(Cache.getUrl());
        } catch (Exception exc) {
            baseUri = null;
        }

        if (baseUri == null) {
            Cache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + Cache.getGcCode());
            try {
                baseUri = URI.create(Cache.getUrl());
            } catch (Exception exc) {
                return images;
            }
        }

        CB_List<Segment> imgTags = Segmentize(Cache.getShortDescription(), "<img", ">");

        imgTags.addAll(Segmentize(Cache.getLongDescription(), "<img", ">"));

        for (int i = 0, n = imgTags.size(); i < n; i++) {
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

                    images.add(imgUri.toString());

                } catch (Exception exc) {
                }
            }
        }

        return images;
    }

    public static LinkedList<URI> GetImageUris(String html, String baseUrl) {

        LinkedList<URI> images = new LinkedList<URI>();

        // chk baseUrl
        try {
            URI.create(baseUrl);
        } catch (Exception exc) {
            return images;
        }

        CB_List<Segment> imgTags = Segmentize(html, "<img", ">");

        for (int i = 0, n = imgTags.size(); i < n; i++) {
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

//    /**
//     * @param ip
//     * @param descriptionImagesUpdated
//     * @param additionalImagesUpdated
//     * @param id
//     * @param gcCode
//     * @param name
//     * @param description
//     * @param url                      Config.settings.socket_timeout.getValue()
//     * @return ErrorCode Use with<br>
//     * if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
//     * {<br>
//     * GL.that.Toast(ConnectionError.INSTANCE);<br>
//     * return;<br>
//     * }<br>
//     * <br>
//     * if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
//     * {<br>
//     * GL.that.Toast(ApiUnavailable.INSTANCE);<br>
//     * return;<br>
//     * }<br>
//     */
//    public static int GrabImagesSelectedByCache(ImporterProgress ip, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id, String gcCode, String name, String description, String url) {
//        boolean imageLoadError = false;

//        if (!descriptionImagesUpdated) {
//            ip.ProgressChangeMsg("importImages", "Importing Description Images for " + gcCode);
//
//            LinkedList<URI> imgUris = GetImageUris(description, url);
//
//            for (URI uri : imgUris) {
//                try {// for cancel/interupt Thread
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    return 0;
//                }
//
//                if (BreakawayImportThread.isCanceled())
//                    return 0;
//
//                String local = BuildImageFilename(gcCode, uri);
//
//                ip.ProgressChangeMsg("importImages", "Importing Description Images for " + gcCode + " - Download: " + uri);
//
//                // build URL
//                for (int j = 0; j < 1 /* && !parent.Cancel */; j++) {
//                    if (Download(uri.toString(), local)) {
//                        // Next image
//                        DeleteMissingImageInformation(local);
//                        break;
//                    } else {
//                        imageLoadError = HandleMissingImages(imageLoadError, uri, local);
//                    }
//                }
//            }
//
//            descriptionImagesUpdated = true;
//
//            if (!imageLoadError) {
//                Database.Parameters args = new Database.Parameters();
//                args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
//                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
//            }
//        }
//
//        if (!additionalImagesUpdated) {
//            // Get additional images (Spoiler)
//
//            // Liste aller Spoiler Images für diesen Cache erstellen
//            // anhand dieser Liste kann überprüft werden, ob ein Spoiler schon geladen ist und muss nicht ein 2. mal geladen werden.
//            // Außerdem können anhand dieser Liste veraltete Spoiler identifiziert werden, die gelöscht werden können / müssen
//            String[] files = getFilesInDirectory(Config.SpoilerFolder.getValue(), gcCode);
//            String[] filesLocal = getFilesInDirectory(Config.SpoilerFolderLocal.getValue(), gcCode);
//            ArrayList<String> afiles = new ArrayList<String>();
//            for (String file : files)
//                afiles.add(file);
//            for (String file : filesLocal)
//                afiles.add(file);
//
//            {
//                ip.ProgressChangeMsg("importImages", "Importing Spoiler Images for " + gcCode);
//                HashMap<String, URI> allimgDict = new HashMap<String, URI>();
//
//                int result = 0;
//                long startTs = System.currentTimeMillis();
//                do {
//                    result = GroundspeakAPI.GetAllImageLinks(gcCode, allimgDict, null);
//
//                    if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
//                        return GroundspeakAPI.CONNECTION_TIMEOUT;
//                    }
//
//                    if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
//                        return GroundspeakAPI.CONNECTION_TIMEOUT;
//                    }
//                    if (result == 140) {
//                        // API-Limit überschritten -> nach 15 Sekunden wiederholen
//                        System.out.println("******* API-Limit überschritten -> 15 Sekunden warten! *******");
//                        try {
//                            Thread.sleep(15000);
//                        } catch (InterruptedException e) {
//                        }
//                        if (System.currentTimeMillis() > startTs + 60000) {
//                            // Aufruf nach 1 min immer noch nicht OK -> raus!
//                            System.out.println("******* Timeout API-Limit überschritten ********");
//                            break;
//                        }
//                    } else {
//                        break;
//                    }
//                } while (true);
//
//                if (allimgDict == null)
//                    return 0;
//
//                for (String key : allimgDict.keySet()) {
//
//                    try {// for cancel/interupt Thread
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        return 0;
//                    }
//
//                    if (BreakawayImportThread.isCanceled())
//                        return 0;
//
//                    URI uri = allimgDict.get(key);
//                    if (uri.toString().contains("/cache/log/"))
//                        continue; // LOG-Image
//
//                    ip.ProgressChangeMsg("importImages", "Importing Spoiler Images for " + gcCode + " - Download: " + uri);
//
//                    String decodedImageName = key;
//
//                    String local = BuildAdditionalImageFilename(gcCode, decodedImageName, uri);
//                    if (FileFactory.createFile(local).exists()) {
//                        // Spoiler ohne den Hash im Dateinamen löschen
//                        try {
//                            FileFactory.createFile(local).delete();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    // Local Filename mit Hash erzeugen, damit Änderungen der Datei ohne Änderungen des Dateinamens erkannt werden können
//                    // Hier erst die alten Version mit den Klammern als Eingrenzung des Hash
//                    // Dies hier machen, damit die Namen der Spoiler ins neue System Konvertiert werden können.
//                    String localOld = BuildAdditionalImageFilenameHash(gcCode, decodedImageName, uri);
//                    // Neuen Local Filename mit Hash erzeugen, damit Änderungen der Datei ohne Änderungen des Dateinamens erkannt werden können
//                    // Hier jetzt mit @ als Eingrenzung des Hashs
//                    local = BuildAdditionalImageFilenameHashNew(gcCode, decodedImageName, uri);
//                    String filename = local.substring(local.lastIndexOf('/') + 1);
//                    File oldFile = FileFactory.createFile(localOld);
//                    if (oldFile.exists()) {
//                        try {
//                            oldFile.renameTo(FileFactory.createFile(local));
//                            afiles.add(filename);
//                        } catch (Exception ex) {
//                            log.error("Error trying to rename Spoiler with old Name format", ex);
//                        }
//                    }
//
//                    // überprüfen, ob dieser Spoiler bereits geladen wurde
//                    if (afiles.contains(filename)) {
//                        // wenn ja, dann aus der Liste der aktuell vorhandenen Spoiler entfernen und mit dem nächsten Spoiler weiter
//                        // machen
//                        // dieser Spoiler muss jetzt nicht mehr geladen werden da er schon vorhanden ist.
//                        afiles.remove(filename);
//                        continue;
//                    }
//
//                    // build URL
//                    for (int j = 0; j < 1; j++) {
//                        if (Download(uri.toString(), local)) {
//                            // Next image
//                            DeleteMissingImageInformation(local);
//                            break;
//                        } else {
//                            imageLoadError = HandleMissingImages(imageLoadError, uri, local);
//                        }
//
//                    }
//                }
//
//                additionalImagesUpdated = true;
//
//                if (!imageLoadError) {
//                    Database.Parameters args = new Database.Parameters();
//                    args.put("ImagesUpdated", additionalImagesUpdated);
//                    Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(id)});
//                    // jetzt können noch alle "alten" Spoiler gelöscht werden. "alte" Spoiler sind die, die auf der SD vorhanden sind,
//                    // aber
//                    // nicht als Link über die API gemeldet wurden
//                    // Alle Spoiler in der Liste afiles sind "alte"
//                    for (String file : afiles) {
//                        String fileNameWithOutExt = file.replaceFirst("[.][^.]+$", "");
//                        // Testen, ob dieser Dateiname einen gültigen ACB Hash hat (eingeschlossen zwischen @....@>
//                        if (fileNameWithOutExt.endsWith("@") && fileNameWithOutExt.contains("@")) {
//                            // file enthält nur den Dateinamen, nicht den Pfad. Diesen Dateinamen um den Pfad erweitern, in dem hier die
//                            // Spoiler gespeichert wurden
//                            String path = getSpoilerPath(gcCode);
//                            File f = FileFactory.createFile(path + '/' + file);
//                            try {
//                                f.delete();
//                            } catch (Exception ex) {
//                                log.error("DescriptionImageGrabber - GrabImagesSelectedByCache - DeleteSpoiler", ex);
//                            }
//                        }
//                    }
//                }
//
//            }
//        }
//        return 0;
//    }

//    private static String[] getFilesInDirectory(String path, final String GcCode) {
//        String imagePath = path + "/" + GcCode.substring(0, 4);
//        boolean imagePathDirExists = Utils.DirectoryExists(imagePath);
//
//        if (imagePathDirExists) {
//            File dir = FileFactory.createFile(imagePath);
//            FilenameFilter filter = new FilenameFilter() {
//                @Override
//                public boolean accept(File dir, String filename) {
//
//                    filename = filename.toLowerCase();
//                    if (filename.indexOf(GcCode.toLowerCase()) == 0) {
//                        return true;
//                    }
//                    return false;
//                }
//            };
//            String[] files = dir.list(filter);
//            return files;
//        }
//        return new String[0];
//    }

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
    public static String BuildAdditionalImageFilename(String GcCode, String ImageName, URI uri) {
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
     * Alte Version mit den Klammern als Eingrenzung des Hashs. Dies funktioniert nicht, da die Klammern nicht in URL's verwendet werden
     * dürfen (CBServer)
     */
    public static String BuildAdditionalImageFilenameHash(String GcCode, String ImageName, URI uri) {
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
        return imagePath + "/" + GcCode + " - " + ImageName + " ([{" + Utils.sdbm(uri.getPath().toString()) + "}])" + extension;
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

//    private static boolean HandleMissingImages(boolean imageLoadError, URI uri, String local) {
//        try {
//            File file = FileFactory.createFile(local + "_broken_link.txt");
//            if (!file.exists()) {
//                File file2 = FileFactory.createFile(local + ".1st");
//                if (file2.exists()) {
//                    // After first try, we can be sure that the image cannot be loaded.
//                    // At this point mark the image as loaded and go ahead.
//                    file2.renameTo(file);
//                } else {
//                    // Crate a local file for marking it that it could not load one time.
//                    // Maybe the link is broken temporarely. So try it next time once again.
//                    try {
//                        String text = "Could not load image from:" + uri;
//                        BufferedWriter out = new BufferedWriter(new FileWriter(local + ".1st"));
//                        out.write(text);
//                        out.close();
//                        imageLoadError = true;
//                    } catch (IOException e) {
//                        System.out.println("Exception ");
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            // Global.AddLog("HandleMissingImages (uri=" + uri + ") (local=" + local + ") - " + ex.ToString());
//        }
//        return imageLoadError;
//    }

//    private static void DeleteMissingImageInformation(String local) {
//        File file = FileFactory.createFile(local + "_broken_link.txt");
//        if (file.exists()) {
//            try {
//                file.delete();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        file = FileFactory.createFile(local + ".1st");
//        if (file.exists()) {
//            try {
//                file.delete();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
