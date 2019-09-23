/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
import de.longri.cachebox3.gui.views.DescriptionView;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Show_DescriptionView extends Abstract_Action_ShowView {
    public Action_Show_DescriptionView() {
        super(DescriptionView.class, IMPLEMENTED, "Description", MenuID.AID_SHOW_DESCRIPTION);
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getActView() instanceof DescriptionView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(DescriptionView.class.getName());
    }

    @Override
    public void execute() {
        if (isActVisible()) return;
        DescriptionView view = new DescriptionView();
        CB.viewmanager.showView(view);
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.docIcon;
    }
}
