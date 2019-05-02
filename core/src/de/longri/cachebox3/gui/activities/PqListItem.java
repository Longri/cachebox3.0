/*
 * Copyright (C) 2018 team-cachebox.de
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
import com.badlogic.gdx.utils.Align;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.skin.styles.PqListItemStyle;
import de.longri.cachebox3.gui.widgets.AligmentLabel;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.translation.Translation;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Longri on 26.03.2018.
 */
public class PqListItem extends ListViewItem {
    private final GroundspeakAPI.PQ pq;
    private final static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final static DecimalFormat decimalFormat = new DecimalFormat("###.##");

    public PqListItem(int index, GroundspeakAPI.PQ pq, PqListItemStyle style) {
        super(index);

        this.pq = pq;

        if (style == null) return;

        Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
        nameLabelStyle.font = style.nameFont;
        nameLabelStyle.fontColor = style.nameFontColor;

        Label.LabelStyle infoLabelStyle = new Label.LabelStyle();
        infoLabelStyle.font = style.infoFont;
        infoLabelStyle.fontColor = style.infoFontColor;

        AligmentLabel label = new AligmentLabel(pq.name, nameLabelStyle, Align.center);
        this.add(label).colspan(3).expandX().fillX();
        this.row();

        this.add(new AligmentLabel(Translation.get("PQcreationDate"), infoLabelStyle, Align.left)).expandX().fillX();
        this.add(new AligmentLabel(iso8601Format.format(pq.lastGenerated), infoLabelStyle, Align.left)).expandX().fillX();
        this.add().expandX().fillX();
        this.row();

        /*
        // there is no size in the API 1.0 list
        this.add(new AligmentLabel(Translation.get("size"), infoLabelStyle, Align.left)).expandX().fillX();
        this.add(new AligmentLabel(decimalFormat.format(pq.sizeMB) + " MB", infoLabelStyle, Align.left)).expandX().fillX();
        this.add().expandX().fillX();
        this.row();
         */

        this.add(new AligmentLabel(Translation.get("count"), infoLabelStyle, Align.left)).expandX().fillX();
        this.add(new AligmentLabel(Integer.toString(pq.cacheCount), infoLabelStyle, Align.left)).expandX().fillX();
        this.add().expandX().fillX();
        this.row();

        this.add(new AligmentLabel(Translation.get("PQlastImport"), infoLabelStyle, Align.left)).expandX().fillX();
        if (pq.lastImported != null) {
            this.add(new AligmentLabel(iso8601Format.format(pq.lastImported), infoLabelStyle, Align.left)).expandX().fillX();
            if (pq.lastImported.after(pq.lastGenerated)) {
                Label.LabelStyle colorLabelStyle = new Label.LabelStyle(infoLabelStyle);
                if (style.readyFontColor != null) colorLabelStyle.fontColor = style.readyFontColor;
                if (style.redyBackgroundColor != null)
                    colorLabelStyle.background = new ColorDrawable(style.redyBackgroundColor);
                this.add(new AligmentLabel(Translation.get("ready"), colorLabelStyle, Align.left)).expandX().fillX();
            } else {
                Label.LabelStyle colorLabelStyle = new Label.LabelStyle(infoLabelStyle);
                if (style.newFontColor != null) colorLabelStyle.fontColor = style.newFontColor;
                if (style.newBackgroundColor != null)
                    colorLabelStyle.background = new ColorDrawable(style.newBackgroundColor);
                this.add(new AligmentLabel(Translation.get("new"), colorLabelStyle, Align.left)).expandX().fillX();
            }
        } else {
            this.add(new AligmentLabel(" ", infoLabelStyle, Align.left)).expandX().fillX();
            Label.LabelStyle colorLabelStyle = new Label.LabelStyle(infoLabelStyle);
            if (style.neverFontColor != null) colorLabelStyle.fontColor = style.neverFontColor;
            if (style.neverBackgroundColor != null)
                colorLabelStyle.background = new ColorDrawable(style.neverBackgroundColor);
            this.add(new AligmentLabel(Translation.get("never"), colorLabelStyle, Align.left)).expandX().fillX();
        }
        this.row();
    }

    public int getCount() {
        return this.pq.cacheCount;
    }

    public GroundspeakAPI.PQ getPocketQuery() {
        return this.pq;
    }
}
