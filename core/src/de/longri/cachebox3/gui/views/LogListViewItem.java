/*
 * Copyright (C) 2020 - 2018 team-cachebox.de
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
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.LogListItemStyle;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.types.LogEntry;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Longri on 31.05.2017.
 */
public class LogListViewItem extends ListViewItem {

    private final LogListItemStyle logListItemStyle;
    private final LogEntry logEntry;
    private boolean needsLayout = true;

    public LogListViewItem(int listIndex, LogEntry logEntry) {
        super(listIndex);
        this.logListItemStyle = VisUI.getSkin().get(LogListItemStyle.class);
        this.logEntry = logEntry;
        internLayout();
    }

    private void internLayout() {

        VisTable headerTable = new VisTable();
        headerTable.add(new Image(this.logEntry.geoCacheLogType.getDrawable(logListItemStyle.logTypesStyle)));

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = this.logListItemStyle.headerFont;
        nameLabelStyle.fontColor = this.logListItemStyle.headerFontColor;
        VisLabel nameLabel = new VisLabel(this.logEntry.finder, nameLabelStyle);
        nameLabel.setWrap(true);
        headerTable.add(nameLabel).left().padLeft(CB.scaledSizes.MARGINx2).expandX().fillX();

        SimpleDateFormat postFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String dateString = postFormatter.format(logEntry.logDate);
        VisLabel dateLabel = new VisLabel(dateString, nameLabelStyle);
        headerTable.add(dateLabel).padRight(CB.scaledSizes.MARGINx2).right();

        //TODO set Background for header over style

        headerTable.pack();
        headerTable.layout();
        this.add(headerTable).left().expandX().fillX();

        this.row().padTop(CB.scaledSizes.MARGINx2);

        Label.LabelStyle commentLabelStyle = new Label.LabelStyle();
        commentLabelStyle.font = this.logListItemStyle.descriptionFont;
        commentLabelStyle.fontColor = this.logListItemStyle.descriptionFontColor;

        String comment = logEntry.logText;
        int maxLength = 500;
        for (int i = 0; i < comment.length(); i += maxLength) {
            String subComment = comment.substring(i, Math.min(i + maxLength, comment.length()));
            VisLabel commentLabel = new VisLabel(subComment, commentLabelStyle);
            commentLabel.setWrap(true);
            this.add(commentLabel).expand().fill();
            this.row();
        }

        needsLayout = false;
    }

    @Override
    public void dispose() {

    }
}
