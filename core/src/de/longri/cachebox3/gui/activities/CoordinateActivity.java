/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.NumPad;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.converter.UTMConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 04.02.17.
 */
public class CoordinateActivity extends ActivityBase {

    private static Logger log = LoggerFactory.getLogger(CoordinateActivity.class);
    final private Coordinate coordinate;
    private final DecValues decValues = new DecValues();
    private final MinValues minValues = new MinValues();
    private final SecValues secValues = new SecValues();
    private final UtmValues utmValues = new UtmValues();
    boolean calculated = false;
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
            if (actValueTable != null) {
                callBack(actValueTable.getValue());
            } else {
                callBack(CoordinateActivity.this.coordinate);
            }
            finish();
        }
    };
    private boolean isCreated = false;
    private double lat, lon;
    private VisTextButton tglBtnDec;
    private VisTextButton tglBtnMin;
    private VisTextButton tglBtnSec;
    private VisTextButton tglBtnUtm;
    private ButtonGroup<VisTextButton> btnGroup;
    private AbstractValueTable actValueTable;
    NumPad.IKeyEventListener keyEventListener = new NumPad.IKeyEventListener() {
        @Override
        public void KeyPressed(String value) {

            if ("C".equals(value)) {
                cancelListener.clicked(StageManager.BACK_KEY_INPUT_EVENT, -1, -1);
                return;
            }

            if (value.equals("<") || value.equals(">")) {
                if (value.equals("<")) {
                    actValueTable.moveFocus(-1);
                } else {
                    actValueTable.moveFocus(1);
                }
                return;
            }
            actValueTable.enterValue(value);
        }
    };
    private NumPad numPad = new NumPad(keyEventListener, NumPad.OptionalButton.none);
    private Cell valueCell;
    private Group placeHolder;
    private final ClickListener tglListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            VisTextButton checkedButton = btnGroup.getChecked();

            log.debug("Toggle to: " + checkedButton.getText());
            if (actValueTable != null) {
                LatLong latLong = actValueTable.getValue();
                if (latLong != null) {
                    lat = latLong.getLatitude();
                    lon = latLong.getLongitude();
                }
            }


            if (checkedButton.equals(tglBtnDec)) {
                actValueTable = decValues;
            } else if (checkedButton.equals(tglBtnMin)) {
                actValueTable = minValues;
            } else if (checkedButton.equals(tglBtnSec)) {
                actValueTable = secValues;
            } else if (checkedButton.equals(tglBtnUtm)) {
                actValueTable = utmValues;
            }

            valueCell.setActor(actValueTable);
            actValueTable.setValue(lat, lon);
            CoordinateActivity.this.invalidate();
            CoordinateActivity.this.layout();


        }
    };

    public CoordinateActivity(Coordinate coordinate) {
        super("CorrdinateActivity");
        this.coordinate = coordinate;
        this.lat = coordinate.getLatitude();
        this.lon = coordinate.getLongitude();
    }

    @Override
    public void onHide() {
        ((InputMultiplexer) Gdx.input.getInputProcessor()).removeProcessor(keyboardListener);
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

        CB_Button btnOk = new CB_Button(Translation.get("ok"));
        CB_Button btnCancel = new CB_Button(Translation.get("cancel"));

        btnOk.addListener(okListener);
        btnCancel.addListener(cancelListener);

        float btnWidth = (Gdx.graphics.getWidth() - CB.scaledSizes.MARGIN_HALF * 3) / 2;

        cancelOkTable.add(btnOk).width(new Value.Fixed(btnWidth));
        cancelOkTable.add(btnCancel).width(new Value.Fixed(btnWidth));

        this.add(cancelOkTable).bottom().width(new Value.Fixed(Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2));
    }

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

    public void callBack(Coordinate coordinate) {
    }

    private static abstract class AbstractValueTable extends Catch_Table {

        VisTextButton[] focusSequence;
        int focusButton;
        int focusLineEnd;
        int focusNextLineBegin;
        int focusBegin;
        double lat, lon;
        ClickListener clickListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                //change focus
                for (int i = 0, n = focusSequence.length; i < n; i++) {
                    if (event.getListenerActor() == focusSequence[i]) {
                        focusSequence[i].setChecked(true);
                        focusButton = i;
                    } else {
                        focusSequence[i].setChecked(false);
                    }
                }
                onFocusSet(focusSequence[focusButton]);
            }
        };

        private void enterValue(String value) {
            if (focusButton < 0 || focusButton > focusSequence.length) return;
            focusSequence[focusButton].setText(value);
            setFocus(true);
        }

        Coordinate getValue() {

            StringBuilder sb = new StringBuilder();
            SnapshotArray<Actor> childs = this.getChildren();
            for (Actor actor : childs) {
                if (actor == null) {
                    sb.append(" ");
                } else {
                    if (actor instanceof VisTextButton) {
                        sb.append(((VisTextButton) actor).getText());
                    } else if (actor instanceof VisLabel) {
                        sb.append(((VisLabel) actor).getText());
                    }
                }
            }
            return new Coordinate(sb.toString());
        }

        abstract void setValue(double lat, double lon);

        protected void setFocus(boolean next) {
            focusSequence[focusButton].setChecked(false);
            if (next) {
                if (focusButton == focusLineEnd) {
                    focusButton = focusNextLineBegin;
                } else {
                    if (++focusButton > focusSequence.length - 1) {
                        focusButton = focusBegin;
                    }
                }
            }
            focusSequence[focusButton].setChecked(true);
            onFocusSet(focusSequence[focusButton]);
        }

        public void moveFocus(int step) {
            focusSequence[focusButton].setChecked(false);
            if (step < 0) {
                if (--focusButton < 0) {
                    focusButton = focusSequence.length - 1;
                }
            } else {
                if (++focusButton > focusSequence.length - 1) {
                    focusButton = 0;
                }
            }
            focusSequence[focusButton].setChecked(true);
            onFocusSet(focusSequence[focusButton]);
        }

        protected void onFocusSet(VisTextButton button) {
        }


    }

    private static class DecValues extends AbstractValueTable {

        final VisTextButton l1_2 = null;
        final VisLabel l1_5 = new VisLabel(".");
        final VisLabel l1_11 = new VisLabel("°");
        final VisLabel l2_5 = new VisLabel(".");
        final VisLabel l2_11 = new VisLabel("°");
        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);
        final VisTextButton l1_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l1_8 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_9 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_10 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("0", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_6 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("9", btnStyle);
        final VisTextButton l2_8 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_9 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_10 = new VisTextButton("8", btnStyle);

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

            focusSequence = new VisTextButton[]{l1_3, l1_4, l1_6, l1_7, l1_8, l1_9, l1_10, l2_2, l2_3, l2_4, l2_6, l2_7, l2_8, l2_9, l2_10};
            focusBegin = 2;
            focusLineEnd = 6;
            focusNextLineBegin = 10;

            //add clickListener
            l1_3.addListener(clickListener);
            l1_4.addListener(clickListener);
            l1_6.addListener(clickListener);
            l1_7.addListener(clickListener);
            l1_8.addListener(clickListener);
            l1_9.addListener(clickListener);
            l1_10.addListener(clickListener);
            l2_2.addListener(clickListener);
            l2_3.addListener(clickListener);
            l2_4.addListener(clickListener);
            l2_6.addListener(clickListener);
            l2_7.addListener(clickListener);
            l2_8.addListener(clickListener);
            l2_9.addListener(clickListener);
            l2_10.addListener(clickListener);

            //clickListener for N/S, E/W
            l1_1.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (l1_1.getText().charAt(0) == 'N') {
                        l1_1.setText("S");
                    } else {
                        l1_1.setText("N");
                    }

                    //disable checked
                    l1_1.setChecked(false);
                }
            });

            l2_1.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (l2_1.getText().charAt(0) == 'E') {
                        l2_1.setText("W");
                    } else {
                        l2_1.setText("E");
                    }

                    //disable checked
                    l2_1.setChecked(false);
                }
            });


        }

        @Override
        void setValue(double lat, double lon) {
            // lat
            if (lat >= 0) l1_1.setText("N");
            else l1_1.setText("S");
            String formatLat = String.format("%09.5f", Math.abs(lat)).replace(",", ".").replace(".", "");
            l1_3.setText(String.valueOf(formatLat.charAt(1)));
            l1_4.setText(String.valueOf(formatLat.charAt(2)));
            l1_6.setText(String.valueOf(formatLat.charAt(3)));
            l1_7.setText(String.valueOf(formatLat.charAt(4)));
            l1_8.setText(String.valueOf(formatLat.charAt(5)));
            l1_9.setText(String.valueOf(formatLat.charAt(6)));
            l1_10.setText(String.valueOf(formatLat.charAt(7)));

            // lon
            if (lon >= 0) l2_1.setText("E");
            else l2_1.setText("W");
            String formatLon = String.format("%09.5f", Math.abs(lon)).replace(",", ".").replace(".", "");
            l2_2.setText(String.valueOf(formatLon.charAt(0)));
            l2_3.setText(String.valueOf(formatLon.charAt(1)));
            l2_4.setText(String.valueOf(formatLon.charAt(2)));
            l2_6.setText(String.valueOf(formatLon.charAt(3)));
            l2_7.setText(String.valueOf(formatLon.charAt(4)));
            l2_8.setText(String.valueOf(formatLon.charAt(5)));
            l2_9.setText(String.valueOf(formatLon.charAt(6)));
            l2_10.setText(String.valueOf(formatLon.charAt(7)));


            focusButton = focusBegin;
            setFocus(false);
        }

    }

    private static class MinValues extends AbstractValueTable {

        final VisTextButton l1_2 = null;
        final VisLabel l1_5 = new VisLabel("°");
        final VisLabel l1_8 = new VisLabel(".");
        final VisLabel l2_5 = new VisLabel("°");
        final VisLabel l2_8 = new VisLabel(".");
        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);
        final VisTextButton l1_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l1_9 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_10 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_11 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("0", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_6 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("9", btnStyle);
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

            focusSequence = new VisTextButton[]{l1_3, l1_4, l1_6, l1_7, l1_9, l1_10, l1_11, l2_2, l2_3, l2_4, l2_6, l2_7, l2_9, l2_10, l2_11};
            focusBegin = 2;
            focusLineEnd = 6;
            focusNextLineBegin = 10;

            //add clickListener
            l1_3.addListener(clickListener);
            l1_4.addListener(clickListener);
            l1_6.addListener(clickListener);
            l1_7.addListener(clickListener);
            l1_9.addListener(clickListener);
            l1_10.addListener(clickListener);
            l1_11.addListener(clickListener);
            l2_2.addListener(clickListener);
            l2_3.addListener(clickListener);
            l2_4.addListener(clickListener);
            l2_6.addListener(clickListener);
            l2_7.addListener(clickListener);
            l2_9.addListener(clickListener);
            l2_10.addListener(clickListener);
            l2_11.addListener(clickListener);

            //clickListener for N/S, E/W
            l1_1.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (l1_1.getText().charAt(0) == 'N') {
                        l1_1.setText("S");
                    } else {
                        l1_1.setText("N");
                    }

                    //disable checked
                    l1_1.setChecked(false);
                }
            });

            l2_1.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (l2_1.getText().charAt(0) == 'E') {
                        l2_1.setText("W");
                    } else {
                        l2_1.setText("E");
                    }

                    //disable checked
                    l2_1.setChecked(false);
                }
            });

        }

        @Override
        void setValue(double lat, double lon) {
            // Lat
            double deg = (int) Math.abs(lat);
            double frac = Math.abs(lat) - deg;
            double min = frac * 60;

            String formatLat = String.format("%03d", (int) deg)
                    + String.format("%02d", (int) min)
                    + String.format("%03d", (int) (0.5 + (min - (int) min) * 1000)); // rounded
            if (lat >= 0) l1_1.setText("N");
            else l1_1.setText("S");
            l1_3.setText(String.valueOf(formatLat.charAt(1)));
            l1_4.setText(String.valueOf(formatLat.charAt(2)));
            l1_6.setText(String.valueOf(formatLat.charAt(3)));
            l1_7.setText(String.valueOf(formatLat.charAt(4)));
            l1_9.setText(String.valueOf(formatLat.charAt(5)));
            l1_10.setText(String.valueOf(formatLat.charAt(6)));
            l1_11.setText(String.valueOf(formatLat.charAt(7)));

            deg = (int) Math.abs(lon);
            frac = Math.abs(lon) - deg;
            min = frac * 60;
            String formatLon = String.format("%03d", (int) deg)
                    + String.format("%02d", (int) min)
                    + String.format("%03d", (int) (0.5 + (min - (int) min) * 1000)); // rounded

            if (lon >= 0) l2_1.setText("E");
            else l2_1.setText("W");
            l2_2.setText(String.valueOf(formatLon.charAt(0)));
            l2_3.setText(String.valueOf(formatLon.charAt(1)));
            l2_4.setText(String.valueOf(formatLon.charAt(2)));
            l2_6.setText(String.valueOf(formatLon.charAt(3)));
            l2_7.setText(String.valueOf(formatLon.charAt(4)));
            l2_9.setText(String.valueOf(formatLon.charAt(5)));
            l2_10.setText(String.valueOf(formatLon.charAt(6)));
            l2_11.setText(String.valueOf(formatLon.charAt(7)));


            focusButton = focusBegin;
            setFocus(false);
        }
    }

    private static class SecValues extends AbstractValueTable {

        final VisTextButton l1_2 = null;
        final VisLabel l1_5 = new VisLabel("°");
        final VisLabel l1_8 = new VisLabel("'");
        final VisLabel l1_11 = new VisLabel(".");
        final VisLabel l2_5 = new VisLabel("°");
        final VisLabel l2_8 = new VisLabel("'");
        final VisLabel l2_11 = new VisLabel(".");
        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);
        final VisTextButton l1_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l1_9 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_10 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_12 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_13 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_1 = new VisTextButton("N", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("0", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_6 = new VisTextButton("3", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("9", btnStyle);
        final VisTextButton l2_9 = new VisTextButton("1", btnStyle);
        final VisTextButton l2_10 = new VisTextButton("7", btnStyle);
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

            focusSequence = new VisTextButton[]{l1_3, l1_4, l1_6, l1_7, l1_9, l1_10, l1_12, l1_13, l2_2, l2_3, l2_4, l2_6, l2_7, l2_9, l2_10, l2_12, l2_13};
            focusBegin = 2;
            focusLineEnd = 7;
            focusNextLineBegin = 11;

            //add clickListener
            l1_3.addListener(clickListener);
            l1_4.addListener(clickListener);
            l1_6.addListener(clickListener);
            l1_7.addListener(clickListener);
            l1_9.addListener(clickListener);
            l1_10.addListener(clickListener);
            l1_11.addListener(clickListener);
            l2_2.addListener(clickListener);
            l2_3.addListener(clickListener);
            l2_4.addListener(clickListener);
            l2_6.addListener(clickListener);
            l2_7.addListener(clickListener);
            l2_9.addListener(clickListener);
            l2_10.addListener(clickListener);
            l2_11.addListener(clickListener);

            //clickListener for N/S, E/W
            l1_1.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (l1_1.getText().charAt(0) == 'N') {
                        l1_1.setText("S");
                    } else {
                        l1_1.setText("N");
                    }

                    //disable checked
                    l1_1.setChecked(false);
                }
            });

            l2_1.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    if (l2_1.getText().charAt(0) == 'E') {
                        l2_1.setText("W");
                    } else {
                        l2_1.setText("E");
                    }

                    //disable checked
                    l2_1.setChecked(false);
                }
            });


        }

        @Override
        void setValue(double lat, double lon) {
            double deg = (int) Math.abs(lat);
            double frac = Math.abs(lat) - deg;
            double min = frac * 60;
            int imin = (int) min;
            frac = min - imin;
            double sec = frac * 60;

            String formatLat = String.format("%03d", (int) deg)
                    + String.format("%02d", imin)
                    + String.format("%02d", (int) sec)
                    + String.format("%02d", (int) (0.5 + (sec - (int) sec) * 100)); // rounded
            if (lat >= 0) l1_1.setText("N");
            else l1_1.setText("S");
            l1_3.setText(String.valueOf(formatLat.charAt(1)));
            l1_4.setText(String.valueOf(formatLat.charAt(2)));
            l1_6.setText(String.valueOf(formatLat.charAt(3)));
            l1_7.setText(String.valueOf(formatLat.charAt(4)));
            l1_9.setText(String.valueOf(formatLat.charAt(5)));
            l1_10.setText(String.valueOf(formatLat.charAt(6)));
            l1_12.setText(String.valueOf(formatLat.charAt(7)));
            l1_13.setText(String.valueOf(formatLat.charAt(8)));

            //Lon
            deg = (int) Math.abs(lon);
            frac = Math.abs(lon) - deg;
            min = frac * 60;
            imin = (int) min;
            frac = min - imin;
            sec = frac * 60;
            String formatLon = String.format("%03d", (int) deg)
                    + String.format("%02d", imin)
                    + String.format("%02d", (int) sec)
                    + String.format("%02d", (int) (0.5 + (sec - (int) sec) * 100)); // rounded

            if (lon >= 0) l2_1.setText("E");
            else l2_1.setText("W");
            l2_2.setText(String.valueOf(formatLon.charAt(0)));
            l2_3.setText(String.valueOf(formatLon.charAt(1)));
            l2_4.setText(String.valueOf(formatLon.charAt(2)));
            l2_6.setText(String.valueOf(formatLon.charAt(3)));
            l2_7.setText(String.valueOf(formatLon.charAt(4)));
            l2_9.setText(String.valueOf(formatLon.charAt(5)));
            l2_10.setText(String.valueOf(formatLon.charAt(6)));
            l2_12.setText(String.valueOf(formatLon.charAt(7)));
            l2_13.setText(String.valueOf(formatLon.charAt(8)));


            focusButton = focusBegin;
            setFocus(false);
        }

    }

    private class UtmValues extends AbstractValueTable {
        final VisLabel l1_1 = new VisLabel("OstW");
        final VisTextButton l1_8 = null;
        final VisTextButton l1_9 = null;
        final VisLabel l2_1 = new VisLabel("NordW");
        final VisTextButton l2_9 = null;
        final VisLabel l3_1 = new VisLabel("Zone");
        private final UTMConvert convert = new UTMConvert();
        VisTextButton.VisTextButtonStyle btnStyle = VisUI.getSkin().get("coordinateValues", VisTextButton.VisTextButtonStyle.class);
        final VisTextButton l1_2 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_5 = new VisTextButton("2", btnStyle);
        final VisTextButton l1_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l1_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_2 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_4 = new VisTextButton("2", btnStyle);
        final VisTextButton l2_5 = new VisTextButton("2", btnStyle);
        final VisTextButton l2_6 = new VisTextButton("5", btnStyle);
        final VisTextButton l2_7 = new VisTextButton("7", btnStyle);
        final VisTextButton l2_8 = new VisTextButton("7", btnStyle);
        final VisTextButton l3_2 = new VisTextButton("5", btnStyle);
        final VisTextButton l3_3 = new VisTextButton("5", btnStyle);
        final VisTextButton l3_4 = new VisTextButton("Z", btnStyle);


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

            focusSequence = new VisTextButton[]{l1_2, l1_3, l1_4, l1_5, l1_6, l1_7, l2_2, l2_3, l2_4, l2_5, l2_6, l2_7, l2_8, l3_2, l3_3, l3_4};
            focusBegin = 0;
            focusLineEnd = -1;
            focusNextLineBegin = 0;

            //add clickListener
            l1_2.addListener(clickListener);
            l1_3.addListener(clickListener);
            l1_4.addListener(clickListener);
            l1_5.addListener(clickListener);
            l1_6.addListener(clickListener);
            l1_7.addListener(clickListener);
            l2_2.addListener(clickListener);
            l2_3.addListener(clickListener);
            l2_4.addListener(clickListener);
            l2_5.addListener(clickListener);
            l2_6.addListener(clickListener);
            l2_7.addListener(clickListener);
            l2_8.addListener(clickListener);
            l3_2.addListener(clickListener);
            l3_3.addListener(clickListener);
            l3_4.addListener(clickListener);
        }

        @Override
        Coordinate getValue() {

            StringBuilder sb = new StringBuilder();
            SnapshotArray<Actor> childs = this.getChildren();
            for (Actor actor : childs) {
                if (actor == null) {
                    sb.append(" ");
                } else {
                    if (actor instanceof VisTextButton) {
                        sb.append(((VisTextButton) actor).getText());
                    } else if (actor instanceof VisLabel) {
                        sb.append(((VisLabel) actor).getText());
                    }
                }
            }

            sb = sb.replace("OstW", "").replace("NordW", " ").replace("Zone", " ");

            //switch Zone to first block
            String s[] = sb.toString().split(" ");
            String utmStr = s[2] + " " + s[0] + " " + s[1];


            return new Coordinate(utmStr);
        }

        @Override
        void setValue(double lat, double lon) {
            convert.iLatLon2UTM(lat, lon);
            String easting = String.format("%d", (int) (convert.UTMEasting + 0.5f));
            String nording = String.format("%d", (int) (convert.UTMNorthing + 0.5f));
            String zone = String.format("%02d", convert.iUTM_Zone_Num);
            String UTMZoneLetter = convert.sUtmLetterActual(lat);

            if (easting != null && easting.length() != 0) l1_2.setText(String.valueOf(easting.charAt(0)));
            if (easting != null && easting.length() > 1) l1_3.setText(String.valueOf(easting.charAt(1)));
            if (easting != null && easting.length() > 2) l1_4.setText(String.valueOf(easting.charAt(2)));
            if (easting != null && easting.length() > 3) l1_5.setText(String.valueOf(easting.charAt(3)));
            if (easting != null && easting.length() > 4) l1_6.setText(String.valueOf(easting.charAt(4)));
            if (easting != null && easting.length() > 5) l1_7.setText(String.valueOf(easting.charAt(5)));

            if (nording != null && nording.length() != 0) l2_2.setText(String.valueOf(nording.charAt(0)));
            if (nording != null && nording.length() > 1) l2_3.setText(String.valueOf(nording.charAt(1)));
            if (nording != null && nording.length() > 2) l2_4.setText(String.valueOf(nording.charAt(2)));
            if (nording != null && nording.length() > 3) l2_5.setText(String.valueOf(nording.charAt(3)));
            if (nording != null && nording.length() > 4) l2_6.setText(String.valueOf(nording.charAt(4)));
            if (nording != null && nording.length() > 5) l2_7.setText(String.valueOf(nording.charAt(5)));
            if (nording != null && nording.length() > 6) l2_8.setText(String.valueOf(nording.charAt(6)));

            l3_2.setText(String.valueOf(zone.charAt(0)));
            l3_3.setText(String.valueOf(zone.charAt(1)));
            l3_4.setText(UTMZoneLetter);

        }

        @Override
        protected void onFocusSet(VisTextButton button) {
            //  Enable/Disable numPad or keyPad

            if (button == l3_4) {
                //disable numPad
                numPad.setVisible(false);
                // enable keyPad
                Gdx.input.setOnscreenKeyboardVisible(true);
                ((InputMultiplexer) Gdx.input.getInputProcessor()).addProcessor(keyboardListener);

            } else {
                //disable keyPad
                Gdx.input.setOnscreenKeyboardVisible(false);
                ((InputMultiplexer) Gdx.input.getInputProcessor()).removeProcessor(keyboardListener);
                // enable numPad
                numPad.setVisible(true);
            }


        }
    }

    private InputProcessor keyboardListener = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            return true;
        }

        private final char[] possibleUtmValues = new char[]{'Z', 'X', 'W', 'V', 'U', 'T', 'S', 'R', 'Q', 'P', 'N', 'M', 'L', 'K', 'J', 'H', 'G', 'F', 'E', 'D', 'C'};

        @Override
        public boolean keyTyped(char character) {
            //check utm Zone
            char upper = Character.toUpperCase(character);
            for (int i = 0, n = possibleUtmValues.length - 1; i < n; i++) {
                if (possibleUtmValues[i] == upper) {
                    actValueTable.enterValue(String.valueOf(upper));
                }
            }
            return true;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return true;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return true;
        }

        @Override
        public boolean scrolled(int amount) {
            return true;
        }
    };

    @Override
    public void dispose() {
        super.dispose();
        numPad.dispose();
        numPad = null;
    }
}
