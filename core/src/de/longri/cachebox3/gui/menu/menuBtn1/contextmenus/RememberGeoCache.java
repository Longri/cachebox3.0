package de.longri.cachebox3.gui.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;

public class RememberGeoCache  extends AbstractAction {

    private static RememberGeoCache rememberGeoCache;

    RememberGeoCache() {
        super("rememberGeoCacheTitle");
    }

    public static RememberGeoCache getInstance() {
        if (rememberGeoCache == null) rememberGeoCache = new RememberGeoCache();
        return rememberGeoCache;
    }

    @Override
    public void execute() {
        if (Config.rememberedGeoCache.getValue().length() > 0) {
            AbstractCache rememberedCache = Database.Data.cacheList.getCacheByGcCode(Config.rememberedGeoCache.getValue());
            if (rememberedCache != null) {
                EventHandler.fireSelectedWaypointChanged(rememberedCache, null);
                EventHandler.fire(new CacheListChangedEvent());
            }
        }
        else {
            Config.rememberedGeoCache.setValue(EventHandler.getSelectedCache().getGeoCacheCode().toString());
            Config.AcceptChanges();
        }
    }

    @Override
    public Drawable getIcon() {
        if (Config.rememberedGeoCache.getValue().length() > 0)
        return VisUI.getSkin().getDrawable("hasRememberedCache");
        else return VisUI.getSkin().getDrawable("noRememberedCache");
    }

    public void longClicked() {
        // forget remembered
        Config.rememberedGeoCache.setValue("");
        Config.AcceptChanges();
    }

}
