package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.skin.styles.CacheListItemStyle;
import de.longri.cachebox3.gui.skin.styles.SelectBoxStyle;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;

import static de.longri.cachebox3.CB.addClickHandler;

/**
 * Created by Longri on 23.08.2016.
 */
public class EditCache extends Activity {
    private final CB_Label lblCachetitle, lblGcCode, lblOwner, lblCountry, lblState, lblDescription;
    private final EditTextField cacheTitle, cacheCode, cacheOwner, cacheCountry, cacheState, cacheDescription;
    // Allgemein
    private final CacheTypes[] CacheTypNumbers = CacheTypes.caches();
    private final CacheSizes[] CacheSizeNumbers = new CacheSizes[]{CacheSizes.other, // 0
            CacheSizes.micro, // 1
            CacheSizes.small, // 2
            CacheSizes.regular, // 3
            CacheSizes.large // 4
    };
    private CoordinateButton cacheCoords;
    private CB_Button cacheTyp, cacheSize, cacheDifficulty, cacheTerrain;
    private AbstractCache cache, newValues;

    private EditCache(String title, Drawable icon) {
        super(title, icon);
        SelectBoxStyle selectBoxStyle = VisUI.getSkin().get("default", SelectBoxStyle.class);
        VisTextButton.VisTextButtonStyle selectButtonStyle = new VisTextButton.VisTextButtonStyle();
        selectButtonStyle.up = selectBoxStyle.up;
        selectButtonStyle.down = selectBoxStyle.down;
        selectButtonStyle.font = selectBoxStyle.font;
        selectButtonStyle.fontColor = selectBoxStyle.fontColor;
        lblCachetitle = new CB_Label(Translation.get("Title"));
        lblGcCode = new CB_Label(Translation.get("GCCode"));
        lblOwner = new CB_Label(Translation.get("Owner"));
        lblCountry = new CB_Label(Translation.get("Country"));
        lblState = new CB_Label(Translation.get("State"));
        lblDescription = new CB_Label(Translation.get("Description"));
        cacheTitle = new EditTextField(true);
        cacheCode = new EditTextField();
        cacheOwner = new EditTextField();
        cacheState = new EditTextField();
        cacheCountry = new EditTextField();
        cacheTyp = new CB_Button("EditCacheType", "toggle"); // selectBoxStyle.selectIcon
        // cacheType.getDrawable(VisUI.getSkin().get("cacheListItems", CacheListItemStyle.class).typeStyle)
        //, cacheTypList(), cacheTypSelection());
        cacheDescription = new EditTextField(true);
        //.setWrapType(WrapType.WRAPPED);
        cacheDifficulty = new CB_Button("EditCacheDifficulty");
        // , cacheDifficultyList(), cacheDifficultySelection()
        cacheSize = new CB_Button("EditCacheSize");
        //, cacheSizeList(), cacheSizeSelection());
        cacheTerrain = new CB_Button("EditCacheTerrain");
        //, cacheTerrainList(), cacheTerrainSelection());
        cacheCoords = new CoordinateButton();
    }

    public static Activity getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new EditCache(title, icon);
            activity.top();
        }
        return activity;
    }

    @Override
    protected Catch_Table createMainContent() {
        mainContent.addNext(lblGcCode);
        mainContent.addLast(cacheCode);
        mainContent.addNext(cacheTyp);
        mainContent.addLast(cacheDifficulty, 0.3f);
        mainContent.addNext(cacheSize);
        mainContent.addLast(cacheTerrain, 0.3f);
        mainContent.addLast(lblCachetitle);
        mainContent.addLast(cacheTitle);
        mainContent.addLast(cacheCoords);
        mainContent.addLast(lblOwner);
        mainContent.addLast(cacheOwner);
        mainContent.addLast(lblOwner);
        mainContent.addLast(cacheOwner);
        mainContent.addLast(lblCountry);
        mainContent.addLast(cacheCountry);
        mainContent.addLast(lblState);
        mainContent.addLast(cacheState);
        mainContent.addLast(cacheDescription);
        addClickHandler(cacheTyp, this::selectCacheTyp);
        addClickHandler(cacheDifficulty, this::selectcacheDifficulty);
        addClickHandler(cacheSize, this::selectCacheSize);
        addClickHandler(cacheTerrain, this::selectCacheTerrain);

        return mainContent;
    }

    private void selectCacheTerrain() {
    }

    private void selectCacheSize() {
    }

    private void selectcacheDifficulty() {
    }

    private void selectCacheTyp() {
        Menu menu = new Menu("");
        for (CacheTypes cacheType : CacheTypes.caches()) {
            menu.addMenuItem("", cacheType.toString(), cacheType.getDrawable(VisUI.getSkin().get("cacheListItems", CacheListItemStyle.class).typeStyle), () -> {
            });
        }
        menu.show();
    }

    @Override
    protected void runAtOk() {
        finish();
    }

    @Override
    protected void runAtCancel() {
        finish();
    }
}
