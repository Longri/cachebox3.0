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
package de.longri.cachebox3.gui.utils;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * Created by Longri on 13.08.16.
 */
public class IgnoreTouchInputListener extends InputListener {

    public static IgnoreTouchInputListener INSTANCE=new IgnoreTouchInputListener();


    private IgnoreTouchInputListener(){}


    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        event.cancel();
        return false;
    }
}
