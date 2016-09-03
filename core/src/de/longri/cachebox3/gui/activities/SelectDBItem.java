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

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.gui.views.listview.ListViewItem;

import java.io.File;

/**
 * Created by Longri on 02.09.2016.
 */
public class SelectDBItem extends ListViewItem {

    private final String fileName;

    public SelectDBItem(int listIndex, File file, String fileInfo, SelectDB_Activity.SelectDbStyle style) {

        super(listIndex);
        Label.LabelStyle nameStyle = new Label.LabelStyle();
        nameStyle.font = style.nameFont;
        nameStyle.fontColor = style.nameColor;

        Label.LabelStyle infoStyle = new Label.LabelStyle();
        infoStyle.font = style.infoFont;
        infoStyle.fontColor = style.infoColor;

        fileName = file.getName();
        VisLabel lblName = new VisLabel(fileName, nameStyle);
        VisLabel lblInfo = new VisLabel(fileInfo, infoStyle);
        this.add(lblName).left().fillX();
        this.row();
        this.add(lblInfo).left().fillX();
    }

    public String getFileName() {
        return fileName;
    }
}
