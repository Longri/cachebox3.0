/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.types.test_caches;

import de.longri.cachebox3.types.*;

import java.text.ParseException;

/**
 * Created by Longri on 11.04.2018.
 */
public class TestCache_OCF19A extends AbstractTestCache {
    @Override
    protected void setValues() {
        this.latitude = 53.00727;
        this.longitude = 9.00923;
        this.cacheType = CacheTypes.Multi;
        this.gcCode = "OCF19A";
        this.name = "Bierdener Marsch";
        this.available = true;
        this.archived = false;
        this.placed_by = "Danlex";
        this.owner = "Danlex";
        this.container = CacheSizes.small;
        this.url = "http://www.opencaching.de/viewcache.php?cacheid=164939";
        this.difficulty = 2f;
        this.terrain = 2.5f;
        this.country = "Germany";
        this.state = "Niedersachsen";
        this.found = true;
        this.tbCount = 0;
        this.hint = "Start: Bei geformtem in gewachsenem Holz\n" +
                "Final: Am Boden";
        this.favoritePoints = 0;
        this.note = "";
        this.solver = "";
        try {
            this.dateHidden = DATE_PATTERN.parse("2013-01-09T00:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.positiveList.add(Attributes.Hunting);
        this.positiveList.add(Attributes.Thorns);
        this.positiveList.add(Attributes.Ticks);
        this.negativeList.add(Attributes.Recommended_at_night);
        this.negativeList.add(Attributes.Seasonal_Access);

        this.shortDescription = "";
        this.longDescription = "<p>Das Zielgebiet dieses Multis ist die Bierdener Marsch, das Gebiet zwischen dem Achimer Ortsteil Bierden und der Weser. Durch dieses Gebiet führt hier ein gut 4 Kilometer langer Rundweg. Bei Weserhochwasser können hier weite Teile bis an den Deich überschwemmt sein. Verschiedene Bauern bauen hier Getreide oder Raps an oder nutzen die Felder als Weideland für ihre Tiere. So kann man hier unter anderem Pferde, Kühe und Schottische Hochlandrinder antreffen. Manchmal sind auch Rehe zu sehen. An der Weser begegnet man häufig Anglern.<br />Dieses Gebiet ist ideal für Fußgänger mit Hund (welche in der Brut- und Setzzeit jedoch an der Leine geführt werden müssen) und Radfahrer geeignet und wird durch diese auch häufig genutzt.</p>\n" +
                "<p>Zum Cache:</p>\n" +
                "<p>Ihr sucht an den Startkoordinaten nach einer Filmdose, die Euch den weiteren Weg zu zwei Zwischenstationen und anschließend dem Final weist. Wenn Ihr diesen Cache zu Fuß angeht, solltet Ihr für den Weg sowie den Rückweg zum Start etwa eine Stunde einplanen. Es muss nicht weit ab der Wege gesucht werden, sodass hier meist normale Straßenschuhe ausreichen, insbesondere bei und nach Regen sollte aber auf wasser- und matschresistente Kleidung zurückgegriffen werden.</p>\n" +
                "<p><span><strong>Hinweis:</strong> Die Finalkoordinaten haben bei mehreren Messungen etwas variiert. Geht im Zweifelsfall noch einige Schritte weiter.</span></p>\n" +
                "<p>Unterwegs bietet es sich an, den Tradi <a href=\"http://coord.info/GC3WBY5\">Am Konfluenzpunkt</a> mitzunehmen. Wenn Ihr etwas mehr Zeit habt, sei Euch der Mystery <a href=\"http://coord.info/GC3EAHM\">Diebische Elster</a> empfohlen, dessen Startpunkt nicht weit vom Start dieses Caches entfernt ist.<br />(Beide Caches sind leider nur bei GC gelistet)</p>\n" +
                "<p>\n" +
                "<br />\n" +
                "Nachdem Ihr diesen Cache gefunden habt bietet es sich an, die Runde zu komplettieren und noch den <a href=\"http://www.Opencaching.de/OCF1BD\">Bonuscache</a> zu dieser Runde zu suchen.</p>\n" +
                "<p><strong>Achtung:</strong></p>\n" +
                "<p>Hier in der Marsch sind in der Nacht häufig Jäger unterwegs. Daher sollte in der Dunkelheit von einer Suche abgesehen werden.<br />Außerdem solltet Ihr insbesondere an den Stationen auf die Ausläufer der Sträucher, Büsche und Bäume aufpassen, damit Ihr euch nicht am Kopf oder an den Augen verletzt.<br />An der ersten Zwischenstation gebt bitte besonders Acht auf den Stacheldraht!</p><p><em>© <a href='http://www.opencaching.de/viewprofile.php?userid=222261' target='_blank'>Danlex</a>, <a href='http://www.opencaching.de/viewcache.php?cacheid=164939' target='_blank'>Opencaching.de</a>, <a href='http://creativecommons.org/licenses/by-nc-nd/3.0/de/' target='_blank'>CC BY-NC-ND</a>, Stand: 19.07.2014; alle Logeinträge © jeweiliger Autor</em></p>\n" +
                "<p>Dieser Geocache liegt vermutlich in den folgenden Schutzgebieten (<a href=\"http://wiki.opencaching.de/index.php/Schutzgebiete\" target=\"_blank\">Info</a>):</p>\n" +
                "<ul>\n" +
                "<li>Landschaftsschutzgebiet: <a href='http://www.google.de/search?q=Landschaftsschutzgebiet+Achim-Bierdener+Marsch' target='_blank'>Achim-Bierdener Marsch</a></li>\n" +
                "</ul>\n" +
                "<br /><div style=\"float:left; padding:8px\"><a href=\"http://www.opencaching.de/images/uploads/137BADCE-BD7B-11E2-96F3-525400A7F25B.jpg\" target=\"_blank\"><img src=\"http://www.opencaching.de/thumbs.php?uuid=137BADCE-BD7B-11E2-96F3-525400A7F25B\" /></a><br />Bierdener Marsch</div>";
    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(53.00888, 9.00828, this.id);
        wp1.setGcCode("OCF19A-1");
        wp1.setType(CacheTypes.ParkingArea);
        wp1.setTitle("Parkplatz");
        wp1.setDescription("Hier haben einige Cachemobile platz");
        wp1.setClue("");
        wp1.setUserWaypoint(false);
        wp1.setStart(false);
        this.waypoints.add(wp1);

        AbstractWaypoint wp2 = new MutableWaypoint(53.00462, 8.99772, this.id);
        wp2.setGcCode("OCF19A-2");
        wp2.setType(CacheTypes.ReferencePoint);
        wp2.setTitle("Station oder Referenzpunkt");
        wp2.setClue("");
        wp2.setDescription("Auf dieser Bank kann sich ausgeruht werden, sofern notwendig");
        wp2.setUserWaypoint(false);
        wp2.setStart(false);
        this.waypoints.add(wp2);

        AbstractWaypoint wp3 = new MutableWaypoint(52.99973, 9.00903, this.id);
        wp3.setGcCode("OCF19A-3");
        wp3.setType(CacheTypes.ReferencePoint);
        wp3.setTitle("Station oder Referenzpunkt");
        wp3.setDescription("Auf dieser Bank kann sich ausgeruht werden, sofern notwendig");
        wp3.setClue("");
        wp3.setUserWaypoint(false);
        wp3.setStart(false);
        this.waypoints.add(wp3);
        return true;
    }

    @Override
    protected boolean addLogs() throws ParseException {

        LogEntry logEntry1 = new LogEntry();
        logEntry1.CacheId = this.id;
        logEntry1.Finder = "Danlex";
        logEntry1.Type = LogTypes.owner_maintenance;
        logEntry1.Comment = "<p>Ich habe hier eben mal nach dem Rechten gesehen, abgesehen davon, dass hier ganz vieles zugewuchert ist, ist alles im Lot. Der Start und Station 1 waren zu finden, Station 2 die zuletzt nicht gefunden wurde war aber auch vor Ort.</p>\n" +
                "<p>Somit also viel Spaß und Erfolg!</p>";
        logEntry1.Timestamp = DATE_PATTERN.parse("2013-07-07T14:30:00Z");
        logEntry1.Id = 903547L;
        this.logEntries.add(logEntry1);

        LogEntry logEntry2 = new LogEntry();
        logEntry2.CacheId = this.id;
        logEntry2.Finder = "Danlex";
        logEntry2.Type = LogTypes.temporarily_disabled;
        logEntry2.Comment = "<p>Ich werde die Runde mal überprüfen und ggf. auch umbauen, daher ist hier vorerst Pause.</p>";
        logEntry2.Timestamp = DATE_PATTERN.parse("2013-06-16T00:00:00Z");
        logEntry2.Id = 899684L;
        this.logEntries.add(logEntry2);
        return true;
    }
}
