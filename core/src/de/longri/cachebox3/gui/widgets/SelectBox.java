/*
 * Copyright (C) 2017 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.skin.styles.SelectBoxStyle;

/**
 * Created by Longri on 15.05.2017.
 */
public class SelectBox<T extends SelectBoxItem> extends IconButton {

    private Array<T> entries;
    private SelectBoxStyle style;
    private final com.badlogic.gdx.scenes.scene2d.ui.Image selectIcon = new com.badlogic.gdx.scenes.scene2d.ui.Image();

    public SelectBox() {
        super("");
        setStyle(VisUI.getSkin().get("default", SelectBoxStyle.class));
        this.addActor(selectIcon);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public void set(Array<T> list) {
        this.entries = list;
    }

    public void setStyle(SelectBoxStyle style) {
        this.style = style;
        VisTextButtonStyle buttonStyle = new VisTextButtonStyle();
        buttonStyle.up = style.up;
        buttonStyle.down = style.down;
        buttonStyle.font = style.font;
        buttonStyle.fontColor = style.fontColor;
        super.setStyle(buttonStyle);
        this.setIcon(style.selectIcon);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                SelectBox.this.layout();
            }
        });
    }

    @Override
    public void layout() {
        super.layout();
        Drawable imageDrawable = image.getDrawable();
        if (imageDrawable != null) {
            float x = this.getWidth() - (imageDrawable.getMinHeight() + CB.scaledSizes.MARGINx2 + style.selectIcon.getMinWidth());
            float y = (this.getHeight() - imageDrawable.getMinHeight()) / 2;
            image.setBounds(x, y, imageDrawable.getMinHeight(), imageDrawable.getMinHeight());
        }
        selectIcon.setBounds(this.getWidth() - (style.selectIcon.getMinWidth() + CB.scaledSizes.MARGIN),
                (this.getHeight() - style.selectIcon.getMinHeight()) / 2,
                style.selectIcon.getMinWidth(),
                style.selectIcon.getMinHeight());

    }


    public void select(int index) {
        SelectBoxItem item = entries.get(index);
        this.setText(item.getName());
        this.setIcon(item.getDrawable());
        this.setSelectIcon(style.selectIcon);
        this.layout();
    }

    public void select(T item) {
        this.setText(item.getName());
        this.setIcon(item.getDrawable());
        this.setSelectIcon(style.selectIcon);
        this.layout();
    }

    private void setSelectIcon(Drawable drawable) {
        selectIcon.setDrawable(drawable);
    }

    @Override
    public float getPrefWidth() {
        if (style == null || style.selectIcon == null) {
            return super.getPrefWidth();
        }
        return super.getPrefWidth() + style.selectIcon.getMinWidth() + CB.scaledSizes.MARGINx2;
    }

    @Override
    public float getPrefHeight() {
        if (style == null || style.selectIcon == null) {
            return super.getPrefHeight();
        }
       return Math.max(super.getPrefHeight(), style.selectIcon.getMinHeight() + CB.scaledSizes.MARGINx2*2);
    }

}
