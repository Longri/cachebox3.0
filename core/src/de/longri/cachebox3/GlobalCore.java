/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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
package de.longri.cachebox3;

import org.slf4j.LoggerFactory;


/**
 * @author ging-buh
 * @author arbor95
 * @author longri
 */
public class GlobalCore {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GlobalCore.class);
    public static final int CurrentRevision = 20160727;

    public static final String CurrentVersion = "3.0.";
    public static final String VersionPrefix = "test";


    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");

    public static final String AboutMsg = "Team Cachebox (2011-2016)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";


    private static boolean isTestVersionCheked = false;
    private static boolean isTestVersion = false;

    public static boolean isTestVersion() {
        if (isTestVersionCheked) return isTestVersion;

        isTestVersion = VersionPrefix.contains("Test") || VersionPrefix.contains("test");
        isTestVersionCheked = true;
        return isTestVersion;
    }

    public static String getVersionString() {
        final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  " + (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
        return ret;
    }
}
