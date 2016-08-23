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
package de.longri.cachebox3.gui;

import com.badlogic.gdx.utils.Disposable;

/**
 * A wrapper class to bring the CB2 Activities to CB3
 * Created by Longri on 23.08.2016.
 */
public class ActivityBase extends Window implements Disposable {

    public final String name;

    public ActivityBase(String name) {
        this.name = name;
    }


    protected void finish() {

    }

    public void onShow() {

    }

    public void onHide() {

    }

    public void show() {

    }

    @Override
    public void dispose() {

    }
}
