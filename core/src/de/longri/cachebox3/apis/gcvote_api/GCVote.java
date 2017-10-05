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

import com.badlogic.gdx.Net;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NetUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;


public class GCVote {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GCVote.class);

    public static RatingData getRating(String user, String password, String waypoint, ICancel icancel) {
        ArrayList<String> waypointList = new ArrayList<String>();
        waypointList.add(waypoint);
        ArrayList<RatingData> result = getRating(user, password, waypointList, icancel);

        if (result == null || result.size() == 0) {
            return new RatingData();
        } else {
            return result.get(0);
        }

    }

    public static ArrayList<RatingData> getRating(String User, String password, ArrayList<String> Waypoints, ICancel icancel) {
        ArrayList<RatingData> result = new ArrayList<RatingData>();

        String data = "userName=" + User + "&password=" + password + "&waypoints=";
        for (int i = 0; i < Waypoints.size(); i++) {
            data += Waypoints.get(i);
            if (i < (Waypoints.size() - 1))
                data += ",";
        }


        try {

            Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
            httpPost.setUrl("http://gcvote.de/getVotes.php");
            httpPost.setTimeOut(Config.socket_timeout.getValue());
            httpPost.setContent(data);


            String responseString = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpPost, icancel);

            log.debug("GCVOTE-Response: ", responseString);

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(responseString));

            Document doc = db.parse(is);

            NodeList nodelist = doc.getElementsByTagName("vote");

            for (Integer i = 0; i < nodelist.getLength(); i++) {
                Node node = nodelist.item(i);

                RatingData ratingData = new RatingData();
                ratingData.Rating = Float.valueOf(node.getAttributes().getNamedItem("voteAvg").getNodeValue());
                String userVote = node.getAttributes().getNamedItem("voteUser").getNodeValue();
                ratingData.Vote = (userVote == "") ? 0 : Float.valueOf(userVote);
                ratingData.Waypoint = node.getAttributes().getNamedItem("waypoint").getNodeValue();
                result.add(ratingData);

            }

        } catch (Exception e) {
            String Ex = "";
            if (e != null) {
                if (e != null && e.getMessage() != null)
                    Ex = "Ex = [" + e.getMessage() + "]";
                else if (e != null && e.getLocalizedMessage() != null)
                    Ex = "Ex = [" + e.getLocalizedMessage() + "]";
                else
                    Ex = "Ex = [" + e.toString() + "]";
            }
            log.error("GcVote-Error", Ex);
            return null;
        }
        return result;

    }

    public static Boolean sendVotes(String User, String password, int vote, String url, String waypoint, ICancel icancel) {
        String guid = url.substring(url.indexOf("guid=") + 5).trim();

        String data = "userName=" + User + "&password=" + password + "&voteUser=" + String.valueOf(vote / 100.0) + "&cacheId=" + guid + "&waypoint=" + waypoint;

        try {
            Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
            httpPost.setUrl("http://gcvote.de/getVotes.php");
            httpPost.setTimeOut(Config.socket_timeout.getValue());
            httpPost.setContent(data);


            String responseString = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpPost, icancel);

            return responseString.equals("OK\n");

        } catch (Exception ex) {
            return false;
        }
    }
}
