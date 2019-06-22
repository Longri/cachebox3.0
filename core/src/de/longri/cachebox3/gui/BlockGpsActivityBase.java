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
package de.longri.cachebox3.gui;


import de.longri.cachebox3.CB;

/**
 * Base Activity that block all Gps-Events with active showing
 * Created by Longri on 08.05.2018.
 */
public class BlockGpsActivityBase extends ActivityBase {
    public BlockGpsActivityBase(String name) {
        super(name);
    }

    public void show() {
        super.show();
        CB.viewmanager.locationReceiver.stopForgroundUpdates();
    }

    public void finish() {
        super.finish();
        CB.viewmanager.locationReceiver.resume();
    }

}
