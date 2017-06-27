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
package de.longri.cachebox3.types;


import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.lists.CB_List;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class CacheDetail implements Serializable {
    private static final long serialVersionUID = 2088367633865443637L;
    // final static Logger log = LoggerFactory.getLogger(CacheDetail.class);

	/*
     * Public Member
	 */

    /**
     * Erschaffer des Caches
     */
    public String PlacedBy = "";

    /**
     * Datum, an dem der Cache versteckt wurde
     */
    public Date DateHidden;
    /**
     * apiState 0: Cache wurde nicht per Api hinzugefuegt
     * 1: Cache wurde per GC Api hinzugefuegt und ist noch nicht komplett geladen (IsLite = true)
     * 2: Cache wurde per GC Api hinzugefuegt und ist komplett geladen (IsLite = false)
     */
    public byte apiState;

    /**
     * for Replication
     */
    public int noteCheckSum = 0;
    public String tmpNote = null; // nur fuer den RPC-Import

    /**
     * for Replication
     */
    public int solverCheckSum = 0;
    public String tmpSolver = null; // nur fuer den RPC-Import

    /**
     * name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
     */
    public String TourName = "";

    /**
     * name der GPX-Datei aus der importiert wurde
     */
    public long GPXFilename_ID = 0;

    /**
     * URL des Caches
     */
    public String Url = "";

    /**
     * Country des Caches
     */
    public String Country = "";

    /**
     * MapMode des Caches
     */
    public String State = "";

    /**
     * Positive Attribute des Caches
     */
    private DLong attributesPositive = new DLong(0, 0);

    /**
     * Negative Attribute des Caches
     */
    private DLong attributesNegative = new DLong(0, 0);

    /**
     * Hinweis fuer diesen Cache
     */
    private String hint = "";

    /**
     * Liste der Spoiler Ressourcen
     */
    private CB_List<ImageEntry> spoilerRessources = null;

    /**
     * Kurz Beschreibung des Caches
     */
    public String shortDescription;

    /**
     * Ausfuehrliche Beschreibung des Caches Nur fuer Import Zwecke. Ist normalerweise leer, da die Description bei aus Speicherplatz
     * Gruenden bei Bedarf aus der DB geladen wird
     */
    public String longDescription;

	/*
	 * Constructors
	 */

    /**
     * Constructor
     */
    public CacheDetail() {
        this.DateHidden = new Date();
    }

    public void dispose() {
        // clear all Lists
        
        if (spoilerRessources != null) {
            for (int i = 0, n = spoilerRessources.size; i < n; i++) {
                ImageEntry entry = spoilerRessources.get(i);
                entry.dispose();
            }
            spoilerRessources.clear();
            spoilerRessources = null;
        }

        // if (waypoints != null)
        // {
        // for (int i = 0, n = waypoints.size(); i < n; i++)
        // {
        // Waypoint entry = waypoints.get(i);
        // entry.dispose();
        // }
        //
        // waypoints.clear();
        // }

        tmpNote = null;
        tmpSolver = null;
        TourName = null;
        PlacedBy = null;
        // setOwner(null);
        DateHidden = null;
        Url = null;
        Country = null;
        State = null;
        // setHint(null);
        shortDescription = null;
        longDescription = null;

    }

    public boolean isAttributePositiveSet(Attributes attribute) {
        return attributesPositive.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
        // return (attributesPositive & Attributes.GetAttributeDlong(attribute))
        // > 0;
    }

    public boolean isAttributeNegativeSet(Attributes attribute) {
        return attributesNegative.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
        // return (attributesNegative & Attributes.GetAttributeDlong(attribute))
        // > 0;
    }

    public void addAttributeNegative(Attributes attribute) {
        if (attributesNegative == null)
            attributesNegative = new DLong(0, 0);
        attributesNegative.BitOr(Attributes.GetAttributeDlong(attribute));
    }

    public void addAttributePositive(Attributes attribute) {
        if (attributesPositive == null)
            attributesPositive = new DLong(0, 0);
        attributesPositive.BitOr(Attributes.GetAttributeDlong(attribute));
    }

    public void setAttributesPositive(DLong i) {
        attributesPositive = i;
    }

    public void setAttributesNegative(DLong i) {
        attributesNegative = i;
    }

    public DLong getAttributesNegative(long Id) {
        if (this.attributesNegative == null) {
            SQLiteGdxDatabaseCursor c = Database.Data.rawQuery("select AttributesNegative,AttributesNegativeHigh from Caches where Id=?", new String[]{String.valueOf(Id)});
            c.moveToFirst();
            while (!c.isAfterLast()) {
                if (!c.isNull(0))
                    this.attributesNegative = new DLong(c.getLong(1), c.getLong(0));
                else
                    this.attributesNegative = new DLong(0, 0);
                break;
            }
            ;
            c.close();
        }
        return this.attributesNegative;
    }

    public DLong getAttributesPositive(long Id) {
        if (this.attributesPositive == null) {
            SQLiteGdxDatabaseCursor c = Database.Data.rawQuery("select AttributesPositive,AttributesPositiveHigh from Caches where Id=?", new String[]{String.valueOf(Id)});
            c.moveToFirst();
            while (!c.isAfterLast()) {
                if (!c.isNull(0))
                    this.attributesPositive = new DLong(c.getLong(1), c.getLong(0));
                else
                    this.attributesPositive = new DLong(0, 0);
                break;
            }
            ;
            c.close();
        }
        return this.attributesPositive;
    }

    public ArrayList<Attributes> getAttributes(long Id) {
        return Attributes.getAttributes(this.getAttributesPositive(Id), this.getAttributesNegative(Id));
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint2) {
        this.hint = hint2;

    }

    /**
     * Returns a List of Spoiler Ressources
     *
     * @return ArrayList of String
     */
    public CB_List<ImageEntry> getSpoilerRessources(Cache cache) {
        if (spoilerRessources == null) {
            //loadSpoilerRessources(cache);
        }

        return spoilerRessources;
    }

    /**
     * Set a List of Spoiler Ressources
     *
     * @param value ArrayList of String
     */
    public void setSpoilerRessources(CB_List<ImageEntry> value) {
        spoilerRessources = value;
    }

    /**
     * Returns true has the Cache Spoilers else returns false
     *
     * @return Boolean
     */
    public boolean hasSpoiler(Cache cache) {
        try {
            if (spoilerRessources == null)
                return false;

//				loadSpoilerRessources(cache);
            return spoilerRessources.size > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void SpoilerForceReEvaluate(Cache cache) {
        spoilerRessources = null;
    }

//	public void loadSpoilerRessources(Cache cache) {
//
//		String gcCode = cache.getGcCode();
//		if (gcCode.length() < 4)
//			return;
//
//		spoilerRessources = new CB_List<ImageEntry>();
//
//		synchronized (spoilerRessources) {
//			String directory = "";
//
//			// from own Repository
//			String path = Config.SpoilerFolderLocal.getValue();
//			// Log.debug(log, "from SpoilerFolderLocal: " + path);
//			try {
//				if (path != null && path.length() > 0) {
//					directory = path + "/" + gcCode.substring(0, 4);
//					loadSpoilerResourcesFromPath(directory, cache);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			// from Description own Repository
//			try {
//				path = Config.DescriptionImageFolderLocal.getValue();
//				// Log.debug(log, "from DescriptionImageFolderLocal: " + path);
//				directory = path + "/" + gcCode.substring(0, 4);
//				loadSpoilerResourcesFromPath(directory, cache);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			// from Description Global Repository
//			try {
//				path = Config.DescriptionImageFolder.getValue();
//				// Log.debug(log, "from DescriptionImageFolder: " + path);
//				directory = path + "/" + gcCode.substring(0, 4);
//				loadSpoilerResourcesFromPath(directory, cache);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			// from Spoiler Global Repository
//			try {
//				path = Config.SpoilerFolder.getValue();
//				// Log.debug(log, "from SpoilerFolder: " + path);
//				directory = path + "/" + gcCode.substring(0, 4);
//				loadSpoilerResourcesFromPath(directory, cache);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			// add own taken photo
//			directory = Config.UserImageFolder.getValue();
//			// Log.debug(log, "from UserImageFolder: " + directory);
//			if (directory != null) {
//				try {
//					loadSpoilerResourcesFromPath(directory, cache);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

//	private void loadSpoilerResourcesFromPath(String directory, final Cache cache) {
//		if (!FileIO.DirectoryExists(directory))
//			return;
//		File dir = FileFactory.createFile(directory);
//		FilenameFilter filter = new FilenameFilter() {
//			@Override
//			public boolean accept(File dir, String filename) {
//				filename = filename.toLowerCase(Locale.getDefault());
//				if (filename.indexOf(cache.getGcCode().toLowerCase(Locale.getDefault())) >= 0) {
//					if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif")) {
//						// don't load Thumbs
//						if (filename.startsWith(FileFactory.THUMB) || filename.startsWith(FileFactory.THUMB_OVERVIEW + FileFactory.THUMB)) {
//							return false;
//						} else {
//							return true;
//						}
//					}
//				}
//				return false;
//			}
//		};
//		String[] files = dir.list(filter);
//		if (!(files == null)) {
//			if (files.length > 0) {
//				for (String file : files) {
//					String ext = FileIO.GetFileExtension(file);
//					if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif")) {
//						ImageEntry imageEntry = new ImageEntry();
//						imageEntry.LocalPath = directory + "/" + file;
//						imageEntry.name = file;
//						// Log.debug(log, imageEntry.name);
//						spoilerRessources.add(imageEntry);
//					}
//				}
//			}
//		}
//	}

    public void setLongDescription(String value) {
        longDescription = value;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setShortDescription(String value) {
        shortDescription = value;
    }

    public String getShortDescription() {
        return shortDescription;
    }

}
