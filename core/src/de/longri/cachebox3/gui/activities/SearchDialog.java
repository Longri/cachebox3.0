package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;

import static de.longri.cachebox3.CB.addClickHandler;

public class SearchDialog extends Activity {
    private final CB_Label lblCachetitle, lblGcCode, lblOwner;
    private final EditTextField edtCachetitle, edtGcCode, edtOwner;
    private final CB_Button btnFilter;
    private String mTitle;
    private String mGCCode;
    private String mOwner;
    private int beginnSearchIndex = -1;

    private SearchDialog(String title, Drawable icon) {
        super(title, icon);
        lblCachetitle = new CB_Label(Translation.get("Title"));
        lblGcCode = new CB_Label(Translation.get("GCCode"));
        lblOwner = new CB_Label(Translation.get("Owner"));
        edtCachetitle = new EditTextField("");
        edtGcCode = new EditTextField("");
        edtOwner = new EditTextField("");
        btnFilter = new CB_Button(Translation.get("Filter"));
        btnFilter.setDisabled(true);
        addClickHandler(btnFilter, () -> {
            // todo implement
        });
        btnCancel.setText(Translation.get("Search"));
        // todo implent buttontext "search next"
    }

    public static Activity getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new SearchDialog(title, icon);
            activity.top();
        }
        return activity;
    }

    protected void createMainContent() {
        mainContent.addLast(lblCachetitle);
        mainContent.addLast(edtCachetitle);
        mainContent.addLast(lblGcCode);
        mainContent.addLast(edtGcCode);
        mainContent.addLast(lblOwner);
        mainContent.addLast(edtOwner);
        mainContent.addLast(btnFilter);
    }

    protected void runAtOk() {
        finish();
    }

    public void runAtCancel() {
        btnCancel.setText(Translation.get("Next"));
        searchNow();
    }

    private void searchNow() {
        if (beginnSearchIndex < 0) {
            mTitle = edtCachetitle.getText().toLowerCase().replace("\n", "").replace("\r", "");
            mGCCode = edtGcCode.getText().toLowerCase().replace("\n", "").replace("\r", "");
            mOwner = edtOwner.getText().toLowerCase().replace("\n", "").replace("\r", "");
        }

        boolean criterionMatches = false;

        synchronized (Database.Data.cacheList) {

            AbstractCache tmp = null;
            if (beginnSearchIndex < 0) beginnSearchIndex = 0;
            for (int i = beginnSearchIndex; i < Database.Data.cacheList.size; i++) {
                tmp = Database.Data.cacheList.get(i);
                if ((mTitle.length() > 0 && tmp.getName().toString().toLowerCase().contains(mTitle))
                        || (mGCCode.length() > 0 && tmp.getGcCode().toString().toLowerCase().contains(mGCCode))
                        || (mOwner.length() > 0 && tmp.getOwner().toString().toLowerCase().contains(mOwner))) {
                    edtCachetitle.setText(tmp.getName());
                    edtGcCode.setText(tmp.getGcCode());
                    edtOwner.setText(tmp.getOwner());
                    criterionMatches = true;
                    beginnSearchIndex = i + 1;
                    break;
                }
            }

            if (criterionMatches) {
                if (tmp != null) {
                    AbstractWaypoint finalWp = tmp.getCorrectedFinal();
                    if (finalWp == null)
                        finalWp = tmp.getStartWaypoint();
                    EventHandler.fireSelectedWaypointChanged(tmp, finalWp);
                    // todo do correct cachelist selection
                    // CacheListView view = new CacheListView();
                    // CB.viewmanager.showView(view);
                    EventHandler.fire(new CacheListChangedEvent());
                }
                CB.setAutoResort(false);
            } else {
                btnCancel.setText(Translation.get("Search"));
                beginnSearchIndex = -1;
                edtCachetitle.setText(mTitle);
                edtGcCode.setText(mGCCode);
                edtOwner.setText(mOwner);
                MessageBox.show(Translation.get("NoCacheFound"), Translation.get("Search"), MessageBoxButtons.OK, MessageBoxIcon.Asterisk, null);
            }
        }
    }

}
