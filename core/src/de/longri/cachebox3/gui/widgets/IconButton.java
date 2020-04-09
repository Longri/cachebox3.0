/*
 * Copyright (C) 2020 team-cachebox.de
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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_VisTextButton;

/**
 * Created by Longri on 15.05.2017.
 */
public class IconButton extends Catch_VisTextButton {
    protected com.badlogic.gdx.scenes.scene2d.ui.Image image;
    private float preferredHeight;
    private float preferredWidth;

    public IconButton(CharSequence text) {
        super(EMPTY);
        getLabel().setText(text);
        preferredHeight = super.getPrefHeight();
        preferredWidth = super.getPrefWidth();
        setSize(preferredWidth, preferredHeight);
    }

    public IconButton(CharSequence text, String styleName) {
        super(EMPTY, styleName);
        getLabel().setText(text);
        preferredHeight = super.getPrefHeight();
        preferredWidth = super.getPrefWidth();
        setSize(preferredWidth, preferredHeight);
    }

    public IconButton(CharSequence text, VisTextButtonStyle buttonStyle) {
        super(EMPTY, buttonStyle);
        getLabel().setText(text);
        preferredHeight = super.getPrefHeight();
        preferredWidth = super.getPrefWidth();
        setSize(preferredWidth, preferredHeight);
    }

    public IconButton(CharSequence text, Drawable icon) {
        super(EMPTY);
        getLabel().setText(text);
        setIcon(icon);
        setSize(preferredWidth, preferredHeight);
    }

    public IconButton(Drawable icon) {
        super(EMPTY);
        this.setIcon(icon);
        setSize(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        if (image != null) {
            this.getLabel().setHeight(this.getHeight());
            this.getCell(this.getLabel()).spaceLeft(CB.scaledSizes.MARGINx2);
            Drawable imageDrawable = image.getDrawable();
            if (imageDrawable != null)
                image.setBounds(this.getWidth() - (imageDrawable.getMinHeight() + CB.scaledSizes.MARGIN),
                        (this.getHeight() - imageDrawable.getMinHeight()) / 2,
                        imageDrawable.getMinWidth(),
                        imageDrawable.getMinHeight());
        }
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
        if (image == null) {
            image = new com.badlogic.gdx.scenes.scene2d.ui.Image(drawableIcon);
            this.getLabel().setAlignment(Align.center | Align.left);
            this.addActor(image);
        }
        image.setDrawable(drawableIcon);
        if (drawableIcon != null) {
            this.preferredHeight = Math.max(super.getPrefHeight(), drawableIcon.getMinHeight() + CB.scaledSizes.MARGINx2);
            this.preferredWidth = super.getPrefWidth() + drawableIcon.getMinWidth() + CB.scaledSizes.MARGINx2;
        } else {
            this.preferredHeight = super.getPrefHeight();
            this.preferredWidth = super.getPrefWidth();
        }
    }

    public float getPreferredHeight() {
        return preferredHeight;
    }

    public void setPreferredHeight(float preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    public float getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(float preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
}
