/*
 * Copyright (C) 2014-2017 team-cachebox.de
 *
 * Licensed under the : GNU General protected License (GPL);
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
package de.longri.cachebox3.settings;

import de.longri.cachebox3.settings.types.SettingsList;

/**
 * Created by Longri on 12.01.2017.
 */
public abstract class Settings_Const {

    public static final SettingsList settingsList = new SettingsList();

    protected static final de.longri.cachebox3.settings.types.SettingMode DEVELOPER = de.longri.cachebox3.settings.types.SettingMode.DEVELOPER;
    protected static final de.longri.cachebox3.settings.types.SettingMode NORMAL = de.longri.cachebox3.settings.types.SettingMode.Normal;
    protected static final de.longri.cachebox3.settings.types.SettingMode EXPERT = de.longri.cachebox3.settings.types.SettingMode.Expert;
    protected static final de.longri.cachebox3.settings.types.SettingMode NEVER = de.longri.cachebox3.settings.types.SettingMode.Never;

    protected static final Integer Level[] = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    protected static final Integer CrossLevel[] = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    protected static final String FOUND = "<br>###finds##, ##time##, Found it with Cachebox!";
    protected static final String ATTENDED = "<br>###finds##, ##time##, Have been there!";
    protected static final String WEBCAM = "<br>###finds##, ##time##, Photo taken!";
    protected static final String DNF = "<br>##time##. Could not find the cache!";
    protected static final String LOG = "Logged it with Cachebox!";
    protected static final String DISCOVERD = "<br> ##time##, Discovered it with Cachebox!";
    protected static final String VISITED = "<br> ##time##, Visited it with Cachebox!";
    protected static final String DROPPED = "<br> ##time##, Dropped off with Cachebox!";
    protected static final String PICKED = "<br> ##time##, Picked it with Cachebox!";
    protected static final String GRABED = "<br> ##time##, Grabed it with Cachebox!";

    protected static final Integer[] approach = new Integer[]{0, 2, 10, 25, 50, 100, 200, 500, 1000};
    protected static final Integer[] TrackDistanceArray = new Integer[]{1, 3, 5, 10, 20};
    protected static final String[] Navis = new String[]{"Navigon", "Google", "Copilot", "OsmAnd", "OsmAnd2", "Waze", "Orux", "Ask"};


}
