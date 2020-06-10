package de.longri.cachebox3.live;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.CacheListChangedListener;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.IChanged;

public class LiveButton extends CB_Button implements CacheListChangedListener, IChanged {
    private static final Drawable iconLiveIsEnabled = VisUI.getSkin().getDrawable("gc_logo");
    private static final Drawable iconLiveIsDisabled = VisUI.getSkin().getDrawable("gc_logo_grey");
    private static final Drawable iconLiveIsActive = VisUI.getSkin().getDrawable("gc_logo_red");
    public static LiveButton liveButton;
    private boolean liveMapEnabled = false;

    private LiveButton() {
        super(Config.liveMapEnabled.getValue() ? iconLiveIsEnabled : iconLiveIsDisabled);
        addCaptureListener(new ClickLongClickListener() {
            @Override
            public boolean clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    setLiveMapEnabled(!liveMapEnabled);
                    return true;
                }
                return false;
            }

            @Override
            public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
                return false;
            }
        });
        liveMapEnabled = Config.liveMapEnabled.getValue();
        if (liveMapEnabled) {
            EventHandler.add(this);
            LiveMapQue.getInstance().addDownloadActiveListener(this);
        }
    }

    public static LiveButton getInstance() {
        if (liveButton == null) liveButton = new LiveButton();
        return liveButton;
    }

    public void setLiveMapEnabled(boolean _liveMapEnabled) {
        liveMapEnabled = _liveMapEnabled;
        Config.liveMapEnabled.setValue(liveMapEnabled);
        Config.AcceptChanges();
        if (liveMapEnabled) {
            EventHandler.add(this);
            setIcon(iconLiveIsEnabled);
            LiveMapQue.getInstance().addDownloadActiveListener(this);
            LiveMapQue.getInstance().quePosition(MapView.getLastCenterPos());
        } else {
            EventHandler.remove(this);
            setIcon(iconLiveIsDisabled);
            LiveMapQue.getInstance().clearDescriptorStack();
            LiveMapQue.getInstance().removeDownloadActiveListener(this);
        }
    }

    @Override
    public void cacheListChanged(CacheListChangedEvent event) {
        // meaning download is no longer active
        setIcon(iconLiveIsEnabled);
    }

    @Override
    public void isChanged() {
        if (LiveMapQue.getInstance().getDownloadIsActive()) {
            setIcon(iconLiveIsActive);
        } else {
            setIcon(iconLiveIsEnabled);
        }
    }

}
