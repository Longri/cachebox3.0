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
package de.longri.cachebox3.gui.stages;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public abstract class AbstractAction {

    protected final static boolean NOT_ENABLED = true;
    protected final static boolean ENABLED = false;

    protected final static Logger log = LoggerFactory.getLogger(AbstractAction.class);

    protected final boolean functionDisabled;
    protected final String titleTranslationId;
    protected final int id;
    protected String nameExtension = "";

    /**
     * Constructor
     *
     * @param titleTranslationId = Translation ID
     * @param id                 = AbstractAction ID ( AID_xxxx )
     */
    public AbstractAction(String titleTranslationId, int id) {
        this(ENABLED, titleTranslationId, "", id);
    }

    public AbstractAction(boolean disabled, String titleTranslationId) {
        this(disabled, titleTranslationId, "", -3);
    }

    public AbstractAction(String titleTranslationId) {
        this(ENABLED, titleTranslationId, "", -3);
    }

    public AbstractAction(boolean disabled, String titleTranslationId, int id) {
        this(disabled, titleTranslationId, "", id);
    }

    public AbstractAction(boolean disabled, String titleTranslationId, String nameExtension, int id) {
        this.titleTranslationId = titleTranslationId;
        this.id = id;
        this.nameExtension = nameExtension;
        this.functionDisabled = disabled;
    }


    public abstract void execute();

    public String getTitleTranslationId() {
        return titleTranslationId;
    }

    public String getNameExtension() {
        if (functionDisabled)
            return nameExtension + " (Not implemented yet)";
        return nameExtension;
    }

    public int getId() {
        return id;
    }

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
