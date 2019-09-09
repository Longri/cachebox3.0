package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ArrayMap;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.apis.gcvote_api.GCVote;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.widgets.CB_CheckBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;

public class SelectableImport extends Activity {
    private final CB_CheckBox cbPocketQuery, cbGPX, cbGCVotes, cbLogs, cbImages, cbSpoiler, cbDeleteLogs, cbCompressDB, cbServer;

    private SelectableImport(String title, Drawable icon) {
        super(title, icon);
        Config.GcLogin.setValue("arbor95");
        Config.GcVotePassword.setValue("ZvncaG_54");
        Config.AcceptChanges();
        cbPocketQuery = new CB_CheckBox(Translation.get("PQfromGC"));
        cbGPX = new CB_CheckBox(Translation.get("GPX"));
        cbGCVotes = new CB_CheckBox(Translation.get("GCVoteRatings"));
        cbLogs = new CB_CheckBox(Translation.get("LoadLogs"));
        cbImages = new CB_CheckBox(Translation.get("PreloadImages"));
        cbSpoiler = new CB_CheckBox(Translation.get("PreloadSpoiler"));
        cbDeleteLogs = new CB_CheckBox(Translation.get("DeleteLogs"));
        cbCompressDB = new CB_CheckBox(Translation.get("CompactDB"));
        cbServer = new CB_CheckBox(Translation.get("FromCBServer"));
        cbServer.setDisabled(true);
    }

    public static SelectableImport getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new SelectableImport(title, icon);
            activity.top();
        }
        return (SelectableImport) activity;
    }

    @Override
    protected void createMainContent() {
        if (GroundspeakAPI.isPremiumMember()) mainContent.addLast(cbPocketQuery);
        mainContent.addLast(cbGPX);
        if (Config.GcVotePassword.getValue().length() > 0) mainContent.addLast(cbGCVotes);
        mainContent.addLast(cbLogs);
        mainContent.addLast(cbImages);
        mainContent.addLast(cbSpoiler);
        mainContent.addLast(cbDeleteLogs);
        mainContent.addLast(cbCompressDB);
        mainContent.addLast(cbServer);
    }

    @Override
    protected void runAtOk() {
        if (cbPocketQuery.isChecked()) {

        }
        if (cbGPX.isChecked()) {

        }
        if (cbGCVotes.isChecked()) {
            importGCVote();
        }
        if (cbLogs.isChecked()) {

        }
        if (cbImages.isChecked()) {

        }
        if (cbSpoiler.isChecked()) {

        }
        if (cbDeleteLogs.isChecked()) {

        }
        if (cbCompressDB.isChecked()) {

        }
        if (cbServer.isChecked()) {

        }
        finish();
    }

    private void importGCVote() {
        int take = 50;
        ArrayMap<String, AbstractCache> waypoints = new ArrayMap<>();
        for (int skip = 0; skip < Database.Data.cacheList.size; skip = skip + take) {
            int limit = Math.min(skip + take, Database.Data.cacheList.size);
            for (int i = skip; i < limit; i++) {
                waypoints.put(Database.Data.cacheList.get(i).getGcCode().toString(), Database.Data.cacheList.get(i));
            }
            Database.Data.beginTransaction();
            Database.Parameters args = new Database.Parameters();
            for (AbstractCache a : GCVote.getVotes(Config.GcLogin.getValue(), Config.GcVotePassword.getValue(), waypoints)) {
                args.put("Rating", (int) (a.getRating() * 200));
                Database.Data.update("CacheCoreInfo", args, "WHERE id=?", new String[]{Long.toString(a.getId())});
                args.clear();
            }
            Database.Data.endTransaction();
        }
    }
}
