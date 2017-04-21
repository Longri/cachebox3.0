/* 
 * Copyright (C) 2013-2017 team-cachebox.de
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

package de.longri.cachebox3.translation;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;


/**
 * @author Longri
 */
public class Translation {
    final static Logger log = LoggerFactory.getLogger(Translation.class);
    /**
     * @uml.property name="that"
     * @uml.associationEnd
     */
    public static Translation that;

    private final String BR;
    private final CB_List<Translations> mStringList;
    private final CB_List<Translations> mRefTranslation;
    public final CB_List<MissingTranslation> mMissingStringList;
    private String mLangID;
    private final String mWorkPath;
    private String mInitialLangPath;

    /**
     * Constructor
     *
     * @param WorkPath
     */
    public Translation(String WorkPath) {
        that = this;
        mWorkPath = WorkPath;
        BR = "\n"; // System.getProperty("line.separator");
        mStringList = new CB_List<Translations>();
        mRefTranslation = new CB_List<Translations>();
        mMissingStringList = new CB_List<MissingTranslation>();
    }

    // #######################################################################
    // Public static access
    // #######################################################################

    /**
     * Load the Translation from File
     *
     * @param LangPath
     * @throws IOException
     */
    public static void LoadTranslation(String LangPath) throws IOException {
        if (that == null)
            return;
        that.mInitialLangPath = LangPath;

        that.ReadTranslationsFile(that.mInitialLangPath);
    }

    /**
     * Returns the translation from StringID </br>
     * with params ??????
     *
     * @param StringId as String
     * @param params   With this a variable number of Strings can be definde Before returning the translation string there will be replaced
     *                 predefined substrings by these parameters Example: the "{1}" will be replaced by the first param, the "{2}" by the
     *                 second... Get("abc {1} def {3} ghi {2}", "123", "456", "789"); Result: "abc 123 def 789 ghi 456"
     * @return String
     */
    public static String Get(String StringId, String... params) {
        if (that == null)
            return "Translation not initial";
        return that.get(StringId, params);
    }

    /**
     * Returns the translation from StringID </br>
     * with params ??????
     *
     * @param hashCode hashCode String as String.hashCode()
     * @param params   With this a variable number of Strings can be defined.<br>
     *                 They replace the corresponding placeholders {*}<br>
     *                 Example:<br>
     *                 Get:("abc {1} def {3} ghi {2}", "123", "456", "789");<br>
     *                 Result: "abc 123 def 789 ghi 456"<br>
     * @return String
     */
    public static String Get(int hashCode, String... params) {
        if (that == null)
            return "Translation not initial";
        return that.get(hashCode, params);
    }

    /**
     * Returns a list of languages found on Path
     *
     * @param FilePath as String
     * @return ArrayList of Lang
     */
    public static ArrayList<Lang> GetLangs(String FilePath) {
        if (that == null)
            return null;
        return that.getLangs(FilePath);
    }

    /**
     * Returns the actual loaded LangID
     *
     * @return LangID as String
     */
    public static String getLangId() {
        if (that == null)
            return "Translation not initial";
        return that.mLangID;
    }

//	/**
//	 * Write list of missing StringId's to debug.txt
//	 *
//	 * @throws IOException
//	 */
//	public static void writeMisingStringsFile() throws IOException {
//		if (that != null)
//			that.writeMissingStrings();
//	}

//	/**
//	 * Read the missing StringID's from debug.txt
//	 */
//	public static void readMissingStringsFile() throws IOException {
//		if (that != null)
//			that.readMissing();
//	}

    /**
     * Return a String from File
     *
     * @param Name File Name
     * @return String from File
     * @throws IOException
     */
    public static String GetTextFile(String Name, String overrideLangId) throws IOException {
        if (that == null)
            return "Translation not initial";
        return that.getTextFile(Name, overrideLangId);
    }

    /**
     * Returns true if Translation initial and reference language is loaded
     *
     * @return boolean
     */
    public static boolean isInitial() {
        if (that != null && that.mRefTranslation != null)
            return true;
        return false;
    }

    // #######################################################################
    // Private access
    // #######################################################################

    private String getLangNameFromFile(String path) throws IOException {

        FileHandle lang = Gdx.files.internal(path);
        String langRead = lang.readString();

        int pos1 = langRead.indexOf("lang=") + 5;
        int pos2 = langRead.indexOf(BR, pos1);

        String Value = langRead.substring(pos1, pos2);
        return Value;
    }

    private void ReadTranslationsFile(String FilePath) throws IOException {
        if (FilePath.equals("")) {
            return;
        }

        readDefFile(FilePath);

        {// read refFile (EN)
            String FileName = Utils.GetFileName(FilePath);
            int pos = FilePath.lastIndexOf("lang") + 4;
            String LangFileName = FilePath.substring(0, pos) + "/en-GB/" + FileName;
            readRefFile(LangFileName);
        }

        mLangID = getLangNameFromFile(FilePath);

        SelectedLangChangedEventList.Call();
    }

    private void readRefFile(String FilePath) {
        readFile(FilePath, false);
    }

    private void readDefFile(String FilePath) {
        readFile(FilePath, true);
    }

    private void readFile(String path, boolean Default) {

        CB_List<Translations> List = Default ? mStringList : mRefTranslation;
        List.clear();

        FileHandle file = Gdx.files.internal(path);

        String text = file.readString("UTF-8");

        String[] lines = text.split("\n");

        for (String line : lines) {
            int pos;

            // skip empty lines
            if (line == "") {
                continue;
            }

            // skip comment line
            pos = line.indexOf("//");
            if (pos > -1) {
                continue;
            }

            // skip line without value
            pos = line.indexOf("=");
            if (pos == -1) {
                continue;
            }

            String readID = line.substring(0, pos).trim();
            String readTransl = line.substring(pos + 1);
            String ReplacedRead = readTransl.trim().replace("\\n", String.format("%n"));

            if (!Default) {
                // dont add if added on Def
                String contains = Get(readID);
                if (contains.startsWith("$ID: "))
                    List.add(new Translations(readID, ReplacedRead));
            } else {
                List.add(new Translations(readID, ReplacedRead));
            }
        }

    }

    private String get(String StringId, String... params) {
        String retString = get(StringId.hashCode(), params);
        if (retString == "") {
            retString = "$ID: " + StringId;// "No translation found";

            MissingTranslation notFound = new MissingTranslation(StringId, "??");
            if (!mMissingStringList.contains(notFound, false)) {
                mMissingStringList.add(notFound);
                log.debug("MissingTranslation: " + notFound.toString());
            }
            return retString;
        }
        return retString;
    }

    private String get(int Id, String... params) {

        if (mStringList == null || mRefTranslation == null)
            return "Translation  not initial";

        String retString = "";
        for (int i = 0, n = mStringList.size; i < n; i++) {
            Translations tmp = mStringList.get(i);
            if (tmp.getIdString() == Id) {
                retString = tmp.getTranslation();
                break;
            }
        }

        if (retString == "") {
            for (int i = 0, n = mRefTranslation.size; i < n; i++) {
                Translations tmp = mRefTranslation.get(i);
                if (tmp.getIdString() == Id) {
                    retString = tmp.getTranslation();
                    break;
                }
            }
        }

        if (retString == "") {
            return retString;
        }

        if (params != null && params.length > 0) {
            retString = replaceParams(retString, params);
        }

        return retString;
    }

    private String replaceParams(String retString, String... params) {
        int i = 1;
        for (String param : params) {
            if ((param.length() >= 1) && (param.charAt(0) == '$'))
                param = Get(param.substring(1));
            retString = retString.replace("{" + i + "}", param);
            i++;
        }
        return retString;
    }

    private ArrayList<Lang> getLangs(String path) {
        ArrayList<Lang> Temp = new ArrayList<Lang>();

        FileHandle Dir = Gdx.files.internal(path);
        final FileHandle[] files;

        if (Dir.type() == FileType.Classpath) {
            // Cannot list a classpath directory
            // so we hardcoded the lang path
            files = new FileHandle[]{ //
                    Gdx.files.internal("data/lang/cs"), //
                    Gdx.files.internal("data/lang/de"), //
                    Gdx.files.internal("data/lang/en-GB"), //
                    Gdx.files.internal("data/lang/fr"), //
                    Gdx.files.internal("data/lang/hu"), //
                    Gdx.files.internal("data/lang/nl"), //
                    Gdx.files.internal("data/lang/pl"), //
                    Gdx.files.internal("data/lang/pt-PT"),//
            };
        } else {
            files = Dir.list();
        }

        for (FileHandle tmp : files) {
            try {

                String stringFile = tmp + "/strings.ini";

                FileHandle langFile = Gdx.files.internal(stringFile);

                if (langFile.exists()) {
                    String tmpName = getLangNameFromFile(stringFile);
                    Temp.add(new Lang(tmpName, stringFile));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return Temp;
    }

//	private void writeMissingStrings() throws IOException {
//		File file = FileFactory.createFile(mWorkPath + "/debug.txt");
//
//		if (file.exists()) {
//
//			BufferedReader reader = new BufferedReader(file.getFileReader());
//			StringBuilder sb = new StringBuilder();
//			String line;
//
//			boolean override = false;
//
//			while ((line = reader.readLine()) != null) {
//				if (!override)
//					sb.append(line + BR);
//				if (line.contains("##########  Missing Lang Strings ######")) {
//					// Beginn des schreibbereichs
//					for (int i = 0, n = mMissingStringList.size(); i < n; i++) {
//						if (i >= mMissingStringList.size())
//							break;
//						MissingTranslation tmp = mMissingStringList.get(i);
//						sb.append(tmp.getMissingString() + BR);
//					}
//					override = true;
//				}
//				if (override && line.contains("############################")) {
//					// jetzt kann weiter gelesen werden
//					override = false;
//					sb.append(line + BR);
//				}
//			}
//			reader.close();
//
//			// zur�ck schreiben
//			PrintWriter writer = new PrintWriter(file.getFileWriter());
//
//			writer.write(sb.toString());
//			writer.close();
//
//		}
//
//	}
//
//	private void readMissing() throws IOException {
//		File file = FileFactory.createFile(mWorkPath + "/debug.txt");
//		BufferedReader reader = new BufferedReader(file.getFileReader());
//		String line;
//
//		boolean read = false;
//
//		while ((line = reader.readLine()) != null) {
//			if (read && line.contains("############################"))
//				break;
//
//			if (read) {
//				MissingTranslation notFound = null;
//				try {
//					notFound = new MissingTranslation(line, "??");
//				} catch (Exception e) {
//				}
//				if (notFound != null && !mMissingStringList.contains(notFound)) {
//					mMissingStringList.add(notFound);
//				}
//			}
//
//			if (line.contains("##########  Missing Lang Strings ######"))
//				read = true;
//
//		}
//		reader.close();
//
//	}

    private String getTextFile(String Name, String overrideLangId) throws IOException {

        String FilePath = "data/string_files/" + Name + "." + overrideLangId + ".txt";
        FileHandle file = Gdx.files.getFileHandle(FilePath, FileType.Internal);
        String text = file.readString();
        return text;
    }

}
