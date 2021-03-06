/*
 * Copyright (C) 2014-2020 team-cachebox.de
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
package de.longri.cachebox3.apis;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.MutableCache;
import de.longri.cachebox3.utils.http.Webb;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;


public class GCVote {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(GCVote.class);
    private final Database database;
    private final String user, password;

    public GCVote(Database database, String user, String password) {
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public void getVotes(ArrayMap<String, AbstractCache> waypoints) {
        if (password.length() > 0) {
            Array<AbstractCache> result = new Array<>();

            StringBuilder data = new StringBuilder("userName=" + user + "&password=" + password + "&waypoints=");
            String separator = "";
            for (String k : waypoints.keys()) {
                data.append(separator).append(k);
                separator = ",";
            }

            try {
                InputStream is = Webb.create()
                        .get("http://gcvote.com/getVotes.php?" + data)
                        .readTimeout(Config.socket_timeout.getValue())
                        .ensureSuccess()
                        .asStream()
                        .getBody();

                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(is);
                is.close();
                NodeList nodelist = doc.getElementsByTagName("vote");
                database.beginTransaction();
                Database.Parameters args = new Database.Parameters();

                for (Integer i = 0; i < nodelist.getLength(); i++) {
                    Node node = nodelist.item(i);
                    String rating = node.getAttributes().getNamedItem("voteAvg").getNodeValue();
                    String vote = node.getAttributes().getNamedItem("voteUser").getNodeValue();
                    String GCCode = node.getAttributes().getNamedItem("waypoint").getNodeValue();
                    MutableCache theCache = (MutableCache) waypoints.get(GCCode);
                    boolean changed = false;
                    try {
                        if (rating.length() > 0) {
                            float ratingFromGCVote = Float.parseFloat(rating);
                            // multiply with 100 would be sufficiant, but for compatibilty with CB2 using 100 * 2
                            float oldRating = theCache.getRating();
                            theCache.setRating(ratingFromGCVote);
                            float newRating = theCache.getRating();
                            if (oldRating != newRating) {
                                changed = true;
                            }
                        }
                        if (vote.length() > 0) {
                            float voteFromGCVote = Float.parseFloat(vote);
                            float oldVote = theCache.getVote();
                            theCache.setVote(voteFromGCVote);
                            float newVote = theCache.getVote();
                            if (oldVote != newVote) {
                                changed = true;
                            }
                        }
                        if (changed) {
                            args.put("Rating", theCache.getRatingInternal());
                            args.put("Vote", theCache.getVoteInternal());
                            database.update("CacheCoreInfo", args, "WHERE id=?", new String[]{Long.toString(theCache.getId())});
                            args.clear();
                        }
                    } catch (Exception ignored) {
                    }
                }
                database.endTransaction();

            } catch (Exception e) {
                log.error("getVotes", e);
            }

        }
    }

    public boolean sendVote(int vote, String url, String waypoint) {
        if (password.length() > 0) {
            url = url.replace("http:", "https:"); // automatic redirect doesn't work from http to https
            int pos = url.indexOf("guid=");
            String guid = "";
            if (pos > -1) {
                guid = url.substring(pos + 5).trim();
            } else {
                // fetch guid from gc : works without login
                try {
                    String page = Webb.create()
                            .get(url)
                            .ensureSuccess()
                            .asString()
                            .getBody();
                    String toSearch = "cache_details.aspx?guid=";
                    pos = page.indexOf(toSearch);
                    if (pos > -1) {
                        int start = pos + toSearch.length();
                        int stop = page.indexOf("\"", start);
                        guid = page.substring(start, stop);
                    }
                } catch (Exception e) {
                    log.error("Send GCVote: Can't get GUID for " + waypoint, e);
                }
            }
            if (guid.length() == 0) return false;

            String data = "userName=" + user + "&password=" + password + "&voteUser=" + String.valueOf(vote / 100.0) + "&cacheId=" + guid + "&waypoint=" + waypoint;

            try {
                String responseString = Webb.create()
                        .get("http://gcvote.com/setVote.php?" + data)
                        .readTimeout(Config.socket_timeout.getValue())
                        .ensureSuccess()
                        .asString()
                        .getBody();
                return responseString.equals("OK");

            } catch (Exception ex) {
                return false;
            }

        }
        return false;
    }

    public boolean isPossible() {
        return password.length() > 0;
    }
}
