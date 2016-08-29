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

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.CB_Core_Settings;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.GCVoteDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.Database;
import CB_Core.GCVote.GCVote;
import CB_Core.GCVote.GCVoteCacheInfo;
import CB_Core.GCVote.RatingData;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipException;

public class Importer {
	final static org.slf4j.Logger logger = LoggerFactory.getLogger(Importer.class);

	public void importGC(ArrayList<PQ> pqList) {
		ProgresssChangedEventList.Call("import Gc.com", "", 0);

	}

	/**
	 * Importiert die GPX files, die sich in diesem Verzeichniss befinden. Auch wenn sie sich in einem Zip-File befinden. Oder das GPX-File
	 * falls eine einzelne Datei übergeben wird.
	 *
	 * @param directoryPath
	 * @param ip
	 * @return Cache_Log_Return mit dem Inhalt aller Importierten GPX Files
	 */
	public void importGpx(String directoryPath, ImporterProgress ip) {
		// resest import Counter

		GPXFileImporter.CacheCount = 0;
		GPXFileImporter.LogCount = 0;

		// Extract all Zip Files!

		File file = FileFactory.createFile(directoryPath);

		if (file.isDirectory()) {
			ArrayList<File> ordnerInhalt_Zip = FileIO.recursiveDirectoryReader(file, new ArrayList<File>(), "zip", false);

			ip.setJobMax("ExtractZip", ordnerInhalt_Zip.size());

			for (File tmpZip : ordnerInhalt_Zip) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e2) {
					return; // Thread Canceld
				}

				ip.ProgressInkrement("ExtractZip", "", false);
				// Extract ZIP
				try {
					UnZip.extractFolder(tmpZip.getAbsolutePath());
				} catch (ZipException e) {
					logger.error("ZipException", e);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("IOException", e);
					e.printStackTrace();
				}
			}

			if (ordnerInhalt_Zip.size() == 0) {
				ip.ProgressInkrement("ExtractZip", "", true);
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e2) {
				return; // Thread Canceld
			}
		}

		// Import all GPX files
		File[] FileList = GetFilesToLoad(directoryPath);

		ip.setJobMax("AnalyseGPX", FileList.length);

		ImportHandler importHandler = new ImportHandler();

		Integer countwpt = 0;
		HashMap<String, Integer> wptCount = new HashMap<String, Integer>();

		for (File fFile : FileList) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e2) {
				return; // Thread Canceld
			}

			ip.ProgressInkrement("AnalyseGPX", fFile.getName(), false);

			BufferedReader br;
			String strLine;
			try {
				br = new BufferedReader(new InputStreamReader(fFile.getFileInputStream()));
				while ((strLine = br.readLine()) != null) {
					if (strLine.contains("<wpt"))
						countwpt++;
				}
			} catch (FileNotFoundException e1) {

				e1.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			wptCount.put(fFile.getAbsolutePath(), countwpt);
			countwpt = 0;
		}

		if (FileList.length == 0) {
			ip.ProgressInkrement("AnalyseGPX", "", true);
		}

		for (Integer count : wptCount.values()) {
			countwpt += count;
		}

		// Indiziere DB
		CacheInfoList.IndexDB();

		ip.setJobMax("ImportGPX", FileList.length + countwpt);
		for (File File : FileList) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e2) {
				return; // Thread Canceled
			}

			ip.ProgressInkrement("ImportGPX", "Import: " + File.getName(), false);
			GPXFileImporter importer = new GPXFileImporter(File, ip);

			try {
				importer.doImport(importHandler, wptCount.get(File.getAbsolutePath()));
			} catch (Exception e) {
				logger.error("importer.doImport => " + File.getAbsolutePath(), e);
				e.printStackTrace();
			}

		}

		if (FileList.length == 0) {
			ip.ProgressInkrement("ImportGPX", "", true);
		}

		importHandler.GPXFilenameUpdateCacheCount();

		importHandler = null;

		// Indexierte CacheInfos zurück schreiben
		CacheInfoList.writeListToDB();
		CacheInfoList.dispose();

	}

	/**
	 * @param whereClause
	 * @param ip
	 */
	public void importGcVote(String whereClause, ImporterProgress ip) {

		GCVoteDAO gcVoteDAO = new GCVoteDAO();

		ArrayList<GCVoteCacheInfo> pendingVotes = gcVoteDAO.getPendingGCVotes();

		ip.setJobMax("sendGcVote", pendingVotes.size());
		int i = 0;

		for (GCVoteCacheInfo info : pendingVotes) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e2) {
				return; // Thread Canceld
			}

			i++;

			ip.ProgressInkrement("sendGcVote", "Sending Votes (" + String.valueOf(i) + " / " + String.valueOf(pendingVotes.size()) + ")", false);

			Boolean ret = GCVote.SendVotes(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), info.Vote, info.URL, info.GcCode);

			if (ret) {
				gcVoteDAO.updatePendingVote(info.Id);
			}
		}

		if (pendingVotes.size() == 0) {
			ip.ProgressInkrement("sendGcVote", "No Votes to send.", true);
		}

		Integer count = gcVoteDAO.getCacheCountToGetVotesFor(whereClause);

		ip.setJobMax("importGcVote", count);

		int packageSize = 100;
		int offset = 0;
		int failCount = 0;
		i = 0;

		while (offset < count) {
			ArrayList<GCVoteCacheInfo> workpackage = gcVoteDAO.getGCVotePackage(whereClause, packageSize, i);
			ArrayList<String> requests = new ArrayList<String>();
			HashMap<String, Boolean> resetVote = new HashMap<String, Boolean>();
			HashMap<String, Long> idLookup = new HashMap<String, Long>();

			for (GCVoteCacheInfo info : workpackage) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e2) {
					return; // Thread Canceld
				}

				if (!info.GcCode.toLowerCase(Locale.getDefault()).startsWith("gc")) {
					ip.ProgressInkrement("importGcVote", "Not a GC.com Cache", false);
					continue;
				}

				requests.add(info.GcCode);
				resetVote.put(info.GcCode, !info.VotePending);
				idLookup.put(info.GcCode, info.Id);
			}

			ArrayList<RatingData> ratingData = GCVote.GetRating(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), requests);

			if (ratingData == null) {
				failCount += packageSize;
				ip.ProgressInkrement("importGcVote", "Query failed...", false);
			} else {
				for (RatingData data : ratingData) {
					if (idLookup.containsKey(data.Waypoint)) {
						if (resetVote.containsKey(data.Waypoint)) {
							gcVoteDAO.updateRatingAndVote(idLookup.get(data.Waypoint), data.Rating, data.Vote);
						} else {
							gcVoteDAO.updateRating(idLookup.get(data.Waypoint), data.Rating);
						}
					}

					i++;

					ip.ProgressInkrement("importGcVote", "Writing Ratings (" + String.valueOf(i + failCount) + " / " + String.valueOf(count) + ")", false);
				}

			}

			offset += packageSize;

		}

		if (count == 0) {
			ip.ProgressInkrement("importGcVote", "", true);
		}

	}

	/***
	 * @param ip
	 * @return ErrorCode Use with<br>
	 * if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 * {<br>
	 * GL.that.Toast(ConnectionError.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 * <br>
	 * if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 * {<br>
	 * GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 */
	public int importImages(ImporterProgress ip) {

		int ret = 0;

		CacheDAO CacheDao = new CacheDAO();

		// Index DB
		CacheInfoList.IndexDB();

		// get all GcCodes from Listing changed caches without Typ==4 (ErthCache)
		ArrayList<String> gcCodes = CacheDao.getGcCodesFromMustLoadImages();

		// refresh all Image Urls
		ip.setJobMax("importImageUrls", gcCodes.size());
		int counter = 0;
		for (String gccode : gcCodes) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e2) {
				return 0; // Thread Canceld
			}

			if (gccode.toLowerCase(Locale.getDefault()).startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
			{
				// API Zugriff nur mit gültigem API Key
				int retChk = GroundspeakAPI.isValidAPI_Key(true);

				if (retChk > 0) {
					ret = importApiImages(gccode, CacheInfoList.getIDfromGcCode(gccode));
					if (ret < 0)
						return ret;
				}

				ip.ProgressInkrement("importImageUrls", "get Image Urls for " + gccode + " (" + String.valueOf(counter++) + " / " + String.valueOf(gcCodes.size()) + ")", false);
			}
		}

		ImageDAO imageDAO = new ImageDAO();

		// Die Where Clausel sorgt dafür, dass nur die Anzahl der zu ladenden Bilder zurück gegeben wird.
		// Da keine Bilder von ErthCaches geladen werden, wird hier auch der Typ 4 ausgelassen.
		String where = " Type<>4 and (ImagesUpdated=0 or DescriptionImagesUpdated=0)";

		Integer count = imageDAO.getImageCount(where);

		ip.setJobMax("importImages", count);

		if (count == 0) {
			ip.ProgressInkrement("importImages", "", true);
			return 0;
		}

		int i = 0;

		for (String gccode : gcCodes) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e2) {
				return 0; // Thread Canceld
			}

			Boolean downloadedImage = false;
			ArrayList<String> imageURLs = imageDAO.getImageURLsForCache(gccode);

			boolean downloadFaild = false;

			for (String url : imageURLs) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e2) {
					return 0; // Thread Canceld
				}
				String localFile = DescriptionImageGrabber.BuildImageFilename(gccode, URI.create(url));

				if (!FileIO.FileExistsNotEmpty(localFile)) {
					downloadedImage = true;
					if (downloadFaild || !DescriptionImageGrabber.Download(url, localFile)) {
						downloadFaild = true;
					}
				}

				i++;

				if (gccode.toLowerCase(Locale.getDefault()).startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
				{
					ip.ProgressInkrement("importImages", "Importing Images for " + gccode + " (" + String.valueOf(i) + " / " + String.valueOf(count) + ")", false);
				}
			}

			if (downloadedImage) {
				if (gccode.toLowerCase(Locale.getDefault()).startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
				{
					ip.ProgressInkrement("importImages", "Importing Images for " + gccode + " (" + String.valueOf(i) + " / " + String.valueOf(count) + ")", false);
				}

			}
			if (!downloadFaild) {
				// set DescriptionImagesUpdated and ImagesUpdated
				CacheInfoList.setImageUpdated(gccode);
			}
		}

		// Write CacheInfoList back
		CacheInfoList.writeListToDB();
		CacheInfoList.dispose();

		return ret;
	}

	/**
	 * @param ip
	 * @param importImages
	 * @param importSpoiler
	 * @param where         [Last Filter]FilterInstances.LastFilter.getSqlWhere();
	 * @return ErrorCode Use with<br>
	 * if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 * {<br>
	 * GL.that.Toast(ConnectionError.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 * <br>
	 * if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 * {<br>
	 * GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 */
	public int importImagesNew(ImporterProgress ip, boolean importImages, boolean importSpoiler, String where) {

		// refresch all Image Urls

		int ret = 0;

		String sql = "select Id, Description, Name, GcCode, Url, ImagesUpdated, DescriptionImagesUpdated from Caches";
		if (where.length() > 0)
			sql += " where " + where;
		CoreCursor reader = Database.Data.rawQuery(sql, null);

		int cnt = -1;
		int numCaches = reader.getCount();
		ip.setJobMax("importImages", numCaches);

		if (reader.getCount() > 0) {
			reader.moveToFirst();
			while (!reader.isAfterLast()) {
				try {// for cancel/interupt Thread
					Thread.sleep(10);
				} catch (InterruptedException e) {
					return 0;
				}

				if (BreakawayImportThread.isCanceled())
					return 0;

				cnt++;
				long id = reader.getLong(0);
				String name = reader.getString(2);
				String gcCode = reader.getString(3);

				if (gcCode.toLowerCase(Locale.getDefault()).startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
				{
					ip.ProgressInkrement("importImages", "Importing Images for " + gcCode + " (" + String.valueOf(cnt) + " / " + String.valueOf(numCaches) + ")", false);

					boolean additionalImagesUpdated = false;
					boolean descriptionImagesUpdated = false;

					if (!reader.isNull(5)) {
						additionalImagesUpdated = reader.getInt(5) != 0;
					}
					if (!reader.isNull(6)) {
						descriptionImagesUpdated = reader.getInt(6) != 0;
					}

					String description = reader.getString(1);
					String uri = reader.getString(4);

					if (!importImages) {
						// do not import Description Images
						descriptionImagesUpdated = true;
					}
					if (!importSpoiler) {
						// do not import Spoiler Images
						additionalImagesUpdated = true;
					}
					ret = importImagesForCacheNew(ip, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, name, description, uri, false);
				}
				reader.moveToNext();
			}
		}

		reader.close();
		return ret;
	}

	/**
	 * Importiert alle Spoiler Images für einen Cache (über die API-Funktion)
	 *
	 * @param ip
	 * @param Cache
	 * @return ErrorCode Use with<br>
	 * if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 * {<br>
	 * GL.that.Toast(ConnectionError.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 * <br>
	 * if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 * {<br>
	 * GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 */
	public int importSpoilerForCacheNew(ImporterProgress ip, Cache Cache) {
		if (Cache == null)
			return 0;
		return importImagesForCacheNew(ip, true, false, Cache.Id, Cache.getGcCode(), Cache.getName(), "", "", true);
	}

	/**
	 * Bilderimport. Wenn descriptionImagesUpdated oder additionalImagesUpdated == false dann werden die entsprechenden Images importiert
	 * Aber nur dann wenn CheckLocalImages dafür false liefert. wenn importAlways == true -> die Bilder werden unabhängig davon, ob
	 * schon welche existieren importiert
	 *
	 * @return ErrorCode Use with<br>
	 * if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 * {<br>
	 * GL.that.Toast(ConnectionError.INSTANCE);<br>
	 * return;<br>
	 * <br>
	 * <br>
	 * if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 * {<br>
	 * GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 * return;<br>
	 * <br>
	 */
	private int importImagesForCacheNew(ImporterProgress progressReporter, boolean descriptionImagesUpdated, boolean additionalImagesUpdated, long id, String gcCode, String name, String description, String uri, boolean importAlways) {
		boolean dbUpdate = false;

		// if (!importAlways)
		// {
		// 2014-06-19 - Ging-Buh:
		// removed this function because spoiler and images was not imported when at least one image of this cache exists.
		// This is not good because in the DB of each cache is stored whether the images are actual. 
		// If they are not actual the image should be loaded.
		// the changed file is no longer used too. 
		// The information about changed caches is stored in DB
		// if (!descriptionImagesUpdated)
		// {
		// if (CB_Core_Settings.DescriptionImageFolderLocal.getValue().length() > 0)
		// {
		// descriptionImagesUpdated = CheckLocalImages(CB_Core_Settings.DescriptionImageFolderLocal.getValue(), gcCode);
		// }
		// else
		// {
		// descriptionImagesUpdated = CheckLocalImages(CB_Core_Settings.DescriptionImageFolder.getValue(), gcCode);
		// }
		//
		// if (descriptionImagesUpdated)
		// {
		// dbUpdate = true;
		// }
		// }
		// if (!additionalImagesUpdated)
		// {
		// if (CB_Core_Settings.SpoilerFolderLocal.getValue().length() > 0)
		// {
		// additionalImagesUpdated = CheckLocalImages(CB_Core_Settings.SpoilerFolderLocal.getValue(), gcCode);
		// }
		// else
		// {
		// additionalImagesUpdated = CheckLocalImages(CB_Core_Settings.SpoilerFolder.getValue(), gcCode);
		// }
		//
		// if (additionalImagesUpdated)
		// {
		// dbUpdate = true;
		// }
		// }
		// }
		// if (dbUpdate)
		// {
		// Parameters args = new Parameters();
		// args.put("ImagesUpdated", additionalImagesUpdated);
		// args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
		// Database.Data.update("Caches", args, "Id = ?", new String[]
		// { String.valueOf(id) });
		// }

		return DescriptionImageGrabber.GrabImagesSelectedByCache(progressReporter, descriptionImagesUpdated, additionalImagesUpdated, id, gcCode, name, description, uri);
	}

	/**
	 * überprüft, ob für den gewählten Cache die Bilder nicht geladen werden müssen
	 *
	 * @return true wenn schon Images existieren und keine .changed oder .1st Datei ansonsten false
	 */
	// nicht mehr benutzt
	// private boolean CheckLocalImages(String path, final String GcCode)
	// {
	// boolean retval = true;
	//
	// String imagePath = path + "/" + GcCode.substring(0, 4);
	// boolean imagePathDirExists = FileIO.DirectoryExists(imagePath);
	//
	// if (imagePathDirExists)
	// {
	// File dir = FileFactory.createFile(imagePath);
	// FilenameFilter filter = new FilenameFilter()
	// {
	// @Override
	// public boolean accept(File dir, String filename)
	// {
	//
	// filename = filename.toLowerCase(Locale.getDefault());
	// if (filename.indexOf(GcCode.toLowerCase(Locale.getDefault())) == 0)
	// {
	// return true;
	// }
	// return false;
	// }
	// };
	// String[] files = dir.list(filter);
	//
	// if (files.length > 0)
	// {
	// for (String file : files)
	// {
	// if (file.endsWith(".1st") || file.endsWith(".changed"))
	// {
	// if (file.endsWith(".changed"))
	// {
	// File f = FileFactory.createFile(file);
	// try
	// {
	// f.delete();
	// }
	// catch (Exception ex)
	// {
	// }
	// }
	// retval = false;
	// }
	// }
	// }
	// else
	// {
	// retval = false;
	// }
	// }
	// else
	// {
	// retval = false;
	// }
	//
	// return retval;
	// }

	/**
	 * @param GcCode
	 * @param ID
	 * @return ErrorCode Use with<br>
	 * if (result == GroundspeakAPI.CONNECTION_TIMEOUT)<br>
	 * {<br>
	 * GL.that.Toast(ConnectionError.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 * <br>
	 * if (result == GroundspeakAPI.API_IS_UNAVAILABLE)<br>
	 * {<br>
	 * GL.that.Toast(ApiUnavailable.INSTANCE);<br>
	 * return;<br>
	 * }<br>
	 */
	private int importApiImages(String GcCode, long ID) {

		int ret = 0;

		ImportHandler importHandler = new ImportHandler();
		LinkedList<String> allImages = new LinkedList<String>();
		ArrayList<String> apiImages = new ArrayList<String>();

		if (GcCode.toLowerCase(Locale.getDefault()).startsWith("gc")) // Abfragen nur, wenn "Cache" von geocaching.com
		{
			int result = GroundspeakAPI.getImagesForGeocache(GcCode, apiImages, null);

			if (result == GroundspeakAPI.IO) {
				for (String image : apiImages) {
					if (image.contains("/log/"))
						continue; // do not import log-images
					if (!allImages.contains(image))
						allImages.add(image);
				}
				while (allImages != null && allImages.size() > 0) {
					String url;
					url = allImages.poll();

					ImageEntry image = new ImageEntry();

					image.CacheId = ID;
					image.GcCode = GcCode;
					image.Name = url.substring(url.lastIndexOf("/") + 1);
					image.Description = "";
					image.ImageUrl = url;
					image.IsCacheImage = true;

					importHandler.handleImage(image, true);

				}
			}

			if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
				ret = GroundspeakAPI.CONNECTION_TIMEOUT;
			}
			if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
				ret = GroundspeakAPI.API_IS_UNAVAILABLE;
			}
		}
		return ret;

	}

	public void importMaps() {
		ProgresssChangedEventList.Call("import Map", "", 0);

	}

	public void importMail() {
		ProgresssChangedEventList.Call("import from Mail", "", 0);

	}

	private File[] GetFilesToLoad(String directoryPath) {
		ArrayList<File> files = new ArrayList<File>();

		File file = FileFactory.createFile(directoryPath);
		if (file.isFile()) {
			files.add(file);
		} else {
			if (FileIO.DirectoryExists(directoryPath)) {
				files = FileIO.recursiveDirectoryReader(FileFactory.createFile(directoryPath), files);
			}
		}

		File[] fileArray = files.toArray(new File[files.size()]);

		Arrays.sort(fileArray, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {

				if (f1.getName().equalsIgnoreCase(f2.getName().replace(".gpx", "") + "-wpts.gpx")) {
					return 1;
				} else if (f2.getName().equalsIgnoreCase(f1.getName().replace(".gpx", "") + "-wpts.gpx")) {
					return -1;
				} else if (f1.lastModified() > f2.lastModified()) {
					return 1;
				} else if (f1.lastModified() < f2.lastModified()) {
					return -1;
				} else {
					return f1.getAbsolutePath().compareToIgnoreCase(f2.getAbsolutePath()) * -1;
				}
			}
		});

		return fileArray;
	}

}
