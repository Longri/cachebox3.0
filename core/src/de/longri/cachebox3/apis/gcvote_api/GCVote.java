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
package de.longri.cachebox3.apis.gcvote_api;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.AbstractCache;
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


    public static Array<AbstractCache> getVotes(String User, String password, ArrayMap<String, AbstractCache> waypoints) {
        Array<AbstractCache> result = new Array<>();

        StringBuilder data = new StringBuilder("userName=" + User + "&password=" + password + "&waypoints=");
        String separator= "";
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

            for (Integer i = 0; i < nodelist.getLength(); i++) {
                Node node = nodelist.item(i);
                // RatingData ratingData = new RatingData();
                String rating = node.getAttributes().getNamedItem("voteAvg").getNodeValue();
                // ratingData.Rating = Float.valueOf(rating);
                String userVote = node.getAttributes().getNamedItem("voteUser").getNodeValue();
                // ratingData.Vote = (userVote == "") ? 0 : Float.valueOf(userVote);
                String GCCode = node.getAttributes().getNamedItem("waypoint").getNodeValue();
                // ratingData.Waypoint = node.getAttributes().getNamedItem("waypoint").getNodeValue();
                AbstractCache changed = waypoints.get(GCCode);
                try {
                    short gotRating = (short) (Double.parseDouble(rating) + 0.5);
                    if (gotRating != changed.getRating()) {
                        changed.setRating(gotRating);
                        result.add(changed);
                    }
                }
                catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            log.error("getVotes", e);
        }
        return result;

    }

    public static boolean sendVote(String User, String password, int vote, String url, String waypoint) {
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

        String data = "userName=" + User + "&password=" + password + "&voteUser=" + String.valueOf(vote / 100.0) + "&cacheId=" + guid + "&waypoint=" + waypoint;

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

}
