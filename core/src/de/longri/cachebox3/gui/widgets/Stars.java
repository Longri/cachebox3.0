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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.skin.styles.StarsStyle;

/**
 * Created by Longri on 07.09.2016.
 */
public class Stars extends AbstractIntValueChangedWidget {

    private final Image image1, image2, image3, image4, image5;
    private final StarsStyle style;

    public Stars(int value, StarsStyle style) {
        this.style = style;

        this.add(image1 = new Image()).left();
        this.add(image2 = new Image()).left();
        this.add(image3 = new Image()).left();
        this.add(image4 = new Image()).left();
        this.add(image5 = new Image()).left();
        this.add((Actor) null).left().expandX().fillX();

        setValue(value);
    }

    public void setValue(int value) {

        if (style == null) return;
        if (value < 0 || value > 10) {
            throw new RuntimeException("value must between 0-10! it was:" + Integer.toString(value));
        }

        Drawable star1 = value >= 2 ? style.star : value >= 1 ? style.star_half : style.star0;
        Drawable star2 = value >= 4 ? style.star : value >= 3 ? style.star_half : style.star0;
        Drawable star3 = value >= 6 ? style.star : value >= 5 ? style.star_half : style.star0;
        Drawable star4 = value >= 8 ? style.star : value >= 7 ? style.star_half : style.star0;
        Drawable star5 = value == 10 ? style.star : value >= 9 ? style.star_half : style.star0;

        setImage(image1, star1);
        setImage(image2, star2);
        setImage(image3, star3);
        setImage(image4, star4);
        setImage(image5, star5);
        this.invalidateHierarchy();
    }

    private void setImage(Image image, Drawable drawable) {
        image.setDrawable(drawable);
        image.setSize(image.getPrefWidth(), image.getPrefHeight());
    }
}
