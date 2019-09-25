/*
 * Copyright (C) 2016 -2017 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.show_views;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.SpoilerView;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Show_SpoilerView extends Abstract_Action_ShowView {

    public Action_Show_SpoilerView() {
        super(SpoilerView.class, IMPLEMENTED, "ShowSpoiler", MenuID.AID_SHOW_SPOILER);
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof SpoilerView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(SpoilerView.class.getName());
    }

    @Override
    public void execute() {
        if (isActVisible()) return;
        CB.viewmanager.showView(new SpoilerView());
    }

    @Override
    public Drawable getIcon() {
        boolean hasSpoiler = CB.selectedCachehasSpoiler();
        if (hasSpoiler)
            return CB.getSkin().getMenuIcon.imagesIcon;
        else
            return CB.getSkin().getMenuIcon.imagesIconOff;
    }
}
