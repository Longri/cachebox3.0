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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.actions.QuickActions;
import de.longri.cachebox3.gui.views.listview.ListViewItem;

/**
 * Created by Longri on 09.09.16.
 */
public class QuickButtonItem extends ListViewItem {
    final Drawable background;
    private AbstractAction mAction;
    private Image mButtonIcon;
    private String mActionDesc;
    private Button mButton;
    private QuickActions quickActionsEnum;
    private int autoResortState = -1;
    private int spoilerState = -1;
    private int hintState = -1;
    private int torchState = -1;
    private boolean needsLayout = true;
    private final float imageMargin;

    public QuickButtonItem(int listIndex, Drawable background, AbstractAction action, String Desc, QuickActions type) {
        super(listIndex);
        this.background = background;
        quickActionsEnum = type;
        mAction = action;
        mActionDesc = Desc;
        imageMargin = CB.scaledSizes.MARGIN_HALF;
        SpriteDrawable spriteDrawable = null;

        try {
            spriteDrawable = new SpriteDrawable(action.getIcon());
        } catch (Exception e) {
            throw new IllegalStateException(action.getName() + " Action has no Icon");
        }

        mButtonIcon = new Image(spriteDrawable);
        this.addActor(mButtonIcon);
        this.addListener(clickListener);
    }

    ClickListener clickListener = new ClickListener() {

        public void clicked(InputEvent event, float x, float y) {
            mAction.execute();
        }

    };


    @Override
    public void layout() {
        if (needsLayout || super.needsLayout()) {
            mButtonIcon.setBounds(imageMargin, imageMargin, getWidth() - (2 * imageMargin), getHeight() - (2 * imageMargin));
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
        mAction=null;
        mButtonIcon=null;
        mActionDesc=null;
        mButton=null;
        quickActionsEnum=null;
    }
}
