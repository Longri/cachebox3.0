package de.longri.cachebox3.gui.stages.initial_tasks;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.show_activities.Action_Show_SelectDB_Dialog;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.FileList;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 28.08.16.
 */
public class LoadDbTask extends AbstractInitTask {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(LoadDbTask.class);

    public LoadDbTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void RUNABLE() {


        // initial DB
        Database.Data = new Database(Database.DatabaseType.CacheBox);

        Action_Show_SelectDB_Dialog selectDbDialog = new Action_Show_SelectDB_Dialog();

        // search number of DB3 files
        FileList fileList = null;
        try {
            fileList = new FileList(CB.WorkPath, "DB3");
        } catch (Exception ex) {
            log.error("slpash.Initial()", "search number of DB3 files", ex);
        }
        if ((fileList.size > 1) && Config.MultiDBAsk.getValue()) {
            selectDbDialog.Execute();
            //TODO wait for return;
        } else {
            selectDbDialog.loadSelectedDB();
        }
    }
}
