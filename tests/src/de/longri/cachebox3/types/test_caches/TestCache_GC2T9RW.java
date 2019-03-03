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
        this.difficulty = 2.0f;
        this.terrain = 5f;
        this.country = "Germany";
        this.state = "Baden-Württemberg";
        this.found = true;
        this.tbCount = 2;
        this.note = "";
        this.solver = "";
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
        this.shortDescription = "<font face=\"tahoma\" size=\"3\" color=\"#330033\"><br />\n" +
                "<br />\n" +
                "T5 Klettercache<br />\n" +
                "Ihr benötigt ein 30 m Seil und Eurer Klettergeraffel<br />\n" +
                "Bigshot wäre von Vorteil!<br />\n" +
                "BITTE NUR KLETTERN, wenn Klettererfahrungen und geeignetes Wissen\n" +
                "vorhanden sind!! Klettern natürlich auf eigene Gefahr!<br />\n" +
                "BITTE:<br />\n" +
                "NICHT alleine Klettern!! Denkt daran, auch ein Fall aus wenigen\n" +
                "Metern Höhe kann böse enden!!<br /></font>";
        this.longDescription = "<br />\n" +
                "<center><img src=\n" +
                "\"http://img.geocaching.com/cache/9b0334c7-c419-41c8-b883-8bb0adf20ac3.jpg\" /><br />\n" +
                "\n" +
                "<br />\n" +
                "<font face=\"tahoma\" size=\"3\" color=\"#330033\"><br />\n" +
                "<br />\n" +
                "Der Hampir, so sagt man, optisch ein liebes zartes Wesen<br />\n" +
                "im dunklen Hardtwald treibt er seine Spesen.<br />\n" +
                "So süß, so flauschig sogleich<br />\n" +
                "auch sein Fell so samtig und weich!<br />\n" +
                "Deshalb lass dich blos nicht blenden,<br />\n" +
                "sonst könnte es sehr böse für dich enden!<br />\n" +
                "<br />\n" +
                "Aaaaaber wenn du ihn entdeckst,<br />\n" +
                "so achte dich vor ihm, die Gefahr besteht dass du vergisst<br />\n" +
                "und vor lauter Kummer und Sorgen ihm tief in die Augen\n" +
                "erblickst!!<br />\n" +
                "<br />\n" +
                "Es ist dann zu spät!<br />\n" +
                "Dann hat dich der Hampir bereits erspäht!!<br />\n" +
                "Der Hampir, so sagt man erschallt sein Gelächter<br />\n" +
                "wenn es Beute vor sich hat, so schaurig so grell,<br />\n" +
                "rette dich wenn du kannst schneller als schnell!<br />\n" +
                "<br />\n" +
                "Und wage dich nicht in den Wald<br />\n" +
                "in der Nacht beim Vollmond ist es dort bitterkalt!<br />\n" +
                "Nebelschwaden dort, aber die schaurige Gestalten<br />\n" +
                "verstecken sich im dunkeln mit dem Gedanken,<br />\n" +
                "ihre Beute noch schneller zu jagen als der Hampir!<br />\n" +
                "Dennoch willst du in den Wald?! Überlege es dir!!<br />\n" +
                "<br />\n" +
                "Du meinst, ach was... Hampire... die gibt es doch nicht?!<br />\n" +
                "Die Hasen die warnen: HIER wartet er auf dich!!!<br />\n" +
                "<br /></font></center>\n" +
                "<font face=\"tahoma\" size=\"3\" color=\"#330033\"><br />\n" +
                "<br />\n" +
                "Fotos dürft Ihr gerne machen <img src=\n" +
                "'http://www.geocaching.com/images/icons/icon_smile_big.gif' border=\n" +
                "\"0\" align=\"middle\" /><br />\n" +
                "<br />\n" +
                "<br />\n" +
                "ein besonderer Dank an Monas Cacherteam, für die handwerkliche\n" +
                "Meisterleistung!!<br />\n" +
                "Es ist genau so geworden, wie es sich die Hasen vorgestellt\n" +
                "haben!!<br />\n" +
                "<br /></font><br />\n" +
                "<a href=\"http://www.andyhoppe.com/\" title=\n" +
                "\"Counter/Zähler\"><img src=\"http://c.andyhoppe.com/1302990447\"\n" +
                "style=\"border:none\" alt=\"Counter/Zähler\" /></a><p>Additional Hidden Waypoints</p>PK2T9RW - GC2T9RW Parking<br />N 49° 21.077 E 008° 37.840<br />Raststätte Hardtwald West.\n" +
                "Und für Ortskundige: einfach Richtung ADAC Übungsgelände. Dann müsst Ihr nicht auf die Autobahn.<br />";

    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(49.351283, 8.630667, 24578729153020743L);
        wp1.setGcCode("PK2T9RW");
        wp1.setType(CacheTypes.ParkingArea);
        wp1.setTitle("GC2T9RW Parking");
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
        logEntry1.Timestamp = DATE_PATTERN.parse("2011-07-04T19:00:00Z");
        logEntry1.Id = 170855167L;
        this.logEntries.add(logEntry1);

        LogEntry logEntry2 = new LogEntry();
        logEntry2.CacheId = this.id;
        logEntry2.Finder = "Vespa6";
        logEntry2.Type = LogTypes.found;
        logEntry2.Comment = "heute auf dem weg nach frankfurt hier kurz gehalten, umgedreht, geparkt und das bäumchen erklommen, wirklich schön angelegtes döslein.TFTC";
        logEntry2.Timestamp = DATE_PATTERN.parse("2011-06-07T19:00:00Z");
        logEntry2.Id = 165483338L;
        this.logEntries.add(logEntry2);

        LogEntry logEntry3 = new LogEntry();
        logEntry3.CacheId = this.id;
        logEntry3.Finder = "ascona2";
        logEntry3.Type = LogTypes.found;
        logEntry3.Comment = "So hat ide heutige Fahrt von Wuppertal nach Karlsruhe doch mal Spass gemacht. Der Cache war allererste Sahne!!!!!!Wenn doch jeder Autobahncache so wäre. Das Seil war schnell eingebaut und dann ging es auch schon zu der tollen Dose. Hat echt Spass gemacht.\n" +
                "\n" +
                "out: Coin\n" +
                "\n" +
                "TFTC";
        logEntry3.Timestamp = DATE_PATTERN.parse("2011-06-07T19:00:00Z");
        logEntry3.Id = 165457525L;
        this.logEntries.add(logEntry3);

        LogEntry logEntry4 = new LogEntry();
        logEntry4.CacheId = this.id;
        logEntry4.Finder = "Goline";
        logEntry4.Type = LogTypes.found;
        logEntry4.Comment = "Als das Listing erschien freute ich mich sofort über den neuen Kletter-T5er in der Nähe.\n" +
                "Beim lesen des Listings stieg dann leise aber stetig die Angst in mir hoch.....nachdem ich mich dann mehrere Nächte lang schlaflos in meinem Bett hin- und hergewältzt habe war klar, dass hier nur eine Therapie helfen kann.....also bei der Krankenkasse angefragt....und als die mir grünes Licht gegeben hatte, konnte es mit der Behandlung losgehen. Heute nun stand dann zum erfolgreichen Abschluß der Therapie der Besuch beim Hampir auf dem Programm. Mit jedem Höhenmeter wurden meine Beine wackeliger....und oben angekommen nahm ich dann all meinen übrig gebliebenen Mut zusammen....[;)]\n" +
                "\n" +
                "Ein wirklich absolut geil gemachter Cache !!!!!  Eine richtig tolle Bastelarbeit !!! [8D]\n" +
                "\n" +
                "\n" +
                "Danke fürs herlocken und hinhängen und liebe Grüße an die Owner !!\n" +
                "\n" +
                ":grin: [red] 1217 [/red] :grin:\n" +
                "\n" +
                "IN: nix\n" +
                "Out: Geocoin \"Micro Compass Rose - AlienHeads 1\"";
        logEntry4.Timestamp = DATE_PATTERN.parse("2011-06-02T19:00:00Z");
        logEntry4.Id = 164390256L;
        this.logEntries.add(logEntry4);

        LogEntry logEntry5 = new LogEntry();
        logEntry5.CacheId = this.id;
        logEntry5.Finder = "Biene97";
        logEntry5.Type = LogTypes.found;
        logEntry5.Comment = "Heute bei schönem Wetter zusammen mit Goline diesen T-5er angegangen.\n" +
                "Der Hampir konnte mich nicht daran hindern, mich ins Logbuch einzutragen [:D]\n" +
                "\n" +
                "Vielen Dank fürs auslegen und herführen und das tolle Versteck.\n" +
                "\n" +
                "[;)] # 20 [;)]\n" +
                "\n" +
                "in: TB \"Betty I. M.\"\n" +
                "    Coin \"Cache am Sonntag Geocoin\"\n" +
                "    Coin \"Hochzeitsmünze von Maren the Martian und $hogun\"\n" +
                " \n" +
                "out: TB \" \"K\" the Cook\"\n" +
                "   Coin \"Eli+Veru\"";
        logEntry5.Timestamp = DATE_PATTERN.parse("2011-06-02T19:00:00Z");
        logEntry5.Id = 164266702L;
        this.logEntries.add(logEntry5);

        LogEntry logEntry6 = new LogEntry();
        logEntry6.CacheId = this.id;
        logEntry6.Finder = "Shadows0001";
        logEntry6.Type = LogTypes.found;
        logEntry6.Comment = "Hi,\n" +
                "\n" +
                "heute ging es zusammen mit der OPWG zum Hampir :-)\n" +
                "So erklomm Eine/r nach dem Anderen diesen schönen Baum und bewunderte die schöne Handwerkskunst :-)\n" +
                "\n" +
                "Vielen Dank für den kreativen Cache :-)\n" +
                "\n" +
                "#899\n" +
                "\n" +
                "Gruß Shadows0001";
        logEntry6.Timestamp = DATE_PATTERN.parse("2011-05-30T19:00:00Z");
        logEntry6.Id = 163987510L;
        this.logEntries.add(logEntry6);

        LogEntry logEntry7 = new LogEntry();
        logEntry7.CacheId = this.id;
        logEntry7.Finder = "DdaA";
        logEntry7.Type = LogTypes.found;
        logEntry7.Comment = "found on 30.05.2011 @ 20:20, Cache #420\n" +
                "\n" +
                "So... Es war mal wieder so weit.\n" +
                "Ein großer Teil der OPWG hat es mal wieder geschafft und sich zum Klettern verabredet.\n" +
                "Wie gut, daß hier zwei t5er dicht zusammenliegen...\n" +
                "\n" +
                "Beim Hampier also ab in Richtung Baumkrone, den Logbucheintrag getätigt, und nach erfolgtem Bodenkontakt mit dem Rest der Crew viel Spaß gehabt...\n" +
                "\n" +
                "Danke für den Cache, '[i][b][orange]A[/orange][/b][/i]' von '[i][b][orange]DdaA[/orange][/b][/i]'\n" +
                "\n" +
                "No trade";
        logEntry7.Timestamp = DATE_PATTERN.parse("2011-05-30T19:00:00Z");
        logEntry7.Id = 163965795L;
        this.logEntries.add(logEntry7);

        LogEntry logEntry8 = new LogEntry();
        logEntry8.CacheId = this.id;
        logEntry8.Finder = "Wotan1";
        logEntry8.Type = LogTypes.found;
        logEntry8.Comment = "Found on 30.05.2011 @ 19:57, Cache #609\n" +
                "\n" +
                "Bei 2 T5er so nah bei einander...\n" +
                "Das bot sich für ein kleines Event der OPWG gerade zu an. Der Hampir rief, und die meisten kamen....[:D]\n" +
                "Das Seil war schnell eingebaut [^] und dann gings los.\n" +
                "Kleines Jubiläum, T5 #25\n" +
                "\n" +
                "Danke,\n" +
                "Wotan1";
        logEntry8.Timestamp = DATE_PATTERN.parse("2011-05-30T19:00:00Z");
        logEntry8.Id = 163926440L;
        this.logEntries.add(logEntry8);

        LogEntry logEntry9 = new LogEntry();
        logEntry9.CacheId = this.id;
        logEntry9.Finder = "Casiopya";
        logEntry9.Type = LogTypes.found;
        logEntry9.Comment = "Mit einem Grossteil der OPWG endlich seit langem mal wieder auf Klettertour gewesen.\n" +
                "Hat mich ein wenig Ueberwindung gekostet, aber dank der fuersorglichen Hilfestellung war alles kein Problem [:D]";
        logEntry9.Timestamp = DATE_PATTERN.parse("2011-05-30T19:00:00Z");
        logEntry9.Id = 163871656L;
        this.logEntries.add(logEntry9);

        LogEntry logEntry10 = new LogEntry();
        logEntry10.CacheId = this.id;
        logEntry10.Finder = "Fanta_2";
        logEntry10.Type = LogTypes.found;
        logEntry10.Comment = "Mal wieder zusammen mit einem Großteil der OPWG auf einer kleinen T5 Tour gewesen.\n" +
                "Bei den Temperaturen war das zwar ziemlich schweißtreibend[:P], hat aber trotzdem jede Menge Spass gemacht [:D] und die Dose da oben ist ja mal genial.[8D]\n" +
                "\n" +
                "Danke für die kleine Klettereinlage\n" +
                "Fanta_2\n" +
                "\n" +
                "In: TB";
        logEntry10.Timestamp = DATE_PATTERN.parse("2011-05-30T19:00:00Z");
        logEntry10.Id = 163870090L;
        this.logEntries.add(logEntry10);

        LogEntry logEntry11 = new LogEntry();
        logEntry11.CacheId = this.id;
        logEntry11.Finder = "Greenmaya";
        logEntry11.Type = LogTypes.found;
        logEntry11.Comment = "Zusammen mit der OPWG\n" +
                "heute auch diesen T5er besucht, welcher mir noch besser gefallen hat als der Erste :-)\n" +
                "\n" +
                "Vielen Dank und Gruß\n" +
                "Greenmaya";
        logEntry11.Timestamp = DATE_PATTERN.parse("2011-05-30T07:00:00Z");
        logEntry11.Id = 163722750L;
        this.logEntries.add(logEntry11);

        LogEntry logEntry12 = new LogEntry();
        logEntry12.CacheId = this.id;
        logEntry12.Finder = "jotriweil";
        logEntry12.Type = LogTypes.found;
        logEntry12.Comment = "Sehr interessante T5er Ecke: 2 Caches dicht beieinander und von der Autobahnraststätte schnell erreichbar.\n" +
                "Heute bei genialen Wetter (25°C und sehr trocken) hat der Seileinbau nicht so recht funktioniert. Die Astgabel zu treffen ist nicht ganz einfach, daher oben mal einen Seilumbau vornehmen müssen, um auf den nächsten Stock zu kommen.\n" +
                "Der Hampir funktioniert hervorragend und hat mich nett verabschiedet.\n" +
                "TFTC  .. jotriweil (16:30)\n" +
                "\n" +
                "This entry was edited by jotriweil on Monday, 30 May 2011 at 10:22:01 UTC.";
        logEntry12.Timestamp = DATE_PATTERN.parse("2011-05-29T19:00:00Z");
        logEntry12.Id = 163450672L;
        this.logEntries.add(logEntry12);

        LogEntry logEntry13 = new LogEntry();
        logEntry13.CacheId = this.id;
        logEntry13.Finder = "Zwerga";
        logEntry13.Type = LogTypes.found;
        logEntry13.Comment = "Der zweite Streich auf der heutigen Tour!\n" +
                "Klasse Idee, superschönes Final,bei dem wir auf uns gut bekannte Cacher trafen. \n" +
                "Katie Eagle sagte noch verwundert:Mensch, das sieht von unten ja aus wie ein....Klar, ist ja auch ein...., wo sollen denn Hampire sonst schlafen[:o)] Herzliche Grüße an die owner.\n" +
                "\n" +
                "TFTC um 12:30Uhr\n" +
                "[b][green]Zwerga[/green][/b]";
        logEntry13.Timestamp = DATE_PATTERN.parse("2011-05-28T19:00:00Z");
        logEntry13.Id = 163700211L;
        this.logEntries.add(logEntry13);

        LogEntry logEntry14 = new LogEntry();
        logEntry14.CacheId = this.id;
        logEntry14.Finder = "Katie Eagle";
        logEntry14.Type = LogTypes.note;
        logEntry14.Comment = "Ach ja, TB abgelegt [8D]";
        logEntry14.Timestamp = DATE_PATTERN.parse("2011-05-28T19:00:00Z");
        logEntry14.Id = 163338659L;
        this.logEntries.add(logEntry14);

        LogEntry logEntry15 = new LogEntry();
        logEntry15.CacheId = this.id;
        logEntry15.Finder = "Katie Eagle";
        logEntry15.Type = LogTypes.found;
        logEntry15.Comment = "Auf dem Weg zu diesem Baum hörten wir vertraute Stimmen im Wald und sahen den Mechaniker, sowie Staubfinger und seinen Sohn Oskar, die gerade fertig wurden. Sie waren so nett und nahmen unsere Pilotleine auf den Baum - An dieser Stelle vielen Dank dafür [:)] Die Cache \"Box\" ist klasse! Erstaunlich, dass der Hampyr nicht zu Staub wurde, als ich am helligten Tag die Tür öffnete [;)]\n" +
                "Vielen Dank an die Owner für die Idee und den Cache\n" +
                "[b][red]Katie Eagle[/red][/b]";
        logEntry15.Timestamp = DATE_PATTERN.parse("2011-05-28T19:00:00Z");
        logEntry15.Id = 163338340L;
        this.logEntries.add(logEntry15);

        LogEntry logEntry16 = new LogEntry();
        logEntry16.CacheId = this.id;
        logEntry16.Finder = "Dolphiner";
        logEntry16.Type = LogTypes.found;
        logEntry16.Comment = "Wow! Der ist ja mal richtig gut gemacht! \n" +
                "\n" +
                "Schöne Arbeit, netter Audioeffekt. [:D]\n" +
                "\n" +
                "Vor Ort trafen wir noch den Mechaniker und staubi mit Oskar. \n" +
                "Glückwunsch an staubi für die ersten T5er! \n" +
                "Danke an das Team auch für die Hilfe beim Seileinbau. [^]\n" +
                "\n" +
                "Vielen Dank für den schönen T5er und Grüße an die Owner!\n" +
                "\n" +
                "TFTC\n" +
                "[b][blue]Dolphiner[/blue][/b] [:)]";
        logEntry16.Timestamp = DATE_PATTERN.parse("2011-05-28T19:00:00Z");
        logEntry16.Id = 163213651L;
        this.logEntries.add(logEntry16);

        LogEntry logEntry17 = new LogEntry();
        logEntry17.CacheId = this.id;
        logEntry17.Finder = "manic.mechanic";
        logEntry17.Type = LogTypes.found;
        logEntry17.Comment = "Schöne kleiner T5er zum Aufwärmen. Besonders begeistert war ich neben der tollen Dose von den vielen Facilities die es in unmittelbarer Nähe gibt [;)]...\n" +
                "\n" +
                "TNLNSL\n" +
                "T4TC\n" +
                "manic.mechanic";
        logEntry17.Timestamp = DATE_PATTERN.parse("2011-05-28T19:00:00Z");
        logEntry17.Id = 163152743L;
        this.logEntries.add(logEntry17);

        LogEntry logEntry18 = new LogEntry();
        logEntry18.CacheId = this.id;
        logEntry18.Finder = "staubfinger0702";
        logEntry18.Type = LogTypes.found;
        logEntry18.Comment = "Nachdem ich gestern die ersten T5 Versuche gemacht hatte, sind heute manic.mechanic und ich mit meinem Sohn Oskar auf eine T5 Tour los gezogen.\n" +
                "Dabei sind wir sogar noch auf hiesige Cacher gestoßen.\n" +
                "Danke fürs Verstecken.";
        logEntry18.Timestamp = DATE_PATTERN.parse("2011-05-28T19:00:00Z");
        logEntry18.Id = 163136663L;
        this.logEntries.add(logEntry18);

        LogEntry logEntry19 = new LogEntry();
        logEntry19.CacheId = this.id;
        logEntry19.Finder = "BlindEagle";
        logEntry19.Type = LogTypes.found;
        logEntry19.Comment = "Gut, dass Biene Maya und ich das T5-Grödel im Auto hatten, denn dieses skurile Kerlchen entdeckten wir mehr zufällig auf der Anfahrt zu unserer heutigen Spontantour. Nachdem wir den Wurfsack aus dem Nachbarbaum geborgen hatten, gab es kein Halten mehr und wir öffneten den knarzenden Sargdeckel........welch ein Schock........erst auf den zweiten Blick erkannte ich, wie ausbruchsicher das Verlies gestaltet ist. So konnten wir uns den seltenen Hampir in aller Ruhe ansehen, prompt fiel mir dazu eine Geschichte ein, die sich angeblich ganz in der Nähe zugetragen haben soll:\n" +
                "\n" +
                "Hoppelt ein Häschen durch den Wald. Begegnet es einem sehr zottigen Hund. \"Was bist Du denn für ein Tier?\" \"Ich bin ein Wolfshund. Meine Mutter war ein Wolf, mein Vater ein Hund.\" Häschen hoppelt weiter und begegnet einem Muli. \"Was bist Du denn für ein Tier?\" \"Ich bin ein Maultier. Mutter Esel, Vater Pferd.\" Häschen wundert sich, was es alles gibt und hoppelt weiter. Begenet es einem ganz unbekannten Tier. \"Was bist Du denn für ein Tier?\" \"Ich bin ein Ameisenbär.\" Häschen entsetzt: \"Ne ne ne ne, das kannst Du mir nicht erzählen!\"\n" +
                "\n" +
                "Chapeau für diese tolle Bastelei, hat uns beiden prima gefallen.\n" +
                "\n" +
                "- Nix getauscht -\n" +
                "\n" +
                "Vielen Dank dafür und schöne Grüße, BlindEagle";
        logEntry19.Timestamp = DATE_PATTERN.parse("2011-05-27T19:00:00Z");
        logEntry19.Id = 163673347L;
        this.logEntries.add(logEntry19);

        LogEntry logEntry20 = new LogEntry();
        logEntry20.CacheId = this.id;
        logEntry20.Finder = "Biene Maya";
        logEntry20.Type = LogTypes.found;
        logEntry20.Comment = "Das hat man nun davon, da möchte man nett sein und dem Hampier einen Besuch abstatten und was macht er, versucht alles um es zu verhindern. [:(!] Nach den ersten Versuchen mit der Pilotleine hat er doch glatt das Wurfsäckchen im Nachbarbaum verankert \t[:O] und so dauerte es eine ganze Weile ehe es dank BlindEagle wieder befreit war. Und dann gings ganz flott,BlindEagle hatte das Seil gelegt und ehe ich mich versah war er auch schon beim Hampier um ihm das Logbuch zu entlocken. Am Boden wurde dann geloggt und ich durfte es dann dem Hampier zurückgeben. Letztendlich war er dann aber doch sehr gastfreundlich. [;)]\n" +
                "Danke!\n" +
                "\n" +
                "Liebe Grüße\n" +
                "Biene Maya";
        logEntry20.Timestamp = DATE_PATTERN.parse("2011-05-27T19:00:00Z");
        logEntry20.Id = 163319268L;
        this.logEntries.add(logEntry20);

        return true;
    }

}
