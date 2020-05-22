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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedListener;
import de.longri.cachebox3.gui.menu.QuickAction;
import de.longri.cachebox3.gui.menu.menuBtn1.contextmenus.Action_Switch_Autoresort;
import de.longri.cachebox3.gui.menu.menuBtn2.Action_HintDialog;
import de.longri.cachebox3.gui.menu.menuBtn5.Action_Switch_Torch;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonItem extends ListViewItem {
    final Drawable button;
    private final AbstractAction action;
    private Image mButtonIcon;
    private QuickAction quickActionEnum;
    private boolean needsLayout = true;
    private Drawable spriteDrawable;

    public QuickButtonItem(int listIndex, Drawable _button, QuickAction quickAction) {
        super(listIndex);
        button = _button;
        quickActionEnum = quickAction;
        action = quickAction.getAction();
        if (action == null) // to satisfy the formal test
            throw new IllegalStateException("quick Action is null");
        try {
            spriteDrawable = action.getIcon();
        } catch (Exception e) {
            throw new IllegalStateException(action.getTitleTranslationId() + " Action has no Icon");
        }

        mButtonIcon = new Image(spriteDrawable, Scaling.none, Align.center);
        addActor(mButtonIcon);

        if (action instanceof Action_HintDialog) {
            EventHandler.add((SelectedCacheChangedListener) event -> {
                spriteDrawable = action.getIcon();
                mButtonIcon.setDrawable(spriteDrawable);
                needsLayout = true;
                mButtonIcon.invalidate();
            });
        }
    }

    public void clicked() {
        action.execute();
        if (action instanceof Action_Switch_Torch || action instanceof Action_Switch_Autoresort) {
            spriteDrawable = action.getIcon();
            mButtonIcon.setDrawable(spriteDrawable);
            needsLayout = true;
            invalidate();
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
        if (button != null) button.draw(batch, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

    public QuickAction getAction() {
        return quickActionEnum;
    }

    @Override
    public void dispose() {
        mButtonIcon = null;
        quickActionEnum = null;
    }
}
