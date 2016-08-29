/*
 * Copyright (C) 2016 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import de.longri.cachebox3.gui.ActivityBase;

/**
 * Created by Longri on 29.08.2016.
 */
public class SelectDB_Activity extends ActivityBase {

    public interface IReturnListener {
        public void back();
    }

    private final IReturnListener returnListener;

    public SelectDB_Activity(IReturnListener returnListener) {
        super("select DB dialog");
        this.returnListener = returnListener;
    }


}
