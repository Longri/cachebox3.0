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
public class TestCache_GC2V0NP extends AbstractTestCache {
    @Override
    protected void setValues() {
        this.latitude = 49.5473;
        this.longitude = 8.673017;
        this.cacheType = CacheTypes.Multi;
        this.gcCode = "GC2V0NP";
        this.name = "WEINHEIM 1645 \"TORE, TÜRME UND PFORTEN - TEIL 1\"";
        this.available = true;
        this.archived = false;
        this.placed_by = "CAIRO_PETE";
        this.owner = "CAIRO_PETE";
        this.container = CacheSizes.small;
        this.url = "http://coord.info/GC2V0NP";
        this.difficulty = 2f;
        this.terrain = 3f;
        this.country = "";
        this.state = "";
        this.found = false;
        this.tbCount = 0;
        this.hint = "Finale - Mikado";
        this.favoritePoints = 0;
        this.note = "Cachebox Note";
        this.solver = "Cachebox Solver";
        try {
            this.dateHidden = DATE_PATTERN.parse("2011-04-25T09:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.positiveList.add(Attributes.Available_at_all_times);
        this.positiveList.add(Attributes.Recommended_for_kids);
        this.positiveList.add(Attributes.Medium_hike);
        this.positiveList.add(Attributes.Bicycles);
        this.positiveList.add(Attributes.Dogs);

        this.shortDescription = "<p align=\"center\"><b><font color=\"#000088\" size=\"+2\">WEINHEIM 1645 \"TORE, TÜRME UND PFORTEN TEIL 1\" -</font></b></p>\n" +
                "<p align=\"center\"><b><font color=\"#000088\" size=\"+2\">Die Preß geschoßen</font></b></p>\n" +
                "<p align=\"center\"><font color=\"#000088\" size=\"+2\">Eine Runde um die Weinheimer Stadtbefestigungen</font></p>\n" +
                "\n";
        this.longDescription = "Die Stadtbefestigungen, deren Anfänge mit der Gründung der Weinheimer Neustadt zusammenfallen, bestanden aus einer Doppelmauer von bis zu 3,50 m Dicke mit Graben, die die Stadt als unregelmäßiges Vieleck umschlossen. An der Ostseite folgte sie dem Lauf des Grundelbaches (oder der Grundelbach, wie der Weinheimer sagt), bis hin zur Mühlgasse und weiter zum Hexenturm, der die Südostseite der Verteidigungsanlagen bildete.<br />\n" +
                "<br />\n" +
                "<p>Station 1: N 49° 32.838 , E 008° 40.381</p>\n" +
                "<p>Der erste Teil der kleinen Runde entlang der Stadtmauern beginnt an der Stelle, an der früher das Niedertor stand. Es war ein gedrungener, rechteckiger Bau, der eine Fachwerketage und ein Walmdach hatte. Der 1796 in Heidelberg geborene Pfarrer Wolf erwähnt außerdem noch eine Zugbrücke, so wie sie im Ratsprotokoll von 1504 vorgeschrieben wurde. 1768 wurde das baufällige innere und 1808 das äußere Tor abgerissen. Wenn ihr euch an dieser Stelle umseht, sucht nach dem himmlischen Wesen und zählt die Buchstaben beider Wörter des Schriftzuges zusammen und bildet die Quersumme. Diese Zahl ist A.</p>\n" +
                "<p>Station 2: 49° 32.(2xA) 1 2 , E 008° 40.A 3 A</p>\n" +
                "<p>Hier stand früher der 1434 als \" des Herzogs Turm, den mann nennet den Juden Thorne \" bezeichnete nordöstliche Teil der Wehranlagen. Es war ein hoher mehrgeschossiger Turm mit Dach. Auf dem Stich von Weinheim aus dem Jahr 1787 fehlt es bereits. Die Reste des Judenturmes befinden sich im Hofe des Hauses, vor dem ihr dann steht. Leider sind sie nicht öffentlich zugänglich. Gut zu sehen ist allerdings die aufgemalte Jahreszahl an der Wand. Wann wurde dieses Haus gebaut?. Bildet von der Zahl die iterierte Quersumme, und ihr habt B.<br /></p>\n" +
                "<p>Station 3: N 49° 32.7 (A - B) 9 , E 008 40.A 2 B</p>\n" +
                "<p>An dieser Stelle verband ein Weg die Stadt mit der Burg Windeck. Zu diesem Zweck gab es hier einen Durchlass mit einem kleinen Türmchen in der Mauer, der 1473 als \"Schweiztörlein\" bezeichnet wurde. Man nannte diese Pforte auch Eselspforte, weil von hier die Lasten auf Eseln zur Burg gebracht wurden. Das Türmchen wurde im Jahre 1882 beim Bau der Grundelbachstrasse abgerissen. In Höhe der Koordinaten sind mehrere Jahreszahlen an die Wand gemalt, hier wird die der ersten urkundlichen Erwähnung des Gebäudes benötigt. Bilde die iterierte Quersumme. Diese Zahl ist C.</p>\n" +
                "<br />\n" +
                "<p>Station 4: N 49° 32.(2xC) 8 (A/2) , E 008° 40.(B+C) 0 B</p>\n" +
                "<p>\"Der Thorn im Wasserloch\" oder der Pulverturm wie er früher genannt wurde, stand an der Südostseite der Stadtmauern. Eigentlich war er ein Doppelturm, aber von seinem Pendant gibt es nur noch die Fundamente. Beide Türme waren in einigen Metern Höhe durch eine Brücke verbunden. Dies hatte einen, aus der damaligen Sicht, guten Grund. Heute nennt man dieses Bauwerk nämlich den Hexenturm. Wie man sehen kann, gibt es an der Basis keinen Eingang. Die als Hexen bezichtigten Frauen kamen vom anderen Turm über die Brücke und wurden dann in das Verlies hinuntergelassen. An der einen Seite kann man heute noch einen Mauervorsprung sehen. Man glaubte, daß der Teufel den auf diese Art hinein gekommenen Frauen genauso wenig helfen konnte, wie ihre Hexerei. Dieses Verließ blieb als Gefängnis für Hexen möglicherweise ungenutzt, da in den Weinheimer Ratsprotokollen keine Hexenprozesse verzeichnet sind. Für andere Gefangene hatte der \"Thorn im Wasserloch \" jedoch durchaus seine Schrecken. An dieser Stelle konnte man nämlich den Grundelbach aufstauen und die Delinquenten zur Strafe in einem Schnappkorb oder einem Sack von oben ins Wasser lassen, bis ihnen die Luft wegblieb. Dies wurde als Schauspiel für die anwesende Menge wiederholt gemacht. Am Hexenturm gibt es eine kleine Treppe, zähle dort die Stufen neben denen rechts und links das Geländer läuft. Diese Zahl ist D.</p>\n" +
                "<p>Station 5: N 49° 32.6 (D-1) (A+C) , E 008° 40.C A (A-B)</p>\n" +
                "Hier stand das Bronn-Tor, der Vorläufer des Müllheimer Tores. Benannt wurde es nach einem außerhalb der Stadtmauern gelegenen Brunnens, es sollte nach dem Ratsbeschluß von 1504 ebenfalls eine Zugbrücke haben. Man nimmt an, daß das Müllheimer- Tor an gleicher Stelle errichtet worden ist. Der alte Torbogen, der hier den Eingang zum Schloßpark überspannt, trägt die Jahreszahl 1608. Auf dem Merian-Stich von 1620 ist an dieser Stelle kein hoher Turm zu sehen. Es ist möglich, daß hier zuerst ein niedriger Turm war, der dann nach dem 30-jährigen Krieg um die oberen Stockwerke ergänzt wurde. Das Müllheimer- Tor hatte eine Schlaguhr, deren Klöppel und Zifferblatt heute im Heimatmuseum ausgestellt sind. 1882 wurde es abgebrochen, um Platz für den Verkehr zu schaffen. Die Steine wurden für den Bau der Maschinenfabrik Badenia verwendet, wo man 1955 den komplett erhaltenen Torbogen entdeckte, der jetzt nahe seines ehemaligen Standortes wieder eine Heimat gefunden hat. Gehe von hier B (A+C) 0 m in Richtung B C 0° und Du kommst zu dem Brunnen, der dem alten Tor seinen Namen gab.<br />\n" +
                "<br />\n" +
                "<p>Station 6: Zähle die Buchstaben des Eigennamens in der Brunnen- Inschrift und Du erhältst E.</p>\n" +
                "<p>Station 7: 49° 32.(D-C) E E , E 008° 40.(C-B) (E-B) (D-E)</p>\n" +
                "<p>Im Jahr 1645 tobte immer noch der 30-jährige Krieg in Deutschland und auch Weinheim blieb nicht verschont. Es wurde so oft besetzt und es waren so viele fremde Truppen in der Stadt, daß so mancher Geschichtsschreiber den Überblick verlor, wer gerade wann die Stadt besetzt und ausgepresst hatte. Die Einwohner hatten die in der Stadt befindlichen Truppen nämlich mit allem zu versorgen was diese gerade brauchten. Den Aufzeichnungen nach hatten die Franzosen unter Marschall Turenne im Frühjahr 1645 wieder einmal Weinheim besetzt. Aber von Süden rückte ein bayerisches Heer an, um die Stadt zu befreien. Die Kanonen wurden am Fuße des Judenbuckels in Stellung gebracht und dann das Feuer auf die Stadtmauer eröffnet. Diese hielt dem konzentrierten Beschuß nicht stand und \"die Preß ward geschoßen\", eine Bresche in der Stadtmauer, durch die die Bayern in die Stadt eindringen und sie befreien konnten. An den Koordinaten ist zur Erinnerung an dieses Ereignis eine Steintafel eingelassen. Hier interessiert der Monat, in dem dies geschah. Seine Position im Jahr ergibt F.</p>\n" +
                "<p>Das Finale liegt bei N 49° 3 (D-F). (F-C) (D-A) (D+F-E) , E 008° 40.(F-A) (D-E) (D+F-A-B-C-E)</p>\n" +
                "<p>Ihr braucht einen Kompass oder ein GPSr mit dem man peilen kann.<br />\n" +
                "Der ganze Weg bis zum Finale ist auch für Kinderwagen geeignet, dann geht es jedoch steil nach oben.</p>\n" +
                "<p><b>In der Dose sind das Logbuch, ein Stift und die FTF- Urkunde. Der Behälter ist ein etwas größerer \"small\", Tauschgegenstände und TB´s dürften ohne weiteres reinpassen.<br />\n" +
                "Die Zeichen in der Dose/ dem Logbuch bitte notieren, sie werden dann für den Bonus gebraucht.</b></p>\n" +
                "\n";
    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(49.5473, 8.673017, this.id);
        wp1.setGcCode("S12V0NP");
        wp1.setType(CacheTypes.MultiQuestion);
        wp1.setTitle("GC2V0NP Stage 1");
        wp1.setClue("");
        wp1.setUserWaypoint(false);
        wp1.setStart(false);
        wp1.setClue("Cachebox WP Clue");
        this.waypoints.add(wp1);
        return true;
    }

    @Override
    protected boolean addLogs() throws ParseException {

        LogEntry logEntry1 = new LogEntry();
        logEntry1.CacheId = this.id;
        logEntry1.Finder = "Vlad_Tepes";
        logEntry1.Type = LogTypes.found;
        logEntry1.Comment = "#2283 | 19.10.2017 | 14:37 Uhr\n" +
                "\n" +
                "Während sich ein Teil unserer Familie im Miramar austobte, zogen ZALE5KA und ich es vor Weinheim zu erkunden. Wir wählten zuerst diesen Cache aus. Wir genossen einen tollen Rundgang entlang der einzelnen Stationen. Zwischendurch gönnten wir uns noch eine kurze Pause in einem der zahlreichen Cafes.\n" +
                "Abschließend können wir sagen, dass Weinheim sehr schön ist und wir viel gelernt haben. \n" +
                "\n" +
                "Danke sagt Vlad_Tepes";
        logEntry1.Timestamp = DATE_PATTERN.parse("2017-10-19T21:00:00Z");
        logEntry1.Id = 728484100L;
        this.logEntries.add(logEntry1);

        LogEntry logEntry2 = new LogEntry();
        logEntry2.CacheId = this.id;
        logEntry2.Finder = "ZALE5KA";
        logEntry2.Type = LogTypes.found;
        logEntry2.Comment = "#0907 | 19.10.2017 | 14:37 Uhr\n" +
                "\n" +
                "Während R9D3 mitsamt Freund im nahem Schwimmbad waren, haben der weibliche Teil von Vlad_Tepes und ich uns Weinheim angeschaut.\n" +
                "\n" +
                "Das Wetter heute war sehr schön, die Sonne hat die ganze Zeit geschienen. Deswegen haben wir nach dem Multi uns noch in ein Café gesetz und einen Kaffee getrunken.\n" +
                "Die Fragen konnten ebenfalls gut beantwortet werden und es war auch eine sehr informative Runde.\n" +
                "\n" +
                "Danke sagt ZALE5KA";
        logEntry2.Timestamp = DATE_PATTERN.parse("2017-10-19T21:00:00Z");
        logEntry2.Id = 728466575L;
        this.logEntries.add(logEntry2);

        LogEntry logEntry3 = new LogEntry();
        logEntry3.CacheId = this.id;
        logEntry3.Finder = "Die Breakers";
        logEntry3.Type = LogTypes.found;
        logEntry3.Comment = "Bei tollem Sonnenschein, ging es auf eine kleine Runde hier in Weinheim um die Tore, Türme und Pforten kennen zu lernen.\n" +
                "Die Wegführung fanden wir beide etwas merkwürdig an einer Stelle, aber wir haben alles gut gefunden.\n" +
                "Das Finale ist dann wieder schön etwas weg vom Trubel, was uns auch gut gefallen hat.\n" +
                "So konnten wir den Kuchen in der City genießen und das Finale im grünen.\n" +
                "Wir haben uns zusammen mit Superbärle im Logbuch eingetragen.\n" +
                "\n" +
                "Danke an CAIRO_PETE für den Cache sagen\n" +
                "**Die Breakers**\n" +
                "\n" +
                "IN: -\n" +
                "OUT: -\n";
        logEntry3.Timestamp = DATE_PATTERN.parse("2017-10-02T01:11:00Z");
        logEntry3.Id = 724161334L;
        this.logEntries.add(logEntry3);

        LogEntry logEntry4 = new LogEntry();
        logEntry4.CacheId = this.id;
        logEntry4.Finder = "Blue two";
        logEntry4.Type = LogTypes.found;
        logEntry4.Comment = "Trotz der großen Hitze in der Stadt, haben wir uns das historische Weinheim angeschaut und diesen Cache schnell gefunden! Leider ist ja nicht mehr viel von den Objekten erhalten, und dann dieses Final soweit abseits?! Trotzdem haben wir viel gelernt! TFTC";
        logEntry4.Timestamp = DATE_PATTERN.parse("2017-08-26T23:55:01Z");
        logEntry4.Id = 715278765L;
        this.logEntries.add(logEntry4);

        LogEntry logEntry5 = new LogEntry();
        logEntry5.CacheId = this.id;
        logEntry5.Finder = "Maus229";
        logEntry5.Type = LogTypes.found;
        logEntry5.Comment = "heut gings mit Frau zwee hoggemer bei bestem Cacherwetterchen nach Weinheim. Schon laaange standen die beiden Multis aufm Programm. \n" +
                "\n" +
                "Weinheim ist einfach schön! Und es macht Spaß, wohlbekannte Ecken immer wieder anzuschauen. \n" +
                "\n" +
                "Alle Stationen haben wir gut gefunden.\n" +
                "\n" +
                "Vielen Dank fürs Herführen und TFTC!\n" +
                "\n" +
                "maus229";
        logEntry5.Timestamp = DATE_PATTERN.parse("2017-08-20T21:00:00Z");
        logEntry5.Id = 714706105L;
        this.logEntries.add(logEntry5);

        return true;
    }
}
