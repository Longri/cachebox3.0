/*
 * Copyright (C) 2019 team-cachebox.de
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
package org.slf4j.impl;

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;

/**
 * Created by Longri on 2019-03-09.
 */
public class LoggerInit {

    static public void initlogger() {
        if (!LibgdxLogger.INITIALIZED) {
            LibgdxLogger l = new LibgdxLogger("init");
            GPSData.addChangeListener(new GPSDataListener() {
                @Override
                public void valueChanged() {

                }
            });
        }
    }


}
