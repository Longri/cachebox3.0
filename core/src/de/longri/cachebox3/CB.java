/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.GpsEventHelper;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.activities.BlockUiProgress_Activity;
import de.longri.cachebox3.gui.dialogs.GetApiKeyQuestionDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.skin.styles.ScaledSize;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.CacheListView;
import de.longri.cachebox3.locator.track.Track;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Categories;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.ScaledSizes;
import de.longri.cachebox3.utils.SkinColor;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Static class
 * Created by Longri on 20.07.2016.
 */
public class CB {
    static final Logger log = LoggerFactory.getLogger(CB.class);

    public static final int CurrentRevision = 20160806;
    public static final String CurrentVersion = "0.1.";
    public static final String VersionPrefix = "Test";


    //LogLevels
    public static final String LOG_LEVEL_INFO = "info";
    public static final String LOG_LEVEL_DEBUG = "debug";
    public static final String LOG_LEVEL_WARN = "warn";
    public static final String LOG_LEVEL_ERROR = "error";
    public static final String LOG_LEVEL_TRACE = "trace";

    public static final String USED_LOG_LEVEL = LOG_LEVEL_DEBUG;
    public static final float WINDOW_FADE_TIME = 0.3f;
    private static boolean displayOff = false;
    public static Categories Categories;
    public static float stateTime;
    private static final AsyncExecutor asyncExecutor = new AsyncExecutor(50);

    public static MapMode mapMode = MapMode.FREE;
    public final static GpsEventHelper eventHelper = new GpsEventHelper();

    public static int androidStatusbarHeight;

    final static float PPI_DEFAULT = 163;
    private static float globalScale = 1;
    public static ViewManager viewmanager;
    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");

    public static final String AboutMsg = "Team Cachebox (2011-2016)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";


    private static AbstractCache nearestAbstractCache = null;
    private static boolean autoResort;
    public static boolean switchToCompassCompleted = false;
    public static String cacheHistory = "";


    /**
     * WorkPath is a String to the used work path.<br>
     * This Path is a absolute path.<br>
     * <br>
     * On iOS this is the path to the "SandBox". <br>
     * On Android this can a path to internal SD "/Cachebox3" <br>
     * or to the "SandBox" on the external SD
     */
    public static String WorkPath;
    private static SvgSkin actSkin;
    public static Color backgroundColor = new Color(0, 1, 0, 1);
    public static ScaledSizes scaledSizes;
    public static Track actRoute;
    public static int actRouteCount;
    public static ThemeFile actThemeFile;
    public static IRenderTheme actTheme;
    public static LinkedHashMap<Object, TextureRegion> textureRegionMap;
    public static Image CB_Logo;
    public static Image backgroundImage;

    private CB() {
    }


    private static boolean isTestVersionCheked = false;
    private static boolean isTestVersion = false;

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


    private static float scalefactor = 0;

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

    public float getGlobalScaleFactor() {
        return globalScale;
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

    public static void callQuit() {
        PlatformConnector.callQuit();
    }

    public static boolean isDisplayOff() {
        return displayOff;
    }

    public static boolean selectedCachehasSpoiler() {
        return false; //TODO
    }

    public static void requestRendering() {
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


    // GL-Thread check
    private static Thread MAIN_THREAD;

    public static void assertMainThread() {
        if (MAIN_THREAD != Thread.currentThread()) {
            throw new RuntimeException("Access from non-GL thread!");
        }
    }

    public static boolean isMainThread() {
        return MAIN_THREAD == Thread.currentThread();
    }

    public static void initThreadCheck() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                MAIN_THREAD = Thread.currentThread();
            }
        });
    }

    public static void setMainThread(Thread mainThread) {
        MAIN_THREAD = mainThread;
    }


    public static void scheduleOnMainThread(final NamedRunnable runnable, long delay) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                postOnMainThread(runnable);
                requestRendering();
            }
        };
        new Timer().schedule(timerTask, delay);
        requestRendering();
    }


    public static void postOnMainThread(final NamedRunnable runnable) {
        postOnMainThread(runnable, false);
    }

    public static void postOnMainThread(final NamedRunnable runnable, boolean wait) {
        if (isMainThread()) {
            runnable.run();
            return;
        }
        final AtomicBoolean WAIT = new AtomicBoolean(wait);
        Gdx.app.postRunnable(new NamedRunnable(runnable.name) {
            @Override
            public void run() {
                runnable.run();
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
        return;
    }

    public static AbstractCache getCacheFromId(long cacheId) {
        return Database.Data.Query.GetCacheById(cacheId);
    }

    public static void postAsyncDelayd(final long delay, final NamedRunnable runnable) {
        postAsync(new NamedRunnable("delayed runnable: " + runnable.name) {
            @Override
            public void run() {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                };
                new Timer().schedule(task, delay);
            }
        });
    }

    final static AtomicInteger executeCount = new AtomicInteger(0);
    final static Array<String> runningRunnables = new Array<>();

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
                    e.printStackTrace();
                    executeCount.decrementAndGet();
                    // throw on main thread, async executor will catch them
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            throw e;
                        }
                    });
                }
                return null;
            }
        });
    }


    /**
     * Returns TRUE with any error!
     *
     * @param result
     * @return
     */
    public static boolean checkApiResultState(ApiResultState result) {
        if (result == ApiResultState.CONNECTION_TIMEOUT) {
            CB.viewmanager.toast(Translation.get("ConnectionError"));
            return true;
        }
        if (result == ApiResultState.API_IS_UNAVAILABLE) {
            CB.viewmanager.toast(Translation.get("API-offline"));
            return true;
        }

        if (result == ApiResultState.EXPIRED_API_KEY) {
            CB.scheduleOnMainThread(new NamedRunnable("CB: ExpiredApiKey") {
                @Override
                public void run() {
                    String msg = Translation.get("apiKeyExpired") + "\n\n"
                            + Translation.get("wantApi");
                    new GetApiKeyQuestionDialog(msg, Translation.get("errorAPI"),
                            MessageBoxIcon.ExpiredApiKey).show();
                }
            }, 300);// wait for closing ProgressDialog before show msg
            return true;
        }

        if (result == ApiResultState.MEMBERSHIP_TYPE_INVALID) {
            CB.scheduleOnMainThread(new NamedRunnable("CB:Invalid membership") {
                @Override
                public void run() {
                    String msg = Translation.get("apiKeyInvalid") + "\n\n"
                            + Translation.get("wantApi");
                    new GetApiKeyQuestionDialog(msg, Translation.get("errorAPI"),
                            MessageBoxIcon.ExpiredApiKey).show();
                }
            }, 300);// wait for closing ProgressDialog before show msg
            return true;
        }

        if (result == ApiResultState.API_DOWNLOAD_LIMIT) {
            CB.scheduleOnMainThread(new NamedRunnable("DownloadLimit") {
                @Override
                public void run() {
                    MessageBox.show(Translation.get("Limit_msg")//Message
                            , Translation.get("Limit_title")//Title
                            , MessageBoxButtons.OK
                            , MessageBoxIcon.Error, null);
                }
            }, 300);// wait for closing ProgressDialog before show msg
            return true;
        }
        return false;
    }

    public static boolean checkApiKeyNeeded() {
        if (Config.GcAPI.getValue() == null || Config.GcAPI.getValue().isEmpty()) {
            postOnMainThread(new NamedRunnable("CB:checkApiKeyNeeded") {
                @Override
                public void run() {
                    new GetApiKeyQuestionDialog().show();
                }
            });
            return true;
        }

        //check if expired or invalid
        final AtomicBoolean wait = new AtomicBoolean(true);
        final AtomicBoolean errror = new AtomicBoolean(false);
        GroundspeakAPI.getMembershipType(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                errror.set(checkApiResultState(value));
                wait.set(false);
            }
        });


        while (wait.get()) {
            if (CB.isMainThread()) {
                throw new RuntimeException("Don't block main thread with check API key!");
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return errror.get();
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
        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
        log.debug("Readed " + Database.Data.Query.size + " Caches into CacheList");

        // set selectedCache from last selected Cache
        String sGc = Config.LastSelectedCache.getValue();
        AbstractCache lastSelectedAbstractCache = null;
        if (sGc != null && !sGc.equals("")) {
            for (int i = 0, n = Database.Data.Query.size; i < n; i++) {
                AbstractCache c = Database.Data.Query.get(i);

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
        if ((lastSelectedAbstractCache == null) && (Database.Data.Query.size > 0)) {
            log.debug("Set selectedCache to " + Database.Data.Query.get(0).getGcCode() + " from firstInDB");
            EventHandler.fire(new SelectedCacheChangedEvent(Database.Data.Query.get(0)));
        }

        CB.setAutoResort(Config.StartWithAutoSelect.getValue());
        CacheListChangedEventList.Call();

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

}

