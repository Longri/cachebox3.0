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
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.FilterStyle;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.filter_settings.CategoriesListView;
import de.longri.cachebox3.gui.widgets.filter_settings.FilterSetListView;
import de.longri.cachebox3.gui.widgets.filter_settings.PresetListView;
import de.longri.cachebox3.gui.widgets.filter_settings.TextFilterView;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.FilterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.08.2016.
 */
public class EditFilterSettings extends ActivityBase {

    private final Logger log = LoggerFactory.getLogger(EditFilterSettings.class);

    public static interface OnShow {
        void onShow();
    }

    private CB_Button tglBtnPreset;
    private CB_Button tglBtnSet;
    private CB_Button tglBtnCategory;
    private CB_Button tglBtnText;
    private ButtonGroup<CB_Button> btnGroup;
    private Table actViewTable;
    private Cell viewCell;
    private Group placeHolder;

    private PresetListView presetListView;
    private FilterSetListView filterSetListView;
    private CategoriesListView categoriesListView;
    private TextFilterView textFilterView;
    private FilterStyle style;

    public final FilterProperties filterProperties;

    public EditFilterSettings(FilterProperties filterProperties) {
        super("EditFilterSettings");

        this.filterProperties = filterProperties.copy();

        try {
            style = VisUI.getSkin().get(FilterStyle.class);
        } catch (Exception e) {
            log.error("No style found, set to default empty");
            style = new FilterStyle();// set empty style
        }

        this.defaults().pad(CB.scaledSizes.MARGIN);

        presetListView = new PresetListView(this, style);
        filterSetListView = new FilterSetListView(this, style);
        categoriesListView = new CategoriesListView();
        textFilterView = new TextFilterView();

        createToggleBtn();
        viewCell = createViewCell();
        createOkCancel();

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                actViewTable = presetListView;
                viewCell.setActor(actViewTable);
                EditFilterSettings.this.invalidate();
                EditFilterSettings.this.layout();

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        CB.requestRendering();
                    }
                });

            }
        });
    }

    public void callBack(FilterProperties properties) {
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
            callBack(EditFilterSettings.this.filterProperties);
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
            ((OnShow) actViewTable).onShow();
        }
    };

    private void createToggleBtn() {

        btnGroup = new ButtonGroup<>();

        VisTextButton.VisTextButtonStyle buttonStyle = VisUI.getSkin().get("toggle", VisTextButton.VisTextButtonStyle.class);

        buttonStyle.font = style.toggleButtonFont;
        buttonStyle.fontColor = style.toggleButtonFontColor;

        tglBtnPreset = new CB_Button(Translation.get("preset"), buttonStyle);
        tglBtnSet = new CB_Button(Translation.get("setting"), buttonStyle);
        tglBtnCategory = new CB_Button(Translation.get("category"), buttonStyle);
        tglBtnText = new CB_Button(Translation.get("text"), buttonStyle);

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

        tglTbl.add(tglBtnPreset).expandX().fillX();
        tglTbl.add(tglBtnSet).expandX().fillX();

        //ISSUE (#167 implement Filter for Category)
//        tglTbl.add(tglBtnCategory).expandX().fillX();

        //ISSUE (#168 implement Filter for Text(CacheName/GcCode/OwnerName))
//        tglTbl.add(tglBtnText).expandX().fillX();

        this.add(tglTbl).width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
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
        cancelOkTable.defaults().pad(CB.scaledSizes.MARGIN);

        CB_Button btnOk = new CB_Button(Translation.get("ok"));
        CB_Button btnCancel = new CB_Button(Translation.get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        cancelOkTable.add(btnOk).expandX().fillX();
        cancelOkTable.add(btnCancel).expandX().fillX();

        this.add(cancelOkTable).width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }
}
