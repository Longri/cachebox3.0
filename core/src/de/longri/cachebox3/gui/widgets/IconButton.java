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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_VisTextButton;
import de.longri.cachebox3.utils.HSV_Color;

/**
 * Created by Longri on 15.05.2017.
 */
public class IconButton extends Catch_VisTextButton {
    protected final com.badlogic.gdx.scenes.scene2d.ui.Image image;
    protected float preferredHeight;
    protected float preferredWidth;

    public IconButton(String text) {
        super(text);
        setStyle(getVisTextButtonStyle());
        image = new com.badlogic.gdx.scenes.scene2d.ui.Image(new ColorDrawable(new HSV_Color(0)));
        this.getLabel().setAlignment(Align.center | Align.left);
        this.addActor(image);
        setSize(super.getPrefWidth(), super.getPrefHeight());
    }

    public IconButton(Drawable icon) {
        super("");
        setStyle(getVisTextButtonStyle());
        image = new com.badlogic.gdx.scenes.scene2d.ui.Image(icon);
        this.getLabel().setAlignment(Align.center | Align.left);
        this.addActor(image);
        this.setIcon(icon);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public IconButton(CharSequence text) {
        super(EMPTY);
        getLabel().setText(text);
        setStyle(getVisTextButtonStyle());
        image = new com.badlogic.gdx.scenes.scene2d.ui.Image(new ColorDrawable(new HSV_Color(0)));
        this.getLabel().setAlignment(Align.center | Align.left);
        this.addActor(image);
        setSize(super.getPrefWidth(), super.getPrefHeight());
    }

    public IconButton(CharSequence text, Drawable icon) {
        super(EMPTY);
        getLabel().setText(text);
        setStyle(getVisTextButtonStyle());
        image = new com.badlogic.gdx.scenes.scene2d.ui.Image(icon);
        this.getLabel().setAlignment(Align.center | Align.left);
        this.addActor(image);
        this.setIcon(icon);
        setSize(getPrefWidth(), getPrefHeight());
    }

    private VisTextButtonStyle getVisTextButtonStyle() {
        return VisUI.getSkin().get(VisTextButtonStyle.class);
    }

    @Override
    public void layout() {
        this.getLabel().setHeight(this.getHeight());
        this.getCell(this.getLabel()).spaceLeft(CB.scaledSizes.MARGINx2);
        Drawable imageDrawable = image.getDrawable();
        if (imageDrawable != null)
            image.setBounds(this.getWidth() - (imageDrawable.getMinHeight() + CB.scaledSizes.MARGIN),
                    (this.getHeight() - imageDrawable.getMinHeight()) / 2,
                    imageDrawable.getMinWidth(),
                    imageDrawable.getMinHeight());
        super.layout();
    }

    @Override
    public float getPrefHeight() {
        return this.preferredHeight;
    }

    @Override
    public float getPrefWidth() {
        return this.preferredWidth;
    }

    protected void setIcon(Drawable drawableIcon) {
        image.setDrawable(drawableIcon);
        if (drawableIcon != null) {
            this.preferredHeight = Math.max(super.getPrefHeight(), drawableIcon.getMinHeight() + CB.scaledSizes.MARGINx2);
            this.preferredWidth = super.getPrefWidth() + drawableIcon.getMinWidth() + CB.scaledSizes.MARGINx2;
        } else {
            this.preferredHeight = super.getPrefHeight();
            this.preferredWidth = super.getPrefWidth();
        }
    }

}
