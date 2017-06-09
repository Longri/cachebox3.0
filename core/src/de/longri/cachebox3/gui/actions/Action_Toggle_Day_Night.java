/* 
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions;


import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 16.08.16.
 */
public class Action_Toggle_Day_Night extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_Toggle_Day_Night.class);

    public Action_Toggle_Day_Night() {
        super(NOT_IMPLEMENTED, "DayNight", MenuID.AID_DAY_NIGHT);
    }

    @Override
    public void execute() {


    }


    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.dayNight;
    }
}
