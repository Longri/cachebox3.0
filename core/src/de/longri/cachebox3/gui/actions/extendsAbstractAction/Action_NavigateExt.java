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
package de.longri.cachebox3.gui.actions.extendsAbstractAction;


import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.menu.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 16.08.16.
 */
public class Action_NavigateExt extends AbstractAction {
    final static Logger log = LoggerFactory.getLogger(Action_NavigateExt.class);

    public Action_NavigateExt() {
        super(ENABLED, "NavigateTo");
    }

    @Override
    public void execute() {
        Menu menu = new Menu("NavigateTo");
        menu.addMenuItem("", "Google", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.Google);
        });
        menu.addMenuItem("", "Sygic", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.Sygic);
        });
        menu.addMenuItem("", "Orux", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.Orux);
        });
        menu.addMenuItem("", "OsmAnd", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.OsmAnd);
        });
        menu.addMenuItem("", "OsmAnd2", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.OsmAnd2);
        });
        menu.addMenuItem("", "Waze", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.Waze);
        });
        menu.addMenuItem("", "Navigon", null, () -> {
            PlatformConnector.navigate(PlatformConnector.Navigation.Navigon);
        });
        menu.show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.me3Navigate;
    }
}
