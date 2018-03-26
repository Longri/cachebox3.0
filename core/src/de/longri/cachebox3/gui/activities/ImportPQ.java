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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewType;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 26.03.2018.
 */
public class ImportPQ extends ActivityBase {

    private final static Logger log = LoggerFactory.getLogger(ImportPQ.class);
    private final ListView pqList = new ListView(ListViewType.VERTICAL);
    private final CharSequenceButton bOK, bCancel;

    public ImportPQ() {
        super("ImportPQ");
        bOK = new CharSequenceButton(Translation.get("import"));
        bCancel = new CharSequenceButton(Translation.get("cancel"));

        float contentWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx4;
        float listHeight = Gdx.graphics.getHeight() / 2;

        pqList.setBackground(this.style.background);

        this.add(pqList).width(new Value.Fixed(contentWidth)).height(new Value.Fixed(listHeight));
        this.row();


        // fill and add Buttons
        this.row().expandY().fillY().bottom();
        this.add();
        this.row();
        Table nestedTable2 = new Table();
        nestedTable2.defaults().pad(CB.scaledSizes.MARGIN).bottom();
        nestedTable2.add(bOK).bottom();
        nestedTable2.add(bCancel).bottom();
        this.add(nestedTable2).colspan(5);
    }

    @Override
    public void onShow() {

    }

    @Override
    public void onHide() {
    }

    private void refreshPQList() {

    }

    @Override
    public void dispose() {

    }
}
