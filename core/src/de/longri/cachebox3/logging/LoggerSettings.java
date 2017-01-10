package de.longri.cachebox3.logging;

import de.longri.cachebox3.gui.stages.StageManager;

import java.text.SimpleDateFormat;

import static de.longri.cachebox3.logging.GdxLogger.*;
import static de.longri.cachebox3.logging.LoggerFactory.EXCLUDE_LIST;

/**
 * Created by Longri on 22.12.16.
 */
public class LoggerSettings {

    static { //global log settings

        Logger.currentLogLevel = LOG_LEVEL_TRACE;

        LEVEL_IN_BRACKETS = true;
        SHOW_THREAD_NAME = false;
        SHOW_SHORT_LOG_NAME = true;
        SHOW_LOG_NAME = false;

        SHOW_DATE_TIME = true;
        DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");


        EXCLUDE_LIST.add("Database.CacheBox");
        EXCLUDE_LIST.add("Database.Settings");
        EXCLUDE_LIST.add("de.longri.cachebox3.settings.Config");
        EXCLUDE_LIST.add("com.badlogic.gdx.sqlite.desktop.DesktopDatabase");
        EXCLUDE_LIST.add(StageManager.class.getName());
        EXCLUDE_LIST.add("com.badlogic.gdx.scenes.scene2d.ui.SvgSkin");
        EXCLUDE_LIST.add("de.longri.cachebox3.locator.Locator");
        EXCLUDE_LIST.add("de.longri.cachebox3.IOS_LocationListener");

//        INCLUDE_LIST.add(StageManager.class.getName());
//        INCLUDE_LIST.add(CacheListDAO.class.getName());
//        INCLUDE_LIST.add(WaypointDAO.class.getName());
//        INCLUDE_LIST.add(Action_Show_SelectDB_Dialog.class.getName());
//
//        INCLUDE_LIST.add("com.badlogic.gdx.sqlite.robovm.RobovmDatabase");
//        INCLUDE_LIST.add("com.badlogic.gdx.sqlite.robovm.RobovmCursor");
//
//        INCLUDE_LIST.add("com.badlogic.gdx.sqlite.desktop.DesktopDatabase");
//        INCLUDE_LIST.add("com.badlogic.gdx.sqlite.desktop.DesktopCursor");
    }


    static boolean isInit = false;

    static void init() {
        isInit = true;
    }


}
