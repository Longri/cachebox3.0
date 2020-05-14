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
package de.longri.cachebox3.gui.menu.menuBtn1.todo;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractShowAction;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.views.TrackableListView;

/**
 * Created by Longri on 24.07.16.
 */
public class Show_TrackableListAction extends AbstractShowAction {
    public Show_TrackableListAction() {
        super(TrackableListView.class, NOT_ENABLED, "TBList", MenuID.AID_SHOW_TRACKABLELIST);
    }

    @Override
    public void execute() {
        if (isActVisible()) return;

        TrackableListView view = new TrackableListView();
        CB.viewmanager.showView(view);

    }

    public Drawable getIcon() {
        return CB.getSkin().menuIcon.me1TrackableList;
    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getCurrentView() instanceof TrackableListView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(TrackableListView.class.getName());
    }
}
