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

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.FieldNoteListItemStyle;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.types.FieldNoteEntry;

import java.text.SimpleDateFormat;

/**
 * Created by Longri on 31.08.2017.
 */
public class FieldNotesViewItem extends ListViewItem {

    private boolean needsLayout = true;
    private FieldNoteEntry entry;
    final private FieldNoteListItemStyle style;

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

        VisTable headerTable = new VisTable();
        headerTable.add(new Image(this.entry.type.getDrawable(style.typeStyle)));

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.style.headerFont;
        nameLabelStyle.fontColor = this.style.headerFontColor;
        VisLabel nameLabel = new VisLabel(this.entry.CacheName, nameLabelStyle);
        nameLabel.setWrap(true);
        headerTable.add(nameLabel).left().padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();

        //TODO replace with formatter from localisation settings
        SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy");
        String dateString = postFormater.format(entry.timestamp);
        VisLabel dateLabel = new VisLabel(dateString, nameLabelStyle);
        headerTable.add(dateLabel).padRight(CB.scaledSizes.MARGINx4).right();

        //TODO set Background for header over style

        headerTable.pack();
        headerTable.layout();
        this.add(headerTable).left().expandX().fillX();

        this.row().padTop(CB.scaledSizes.MARGINx4);

        Label.LabelStyle commentLabelStyle = new Label.LabelStyle();
        commentLabelStyle.font = this.style.descriptionFont;
        commentLabelStyle.fontColor = this.style.descriptionFontColor;
        VisLabel commentLabel = new VisLabel(entry.comment, commentLabelStyle);
        commentLabel.setWrap(true);
        this.add(commentLabel).expand().fill();


        super.layout();
        needsLayout = false;
    }

    @Override
    public void dispose() {

    }
}
