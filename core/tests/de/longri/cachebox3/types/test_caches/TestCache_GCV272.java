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
 * Created by Longri on 10.04.2018.
 */
public class TestCache_GCV272 extends AbstractTestCache {
    @Override
    protected void setValues() {
        this.latitude = 52.564783;
        this.longitude = 13.393233;
        this.cacheType = CacheTypes.Multi;
        this.gcCode = "GCV272";
        this.name = "Wollankstraße (Berlin)";
        this.available = true;
        this.archived = false;
        this.placed_by = "oldfield";
        this.owner = "oldfield";
        this.container = CacheSizes.micro;
        this.url = "http://www.geocaching.com/seek/cache_details.aspx?guid=c3c2f1df-6632-4f5f-b2ba-d3aa19d862ee";
        this.difficulty = 2.5f;
        this.terrain = 1.5f;
        this.country = "Germany";
        this.state = "Berlin";
        this.found = false;
        this.tbCount = 0;
        this.hint = "\n      ";
        this.note = "";
        this.solver = "";
        try {
            this.dateHidden = DATE_PATTERN.parse("2006-03-25T08:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.negativeList.add(Attributes.Takes_less_than_an_hour);
        this.positiveList.add(Attributes.Bicycles);
        this.positiveList.add(Attributes.Available_at_all_times);
        this.positiveList.add(Attributes.Public_transportation);
        this.positiveList.add(Attributes.Wheelchair_accessible);
        this.positiveList.add(Attributes.Needs_maintenance);
        this.positiveList.add(Attributes.Recommended_for_kids);

        this.shortDescription = "<p>Ein Spaziergang führt Euch durch die Geschichte der\n" +
                "Wollankstraße in Berlin, die auch durch die Berliner Mauer stark\n" +
                "geprägt wurde. Die angegebenen Koordinaten markieren den\n" +
                "Ausgangspunkt des Spazierganges, den S-Bahnhof Wollankstraße.</p>\n" +
                "<!-- Ende Kurzbeschreibung -->";

        this.longDescription = "<p align=\"justify\">Zunächst folgt eine kleine Beschreibung zur\n" +
                "   Geschichte dieser Straße, das ist jedoch keine\n" +
                "   Pflichtlektüre.<br>\n" +
                "Zum Loggen des Caches genügt die Beantwortung aller sieben Fragen.\n" +
                "Das dürfte nach einem Spaziergang von ca. 45 Minuten und einer\n" +
                "kurzen Denkpause nicht schwer fallen. Ersetzt die fehlenden\n" +
                "Variablen und errechnet mit der unten aufgeführten Formel die\n" +
                "Zielkoordinate.</p>\n" +
                "<font color=\"red\">Bitte achtet darauf, dass der Cache immer wieder\n" +
                "genau an dieser Stelle befestigt wird und<br>\n" +
                "dadurch immer mit einem SCHNELLEN und BEQUEMEN Zugriff zu loggen\n" +
                "ist!!!</font> <!-- Beginn Geschichte -->\n" +
                "<p>&nbsp;</p>\n" +
                "<p><b>Geschichte</b></p>\n" +
                "<p align=\"justify\">Die Berliner Wollankstraße ist eine ganz normale\n" +
                "Straße in Berlin. Wirklich eine ganz normale Straße? Auf den\n" +
                "zweiten Blick ist sie es tatsächlich nicht, denn es gibt viele\n" +
                "Kleinode und Geschichten, die sich dem Betrachter erst auf den\n" +
                "zweiten Blick erschließen.</p>\n" +
                "<p align=\"justify\">Die Straße ist seit 1882 nach dem Gutsbesitzer\n" +
                "Adolf Friedrich Wollank (1833-1877) benannt. Vorher trug sie die\n" +
                "Namen Prinzenweg (1703-1877) und Prinzenstraße (1877-1882).</p>\n" +
                "<p align=\"justify\">Durch diese Straße führte ab dem Jahre 1895 die\n" +
                "erste elektrische Berliner Straßenbahn von Siemens und Halske vom\n" +
                "Gesundbrunnen bis nach Pankow. Im Jahre 1938 wird im Rahmen\n" +
                "Grenzbereinigung der südliche Teil der Wollankstraße dem Bezirk\n" +
                "Wedding (heute Mitte) zugeschlagen. Dies stieß zu dieser Zeit nicht\n" +
                "auf die Gegenliebe der Bewohner, denn der Bezirk Pankow ist eher\n" +
                "bürgerlich geprägt, während der Wedding ein Arbeiterbezirk ist.</p>\n" +
                "<p align=\"justify\">Das villenartige Empfangsgebäude des 1903\n" +
                "errichteten Bahnhofes „Pankow Nordbahn“ befindet sich nicht an der\n" +
                "Wollankstraße, sondern etwa einhundert Meter weiter westlich an der\n" +
                "Nordbahnstraße. Die elektrische S-Bahn hält hier bereits seit dem\n" +
                "5. Juni 1925. Den heutigen Namen „Wollankstraße“ trägt dieser\n" +
                "Bahnhof erst seit dem Jahr 1937.</p>\n" +
                "<p align=\"justify\">Die Teilung Berlins nach dem zweiten Weltkrieg\n" +
                "und der Bau der Mauer 1961 prägen die Straße bis zum heutigen Tage.\n" +
                "Da die Bezirksgrenzen maßgeblich für die Aufteilung der Stadt in\n" +
                "Besatzungszonen waren, fiel der nördliche Pankower Teil der\n" +
                "Wollankstraße in die sowjetische Zone, während der Weddinger Teil\n" +
                "der französischen Besatzungszone zugeordnet wurde. Im Weddinger\n" +
                "Teil der Wollankstraße wurde die Straßenbahn 1960 stillgelegt, im\n" +
                "Pankower Teil bereits 1953.</p>\n" +
                "<p align=\"justify\">Mit dem Bau der Mauer 1961 wurde die\n" +
                "Straßenunterführung an der die S-Bahn-Brücke von den\n" +
                "DDR-Grenztruppen verbarrikadiert. Bis zur Maueröffnung sollte diese\n" +
                "Durchfahrt hermetisch abgeriegelt bleiben. Es gab in diesem Bereich\n" +
                "keinen Grenzübergang. Wer von Pankow nach Wedding wollte, riskierte\n" +
                "an dieser Stelle sein Leben. Es wird berichtet, dass am 27.01.1962\n" +
                "durch Risse und Absenkungen auf dem S-Bahnsteig ein von Wedding in\n" +
                "Richtung Pankow vorgetriebener Fluchttunnel entdeckt wurde, bevor\n" +
                "er zur Flucht benutzt werden konnte. Obwohl der S-Bahnhof und der\n" +
                "gesamte Bahndamm in vollem Umfang zu Pankow gehört, war er für die\n" +
                "Zeit der Mauer ausschließlich für West-Berliner zugänglich.</p>\n" +
                "<p align=\"justify\">In den achtziger Jahren wurde die Wollankstraße\n" +
                "im Wedding zurückgebaut und umgestaltet. Dies geschah auch deshalb,\n" +
                "da diese Straße an der Mauer am S-Bahnhof endete und nicht mehr als\n" +
                "Durchgangsstraße fungierte. Die letzten Überreste der\n" +
                "Straßenbahnschienen in Pankow wurden erst nach dem Jahre 2000\n" +
                "entfernt. Durch die unterschiedlichen Entwicklungen vor allen\n" +
                "während der Zeit der Teilung geben die Straßenhälften ein sehr\n" +
                "unterschiedliches Bild ab.</p>\n" +
                "<p align=\"justify\">Von den Grenzanlagen der Berliner Mauer ist\n" +
                "heute nur noch wenig zu erkennen. Nur der Todesstreifen, der\n" +
                "östlich des Bahndammes verlief, ist noch zu erahnen.</p>\n" +
                "<br>\n" +
                " <!-- Ende Geschichte -->\n" +
                " \n" +
                "<p><b>&nbsp;</b></p>\n" +
                "<table cellpadding=\"0\" cellspacing=\"0\" width=\"99%\">\n" +
                "<tr>\n" +
                "<td width=\"180\" bgcolor=\"#FFFF99\" style=\n" +
                "\"text-align:left; text-indent:6px; margin:0; border-width:1px; border-color:black; border-top-style:solid; border-right-style:none; border-bottom-style:solid; border-left-style:none;\">\n" +
                "<p><b>Station 1</b></p></td>\n" +
                "<td width=\"1058\" bgcolor=\"#FFFF99\" style=\n" +
                "\"margin:0; border-width:1px; border-color:black; border-top-style:solid; border-right-style:none; border-bottom-style:solid; border-left-style:none;\">\n" +
                "<p>N 52° 33.887 E 013° 23.594</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\">Am S-Bahnhof ist ein Schild befestigt.\n" +
                "Welche Höhenangabe üNN ist hier angegeben?<br>\n" +
                "<br>\n" +
                "Lösung: 4A,3B4<br>\n" +
                "&nbsp;</td></tr>\n" +
                "<tr>\n" +
                "<td width=\"180\" height=\"16\" bgcolor=\"#FFFF99\" style=\n" +
                "\"text-align:left; text-indent:6px; border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p><b>Station 2</b></p></td>\n" +
                "<td width=\"1058\" height=\"16\" bgcolor=\"#FFFF99\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "N 52° 33.622 E 013° 23.266</td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\" height=\"16\">\n" +
                "<p>In der Wollankstraße befinden sich zwei Friedhöfe. Gehe zum\n" +
                "südlichen Eingang des St. Elisabeth-Friedhofs in der Wollankstraße.\n" +
                "Neben dem sehenswerten Eingangsportal des Friedhofs befindet sich\n" +
                "eine Blumenhandlung. Wann würde dieses Geschäft gegründet?<br>\n" +
                "<br>\n" +
                " Lösung: 1C7D<br>\n" +
                "&nbsp;</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"180\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p><b>Station 3</b></p></td>\n" +
                "<td width=\"1058\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p>N 52° 33.724 E 013° 23.382</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\" height=\"16\">\n" +
                "<p>Die östliche Straßenseite in Mitte ist zu großen Teilen von der\n" +
                "Wohnungsbaugenossenschaft „Vaterländischer Bauverein“ bebaut\n" +
                "worden. Wann wurden die Häuser Wollankstraße 75-83b erbaut?<br>\n" +
                "<br>\n" +
                "Lösung: 1EF6<br>\n" +
                "&nbsp;</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"180\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p><b>Station 4</b></p></td>\n" +
                "<td width=\"1058\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p>N 52° 33.795 E 013° 23.458</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\" height=\"16\">\n" +
                "<p>Etwas weiter nördlich folgt ein Kirchengemeindehaus. Durch den\n" +
                "Bau der Mauer 1961 wurde der Weddinger Teil der\n" +
                "Martin-Luther-Gemeinde von ihrem Gemeindehaus in der Pradelstraße\n" +
                "in Pankow getrennt. Das Haus, vor dem ihr jetzt steht, wurde 1962\n" +
                "als Behelfsbau neu errichtet. Wer hat diesen Neubau und den Bau\n" +
                "einer Kirche (inzwischen abgerissen) gesponsort?<br>\n" +
                "<br>\n" +
                "Lösung:<br>\n" +
                "- US-Präsident J.F.Kennedy (G=9) oder<br>\n" +
                "- Berliner Bürgermeister Willy Brandt (G=7) oder<br>\n" +
                "- schwedische Gräfin Lili Hamilton (G=4)</p>\n" +
                "<p>Tipp: ganz in der Nähe findet ihr einen kleinen Hinweis...<br>\n" +
                "&nbsp;</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"180\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p><b>Station 5</b></p></td>\n" +
                "<td width=\"1058\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p>N 52° 33.882 E 013° 23.616</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\">\n" +
                "<p>Nun könnt ihr die Straßenseite wechseln und die ehemalige\n" +
                "Sektorengrenze überschreiten, in dem ihr die S-Bahn-Brücken\n" +
                "unterquert. Doch halt! Im Boden ist eine kleine Gedenktafel\n" +
                "eingelassen. Übrigens an der falschen Stelle, sie müsste auf der\n" +
                "südlichen Seite des Bahndammes im Boden versenkt sein! Merkt Euch\n" +
                "bitte die vorletzte Ziffer dieser Inschrift.<br>\n" +
                "<br>\n" +
                "Lösung: H<br>\n" +
                "&nbsp;</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"180\" bgcolor=\"#FFFF99\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p><b>Station 6</b></p></td>\n" +
                "<td width=\"1058\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p>N 52° 33.944 E 013° 23.721</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\">\n" +
                "<p>Einige Meter weiter befindet sich das Franziskanerkloster. 1921\n" +
                "erwarben die Franziskaner von Breslau hier zwei Grundstücke, um\n" +
                "sich niederzulassen. Die ärmsten der Stadt können sich hier eine\n" +
                "warme Mahlzeit holen, sich waschen oder sich neu einkleiden lassen.\n" +
                "Im Vorgarten befindet sich eine Glocke, die täglich zum Gebet\n" +
                "aufruft. Nenne die erste Ziffer der Hausnummer.<br>\n" +
                "<br>\n" +
                "Lösung: J<br>\n" +
                "&nbsp;</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"180\" bgcolor=\"#FFFF99\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p><b>Station 7</b></p></td>\n" +
                "<td width=\"1058\" bgcolor=\"#FFFF99\" height=\"16\" style=\n" +
                "\"border-width:1px; border-color:black; border-top-style:solid; border-bottom-style:solid;\">\n" +
                "<p>N 52° 34.110 E 013° 23.979</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"1238\" colspan=\"2\">\n" +
                "<p>Hier findet Ihr ein sehr altes, wenn nicht sogar das älteste\n" +
                "Gebäude in dieser Straße. Die Anlage wurde um 1860 erbaut und ab\n" +
                "1875 betrieb Carl Hartmann hier ein Geschäft. Um welches Gewerbe\n" +
                "handelte es sich?<br>\n" +
                "<br>\n" +
                "Lösung:<br>\n" +
                "- eine Bank (K=3) oder<br>\n" +
                "- eine Bäckerei (K=2) oder<br>\n" +
                "- ein Museum (K=7)<br>\n" +
                "&nbsp;</p></td></tr></table>\n" +
                "<p><!-- Beginn Lösungstabelle -->\n" +
                "</p>\n" +
                "<table border=\"1\" cellspacing=\"0\" bordercolordark=\"white\"\n" +
                "bordercolorlight=\"black\">\n" +
                "<tr>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">A</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">B</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">C</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">D</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">E</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">F</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">G</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">H</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">J</p></td>\n" +
                "<td width=\"45\" height=\"25\" bgcolor=\"#FFFF99\">\n" +
                "<p align=\"center\">K</p></td></tr>\n" +
                "<tr>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\"><b>&nbsp;</b></p></td>\n" +
                "<td width=\"45\" height=\"25\">\n" +
                "<p align=\"center\">&nbsp;</p></td></tr></table>\n" +
                "<!-- Ende Lösungstabelle -->\n" +
                "<br>\n" +
                "<p>Die Zielkoordinate errechnet sich wie folgt:</p>\n" +
                "<b><br></b>N 52° 34'(F+H-8)(D+K)(E-9)''<br>\n" +
                "E 13° 23'(C)(G+J+1)(A+B+D)'' <br>\n" +
                "<br>\n" +
                "<br>\n" +
                "<br>\n" +
                "<!-- Beginn Zähler -->\n" +
                " <a rel=\"nofollow\" href=\"http://www.andyhoppe.com/\" target=\"_blank\"\n" +
                "title=\"Counter\"><img src=\n" +
                "\"http://www.andyhoppe.com/count/?id=1143359603\" border=\"0\"\n" +
                "     alt=\"Counter\"></a> <br>\n" +
                " <!-- Ende Zähler -->\n" +
                "<br>\n" +
                "<br>\n" +
                "<br>\n" +
                " \n" +
                "<p>&nbsp;</p>\n" +
                "<!-- Ende Cachebeschreibung --><p>Additional Waypoints</p>AAV272 - Station 1<br />N 52° 33.888 E 013° 23.594<br /><br />ABV272 - Station 2<br />N 52° 33.622 E 013° 23.266<br /><br />ACV272 - Station 3<br />N 52° 33.724 E 013° 23.382<br /><br />ADV272 - Station 4<br />N 52° 33.795 E 013° 23.458<br /><br />AEV272 - Station 5<br />N 52° 33.882 E 013° 23.616<br /><br />AFV272 - Station 6<br />N 52° 33.944 E 013° 23.721<br /><br />AGV272 - Station 7<br />N 52° 34.110 E 013° 23.979<br /><br />";
    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(52.5648, 13.393233, this.id);
        wp1.setGcCode("AAV272");
        wp1.setType(CacheTypes.MultiStage);
        wp1.setTitle("Station 1");
        wp1.setClue("");
        wp1.setUserWaypoint(false);
        wp1.setStart(false);
        this.waypoints.add(wp1);

        AbstractWaypoint wp2 = new MutableWaypoint(52.560367, 13.387767, this.id);
        wp2.setGcCode("ABV272");
        wp2.setType(CacheTypes.MultiStage);
        wp2.setTitle("Station 2");
        wp2.setClue("");
        wp2.setUserWaypoint(false);
        wp2.setStart(false);
        this.waypoints.add(wp2);

        AbstractWaypoint wp3 = new MutableWaypoint(52.562067, 13.3897, this.id);
        wp3.setGcCode("ACV272");
        wp3.setType(CacheTypes.MultiStage);
        wp3.setTitle("Station 3");
        wp3.setClue("");
        wp3.setUserWaypoint(false);
        wp3.setStart(false);
        this.waypoints.add(wp3);

        AbstractWaypoint wp4 = new MutableWaypoint(52.56325, 13.390967, this.id);
        wp4.setGcCode("ADV272");
        wp4.setType(CacheTypes.MultiStage);
        wp4.setTitle("Station 4");
        wp4.setClue("");
        wp4.setUserWaypoint(false);
        wp4.setStart(false);
        this.waypoints.add(wp4);

        AbstractWaypoint wp5 = new MutableWaypoint(52.5647, 13.3936, this.id);
        wp5.setGcCode("AEV272");
        wp5.setType(CacheTypes.MultiStage);
        wp5.setTitle("Station 5");
        wp5.setClue("");
        wp5.setUserWaypoint(false);
        wp5.setStart(false);
        this.waypoints.add(wp5);

        AbstractWaypoint wp6 = new MutableWaypoint(52.565733, 13.39535, this.id);
        wp6.setGcCode("AFV272");
        wp6.setType(CacheTypes.MultiQuestion);
        wp6.setTitle("Station 6");
        wp6.setClue("");
        wp6.setUserWaypoint(false);
        wp6.setStart(false);
        this.waypoints.add(wp6);

        AbstractWaypoint wp7 = new MutableWaypoint(52.5685, 13.39965, this.id);
        wp7.setGcCode("AGV272");
        wp7.setType(CacheTypes.MultiStage);
        wp7.setTitle("Station 7");
        wp7.setClue("");
        wp7.setUserWaypoint(false);
        wp7.setStart(false);
        this.waypoints.add(wp7);

        return true;
    }

    @Override
    protected boolean addLogs() throws ParseException {

        LogEntry logEntry1 = new LogEntry();
        logEntry1.CacheId = this.id;
        logEntry1.Finder = "berlingser";
        logEntry1.Type = LogTypes.found;
        logEntry1.Comment = "Danke für die Geschichtsstunde. Den Hinweis an S4 habe ich nicht gefunden. Sonst klappte alles. Die Stationen waren gut ausgearbeitet und gut zu finden.  TFTC und Gruß vom Haarstrang.";
        logEntry1.Timestamp = DATE_PATTERN.parse("2011-08-17T20:20:15Z");
        logEntry1.Id = 180491712L;
        this.logEntries.add(logEntry1);

        LogEntry logEntry2 = new LogEntry();
        logEntry2.CacheId = this.id;
        logEntry2.Finder = "Egon0815";
        logEntry2.Type = LogTypes.found;
        logEntry2.Comment = "TFTC";
        logEntry2.Timestamp = DATE_PATTERN.parse("2011-08-08T19:00:00Z");
        logEntry2.Id = 178523602L;
        this.logEntries.add(logEntry2);

        LogEntry logEntry3 = new LogEntry();
        logEntry3.CacheId = this.id;
        logEntry3.Finder = "themurkel0815";
        logEntry3.Type = LogTypes.found;
        logEntry3.Comment = "Netter Multi, TFTC";
        logEntry3.Timestamp = DATE_PATTERN.parse("2011-08-08T19:00:00Z");
        logEntry3.Id = 178488480L;
        this.logEntries.add(logEntry3);

        LogEntry logEntry4 = new LogEntry();
        logEntry4.CacheId = this.id;
        logEntry4.Finder = "Logan Silver";
        logEntry4.Type = LogTypes.found;
        logEntry4.Comment = "# 372 # 21:00 #\n" +
                "\n" +
                "Der Letzte Cache der heutigen Runde war auch der stationenreichste. Bei zei Stationen haben sich Baldur unsd ich mächtig dumm angestellt und eine der beiden gar nicht gefunden. Durch logisches Rechnen konnten wir aber die potenziellen Finalgegenden stark eingrenzen und das nach Baldurs Vermutung wahrscheinlichere Final war es letztlich dann auch. Das Finale war dann auch unsere schnellste Station. Baldurs GPS hatte ih  selbst gerade erst eingeholt^^ da warf ich ihm schon die Dose zu. Ein wirklich schöner Multi, der uns wieder einmal die Deutsch-Deutsche Geschichte etwas näher bringen konnte.\n" +
                "\n" +
                "Vielen Dank für die Geschichtsstunde und den Cache.\n" +
                "\n" +
                "Logan Silver\n" +
                "\n" +
                "PS: Nochmal zur Klarstellung: Wir haben noch eine leere Seite im Logbuch entdeckt, aber jetzt ist wirklich nur noch ein einziger Platz frei. Also, die nächsten Besucher sollten sicherheitshalber einen Ersatzlogstreifen dabei haben.";
        logEntry4.Timestamp = DATE_PATTERN.parse("2011-07-16T19:00:00Z");
        logEntry4.Id = 174245656L;
        this.logEntries.add(logEntry4);

        LogEntry logEntry5 = new LogEntry();
        logEntry5.CacheId = this.id;
        logEntry5.Finder = "BaldurMorgan";
        logEntry5.Type = LogTypes.found;
        logEntry5.Comment = "# 644 - 21:00 Uhr\n" +
                "\n" +
                "Dieser feine Multi war der letzte auf meiner heutigen Tour zusammen mit LoganSilver. Die Stationen konnten allesamt gut gefunden werden, auch wenn wir bei der Station mit der Höhenangabe erst eine zeitlang Tomaten auf den Augen hatten. Wer der/die Spender/in des Kirchengebäudes war fanden wir jedoch nicht heraus, ermittelten aber mit etwas logischem Denken 2 mögliche Finalorte. Mein Gefühl sagte mir, dass wir zuerst zu dem Einen fahren sollten. Und nach kurzer Suche bewahrheitete sich meine Vorahnung, als LoganSilver die Dose aus ihrem Versteck zog. Durch die zahlreichen Vorlogs, welche von mehreren notwendigen Anläufen berichteten, hätten wir uns die Suche insgesamt etwas schwieriger vorgestellt, aber es lief doch ganz rund [;)]\n" +
                "\n" +
                "TFTC BaldurMorgan";
        logEntry5.Timestamp = DATE_PATTERN.parse("2011-07-16T19:00:00Z");
        logEntry5.Id = 173276929L;
        this.logEntries.add(logEntry5);

        return true;
    }
}
