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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.gui.views.listview.ListViewItem;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonItem extends ListViewItem {

    private AbstractAction mAction;
    private Image mButtonIcon;
    private String mActionDesc;
    private Button mButton;
    private QuickActions quickActionsEnum;
    private int autoResortState = -1;
    private int spoilerState = -1;
    private int hintState = -1;
    private int torchState = -1;


    public QuickButtonItem(int listIndex, AbstractAction action, String Desc, QuickActions type) {
        super(listIndex);
        quickActionsEnum = type;
        mAction = action;
        //mButtonIcon = new Image();
        mButtonIcon.setDrawable(new SpriteDrawable(action.getIcon()));

    }

    public QuickActions getAction() {
        return quickActionsEnum;
    }
}
