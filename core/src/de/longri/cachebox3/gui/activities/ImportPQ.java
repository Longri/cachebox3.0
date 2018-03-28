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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.PqListItemStyle;
import de.longri.cachebox3.gui.widgets.AligmentLabel;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Longri on 26.03.2018.
 */
public class ImportPQ extends ActivityBase {

    private final static Logger log = LoggerFactory.getLogger(ImportPQ.class);
    private final ListView pqList = new ListView(ListViewType.VERTICAL, false);
    private final CharSequenceButton bOK, bCancel;
    private final DefaultListViewAdapter itemArray = new DefaultListViewAdapter();
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private final ICancel iCancel = new ICancel() {
        @Override
        public boolean cancel() {
            return canceled.get();
        }
    };
    private final static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final PqListItemStyle itemStyle;

    public ImportPQ() {
        super("ImportPQ");
        this.itemStyle = VisUI.getSkin().get(PqListItemStyle.class);

        float contentWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx4;
        float listHeight = Gdx.graphics.getHeight() / 2;

        pqList.setBackground(this.style.background);
        this.add(pqList).width(new Value.Fixed(contentWidth)).height(new Value.Fixed(listHeight));
        this.row();

        // line for selected PQ's
        Label.LabelStyle selectedLabelStyle = new Label.LabelStyle();
        selectedLabelStyle.font = itemStyle.infoFont;
        selectedLabelStyle.fontColor = itemStyle.infoFontColor;
        final AligmentLabel selectedLabel = new AligmentLabel(Translation.get("PQnoSelection"), selectedLabelStyle, Align.left);
        pqList.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                Array<ListViewItemInterface> selectedItems = pqList.getSelectedItems();
                if (selectedItems == null) {
                    selectedLabel.setText(Translation.get("PQnoSelection"));
                    bOK.setDisabled(true);
                } else {
                    selectedLabel.setText(Translation.get("PQSelectionCount", Integer.toString(selectedItems.size)));
                    bOK.setDisabled(false);
                }
            }
        });
        this.add(selectedLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();


        // fill and add Buttons
        this.row().expandY().fillY().bottom();
        this.add();
        this.row();
        bOK = new CharSequenceButton(Translation.get("import"));
        bCancel = new CharSequenceButton(Translation.get("cancel"));
        bOK.setDisabled(true);
        bCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                canceled.set(true);
                ImportPQ.this.finish();
            }
        });
        Table nestedTable2 = new Table();
        nestedTable2.defaults().pad(CB.scaledSizes.MARGIN).bottom();
        nestedTable2.add(bOK).bottom();
        nestedTable2.add(bCancel).bottom();
        this.add(nestedTable2).colspan(5);
    }

    @Override
    public void onShow() {
        refreshPQList();
    }

    @Override
    public void onHide() {
    }

    private void refreshPQList() {
        pqList.setEmptyString(Translation.get("EmptyPqList"));
        pqList.showWorkAnimationUntilSetAdapter();

        CB.postAsync(new NamedRunnable("refreshPQList") {
            @Override
            public void run() {
                itemArray.clear();
                Array<PocketQuery.PQ> list = new Array<>();
                PocketQuery pocketQuery = new PocketQuery(GroundspeakAPI.getAccessToken(true), iCancel, list);

                final AtomicBoolean WAIT = new AtomicBoolean(true);
                final ApiResultState[] state = new ApiResultState[1];
                pocketQuery.post(new GenericCallBack<ApiResultState>() {
                    @Override
                    public void callBack(ApiResultState value) {
                        state[0] = value;
                        WAIT.set(false);
                    }
                });
                CB.wait(WAIT);

                if (CB.checkApiResultState(state[0])) {
                    ImportPQ.this.finish();
                }

                if (canceled.get()) return;

                int idx = 0;
                for (PocketQuery.PQ pq : list) {
                    //Check last import
                    GdxSqliteCursor cursor = Database.Data.myDB.rawQuery("SELECT * FROM PocketQueries WHERE PQName=\"" + pq.name + "\"");
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String dateTimeString = cursor.getString(2);
                        try {
                            Date date = iso8601Format.parse(dateTimeString);
                            pq.lastImported = date;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    //add only PQ's their download available
                    if (pq.downloadAvailable)
                        itemArray.add(new PqListItem(idx++, pq, itemStyle));
                }
                CB.postOnGlThread(new NamedRunnable("SetAdapter") {
                    @Override
                    public void run() {
                        if (canceled.get()) return;
                        pqList.setAdapter(itemArray);
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {

    }
}
