package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ArrayMap;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GCVote;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.dialogs.InfoBox;
import de.longri.cachebox3.gui.widgets.CB_CheckBox;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NamedRunnable;

public class SelectableImport extends Activity {
    final InfoBox infoBox;
    private final CB_CheckBox cbPocketQuery, cbGPX, cbGCVotes, cbLogs, cbImages, cbSpoiler, cbDeleteLogs, cbCompressDB, cbServer;

    private SelectableImport(String title, Drawable icon) {
        super(title, icon);
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
        infoBox = new InfoBox(InfoBox.Infotype.PROGRESS, Translation.get(title).toString());
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
        if (GroundspeakAPI.getInstance().isPremiumMember()) mainContent.addLast(cbPocketQuery);
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
        infoBox.open();
        CB.postAsync(new NamedRunnable("SelectableImport") {
            @Override
            public void run() {
                if (cbPocketQuery.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbGPX.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbGCVotes.isChecked() && !infoBox.isCanceled()) {
                    importGCVote();
                }
                if (cbLogs.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbImages.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbSpoiler.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbDeleteLogs.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbCompressDB.isChecked() && !infoBox.isCanceled()) {

                }
                if (cbServer.isChecked() && !infoBox.isCanceled()) {

                }
                // todo make changes visible (without reload ?)
                infoBox.close();
                finish();
            }
        });
    }

    public void importGCVote() {

        boolean mustCloseInfoBox = false;
        if (!infoBox.isRunning()) {
            infoBox.open();
            mustCloseInfoBox = true;
        }
        infoBox.setTitle(Translation.get("GCVoteRatings").toString());

        int take = 50;
        ArrayMap<String, AbstractCache> waypoints = new ArrayMap<>();
        GCVote gcVote = new GCVote(Database.Data, Config.GcLogin.getValue(), Config.GcVotePassword.getValue());
        if (gcVote.isPossible()) {
            for (int skip = 0; skip < Database.Data.cacheList.size; skip = skip + take) {
                int limit = Math.min(skip + take, Database.Data.cacheList.size);
                for (int i = skip; i < limit; i++) {
                    waypoints.put(Database.Data.cacheList.get(i).getGcCode().toString(), Database.Data.cacheList.get(i));
                }
                infoBox.setProgress(100 * skip / Database.Data.cacheList.size, Translation.get("GCVoteRatings").toString());
                gcVote.getVotes(waypoints);
            }
        }
        if (mustCloseInfoBox) infoBox.close();
    }
}
