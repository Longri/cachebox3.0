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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.FieldNoteListItemStyle;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.types.FieldNoteEntry;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Longri on 31.08.2017.
 */
public class FieldNotesViewItem extends ListViewItem {
    private final static SimpleDateFormat postFormatter = new SimpleDateFormat("dd.MMM.yy (HH:mm)", Locale.getDefault());

    final private FieldNoteListItemStyle style;

    private boolean needsLayout = true;
    private FieldNoteEntry entry;
    private VisTable headerTable;

    public FieldNotesViewItem(int listIndex, FieldNoteEntry entry, FieldNoteListItemStyle style) {
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
        headerTable.add((Actor) null).left().padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();

        String foundNumber = "";
        if (entry.foundNumber > 0) {
            foundNumber = "#" + entry.foundNumber + " @ ";
        }

        VisLabel dateLabel = new VisLabel(foundNumber + postFormatter.format(entry.timestamp), headerLabelStyle);
        headerTable.add(dateLabel).padRight(CB.scaledSizes.MARGINx4).right();
        headerTable.pack();
        headerTable.layout();
        this.add(headerTable).left().expandX().fillX();

        this.row().padTop(CB.scaledSizes.MARGINx4);

        VisTable cacheTable = new VisTable();

        VisTable iconTable = new VisTable();
        iconTable.add(entry.cacheType.getCacheWidget(style.cacheTypeStyle, null, null));
        iconTable.pack();
        iconTable.layout();

        cacheTable.add(iconTable).left().padRight(CB.scaledSizes.MARGINx4);

        VisLabel nameLabel = new VisLabel(entry.CacheName, headerLabelStyle);
        nameLabel.setWrap(true);
        cacheTable.add(nameLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();

        cacheTable.row();

        cacheTable.add((Actor) null).left().padRight(CB.scaledSizes.MARGINx4);

        VisLabel gcLabel = new VisLabel(entry.gcCode, headerLabelStyle);
        gcLabel.setWrap(true);
        cacheTable.add(gcLabel).padRight(CB.scaledSizes.MARGIN).expandX().fillX();


        this.add(cacheTable).top().expandX().fillX();
        this.row().padTop(CB.scaledSizes.MARGINx4);


        VisLabel commentLabel = new VisLabel(entry.comment, commentLabelStyle);
        commentLabel.setWrap(true);
        this.add(commentLabel).expand().fill();

        super.layout();
        needsLayout = false;
    }

    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        super.drawBackground(batch, parentAlpha, x, y);
        if (style.headerBackground != null && headerTable != null) {
            float height = headerTable.getHeight() + this.getPadTop() + this.getPadBottom();
            style.headerBackground.draw(batch, x, y + (getHeight() - height), getWidth(), height);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (entry.uploaded && style.upploadedOverlay != null) {
            //draw uploaded overlay
            style.upploadedOverlay.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public void dispose() {

    }
}
