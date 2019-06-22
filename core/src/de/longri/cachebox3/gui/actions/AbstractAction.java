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
package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public abstract class AbstractAction {

    protected final static boolean NOT_IMPLEMENTED = true;
    protected final static boolean IMPLEMENTED = false;

    protected final static Logger log = LoggerFactory.getLogger(AbstractAction.class);

    protected final boolean functionDisabled;
    protected final String name;
    protected final int id;
    protected String nameExtention = "";

    /**
     * Constructor
     *
     * @param name = Translation ID
     * @param id   = AbstractAction ID ( AID_xxxx )
     */
    public AbstractAction(String name, int id) {
        this.name = name;
        this.id = id;
        this.functionDisabled = IMPLEMENTED;
    }

    public AbstractAction(boolean disabled, String name, int id) {
        //super();
        this.name = name;
        this.id = id;
        this.functionDisabled = disabled;
    }

    public AbstractAction(boolean disabled, String name, String nameExtention, int id) {
        //super();
        this.name = name;
        this.id = id;
        this.nameExtention = nameExtention;
        this.functionDisabled = disabled;
    }


    public abstract void execute();

    public String getName() {
        return name;
    }

    public String getNameExtention() {
        return nameExtention;
    }

    public int getId() {
        return id;
    }

    /**
     * hiermit kann der Menüpunkt enabled oder disabled werden
     *
     * @return
     */
    public final boolean getEnabled() {
        return !functionDisabled;
    }

    public abstract Drawable getIcon();

    public boolean getIsCheckable() {
        return false;
    }

    public boolean getIsChecked() {
        return false;
    }

}
