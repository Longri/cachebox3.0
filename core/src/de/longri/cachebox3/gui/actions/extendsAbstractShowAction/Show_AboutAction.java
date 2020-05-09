/*
 * Copyright (C) 2016 - 2020 team-cachebox.de
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
package de.longri.cachebox3.gui.actions.extendsAbstractShowAction;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractShowAction;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.AboutView;
import de.longri.cachebox3.gui.views.AbstractView;

/**
 * Created by Longri on 24.07.16.
 */
public class Show_AboutAction extends AbstractShowAction {

    public Show_AboutAction() {
        super(AboutView.class, ENABLED, "about", MenuID.AID_SHOW_CACHELIST);
    }


    @Override
    public void execute() {
        if (isActVisible()) return;
        AboutView view = new AboutView();
        CB.viewmanager.showView(view);
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getCurrentView() instanceof AboutView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(AboutView.class.getName());
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.me5CacheBox;
    }
}
