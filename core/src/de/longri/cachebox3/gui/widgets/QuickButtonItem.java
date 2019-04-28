/*
 * Copyright (C) 2016-2018 team-cachebox.de
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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.gui.actions.show_activities.Action_HintDialog;
import de.longri.cachebox3.gui.actions.show_activities.Action_Switch_Torch;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonItem extends ListViewItem {
    final Drawable background;
    private AbstractAction mAction;
    private Image mButtonIcon;
    private CharSequence mActionDesc;
    private Button mButton;
    private QuickActions quickActionsEnum;
    private int autoResortState = -1;
    private int spoilerState = -1;
    private int hintState = -1;
    private int torchState = -1;
    private boolean needsLayout = true;
    private Drawable spriteDrawable;

    public QuickButtonItem(int listIndex, Drawable background, final AbstractAction action, CharSequence Desc, QuickActions type) {
        super(listIndex);
        this.background = background;
        quickActionsEnum = type;
        mAction = action;
        mActionDesc = Desc;
        try {
            spriteDrawable = action.getIcon();
        } catch (Exception e) {
            throw new IllegalStateException(action.getName() + " Action has no Icon");
        }

        mButtonIcon = new Image(spriteDrawable, Scaling.none, Align.center);
        this.addActor(mButtonIcon);

        if (action instanceof Action_HintDialog) {
            EventHandler.add(new SelectedCacheChangedListener() {
                @Override
                public void selectedCacheChanged(SelectedCacheChangedEvent event) {
                    spriteDrawable = action.getIcon();
                    mButtonIcon.setDrawable(spriteDrawable);
                    needsLayout = true;
                    QuickButtonItem.this.invalidate();
                }
            });
        }
    }

    public void clicked() {
        mAction.execute();
        if (mAction instanceof Action_Switch_Torch) {
            spriteDrawable = mAction.getIcon();
            mButtonIcon.setDrawable(spriteDrawable);
            needsLayout = true;
            this.invalidate();
        }
    }

    @Override
    public void layout() {
        if (needsLayout || super.needsLayout()) {
            if (spriteDrawable != null) {
                float ratio = spriteDrawable.getMinWidth() / spriteDrawable.getMinHeight();
                float imageHeight = getHeight() - CB.scaledSizes.MARGINx2;
                float imageWidth = imageHeight * ratio;
                float x = (getWidth() - imageWidth) / 2;
                float y = (getHeight() - imageHeight) / 2;
                mButtonIcon.setBounds(x, y, imageWidth, imageHeight);
            }
        }
        needsLayout = false;
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();
        needsLayout = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        background.draw(batch, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        super.draw(batch, parentAlpha);
    }

    public QuickActions getAction() {
        return quickActionsEnum;
    }

    @Override
    public void dispose() {
        mAction = null;
        mButtonIcon = null;
        mActionDesc = null;
        mButton = null;
        quickActionsEnum = null;
    }
}
