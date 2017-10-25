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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.ListViewItem;

import java.io.File;

/**
 * Created by Longri on 02.09.2016.
 */
public class SelectDBItem extends ListViewItem {

    private final String fileName;
    private final VisLabel lblName, lblInfo;

    public SelectDBItem(int listIndex, File file, String fileInfo, SelectDB_Activity.SelectDbStyle style) {

        super(listIndex);
        Label.LabelStyle nameStyle = new Label.LabelStyle();
        nameStyle.font = style.nameFont;
        nameStyle.fontColor = style.nameColor;

        Label.LabelStyle infoStyle = new Label.LabelStyle();
        infoStyle.font = style.infoFont;
        infoStyle.fontColor = style.infoColor;

        Table infoTable = new VisTable();

        fileName = file.getName();
        lblName = new VisLabel(fileName, nameStyle);
        lblInfo = new VisLabel(fileInfo, infoStyle);
        infoTable.add(lblName).left().fillX();
        infoTable.row();
        infoTable.add(lblInfo).left().fillX();

        Image iconImage = new Image(CB.getSkin().getMenuIcon.manageDB, Scaling.none);
        this.add(iconImage).center().padRight(CB.scaledSizes.MARGIN_HALF);

        this.add(infoTable).expandX().fillX();
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void dispose() {

    }

    public void updateFileInfo(String fileInfo) {
        lblInfo.setText(fileInfo);
    }
}
