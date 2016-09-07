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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.types.CacheTypes;

/**
 * Created by Longri on 05.09.2016.
 */
public class CacheListItem extends ListViewItem {

    private final CacheListItemStyle style;
    private final CacheTypes type;
    private final CharSequence cacheName;
    private boolean needsLayout = true;
    private Image arrowImage;
    private VisLabel distanceLabel;
    private boolean distanceOrBearingChanged = true;


    public CacheListItem(int listIndex, CacheTypes type, CharSequence cacheName) {
        super(listIndex);
        this.style = VisUI.getSkin().get("default", CacheListItemStyle.class);
        this.type = type;
        this.cacheName = cacheName;
    }


    public void layout() {
//        this.setDebug(true, true);
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();

        VisTable iconTable = new VisTable();
        iconTable.add(type.getCacheWidget());

        iconTable.pack();
        iconTable.layout();

        this.add(iconTable).left().top();


        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.nameFont;
        nameLabelStyle.fontColor = this.style.nameFontColor;
        VisLabel nameLabel = new VisLabel(cacheName, nameLabelStyle);
        nameLabel.setWrap(true);
        this.add(nameLabel).top().expandX().fillX();


        VisTable arrowTable = new VisTable();
        arrowImage = new Image(this.style.arrow);
        arrowImage.setOrigin(this.style.arrow.getMinWidth() / 2, this.style.arrow.getMinHeight() / 2);

        Label.LabelStyle distanceLabelStyle = new Label.LabelStyle();
        distanceLabelStyle.font = this.style.distanceFont;
        distanceLabelStyle.fontColor = this.style.distanceFontColor;
        distanceLabel = new VisLabel("---- --", distanceLabelStyle);

        arrowTable.add(arrowImage);
        arrowTable.row();
        arrowTable.add(distanceLabel).padTop(CB.scaledSizes.MARGIN);
        this.add(arrowTable).right();

        this.row();

        super.layout();
        needsLayout = false;
    }

    public boolean update(float bearing, CharSequence distance) {
        if (!distanceOrBearingChanged) return false;
        arrowImage.setRotation(bearing);
        distanceLabel.setText(distance);

        arrowImage.layout();
        distanceLabel.layout();
        distanceOrBearingChanged = false;
        return true;
    }

    public void posOrBearingChanged() {
        distanceOrBearingChanged = true;
    }


    public static class CacheListItemStyle {
        BitmapFont nameFont;
        Color nameFontColor;
        Drawable arrow;
        BitmapFont distanceFont;
        Color distanceFontColor;
    }

}
