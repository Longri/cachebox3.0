package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.skin.styles.EditTextStyle;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.*;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.Cache3DAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableCache;
import de.longri.cachebox3.utils.NamedRunnable;

import java.util.Date;

/**
 * Created by Longri on 23.08.2016.
 */
public class EditCache extends Activity {
    private final CB_Label lblCachetitle, lblGcCode, lblOwner, lblCountry, lblState, lblDescription;
    private final EditTextField cacheTitle, cacheCode, cacheOwner, cacheCountry, cacheState;
    private final CB_Label cacheDescription;
    private CoordinateButton cacheCoords;
    private SelectBox<CacheTypes> cacheTyp;
    private SelectBox<CacheSizes> cacheSize;
    private AdjustableStarWidget cacheDifficulty;
    private AdjustableStarWidget cacheTerrain;
    private MutableCache cache, newValues;

    private EditCache(String title, Drawable icon) {
        super(title, icon);
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
        cacheTyp = new SelectBox();
        cacheTyp.setSelectTitle("EditCacheType");
        cacheTyp.set(CacheTypes.caches());
        cacheDifficulty = new AdjustableStarWidget(Translation.get("EditCacheDifficulty"));
        cacheTerrain = new AdjustableStarWidget(Translation.get("EditCacheTerrain"));
        cacheSize = new SelectBox();
        cacheSize.setSelectTitle("EditCacheSize");
        cacheSize.set(CacheSizes.Values());
        cacheCoords = new CoordinateButton();
        EditTextStyle edtStyle = VisUI.getSkin().get("default", EditTextStyle.class);
        Label.LabelStyle  cacheDescriptionStyle = new Label.LabelStyle(edtStyle.font, edtStyle.fontColor);
        cacheDescriptionStyle.background = edtStyle.background;
        cacheDescription = new CB_Label();
        cacheDescription.setWrap(true);
        cacheDescription.setStyle(cacheDescriptionStyle);
    }

    public static EditCache getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new EditCache(title, icon);
            activity.top();
        }
        return (EditCache) activity;
    }

    @Override
    protected Catch_Table createMainContent() {
        mainContent.addNext(lblGcCode, -0.3f);
        mainContent.addLast(cacheCode);
        mainContent.addNext(lblCachetitle, -0.3f);
        mainContent.addLast(cacheTitle);
        mainContent.addLast(cacheTyp);
        mainContent.addLast(cacheDifficulty);
        mainContent.addLast(cacheTerrain);
        mainContent.addLast(cacheSize);
        mainContent.addLast(cacheCoords);
        mainContent.addNext(lblOwner, -0.3f);
        mainContent.addLast(cacheOwner);
        mainContent.addNext(lblCountry, -0.3f);
        mainContent.addLast(cacheCountry);
        mainContent.addNext(lblState, -0.3f);
        mainContent.addLast(cacheState);
        mainContent.addLast(cacheDescription); //.width(getWidth() - 4 * CB.scaledSizes.MARGIN);

        cacheTyp.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            }
        });

        cacheSize.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            }
        });


        cacheDescription.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                Input.TextInputListener listener = new Input.TextInputListener() {
                    @Override
                    public void input(final String text) {
                        CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
                            @Override
                            public void run() {
                                cacheDescription.setText(text);
                                invalidate();
                            }
                        });
                    }

                    @Override
                    public void canceled() {
                        // do nothing
                    }
                };
                PlatformConnector.getMultilineTextInput(listener, 0, "", cacheDescription.getText().toString(), "");
            }
        });


        return mainContent;
    }

    public void update(AbstractCache cache) {
        newValues = new MutableCache(cache); // copy from cache with Details
        newValues.setShortDescription("");
        if (newValues.getLongDescription() == null) {
            newValues.setLongDescription(getStringFromDB(Database.Data, "SELECT Description FROM CacheText WHERE Id=?", newValues.getId()));
            //set on Cache Object for next showing
            cache.setLongDescription(newValues.getLongDescription() );
        }

        if (newValues.getShortDescription() == null) {
            newValues.setShortDescription(getStringFromDB(Database.Data, "SELECT ShortDescription FROM CacheText WHERE Id=?", newValues.getId()));
            //set on Cache Object for next showing
            cache.setShortDescription(newValues.getShortDescription());
        }
        cache.setLongDescription(newValues.getLongDescription());
        this.cache = (MutableCache) cache;
        setValues();
    }

    private static String getStringFromDB(Database database, String statement, long cacheID) {
        String[] args = new String[]{Long.toString(cacheID)};
        return database.getString(statement, args);
    }

    public void create() {
        String tmpGCCode;
        // GC - Code bestimmen für freies CWxxxx = CustomWaypint
        String prefix = "CW";
        int count = 0;
        do {
            count++;
            tmpGCCode = prefix + String.format("%04d", count);
        } while (Database.Data.cacheList.GetCacheById(MutableCache.GenerateCacheId(tmpGCCode)) != null);
        Coordinate actSearchPos;
        Coordinate lastStoredPos = CB.lastMapState.getFreePosition();
        Coordinate mapCenterPos = MapView.getLastCenterPos();
        if (mapCenterPos == null) {
            actSearchPos = new Coordinate(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
        } else {
            actSearchPos = mapCenterPos;
        }
        newValues = new MutableCache(actSearchPos.getLatitude(), actSearchPos.getLongitude(), tmpGCCode, CacheTypes.Traditional, tmpGCCode); // with Details
        newValues.setSize(CacheSizes.micro);
        newValues.setDifficulty(1);
        newValues.setTerrain(1);
        newValues.setOwner("Unbekannt");
        newValues.setState("");
        newValues.setCountry("");
        newValues.setDateHidden(new Date());
        newValues.setArchived(false);
        newValues.setAvailable(true);
        newValues.setFound(false);
        newValues.setNumTravelbugs((short) 0);
        newValues.setShortDescription("");
        newValues.setLongDescription("");
        this.cache = newValues;
        setValues();
    }

    private void setValues() {
        cacheCode.setText(cache.getGcCode());
        cacheTyp.select(cache.getType());
        cacheSize.select(cache.getSize());
        cacheDifficulty.setValue((int) (cache.getDifficulty() * 2));
        cacheTerrain.setValue((int) (cache.getTerrain() * 2));
        cacheCoords.setCoordinate(new Coordinate(cache.getLatitude(), cache.getLongitude()));
        cacheTitle.setText(cache.getName());
        cacheOwner.setText(cache.getOwner());
        cacheState.setText(cache.getState());
        cacheCountry.setText(cache.getCountry());
        if (cache.getLongDescription().equals("\n") || cache.getLongDescription().equals("\n\r"))
            cache.setLongDescription("");
        cacheDescription.setText(cache.getLongDescription());
        show();
    }

    @Override
    protected void runAtOk() {
        boolean update = false;
        String gcc = cacheCode.getText().toUpperCase();
        cache.setId(AbstractCache.GenerateCacheId(gcc));

        AbstractCache cl = Database.Data.cacheList.GetCacheById(cache.getId());

        if (cl != null) {
            update = true;
            // if (newValues.getType() == CacheTypes.Mystery) {
                if (cache.getLatitude() != newValues.getLatitude() || cache.getLongitude() != newValues.getLongitude()) {
                    cache.setHasCorrectedCoordinates(true);
                }
            //}
        }

        cache.setGcCode(gcc);
        cache.setType(cacheTyp.getSelected());
        cache.setSize(cacheSize.getSelected());
        cache.setDifficulty(cacheDifficulty.getValue() / 2f);
        cache.setTerrain(cacheTerrain.getValue() / 2f);
        cache.setLatitude(cacheCoords.getCoordinate().getLatitude());
        cache.setLongitude(cacheCoords.getCoordinate().getLongitude());
        cache.setName(cacheTitle.getText());
        cache.setOwner(cacheOwner.getText());
        cache.setState(cacheState.getText());
        cache.setCountry(cacheCountry.getText());
        cache.setLongDescription(cacheDescription.getText());
        Cache3DAO dao = new Cache3DAO();
        if (update) {
            dao.updateDatabase(Database.Data, cache, true);
        } else {
            Database.Data.cacheList.add(cache);
            dao.writeToDatabase(Database.Data, cache, true);
            EventHandler.updateSelectedCache(cache);
        }

        // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
        // cache.setLongDescription("");
        finish();
    }

    @Override
    protected void runAtCancel() {
        finish();
    }
}
