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
 * Created by Longri on 31.03.18.
 */
public class TestCache_GC2T9RW extends AbstractTestCache {

    @Override
    protected void setValues() {
        this.latitude = 49.349817;
        this.longitude = 8.62925;
        this.cacheType = CacheTypes.Traditional;
        this.gcCode = "GC2T9RW";
        this.name = "der Hampir - T5 -";
        this.available = true;
        this.archived = false;
        this.placed_by = "Team Rabbits";
        this.owner = "Team Rabbits";
        this.container = CacheSizes.small;
        this.url = "http://www.geocaching.com/seek/cache_details.aspx?guid=f26f18bd-9aaa-4499-944b-3e8cb62e41a7";
        this.difficulty = 1.0f; // half int value(2)
        this.terrain = 2.5f;// half int value(5)
        this.country = "Germany";
        this.state = "Baden-Württemberg";
        this.found = true;
        try {
            this.dateHidden = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.positiveList.add(Attributes.Bicycles);
        this.positiveList.add(Attributes.Available_at_all_times);
        this.positiveList.add(Attributes.Public_restrooms_nearby);
        this.positiveList.add(Attributes.Parking_available);
        this.positiveList.add(Attributes.Fuel_Nearby);
        this.positiveList.add(Attributes.Hunting);
        this.positiveList.add(Attributes.Short_hike);
        this.positiveList.add(Attributes.Climbing_gear);
        this.positiveList.add(Attributes.Ticks);
        this.positiveList.add(Attributes.Dogs);


        this.hint = "wenn du ihn nicht findest, findet er dich!!";
        this.shortDescription = "&lt;font face=\"tahoma\" size=\"3\" color=\"#330033\"&gt;&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "T5 Klettercache&lt;br /&gt;\n" +
                "Ihr benötigt ein 30 m Seil und Eurer Klettergeraffel&lt;br /&gt;\n" +
                "Bigshot wäre von Vorteil!&lt;br /&gt;\n" +
                "BITTE NUR KLETTERN, wenn Klettererfahrungen und geeignetes Wissen\n" +
                "vorhanden sind!! Klettern natürlich auf eigene Gefahr!&lt;br /&gt;\n" +
                "BITTE:&lt;br /&gt;\n" +
                "NICHT alleine Klettern!! Denkt daran, auch ein Fall aus wenigen\n" +
                "Metern Höhe kann böse enden!!&lt;br /&gt;&lt;/font&gt;";
        this.longDescription = "&lt;br /&gt;\n" +
                "&lt;center&gt;&lt;img src=\n" +
                "\"http://img.geocaching.com/cache/9b0334c7-c419-41c8-b883-8bb0adf20ac3.jpg\" /&gt;&lt;br /&gt;\n" +
                "\n" +
                "&lt;br /&gt;\n" +
                "&lt;font face=\"tahoma\" size=\"3\" color=\"#330033\"&gt;&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "Der Hampir, so sagt man, optisch ein liebes zartes Wesen&lt;br /&gt;\n" +
                "im dunklen Hardtwald treibt er seine Spesen.&lt;br /&gt;\n" +
                "So süß, so flauschig sogleich&lt;br /&gt;\n" +
                "auch sein Fell so samtig und weich!&lt;br /&gt;\n" +
                "Deshalb lass dich blos nicht blenden,&lt;br /&gt;\n" +
                "sonst könnte es sehr böse für dich enden!&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "Aaaaaber wenn du ihn entdeckst,&lt;br /&gt;\n" +
                "so achte dich vor ihm, die Gefahr besteht dass du vergisst&lt;br /&gt;\n" +
                "und vor lauter Kummer und Sorgen ihm tief in die Augen\n" +
                "erblickst!!&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "Es ist dann zu spät!&lt;br /&gt;\n" +
                "Dann hat dich der Hampir bereits erspäht!!&lt;br /&gt;\n" +
                "Der Hampir, so sagt man erschallt sein Gelächter&lt;br /&gt;\n" +
                "wenn es Beute vor sich hat, so schaurig so grell,&lt;br /&gt;\n" +
                "rette dich wenn du kannst schneller als schnell!&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "Und wage dich nicht in den Wald&lt;br /&gt;\n" +
                "in der Nacht beim Vollmond ist es dort bitterkalt!&lt;br /&gt;\n" +
                "Nebelschwaden dort, aber die schaurige Gestalten&lt;br /&gt;\n" +
                "verstecken sich im dunkeln mit dem Gedanken,&lt;br /&gt;\n" +
                "ihre Beute noch schneller zu jagen als der Hampir!&lt;br /&gt;\n" +
                "Dennoch willst du in den Wald?! Überlege es dir!!&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "Du meinst, ach was... Hampire... die gibt es doch nicht?!&lt;br /&gt;\n" +
                "Die Hasen die warnen: HIER wartet er auf dich!!!&lt;br /&gt;\n" +
                "&lt;br /&gt;&lt;/font&gt;&lt;/center&gt;\n" +
                "&lt;font face=\"tahoma\" size=\"3\" color=\"#330033\"&gt;&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "Fotos dürft Ihr gerne machen &lt;img src=\n" +
                "'http://www.geocaching.com/images/icons/icon_smile_big.gif' border=\n" +
                "\"0\" align=\"middle\" /&gt;&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "&lt;br /&gt;\n" +
                "ein besonderer Dank an Monas Cacherteam, für die handwerkliche\n" +
                "Meisterleistung!!&lt;br /&gt;\n" +
                "Es ist genau so geworden, wie es sich die Hasen vorgestellt\n" +
                "haben!!&lt;br /&gt;\n" +
                "&lt;br /&gt;&lt;/font&gt;&lt;br /&gt;\n" +
                "&lt;a href=\"http://www.andyhoppe.com/\" title=\n" +
                "\"Counter/Zähler\"&gt;&lt;img src=\"http://c.andyhoppe.com/1302990447\"\n" +
                "style=\"border:none\" alt=\"Counter/Zähler\" /&gt;&lt;/a&gt;&lt;p&gt;Additional Hidden Waypoints&lt;/p&gt;PK2T9RW - GC2T9RW Parking&lt;br /&gt;N 49° 21.077 E 008° 37.840&lt;br /&gt;Raststätte Hardtwald West.\n" +
                "Und für Ortskundige: einfach Richtung ADAC Übungsgelände. Dann müsst Ihr nicht auf die Autobahn.&lt;br /&gt;";

    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(49.351283, 8.630667, 24578729153020743L);
        wp1.setGcCode("PK2T9RW");
        wp1.setType(CacheTypes.ParkingArea);
        wp1.setDescription("Raststätte Hardtwald West.\n" +
                "Und für Ortskundige: einfach Richtung ADAC Übungsgelände. Dann müsst Ihr nicht auf die Autobahn.");

        wp1.setClue("");
        wp1.setUserWaypoint(false);
        wp1.setStart(false);

        this.waypoints.add(wp1);
        return true;
    }

    @Override
    protected boolean addLogs() throws ParseException {
        LogEntry logEntry1 = new LogEntry();
        logEntry1.CacheId = this.id;
        logEntry1.Finder = "SaarFuchs";
        logEntry1.Type = LogTypes.found;
        logEntry1.Comment = "Heute auf dem Rückweg vom Business-Meeting in Darmstadt gesucht, gefunden und geloggt... [:D]\n" +
                "\n" +
                "So ein Cache nach der Arbeit kann den Feierabend doch immer etwas auflockern! [8D]\n" +
                "\n" +
                "Dafür auch gerne einen kleinen Umweg gemacht... [;)]\n" +
                "\n" +
                "Sehr schöne Box - klasse Idee - so macht das Baumklettern natürlich um so mehr Spaß!\n" +
                "\n" +
                "TFTC,\n" +
                "Joerg\n" +
                "([url=http://saarfuchs.blogspot.com/]Follow my blog![/url])\n" +
                "\n" +
                "T5 #255";
        logEntry1.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry1.Id = 170855167L;
        this.logEntries.add(logEntry1);

        LogEntry logEntry2 = new LogEntry();
        logEntry2.CacheId = this.id;
        logEntry2.Finder = "";
        logEntry2.Type = LogTypes.found;
        logEntry2.Comment = "";
        logEntry2.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry2.Id = 165483338L;
        this.logEntries.add(logEntry2);

        LogEntry logEntry3 = new LogEntry();
        logEntry3.CacheId = this.id;
        logEntry3.Finder = "";
        logEntry3.Type = LogTypes.found;
        logEntry3.Comment = "";
        logEntry3.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry3.Id = 165457525L;
        this.logEntries.add(logEntry3);

        LogEntry logEntry4 = new LogEntry();
        logEntry4.CacheId = this.id;
        logEntry4.Finder = "";
        logEntry4.Type = LogTypes.found;
        logEntry4.Comment = "";
        logEntry4.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry4.Id = 164390256L;
        this.logEntries.add(logEntry4);

        LogEntry logEntry5 = new LogEntry();
        logEntry5.CacheId = this.id;
        logEntry5.Finder = "";
        logEntry5.Type = LogTypes.found;
        logEntry5.Comment = "";
        logEntry5.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry5.Id = 164266702L;
        this.logEntries.add(logEntry5);

        LogEntry logEntry6 = new LogEntry();
        logEntry6.CacheId = this.id;
        logEntry6.Finder = "";
        logEntry6.Type = LogTypes.found;
        logEntry6.Comment = "";
        logEntry6.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry6.Id = 163987510L;
        this.logEntries.add(logEntry6);

        LogEntry logEntry7 = new LogEntry();
        logEntry7.CacheId = this.id;
        logEntry7.Finder = "";
        logEntry7.Type = LogTypes.found;
        logEntry7.Comment = "";
        logEntry7.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry7.Id = 163965795L;
        this.logEntries.add(logEntry7);

        LogEntry logEntry8 = new LogEntry();
        logEntry8.CacheId = this.id;
        logEntry8.Finder = "";
        logEntry8.Type = LogTypes.found;
        logEntry8.Comment = "";
        logEntry8.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry8.Id = 163926440L;
        this.logEntries.add(logEntry8);

        LogEntry logEntry9 = new LogEntry();
        logEntry9.CacheId = this.id;
        logEntry9.Finder = "";
        logEntry9.Type = LogTypes.found;
        logEntry9.Comment = "";
        logEntry9.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry9.Id = 163871656L;
        this.logEntries.add(logEntry9);

        LogEntry logEntry10 = new LogEntry();
        logEntry10.CacheId = this.id;
        logEntry10.Finder = "";
        logEntry10.Type = LogTypes.found;
        logEntry10.Comment = "";
        logEntry10.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry10.Id = 163870090L;
        this.logEntries.add(logEntry10);

        LogEntry logEntry11 = new LogEntry();
        logEntry11.CacheId = this.id;
        logEntry11.Finder = "";
        logEntry11.Type = LogTypes.found;
        logEntry11.Comment = "";
        logEntry11.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry11.Id = 163722750L;
        this.logEntries.add(logEntry11);

        LogEntry logEntry12 = new LogEntry();
        logEntry12.CacheId = this.id;
        logEntry12.Finder = "";
        logEntry12.Type = LogTypes.found;
        logEntry12.Comment = "";
        logEntry12.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry12.Id = 163450672L;
        this.logEntries.add(logEntry12);

        LogEntry logEntry13 = new LogEntry();
        logEntry13.CacheId = this.id;
        logEntry13.Finder = "";
        logEntry13.Type = LogTypes.found;
        logEntry13.Comment = "";
        logEntry13.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry13.Id = 163700211L;
        this.logEntries.add(logEntry13);

        LogEntry logEntry14 = new LogEntry();
        logEntry14.CacheId = this.id;
        logEntry14.Finder = "";
        logEntry14.Type = LogTypes.found;
        logEntry14.Comment = "";
        logEntry14.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry14.Id = 163338659L;
        this.logEntries.add(logEntry14);

        LogEntry logEntry15 = new LogEntry();
        logEntry15.CacheId = this.id;
        logEntry15.Finder = "";
        logEntry15.Type = LogTypes.found;
        logEntry15.Comment = "";
        logEntry15.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry15.Id = 163338340L;
        this.logEntries.add(logEntry15);

        LogEntry logEntry16 = new LogEntry();
        logEntry16.CacheId = this.id;
        logEntry16.Finder = "";
        logEntry16.Type = LogTypes.found;
        logEntry16.Comment = "";
        logEntry16.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry16.Id = 163213651L;
        this.logEntries.add(logEntry16);

        LogEntry logEntry17 = new LogEntry();
        logEntry17.CacheId = this.id;
        logEntry17.Finder = "";
        logEntry17.Type = LogTypes.found;
        logEntry17.Comment = "";
        logEntry17.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry17.Id = 163152743L;
        this.logEntries.add(logEntry17);

        LogEntry logEntry18 = new LogEntry();
        logEntry18.CacheId = this.id;
        logEntry18.Finder = "";
        logEntry18.Type = LogTypes.found;
        logEntry18.Comment = "";
        logEntry18.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry18.Id = 163136663L;
        this.logEntries.add(logEntry18);

        LogEntry logEntry19 = new LogEntry();
        logEntry19.CacheId = this.id;
        logEntry19.Finder = "";
        logEntry19.Type = LogTypes.found;
        logEntry19.Comment = "";
        logEntry19.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry19.Id = 163673347L;
        this.logEntries.add(logEntry19);

        LogEntry logEntry20 = new LogEntry();
        logEntry20.CacheId = this.id;
        logEntry20.Finder = "";
        logEntry20.Type = LogTypes.found;
        logEntry20.Comment = "";
        logEntry20.Timestamp = DATE_PATTERN.parse("2011-04-16T07:00:00Z");
        logEntry20.Id = 163319268L;
        this.logEntries.add(logEntry20);

        return true;
    }

}
