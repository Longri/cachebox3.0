/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Created by Longri on 20.03.2017.
 */
public class BuildInfo {

    private BuildInfo() {
    }

    private static String rev;
    private static String detail;

    public static String getRevison() {
        if (rev == null) readInfo();
        return rev;
    }

    public static String getDetail() {
        if (detail == null) readInfo();
        return detail;
    }

    private static void readInfo() {
        FileHandle fileHandle = Gdx.files.internal("build.info");
        String info = fileHandle.readString("utf-8");

        int pos = info.indexOf(" ");
        rev = info.substring(0, pos);
        detail = info.substring(pos);
    }

    /**
     * Set only from JUnit test
     *
     * @param testBuildInfo
     */
    public static void setTestBuildInfo(String testBuildInfo) {
        rev = testBuildInfo;
    }
}
