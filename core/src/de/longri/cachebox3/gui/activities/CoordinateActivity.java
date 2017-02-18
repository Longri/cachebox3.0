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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.widgets.NumPad;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 04.02.17.
 */
public class CoordinateActivity extends ActivityBase {

    private static Logger log = LoggerFactory.getLogger(CoordinateActivity.class);

    private boolean isCreated = false;
    final private Coordinate coordinate;
    private VisTextButton tglBtnDec;
    private VisTextButton tglBtnMin;
    private VisTextButton tglBtnSec;
    private VisTextButton tglBtnUtm;
    private ButtonGroup<VisTextButton> btnGroup;

    private final DecValues decValues = new DecValues();
    private final MinValues minValues = new MinValues();
    private final SecValues secValues = new SecValues();
    private final UtmValues utmValues = new UtmValues();


    NumPad.IKeyEventListener keyEventListener = new NumPad.IKeyEventListener() {
        @Override
        public void KeyPressed(String value) {

        }
    };


    private final NumPad numPad = new NumPad(keyEventListener, NumPad.OptionalButton.none);

    private Cell valueCell;
    private Group placeHolder;


    public CoordinateActivity(Coordinate coordinate) {
        super("CorrdinateActivity");
        this.setDebug(true);
        this.coordinate = coordinate;
    }

    @Override
    public void layout() {
        super.layout();
        if (!isCreated) create();
    }


    private void create() {

        this.defaults().pad(CB.scaledSizes.MARGIN_HALF);


        createToggleBtn();
        valueCell = createValues();
        placeHolder = createPlaceHolder();
        createNumPad();
        createOkCancel();

        isCreated = true;

        btnGroup.setChecked("Min");
        tglListener.clicked(null, 0, 0);

    }

    private Group createPlaceHolder() {
        this.row();

        Group group = new Group();
        group.addActor(new VisLabel(""));
        this.add(group);

        return group;
    }

    private void createNumPad() {
        this.row();
        this.add(numPad);
    }

    private Cell createValues() {
        this.row();

        Group group = new Group();

        group.addActor(decValues);

        float height = (CB.scaledSizes.BUTTON_HEIGHT * 2) + CB.scaledSizes.MARGIN * 4;

        return this.add(group).height(new Value.Fixed(height));
    }


    private void createToggleBtn() {

        btnGroup = new ButtonGroup<VisTextButton>();

        tglBtnDec = new VisTextButton("Dec", "toggle");
        tglBtnMin = new VisTextButton("Min", "toggle");
        tglBtnSec = new VisTextButton("Sec", "toggle");
        tglBtnUtm = new VisTextButton("UTM", "toggle");

        tglBtnDec.addListener(tglListener);
        tglBtnMin.addListener(tglListener);
        tglBtnSec.addListener(tglListener);
        tglBtnUtm.addListener(tglListener);

        btnGroup.add(tglBtnDec);
        btnGroup.add(tglBtnMin);
        btnGroup.add(tglBtnSec);
        btnGroup.add(tglBtnUtm);


        Table tglTbl = new Table();
        tglTbl.defaults().space(CB.scaledSizes.MARGIN / 4);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 4;


        tglTbl.add(tglBtnDec).width(new Value.Fixed(btnWidth));
        tglTbl.add(tglBtnMin).width(new Value.Fixed(btnWidth));
        tglTbl.add(tglBtnSec).width(new Value.Fixed(btnWidth));
        tglTbl.add(tglBtnUtm).width(new Value.Fixed(btnWidth));

        this.add(tglTbl).top().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }

    private void createOkCancel() {
        this.row();
        Table cancelOkTable = new Table();

        VisTextButton btnOk = new VisTextButton(Translation.Get("ok"));
        VisTextButton btnCancel = new VisTextButton(Translation.Get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN_HALF * 3) / 2;

        cancelOkTable.add(btnOk).width(new Value.Fixed(btnWidth));
        cancelOkTable.add(btnCancel).width(new Value.Fixed(btnWidth));


        this.add(cancelOkTable).bottom().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));

    }


    boolean calculated = false;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!calculated && this.getMinHeight() < this.getHeight()) {
            placeHolder.setHeight(this.getHeight() - this.getMinHeight());
            invalidate();
            layout();
            calculated = true;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    CoordinateActivity.this.invalidate();
                    CoordinateActivity.this.layout();
                }
            });
        }
    }

    ClickListener tglListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            VisTextButton checkedButton = btnGroup.getChecked();

            log.debug("Toggle to: " + checkedButton.getText());

            if (checkedButton.equals(tglBtnDec)) {
                valueCell.setActor(decValues);
            } else if (checkedButton.equals(tglBtnMin)) {
                valueCell.setActor(minValues);
            } else if (checkedButton.equals(tglBtnSec)) {
                valueCell.setActor(secValues);
            } else if (checkedButton.equals(tglBtnUtm)) {
                valueCell.setActor(utmValues);
            }

            CoordinateActivity.this.invalidate();
            CoordinateActivity.this.layout();


        }
    };


    ClickListener cancelListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            callBack(null);
            finish();
        }
    };


    ClickListener okListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            callBack(CoordinateActivity.this.coordinate);
            finish();
        }
    };


    public void callBack(Coordinate coordinate) {
    }


    private static abstract class AbstractValueTable extends Table {

        VisTextButton focusButton;
        double lat, lon;

        private void enterValue(int value) {

            if (focusButton == null) return;


        }

        abstract double getLat();

        abstract double getLon();

        abstract void setValue(double lat, double lon);

    }

    private static class DecValues extends AbstractValueTable {

        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);

        final VisTextButton l1_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l1_2 = null;
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisLabel l1_5 = new VisLabel(".");
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l1_8 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_9 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_10 = new VisTextButton("5", btnStyle);
        final VisLabel l1_11 = new VisLabel("°");

        final VisTextButton l2_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("0", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("3", btnStyle);
        final VisLabel l2_5 = new VisLabel(".");
        final VisTextButton l2_6 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("9", btnStyle);
        final VisTextButton l2_8 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_9 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_10 = new VisTextButton("8", btnStyle);
        final VisLabel l2_11 = new VisLabel("°");

        DecValues() {

            this.defaults().padLeft(CB.scaledSizes.MARGIN_HALF / 2).padBottom(CB.scaledSizes.MARGIN_HALF);

            float minBtnWidth = ((Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 10);

            this.add(l1_1).width(new Value.Fixed(minBtnWidth));
            this.add(l1_2).width(new Value.Fixed(minBtnWidth));
            this.add(l1_3).width(new Value.Fixed(minBtnWidth));
            this.add(l1_4).width(new Value.Fixed(minBtnWidth));
            this.add(l1_5).bottom();
            this.add(l1_6).width(new Value.Fixed(minBtnWidth));
            this.add(l1_7).width(new Value.Fixed(minBtnWidth));
            this.add(l1_8).width(new Value.Fixed(minBtnWidth));
            this.add(l1_9).width(new Value.Fixed(minBtnWidth));
            this.add(l1_10).width(new Value.Fixed(minBtnWidth));
            this.add(l1_11).top();

            this.row();

            this.add(l2_1).width(new Value.Fixed(minBtnWidth));
            this.add(l2_2).width(new Value.Fixed(minBtnWidth));
            this.add(l2_3).width(new Value.Fixed(minBtnWidth));
            this.add(l2_4).width(new Value.Fixed(minBtnWidth));
            this.add(l2_5).bottom();
            this.add(l2_6).width(new Value.Fixed(minBtnWidth));
            this.add(l2_7).width(new Value.Fixed(minBtnWidth));
            this.add(l2_8).width(new Value.Fixed(minBtnWidth));
            this.add(l2_9).width(new Value.Fixed(minBtnWidth));
            this.add(l2_10).width(new Value.Fixed(minBtnWidth));
            this.add(l2_11).top();

        }

        @Override
        double getLat() {
            return 0;
        }

        @Override
        double getLon() {
            return 0;
        }

        @Override
        void setValue(double lat, double lon) {

        }
    }

    private static class MinValues extends AbstractValueTable {

        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);

        final VisTextButton l1_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l1_2 = null;
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisLabel l1_5 = new VisLabel(".");
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisLabel l1_8 = new VisLabel(".");
        final VisTextButton l1_9 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_10 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_11 = new VisTextButton("5", btnStyle);


        final VisTextButton l2_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("0", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("3", btnStyle);
        final VisLabel l2_5 = new VisLabel(".");
        final VisTextButton l2_6 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("9", btnStyle);
        final VisLabel l2_8 = new VisLabel(".");
        final VisTextButton l2_9 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_10 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_11 = new VisTextButton("8", btnStyle);


        MinValues() {

            this.defaults().padLeft(CB.scaledSizes.MARGIN_HALF / 2).padBottom(CB.scaledSizes.MARGIN_HALF);

            float minBtnWidth = ((Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 10);

            this.add(l1_1).width(new Value.Fixed(minBtnWidth));
            this.add(l1_2).width(new Value.Fixed(minBtnWidth));
            this.add(l1_3).width(new Value.Fixed(minBtnWidth));
            this.add(l1_4).width(new Value.Fixed(minBtnWidth));
            this.add(l1_5).bottom();
            this.add(l1_6).width(new Value.Fixed(minBtnWidth));
            this.add(l1_7).width(new Value.Fixed(minBtnWidth));
            this.add(l1_8).bottom();
            this.add(l1_9).width(new Value.Fixed(minBtnWidth));
            this.add(l1_10).width(new Value.Fixed(minBtnWidth));
            this.add(l1_11).width(new Value.Fixed(minBtnWidth));

            this.row();

            this.add(l2_1).width(new Value.Fixed(minBtnWidth));
            this.add(l2_2).width(new Value.Fixed(minBtnWidth));
            this.add(l2_3).width(new Value.Fixed(minBtnWidth));
            this.add(l2_4).width(new Value.Fixed(minBtnWidth));
            this.add(l2_5).bottom();
            this.add(l2_6).width(new Value.Fixed(minBtnWidth));
            this.add(l2_7).width(new Value.Fixed(minBtnWidth));
            this.add(l2_8).bottom();
            this.add(l2_9).width(new Value.Fixed(minBtnWidth));
            this.add(l2_10).width(new Value.Fixed(minBtnWidth));
            this.add(l2_11).width(new Value.Fixed(minBtnWidth));

        }

        @Override
        double getLat() {
            return 0;
        }

        @Override
        double getLon() {
            return 0;
        }

        @Override
        void setValue(double lat, double lon) {

        }
    }

    private static class SecValues extends AbstractValueTable {

        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);

        final VisTextButton l1_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l1_2 = null;
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisLabel l1_5 = new VisLabel("°");
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisLabel l1_8 = new VisLabel("'");
        final VisTextButton l1_9 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_10 = new VisTextButton("5", btnStyle);
        final VisLabel l1_11 = new VisLabel(".");
        final VisTextButton l1_12 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_13 = new VisTextButton("5", btnStyle);


        final VisTextButton l2_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("0", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("3", btnStyle);
        final VisLabel l2_5 = new VisLabel("°");
        final VisTextButton l2_6 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("9", btnStyle);
        final VisLabel l2_8 = new VisLabel("'");
        final VisTextButton l2_9 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_10 = new VisTextButton("7", btnStyle);
        final VisLabel l2_11 = new VisLabel(".");
        final VisTextButton l2_12 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_13 = new VisTextButton("5", btnStyle);


        SecValues() {

            this.defaults().padLeft(CB.scaledSizes.MARGIN_HALF / 2).padBottom(CB.scaledSizes.MARGIN_HALF);

            float minBtnWidth = ((Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 12);

            this.add(l1_1).width(new Value.Fixed(minBtnWidth));
            this.add(l1_2).width(new Value.Fixed(minBtnWidth));
            this.add(l1_3).width(new Value.Fixed(minBtnWidth));
            this.add(l1_4).width(new Value.Fixed(minBtnWidth));
            this.add(l1_5).top();
            this.add(l1_6).width(new Value.Fixed(minBtnWidth));
            this.add(l1_7).width(new Value.Fixed(minBtnWidth));
            this.add(l1_8).top();
            this.add(l1_9).width(new Value.Fixed(minBtnWidth));
            this.add(l1_10).width(new Value.Fixed(minBtnWidth));
            this.add(l1_11).bottom();
            this.add(l1_12).width(new Value.Fixed(minBtnWidth));
            this.add(l1_13).width(new Value.Fixed(minBtnWidth));

            this.row();

            this.add(l2_1).width(new Value.Fixed(minBtnWidth));
            this.add(l2_2).width(new Value.Fixed(minBtnWidth));
            this.add(l2_3).width(new Value.Fixed(minBtnWidth));
            this.add(l2_4).width(new Value.Fixed(minBtnWidth));
            this.add(l2_5).top();
            this.add(l2_6).width(new Value.Fixed(minBtnWidth));
            this.add(l2_7).width(new Value.Fixed(minBtnWidth));
            this.add(l2_8).top();
            this.add(l2_9).width(new Value.Fixed(minBtnWidth));
            this.add(l2_10).width(new Value.Fixed(minBtnWidth));
            this.add(l2_11).bottom();
            this.add(l2_12).width(new Value.Fixed(minBtnWidth));
            this.add(l2_13).width(new Value.Fixed(minBtnWidth));

        }

        @Override
        double getLat() {
            return 0;
        }

        @Override
        double getLon() {
            return 0;
        }

        @Override
        void setValue(double lat, double lon) {

        }
    }

    private static class UtmValues extends AbstractValueTable {

        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);


        final VisTextButton l1_1 = new VisTextButton("OstW");
        final VisTextButton l1_2 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_5 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l1_8 = null;
        final VisTextButton l1_9 = null;

        final VisTextButton l2_1 = new VisTextButton("OstW");
        final VisTextButton l2_2 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l2_5 = new VisTextButton("2", btnStyle);
        final VisTextButton l2_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_8 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_9 = new VisTextButton("7", btnStyle);


        final VisLabel l3_1 = new VisLabel("Zone");
        final VisTextButton l3_2 = new VisTextButton("5", btnStyle);
        final VisTextButton l3_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l3_4 = new VisTextButton("2", btnStyle);


        UtmValues() {

            this.defaults().padLeft(CB.scaledSizes.MARGIN_HALF / 2).padBottom(CB.scaledSizes.MARGIN_HALF);

            float minBtnWidth = ((Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN * 3) / 11);

            this.add(l1_1);
            this.add(l1_2).width(new Value.Fixed(minBtnWidth));
            this.add(l1_3).width(new Value.Fixed(minBtnWidth));
            this.add(l1_4).width(new Value.Fixed(minBtnWidth));
            this.add(l1_5).width(new Value.Fixed(minBtnWidth));
            this.add(l1_6).width(new Value.Fixed(minBtnWidth));
            this.add(l1_7).width(new Value.Fixed(minBtnWidth));
            this.add(l1_8).width(new Value.Fixed(minBtnWidth));
            this.add(l1_9).width(new Value.Fixed(minBtnWidth));

            this.row();

            this.add(l2_1);
            this.add(l2_2).width(new Value.Fixed(minBtnWidth));
            this.add(l2_3).width(new Value.Fixed(minBtnWidth));
            this.add(l2_4).width(new Value.Fixed(minBtnWidth));
            this.add(l2_5).width(new Value.Fixed(minBtnWidth));
            this.add(l2_6).width(new Value.Fixed(minBtnWidth));
            this.add(l2_7).width(new Value.Fixed(minBtnWidth));
            this.add(l2_8).width(new Value.Fixed(minBtnWidth));
            this.add(l2_9).width(new Value.Fixed(minBtnWidth));

            this.row();

            this.add(l3_1);
            this.add(l3_2).width(new Value.Fixed(minBtnWidth));
            this.add(l3_3).width(new Value.Fixed(minBtnWidth));
            this.add(l3_4).width(new Value.Fixed(minBtnWidth));
        }

        @Override
        double getLat() {
            return 0;
        }

        @Override
        double getLon() {
            return 0;
        }

        @Override
        void setValue(double lat, double lon) {

        }
    }

}
