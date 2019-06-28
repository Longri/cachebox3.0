/*
 * Copyright (C) 2016-2018 team-cachebox.de
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
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.activities.BlockUiProgress_Activity;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.gui.map.layer.ThemeMenu;
import de.longri.cachebox3.gui.skin.styles.ScaledSize;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.CacheListView;
import de.longri.cachebox3.locator.manager.LocationHandler;
import de.longri.cachebox3.locator.track.Track;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Categories;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.cachebox3.utils.*;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.oscim.core.Tile;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeLoader;
import org.oscim.theme.VtmThemes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.longri.cachebox3.apis.GroundspeakAPI.isAccessTokenInvalid;

/**
 * Static class
 * Created by Longri on 20.07.2016.
 */
public class CB {
    public static final boolean DRAW_EXCEPTION_INDICATOR = true;
    public static final Color EXCEPTION_COLOR_DRAWING = Color.RED;
    public static final Color EXCEPTION_COLOR_POST = Color.YELLOW;
    public static final Color EXCEPTION_COLOR_EVENT = Color.GREEN;
    public static final Color EXCEPTION_COLOR_LOCATION = Color.BLUE;
    public static final String VersionPrefix = "Test";
    //LogLevels
    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_DEBUG = "DEBUG";
    public static final String LOG_LEVEL_WARN = "WARN";
    public static final String LOG_LEVEL_ERROR = "ERROR";
    public static final String LOG_LEVEL_TRACE = "TRACE";
    public static final String USED_LOG_LEVEL = LOG_LEVEL_DEBUG;
    public static final float WINDOW_FADE_TIME = 0.5f;
    public static final MapState actMapState = new MapState();
    public static final MapState lastMapState = new MapState();
    public static final MapState lastMapStateBeforeCar = new MapState();
    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");
    public static final String AboutMsg = "Team Cachebox (2011-2016)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";
    public final static SensorIO sensoerIO = new SensorIO();
    static final Logger log = LoggerFactory.getLogger(CB.class);
    final static float PPI_DEFAULT = 163;
    final static AtomicInteger executeCount = new AtomicInteger(0);
    final static Array<String> runningRunnables = new Array<>();
    private static final AsyncExecutor asyncExecutor = new AsyncExecutor(50);
    public static LocationHandler locationHandler;
    public static Categories Categories;
    public static float stateTime;
    public static int androidStatusbarHeight;
    public static ViewManager viewmanager;
    public static boolean switchToCompassCompleted = false;
    public static String cacheHistory = "";
    public static CacheboxMain cbMain;
    public static StageManager stageManager;
    /**
     * WorkPath is a String to the used work path.<br>
     * This Path is a absolute path.<br>
     * <br>
     * On iOS this is the path to the "SandBox". <br>
     * On Android this can a path to internal SD "/Cachebox3" <br>
     * or to the "SandBox" on the external SD
     */
    public static String WorkPath;
    public static FileHandle WorkPathFileHandle;
    public static Color backgroundColor = new Color(0, 1, 0, 1);
    public static ScaledSizes scaledSizes;
    public static Track actRoute;
    public static int actRouteCount;
    public static LinkedHashMap<Object, TextureRegion> textureRegionMap;
    public static Image CB_Logo;
    public static Image backgroundImage;
    public static boolean isBackground = false;
    static boolean mapScaleInitial = false;
    private static IRenderTheme actTheme;
    private static float globalScale = 1;
    private static AbstractCache nearestAbstractCache = null;
    private static boolean autoResort;
    private static SvgSkin actSkin;
    private static boolean isTestVersionCheked = false;
    private static boolean isTestVersion = false;
    private static float scalefactor = 0;
    private static AtomicBoolean quitCalled = new AtomicBoolean(false);
    // GL-Thread check
    private static Thread GL_THREAD;
    private static boolean mockChecked = false;
    private static boolean isMocked = false;
    private static IChanged mapScaleSettingChanged = new IChanged() {

        private float lastDpi = 0;
        private float lastText = 0;

        @Override
        public void isChanged() {
            float dpi = Settings.MapViewDPIFaktor.getValue();
            float text = Settings.MapViewTextFaktor.getValue();
            if (dpi != lastDpi || text != lastText) {
                lastDpi = dpi;
                lastText = text;
                //calculate CanvasAdapter.dpi
                float scaleFactor = CB.getScaledFloat(dpi);
                CanvasAdapter.dpi = CanvasAdapter.DEFAULT_DPI * scaleFactor;
                CanvasAdapter.textScale = text;
                Tile.SIZE = Tile.calculateTileSize();
                setCurrentTheme(ThemeIsFor.day); // todo set the correct parameter
            }
        }
    };
    static private Runtime runtime;
    static private StringBuilder memoryStringBuilder = new StringBuilder();
    static private NumberFormat format = NumberFormat.getInstance();

    private CB() {
    }

    public static boolean isTestVersion() {
        if (isTestVersionCheked)
            return isTestVersion;

        isTestVersion = VersionPrefix.toLowerCase().contains("test");
        isTestVersionCheked = true;
        return isTestVersion;
    }

    public static void setActSkin(SvgSkin skin) {
        if (actSkin != null) {
            VisUI.dispose();
        }
        actSkin = skin;
        VisUI.load(actSkin);

        // calculate scaled sizes
        float button_width = CB.getScaledFloat(actSkin.get("button_width", ScaledSize.class).value);
        float button_height = CB.getScaledFloat(actSkin.get("button_height", ScaledSize.class).value);
        float button_width_wide = CB.getScaledFloat(actSkin.get("button_width_wide", ScaledSize.class).value);
        float margin = CB.getScaledFloat(actSkin.get("margin", ScaledSize.class).value);
        float check_box_height = CB.getScaledFloat(actSkin.get("check_box_height", ScaledSize.class).value);
        float window_margin = CB.getScaledFloat(actSkin.get("check_box_height", ScaledSize.class).value);
        CB.scaledSizes = new ScaledSizes(button_width, button_height, button_width_wide, margin,
                check_box_height, window_margin);
    }

    public static SkinColor getColor(String name) {
        return actSkin.get(name, SkinColor.class);
    }

    public static SvgSkin getSkin() {
        return actSkin;
    }

    public static float getScaledFloat(float value) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (value * scalefactor);
    }

    public static float getScaledFloat(int value) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (value * scalefactor);
    }

    public static float getUnScaledFloat(float value) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (value / scalefactor);
    }

    public static int getScaledInt(int i) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (int) Math.ceil((float) i * scalefactor);
    }

    private static void calcScaleFactor() {
        if (CanvasAdapter.platform.isDesktop()) {
            //Desktop
            scalefactor = (Math.max(Gdx.graphics.getPpiX(), Gdx.graphics.getPpiY()) / PPI_DEFAULT) * globalScale;
        } else if (CanvasAdapter.platform == Platform.IOS) {
            //iOS
            scalefactor = ((Math.max(Gdx.graphics.getPpiX(), Gdx.graphics.getPpiY()) / PPI_DEFAULT) * globalScale);
        } else {
            //Android
            scalefactor = (float) (0.0325520 * androidStatusbarHeight) * globalScale;
        }
    }

    public static void setGlobalScale(float scale) {
        globalScale = scale;
    }

    public static float getScalefactor() {
        if (scalefactor == 0)
            calcScaleFactor();
        return scalefactor;
    }

    public static boolean isLogLevel(String logLevel) {
        if (logLevelToInt(USED_LOG_LEVEL) >= logLevelToInt(logLevel)) return true;
        return false;
    }

    private static int logLevelToInt(String logLevel) {
        if (LOG_LEVEL_TRACE.equals(logLevel)) return 10;
        if (LOG_LEVEL_ERROR.equals(logLevel)) return 8;
        if (LOG_LEVEL_WARN.equals(logLevel)) return 6;
        if (LOG_LEVEL_DEBUG.equals(logLevel)) return 4;
        if (LOG_LEVEL_INFO.equals(logLevel)) return 2;
        return 0;
    }

    public static Sprite getSprite(String name) {
        return actSkin != null ? actSkin.getSprite(name) : null;
    }

    public static boolean isQuitCalled() {
        return quitCalled.get();
    }

    public static void callQuit() {
        PlatformConnector.callQuit();
        quitCalled.set(true);
    }

    public static boolean selectedCachehasSpoiler() {
        return EventHandler.actCacheHasSpoiler();
    }

    public static void requestRendering() {
        if (Gdx.graphics == null) return;
        Gdx.graphics.requestRendering();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Gdx.graphics.requestRendering();
            }
        });
    }

    public static boolean getAutoResort() {
        return autoResort;
    }

    public static void setAutoResort(boolean value) {
        autoResort = value;
    }

    public static void setNearestCache(AbstractCache AbstractCache) {
        nearestAbstractCache = AbstractCache;
    }

    public static AbstractCache NearestCache() {
        return nearestAbstractCache;
    }

    public static boolean isMocked() {
        if (!mockChecked) {
            isMocked = Gdx.gl.toString().toLowerCase().contains("mock");
            mockChecked = true;
        }
        return isMocked;
    }

    public static void assertGlThread() {

        if (!isMocked() && GL_THREAD != Thread.currentThread()) {
            throw new RuntimeException("Access from non-GL thread!");
        }
    }

    public static boolean isGlThread() {
        return GL_THREAD == Thread.currentThread();
    }

    public static void setGlThread(Thread glThread) {
        GL_THREAD = glThread;
    }

    public static void initThreadCheck() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GL_THREAD = Thread.currentThread();
            }
        });
    }

    public static void scheduleOnGlThread(final NamedRunnable runnable, long delay) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                postOnGlThread(runnable);
                requestRendering();
            }
        };
        new Timer().schedule(timerTask, delay);
        requestRendering();
    }

    public static void postOnGlThread(final NamedRunnable runnable) {
        postOnGlThread(runnable, false);
    }

    public static void postOnGlThread(final NamedRunnable runnable, boolean wait) {
        if (isGlThread()) {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("postOnGlThread:" + runnable.name, e);
                CB.stageManager.indicateException(EXCEPTION_COLOR_POST);
            }
            return;
        }
        final AtomicBoolean WAIT = new AtomicBoolean(wait);
        Gdx.app.postRunnable(new NamedRunnable(runnable.name) {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.error("postOnGlThread:" + runnable.name, e);
                    if (CB.stageManager != null) CB.stageManager.indicateException(EXCEPTION_COLOR_POST);
                }
                WAIT.set(false);
            }
        });

        while (WAIT.get()) {
            if (Gdx.graphics != null) Gdx.graphics.requestRendering();// in case of JUnit test
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void postAsyncDelayd(final long delay, final NamedRunnable runnable) {
        if (runnable == null) return;
        postAsync(new NamedRunnable("delayed runnable: " + runnable.name) {
            @Override
            public void run() {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            log.error("postAsyncDelayd:" + runnable.name, e);
                            CB.stageManager.indicateException(EXCEPTION_COLOR_POST);
                        }
                    }
                };
                new Timer().schedule(task, delay);
            }
        });
    }

    public static void postAsync(final NamedRunnable runnable) {
        runningRunnables.add(runnable.name);
        log.debug("Submit Async execute count {} runs: {}", executeCount.incrementAndGet(), runningRunnables.toString());

        asyncExecutor.submit(new AsyncTask<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    log.debug("Start runnable: {}", runnable.name);
                    runnable.run();
                    log.debug("Finish runnable: {}", runnable.name);
                    runningRunnables.removeValue(runnable.name, false);
                    log.debug("Ready Async executed runnable, count {} runs: {}", executeCount.decrementAndGet(), runningRunnables.toString());
                } catch (final Exception e) {
                    log.error("postAsync:" + runnable.name, e);
                    CB.stageManager.indicateException(EXCEPTION_COLOR_POST);
                    executeCount.decrementAndGet();
                }
                return null;
            }
        });
    }

    public static boolean checkApiKeyNeeded() {
        return isAccessTokenInvalid();
    }

    public static void wait(AtomicBoolean wait) {
        wait(wait, false, null);
    }

    public static void wait(AtomicBoolean wait, ICancel iCancel) {
        wait(wait, false, iCancel);
    }

    public static void wait(AtomicBoolean wait, boolean negate, ICancel iCancel) {

        boolean checkCancel = iCancel != null;

        while (negate ? !wait.get() : wait.get()) {
            if (checkCancel) {
                if (iCancel.cancel()) return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadFilteredCacheList(FilterProperties filter) {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                new BlockUiProgress_Activity(Translation.get("LoadCacheList")).show();
            }
        });

        log.debug("load filtered Cache list on Thread[{}]", Thread.currentThread().getName());

        Config.readFromDB(true);
        CB.Categories = new Categories();

        String sqlWhere = "";
        if (CB.viewmanager != null) {
            if (filter == null) {
                String filterString = Config.FilterNew.getValue();
                try {
                    FilterProperties filterProperties = null;
                    try {
                        filterProperties = new FilterProperties("?", filterString);
                    } catch (Exception e) {
                        log.warn("Can't instance FilterProperties with FilterString: {}", filterString);
                        filterProperties = FilterInstances.ALL;
                    }

                    CB.viewmanager.setNewFilter(filterProperties, true);
                    sqlWhere = CB.viewmanager.getActFilter().getSqlWhere(Config.GcLogin.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sqlWhere = filter.getSqlWhere(Config.GcLogin.getValue());
            }
        }

        Database.Data.gpxFilenameUpdateCacheCount();

        log.debug("Read CacheList");
        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
        log.debug("Readed " + Database.Data.cacheList.size + " Caches into CacheList");

        // set selectedCache from last selected Cache
        String sGc = Config.LastSelectedCache.getValue();
        AbstractCache lastSelectedAbstractCache = null;
        if (sGc != null && !sGc.equals("")) {
            for (int i = 0, n = Database.Data.cacheList.size; i < n; i++) {
                AbstractCache c = Database.Data.cacheList.get(i);

                if (c.getGcCode().toString().equalsIgnoreCase(sGc)) {
                    try {
                        log.debug("returnFromSelectDB:Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                        EventHandler.fire(new SelectedCacheChangedEvent(c));
                        lastSelectedAbstractCache = c;
                    } catch (Exception e) {
                        log.error("set last selected Cache", e);
                    }
                    break;
                }
            }
        }
        // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
        if ((lastSelectedAbstractCache == null) && (Database.Data.cacheList.size > 0)) {
            log.debug("Set selectedCache to " + Database.Data.cacheList.get(0).getGcCode() + " from firstInDB");
            EventHandler.fire(new SelectedCacheChangedEvent(Database.Data.cacheList.get(0)));
        }

        CB.setAutoResort(Config.StartWithAutoSelect.getValue());
        EventHandler.fire(new CacheListChangedEvent());

        if (CB.viewmanager != null && CB.viewmanager.getActView() instanceof CacheListView) {
            CacheListView cacheListView = (CacheListView) CB.viewmanager.getActView();
            cacheListView.setWaitToastLength(ViewManager.ToastLength.WAIT);
        } else {
            Gdx.app.postRunnable(new NamedRunnable("CB:Toast") {
                @Override
                public void run() {
                    ViewManager.ToastLength.WAIT.close();
                }
            });
        }
    }

    public static void postOnMainThreadDelayed(int delay, final NamedRunnable runnable) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                postOnMainThread(runnable);
            }
        };
        new Timer().schedule(task, delay);
    }

    public static void postOnGLThreadDelayed(int delay, final NamedRunnable runnable) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                postOnGlThread(runnable);
            }
        };
        new Timer().schedule(task, delay);
    }

    public static void postOnMainThread(NamedRunnable runnable) {
        PlatformConnector.postOnMainThread(runnable);
    }

    public static void postOnNextGlThread(final Runnable runnable) {
        Gdx.app.postRunnable(() -> Gdx.app.postRunnable(() -> {
            runnable.run();
            requestRendering();
        }));
    }

    public static boolean isCarMode() {
        return actMapState.getMapMode() == MapMode.CAR;
    }

    public static IRenderTheme getCurrentTheme() {
        return actTheme;
    }

    public static void setCurrentTheme(ThemeIsFor themeIsFor) {
        if (!mapScaleInitial) {
            Settings.MapViewDPIFaktor.addChangedEventListener(mapScaleSettingChanged);
            Settings.MapViewTextFaktor.addChangedEventListener(mapScaleSettingChanged);
            mapScaleInitial = true;
        }

        String path = getConfigsThemePath(themeIsFor);
        if (path.startsWith("VTM:") || path.length() == 0) {
            VtmThemes themeFile;
            if (path.length() == 0) {
                themeFile = VtmThemes.DEFAULT; // or VtmThemes.OSMARENDER
            } else {
                String name = path.replace("VTM:", "");
                themeFile = VtmThemes.valueOf(name);
            }
            actTheme = ThemeLoader.load(themeFile);
        } else {
            ThemeMenu themeMenu = new ThemeMenu(getConfigsThemePath(themeIsFor));
            themeMenu.applyConfig(getConfigsMapStyle(themeIsFor));
            actTheme = themeMenu.getRenderTheme();
        }
    }

    public static String getConfigsThemePath(ThemeIsFor themeIsFor) {
        switch (themeIsFor) {
            case day:
                return Config.MapsforgeDayTheme.getValue();
            case night:
                return Config.MapsforgeNightTheme.getValue();
            case carday:
                return Config.MapsforgeCarDayTheme.getValue();
            default: //case carnight:
                return Config.MapsforgeCarNightTheme.getValue();
        }
    }

    public static void setConfigsThemePath(ThemeIsFor themeIsFor, String path) {
        switch (themeIsFor) {
            case day:
                Config.MapsforgeDayTheme.setValue(path);
                break;
            case night:
                Config.MapsforgeNightTheme.setValue(path);
                break;
            case carday:
                Config.MapsforgeCarDayTheme.setValue(path);
                break;
            default: //case carnight:
                Config.MapsforgeCarNightTheme.setValue(path);
        }
    }

    public static String getConfigsMapStyle(ThemeIsFor themeIsFor) {
        switch (themeIsFor) {
            case day:
                return Config.MapsforgeDayStyle.getValue();
            case night:
                return Config.MapsforgeNightStyle.getValue();
            case carday:
                return Config.MapsforgeCarDayStyle.getValue();
            default: //case carnight:
                return Config.MapsforgeCarNightStyle.getValue();
        }
    }

    public static void setConfigsMapStyle(ThemeIsFor themeIsFor, String mapStyle) {
        switch (themeIsFor) {
            case day:
                Config.MapsforgeDayStyle.setValue(mapStyle);
                break;
            case night:
                Config.MapsforgeNightStyle.setValue(mapStyle);
                break;
            case carday:
                Config.MapsforgeCarDayStyle.setValue(mapStyle);
                break;
            case carnight:
                Config.MapsforgeCarNightStyle.setValue(mapStyle);
                break;
        }
    }

    public static String getMemoryUsage() {
        if (runtime == null) runtime = Runtime.getRuntime();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        memoryStringBuilder.clear();
        memoryStringBuilder.append("used: ");
        memoryStringBuilder.append(format.format((allocatedMemory - freeMemory) / 1048576));
        memoryStringBuilder.append(" kb");
        return memoryStringBuilder.toString();
    }

    public float getGlobalScaleFactor() {
        return globalScale;
    }

    public enum ThemeIsFor {
        day, night, carday, carnight
    }

}

