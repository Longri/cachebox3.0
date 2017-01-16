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
package de.longri.cachebox3.gui.actions.show_vies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.SolverView;
import de.longri.cachebox3.gui.views.SpoilerView;
import de.longri.cachebox3.utils.IconNames;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Show_SpoilerView extends Abstract_Action_ShowView {
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

    public Action_Show_SpoilerView() {
        super("ShowSpoiler", MenuID.AID_SHOW_SOLVER);
    }


    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        if (CB.viewmanager.getActView() instanceof SpoilerView) {
            SpoilerView solverView = (SpoilerView) CB.viewmanager.getActView();
            return solverView.getContextMenu();
        }
        return null;
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
        SpoilerView view = new SpoilerView();
        CB.viewmanager.showView(view);
    }

    int spoilerState = -1;
    Sprite SpoilerIcon;

    @Override
    public Sprite getIcon() {
        boolean hasSpoiler = CB.selectedCachehasSpoiler();
        if (hasSpoiler && spoilerState != 1) {
            SpoilerIcon = CB.getSprite(IconNames.imagesIcon.name());
            spoilerState = 1;
        } else if (!hasSpoiler && spoilerState != 0) {
            SpoilerIcon = new Sprite(CB.getSprite(IconNames.imagesIcon.name()));
            SpoilerIcon.setColor(DISABLE_COLOR);
            spoilerState = 0;
        }
        return SpoilerIcon;
    }
}
