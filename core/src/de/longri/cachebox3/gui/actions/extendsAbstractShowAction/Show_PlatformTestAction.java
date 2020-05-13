/*
 * Copyright (C) 2019 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractShowAction;
import de.longri.cachebox3.gui.actions.MenuID;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.platform_test.gui.PlatformTestView;

/**
 * Created by Longri on 18.03.19.
 */
public class Show_PlatformTestAction extends AbstractShowAction {

    public Show_PlatformTestAction() {
        super(PlatformTestView.class, isImplemented(), "PlatformTestView", MenuID.AID_TEST_Platform_View);
    }

    static boolean isImplemented() {
        FileHandle jsnFile = Gdx.files.internal("platform_test/tests.json");
        return !jsnFile.exists();
    }

    @Override
    public void execute() {
        if (isActVisible()) return;

        PlatformTestView view = new PlatformTestView();
        CB.viewmanager.showView(view);

    }

    @Override
    public boolean isActVisible() {
        return CB.viewmanager.getCurrentView() instanceof PlatformTestView;
    }

    @Override
    public boolean viewTypeEquals(AbstractView actView) {
        return actView.getClass().getName().equals(PlatformTestView.class.getName());
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.me5CacheBox;
    }
}
