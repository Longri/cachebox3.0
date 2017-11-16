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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.gui.widgets.filter_settings.*;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.08.2016.
 */
public class EditFilterSettings extends ActivityBase {

    private final Logger log = LoggerFactory.getLogger(EditFilterSettings.class);

    private CharSequenceButton tglBtnPreset;
    private CharSequenceButton tglBtnSet;
    private CharSequenceButton tglBtnCategory;
    private CharSequenceButton tglBtnText;
    private ButtonGroup<CharSequenceButton> btnGroup;
    private Table actViewTable;
    private Cell viewCell;
    private Group placeHolder;

    private PresetListView presetListView;
    private FilterSetListView filterSetListView;
    private CategoriesListView categoriesListView;
    private TextFilterView textFilterView;

    public EditFilterSettings(String name) {
        super(name);

        presetListView = new PresetListView();
        filterSetListView = new FilterSetListView();
        categoriesListView = new CategoriesListView();
        textFilterView = new TextFilterView();

        createToggleBtn();
        viewCell = createViewCell();
        createOkCancel();

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                EditFilterSettings.this.invalidate();
                EditFilterSettings.this.layout();
            }
        });
    }

    private final ClickListener cancelListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {

            finish();
        }
    };

    private final ClickListener okListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {

            finish();
        }
    };

    private final ClickListener tglListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            VisTextButton checkedButton = btnGroup.getChecked();

            log.debug("Toggle to: " + checkedButton.getText());

            if (checkedButton.equals(tglBtnPreset)) {
                actViewTable = presetListView;
            } else if (checkedButton.equals(tglBtnSet)) {
                actViewTable = filterSetListView;
            } else if (checkedButton.equals(tglBtnCategory)) {
                actViewTable = categoriesListView;
            } else if (checkedButton.equals(tglBtnText)) {
                actViewTable = textFilterView;
            }

            viewCell.setActor(actViewTable);
            EditFilterSettings.this.invalidate();
            EditFilterSettings.this.layout();
        }
    };

    private void createToggleBtn() {

        btnGroup = new ButtonGroup<>();

        tglBtnPreset = new CharSequenceButton(Translation.get("preset"), "toggle");
        tglBtnSet = new CharSequenceButton( Translation.get("setting"), "toggle");
        tglBtnCategory = new CharSequenceButton(Translation.get("category"), "toggle");
        tglBtnText = new CharSequenceButton(Translation.get("text"), "toggle");

        tglBtnPreset.addListener(tglListener);
        tglBtnSet.addListener(tglListener);
        tglBtnCategory.addListener(tglListener);
        tglBtnText.addListener(tglListener);

        btnGroup.add(tglBtnPreset);
        btnGroup.add(tglBtnSet);
        btnGroup.add(tglBtnCategory);
        btnGroup.add(tglBtnText);


        Table tglTbl = new Table();
        tglTbl.defaults().space(CB.scaledSizes.MARGIN / 4);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 4;


        tglTbl.add(tglBtnPreset).width(new Value.Fixed(btnWidth));
        tglTbl.add(tglBtnSet).width(new Value.Fixed(btnWidth));
        tglTbl.add(tglBtnCategory).width(new Value.Fixed(btnWidth));
        tglTbl.add(tglBtnText).width(new Value.Fixed(btnWidth));

        this.add(tglTbl).top().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }

    private Cell createViewCell() {
        this.row();
        Group group = new Group();
        group.addActor(presetListView);
        return this.add(group).expand().fill();
    }

    private void createOkCancel() {
        this.row();
        Table cancelOkTable = new Table();

        CharSequenceButton btnOk = new CharSequenceButton(Translation.get("ok"));
        CharSequenceButton btnCancel = new CharSequenceButton(Translation.get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN_HALF * 3) / 2;

        cancelOkTable.add(btnOk).width(new Value.Fixed(btnWidth));
        cancelOkTable.add(btnCancel).width(new Value.Fixed(btnWidth));

        this.add(cancelOkTable).bottom().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }

}
