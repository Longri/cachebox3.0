/*
 * Copyright (C) 2017-2018 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.DraftListItemStyle;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.types.DraftEntry;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Longri on 31.08.2017.
 */
public class DraftsViewItem extends ListViewItem {
    private final static SimpleDateFormat postFormatter = new SimpleDateFormat("dd.MMM.yy (HH:mm)", Locale.getDefault());

    final private DraftListItemStyle style;

    private boolean needsLayout = true;
    private DraftEntry entry;
    private VisTable headerTable;

    public DraftsViewItem(int listIndex, DraftEntry entry, DraftListItemStyle style) {
        super(listIndex);
        this.entry = entry;
        this.style = style;
    }

    @Override
    public synchronized void layout() {
//        this.setDebug(true, false);
        if (!needsLayout) {
            super.layout();
            return;
        }

        this.clear();

        Label.LabelStyle headerLabelStyle = new Label.LabelStyle();
        headerLabelStyle.font = this.style.headerFont;
        headerLabelStyle.fontColor = this.style.headerFontColor;

        Label.LabelStyle commentLabelStyle = new Label.LabelStyle();
        commentLabelStyle.font = this.style.descriptionFont;
        commentLabelStyle.fontColor = this.style.descriptionFontColor;

        headerTable = new VisTable();
        headerTable.add(new Image(this.entry.type.getDrawable(style.typeStyle)));
        if (entry.uploaded) headerTable.add(new Image(style.uploadedIcon));
        headerTable.add((Actor) null).left().padLeft(CB.scaledSizes.MARGINx2).expandX().fillX();

        String foundNumber = "";
        if (entry.foundNumber > 0) {
            foundNumber = "#" + entry.foundNumber + " @ ";
        }

        VisLabel dateLabel = new VisLabel(foundNumber + postFormatter.format(entry.timestamp), headerLabelStyle);
        headerTable.add(dateLabel).padRight(CB.scaledSizes.MARGINx2).right();
        headerTable.pack();
        headerTable.layout();
        this.add(headerTable).left().expandX().fillX();

        this.row().padTop(CB.scaledSizes.MARGINx2);

        VisTable entryTable = new VisTable();


        VisTable cacheTable = new VisTable();

        VisTable iconTable = new VisTable();
        iconTable.add(entry.cacheType.getCacheWidget(style.cacheTypeStyle, null, null, null, null));
        iconTable.pack();
        iconTable.layout();

        cacheTable.add(iconTable).left().padRight(CB.scaledSizes.MARGINx2);

        VisLabel nameLabel = new VisLabel(entry.CacheName, headerLabelStyle);
        nameLabel.setWrap(true);
        cacheTable.add(nameLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        cacheTable.row();

        cacheTable.add((Actor) null).left().padRight(CB.scaledSizes.MARGINx2);

        VisLabel gcLabel = new VisLabel(entry.gcCode, headerLabelStyle);
        gcLabel.setWrap(true);
        cacheTable.add(gcLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();


        entryTable.add(cacheTable).top().expandX().fillX();
        entryTable.row().padTop(CB.scaledSizes.MARGINx2);


        VisLabel commentLabel = new VisLabel(entry.comment, commentLabelStyle);
        commentLabel.setWrap(true);
        entryTable.add(commentLabel).expand().fill();


        if (entry.uploaded) entryTable.setColor(new Color(1, 1, 1, 0.4f));


        this.add(entryTable).expand().fill();

        super.layout();
        needsLayout = false;
    }

    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        super.drawBackground(batch, parentAlpha, x, y);
        super.drawBackground(batch, 0.4f, x, y);
        if (style.headerBackground != null && headerTable != null) {
            float height = headerTable.getHeight() + this.getPadTop() + this.getPadBottom();
            batch.setColor(1, 1, 1, 1);
            style.headerBackground.draw(batch, x, y + (getHeight() - height), getWidth(), height);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (entry.uploaded && style.uploadedOverlay != null) {
            //draw uploaded overlay
            style.uploadedOverlay.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public void dispose() {

    }
}
