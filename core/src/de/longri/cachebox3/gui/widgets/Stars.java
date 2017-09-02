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

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Created by Longri on 07.09.2016.
 */
public class Stars extends VisTable {

    private final Image image1, image2, image3, image4, image5;

    public Stars(int value) {

        this.add(image1 = new Image());
        this.add(image2 = new Image());
        this.add(image3 = new Image());
        this.add(image4 = new Image());
        this.add(image5 = new Image());

        setValue(value);
    }

    public void setValue(int value) {

        if (value < 0 || value > 10) {
            throw new RuntimeException("value must between 0-10! it was:" + Integer.toString(value));
        }


        Skin skin = VisUI.getSkin();
        Drawable star1 = value >= 2 ? skin.getDrawable("star") : value >= 1 ? skin.getDrawable("star_half") : skin.getDrawable("star0");
        Drawable star2 = value >= 4 ? skin.getDrawable("star") : value >= 3 ? skin.getDrawable("star_half") : skin.getDrawable("star0");
        Drawable star3 = value >= 6 ? skin.getDrawable("star") : value >= 5 ? skin.getDrawable("star_half") : skin.getDrawable("star0");
        Drawable star4 = value >= 8 ? skin.getDrawable("star") : value >= 7 ? skin.getDrawable("star_half") : skin.getDrawable("star0");
        Drawable star5 = value == 10 ? skin.getDrawable("star") : value >= 9 ? skin.getDrawable("star_half") : skin.getDrawable("star0");

        setImage(image1, star1);
        setImage(image2, star2);
        setImage(image3, star3);
        setImage(image4, star4);
        setImage(image5, star5);
        this.invalidateHierarchy();
        this.pack();
        this.layout();
    }

    private void setImage(Image image, Drawable drawable) {
        image.setDrawable(drawable);
        image.setSize(image.getPrefWidth(), image.getPrefHeight());
    }
}
