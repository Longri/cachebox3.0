/*
 * Copyright (C) 2017 - 2019 team-cachebox.de
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

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.skin.styles.*;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.MapStateButton;
import de.longri.cachebox3.gui.widgets.ZoomButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.AbstractTranslationHandler;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.utils.BuildInfo;
import de.longri.cachebox3.utils.GeoUtils;
import de.longri.cachebox3.utils.ScaledSizes;
import org.apache.commons.codec.Charsets;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by longri on 14.04.17.
 */
public class TestUtils {

    public static final Coordinate LONGRI_HOME_COORDS = new Coordinate(52.581892, 13.398128);

    public static boolean isPlatformTest() {
        return false;
    }

    private static boolean gdxIsInitial = false;

    public static void initialGdx() {
        if (gdxIsInitial) return;
        gdxIsInitial = true;
        BuildInfo.setTestBuildInfo("JUnitTest");
        Gdx.app = new HeadlessApplication(new Game() {
            @Override
            public void create() {

            }
        });
        Gdx.files = Gdx.app.getFiles();
        Gdx.net = Gdx.app.getNet();
        CB.WorkPath = new File("./").getAbsolutePath();
        VisUI.load(new Skin());
        CB.initThreadCheck();
        Gdx.gl = mock(GL20.class);
    }

    public static void initialVisUI() {
        initialGdx();
        if (!VisUI.isLoaded()) VisUI.load();
        CB.scaledSizes = new ScaledSizes(100, 50, 150, 10,
                50, 10);
        CB.backgroundImage = new Image((Drawable) null);

        //init mock translation
        Translation.translation = new AbstractTranslationHandler(null, null) {
            @Override
            public void loadTranslation(String langPath) throws IOException {

            }

            @Override
            public CompoundCharSequence getTranslation(String stringId, CharSequence... params) {
                return new CompoundCharSequence("Test");
            }

            @Override
            public CompoundCharSequence getTranslation(int hashCode, CharSequence... params) {
                return new CompoundCharSequence("Test");
            }

            @Override
            public boolean isInitial() {
                return true;
            }

            @Override
            public String getLangNameFromFile(String path) throws IOException {
                return "test";
            }
        };


        // add missing styles as test styles
        //com.badlogic.gdx.scenes.scene2d.ui.Label$LabelStyle registered with name: default
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont();
        VisUI.getSkin().add("default", labelStyle, Label.LabelStyle.class);

        //de.longri.cachebox3.gui.skin.styles.ListViewStyle registered with name: default
        ListViewStyle listViewStyle = new ListViewStyle();
        VisUI.getSkin().add("default", listViewStyle, ListViewStyle.class);

        //de.longri.cachebox3.gui.skin.styles.CompassViewStyle registered with name: compassViewStyle
        CompassViewStyle compassViewStyle = new CompassViewStyle();
        VisUI.getSkin().add("compassViewStyle", compassViewStyle, CompassViewStyle.class);

        //com.badlogic.gdx.scenes.scene2d.ui.ScrollPane$ScrollPaneStyle registered with name: list
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        VisUI.getSkin().add("list", scrollPaneStyle, ScrollPane.ScrollPaneStyle.class);

        //de.longri.cachebox3.gui.skin.styles.DraftListItemStyle registered with name: fieldNoteListItemStyle
        DraftListItemStyle draftListItemStyle = new DraftListItemStyle();
        VisUI.getSkin().add("fieldNoteListItemStyle", draftListItemStyle, DraftListItemStyle.class);

        //de.longri.cachebox3.gui.ActivityBase$ActivityBaseStyle registered with name: default
        ActivityBase.ActivityBaseStyle activityBaseStyle = new ActivityBase.ActivityBaseStyle();
        VisUI.getSkin().add("default", activityBaseStyle, ActivityBase.ActivityBaseStyle.class);

        //de.longri.cachebox3.gui.menu.MenuItem$MenuItemStyle registered with name: default
        MenuItem.MenuItemStyle menuItemStyle = new MenuItem.MenuItemStyle();
        menuItemStyle.font = new BitmapFont();
        VisUI.getSkin().add("default", menuItemStyle, MenuItem.MenuItemStyle.class);

        //de.longri.cachebox3.gui.skin.styles.CircularProgressStyle registered with name: circularProgressStyle
        CircularProgressStyle circularProgressStyle = new CircularProgressStyle();
        VisUI.getSkin().add("circularProgressStyle", circularProgressStyle, CircularProgressStyle.class);

        //de.longri.cachebox3.gui.skin.styles.EditTextStyle registered with name: default
        EditTextStyle editTextStyle = new EditTextStyle();
        VisUI.getSkin().add("default", editTextStyle, EditTextStyle.class);

        //de.longri.cachebox3.gui.skin.styles.CacheListItemStyle registered with name: cacheListItems
        CacheListItemStyle cacheListItemStyle = new CacheListItemStyle();
        cacheListItemStyle.nameFont = new BitmapFont();
        cacheListItemStyle.distanceFont = new BitmapFont();
        VisUI.getSkin().add("cacheListItems", cacheListItemStyle, CacheListItemStyle.class);

        //de.longri.cachebox3.gui.skin.styles.MapBubbleStyle registered with name: bubble
        MapBubbleStyle mapBubbleStyle = new MapBubbleStyle();
        VisUI.getSkin().add("bubble", mapBubbleStyle, MapBubbleStyle.class);

        //de.longri.cachebox3.gui.skin.styles.FloatControlStyle registered with name: default
        FloatControlStyle floatControlStyle = new FloatControlStyle();
        VisUI.getSkin().add("default", floatControlStyle, FloatControlStyle.class);

        //com.kotcrab.vis.ui.widget.VisTextButton$VisTextButtonStyle registered with name: default
        VisTextButton.VisTextButtonStyle visTextButtonStyle = new VisTextButton.VisTextButtonStyle();
        visTextButtonStyle.font = new BitmapFont();
        VisUI.getSkin().add("default", visTextButtonStyle, VisTextButton.VisTextButtonStyle.class);

        //de.longri.cachebox3.gui.skin.styles.SelectBoxStyle registered with name: default
        SelectBoxStyle selectBoxStyle = new SelectBoxStyle();
        selectBoxStyle.font = new BitmapFont();
        VisUI.getSkin().add("default", selectBoxStyle, SelectBoxStyle.class);

        //de.longri.cachebox3.gui.widgets.MapStateButton$MapStateButtonStyle registered with name: default
        MapStateButton.MapStateButtonStyle mapStateButtonStyle = new MapStateButton.MapStateButtonStyle();
        VisUI.getSkin().add("default", mapStateButtonStyle, MapStateButton.MapStateButtonStyle.class);

        //de.longri.cachebox3.gui.skin.styles.MapInfoPanelStyle registered with name: infoPanel
        MapInfoPanelStyle mapInfoPanelStyle = new MapInfoPanelStyle();
        mapInfoPanelStyle.coordinateLabel_Font = mapInfoPanelStyle.distanceLabel_Font
                = mapInfoPanelStyle.distanceUnitLabel_Font
                = mapInfoPanelStyle.speedUnitLabel_Font
                = mapInfoPanelStyle.speedLabel_Font
                = new BitmapFont();
        VisUI.getSkin().add("infoPanel", mapInfoPanelStyle, MapInfoPanelStyle.class);

        //de.longri.cachebox3.gui.skin.styles.CompassStyle registered with name: mapCompassStyle
        CompassStyle compassStyle = new CompassStyle();
        VisUI.getSkin().add("mapCompassStyle", compassStyle, CompassStyle.class);

        //de.longri.cachebox3.gui.widgets.ZoomButton$ZoomButtonStyle registered with name: default
        ZoomButton.ZoomButtonStyle zoomButtonStyle = new ZoomButton.ZoomButtonStyle();
        VisUI.getSkin().add("default", zoomButtonStyle, ZoomButton.ZoomButtonStyle.class);

        //de.longri.cachebox3.gui.skin.styles.ButtonDialogStyle registered with name: default
        ButtonDialogStyle buttonDialogStyle = new ButtonDialogStyle();
        buttonDialogStyle.titleFont = new BitmapFont();
        VisUI.getSkin().add("default", buttonDialogStyle, ButtonDialogStyle.class);

        //de.longri.cachebox3.gui.skin.styles.ButtonDialogStyle registered with name: default
        IconsStyle iconsStyle = new IconsStyle();
        VisUI.getSkin().add("default", iconsStyle, IconsStyle.class);

        //de.longri.cachebox3.gui.skin.styles.GalleryViewStyle registered with name: default
        GalleryViewStyle galleryViewStyle = new GalleryViewStyle();
        galleryViewStyle.galleryListStyle = listViewStyle;
        galleryViewStyle.overviewListStyle = listViewStyle;

        VisUI.getSkin().add("default", galleryViewStyle, GalleryViewStyle.class);

    }

    public static double roundDoubleCoordinate(double value) {
        value = Math.round(GeoUtils.degreesToMicrodegrees(value));
        value = GeoUtils.microdegreesToDegrees((int) value);
        return value;
    }

    public static String getResourceRequestString(String path, String apiKey) throws IOException {
        File file = new File(path);

        if (!file.exists()) {
            //try set /tests path
            path = "tests/" + path;
            file = new File(path);
        }

        InputStream stream = getResourceRequestStream(path);

        byte[] b = new byte[(int) file.length()];
        int len = b.length;
        int total = 0;

        while (total < len) {
            int result = stream.read(b, total, len - total);
            if (result == -1) {
                break;
            }
            total += result;
        }
        String expected = new String(b, Charsets.UTF_8);
        if (apiKey != null && !apiKey.isEmpty()) {
            expected = expected.replace("\"AccessToken\":\"+DummyKEY\"",
                    "\"AccessToken\":\"" + apiKey + "\"");
        }
        return expected;
    }

    public static InputStream getResourceRequestStream(String path) throws FileNotFoundException {
        File file = new File(path);

        if (!file.exists()) {
            //try set /core path
            path = "tests/" + path;
            file = new File(path);
        }

        FileInputStream stream = new FileInputStream(file);

        return stream;
    }

    public static void assetCacheAttributes(Database database, AbstractCache abstractCache, ArrayList<Attributes> positiveList, ArrayList<Attributes> negativeList) {
        Iterator<Attributes> positiveIterator = positiveList.iterator();
        Iterator<Attributes> negativeIterator = negativeList.iterator();

        while (positiveIterator.hasNext()) {
            assertThat("Attribute wrong", abstractCache.isAttributePositiveSet((Attributes) positiveIterator.next()));
        }

        while (negativeIterator.hasNext()) {
            Attributes tmp = negativeIterator.next();
            assertThat(tmp.name() + " negative Attribute wrong", abstractCache.isAttributeNegativeSet((tmp)));
        }

        // f�lle eine Liste mit allen Attributen
        ArrayList<Attributes> attributes = new ArrayList<Attributes>();
        Attributes[] tmp = Attributes.values();
        for (Attributes item : tmp) {
            attributes.add(item);
        }

        // L�sche die vergebenen Atribute aus der Kommplett Liste
        positiveIterator = positiveList.iterator();
        negativeIterator = negativeList.iterator();

        while (positiveIterator.hasNext()) {
            attributes.remove(positiveIterator.next());
        }

        while (negativeIterator.hasNext()) {
            attributes.remove(negativeIterator.next());
        }

        attributes.remove(Attributes.getAttributeEnumByGcComId(64));
        attributes.remove(Attributes.getAttributeEnumByGcComId(65));
        attributes.remove(Attributes.getAttributeEnumByGcComId(66));

        // Teste ob die �brig gebliebenen Atributte auch nicht vergeben wurden.
        Iterator<Attributes> RestInterator = attributes.iterator();

        while (RestInterator.hasNext()) {
            Attributes attr = (Attributes) RestInterator.next();
            assertThat(attr.name() + "Attribute wrong", !abstractCache.isAttributePositiveSet(attr));
            assertThat(attr.name() + "Attribute wrong", !abstractCache.isAttributeNegativeSet(attr));
        }
    }

    public static FileHandle getResourceFileHandle(String path, boolean mustexist) {
        File file = new File(path);

        if (mustexist && !file.exists()) {
            //try set /tests path
            path = "tests/" + path;
            file = new File(path);
        }

        FileHandle fileHandle = Gdx.files.absolute(file.getAbsolutePath());
        return fileHandle;
    }

    static int dbCount = 0;

    public static Database getTestDB(boolean inMemory) {
        if (inMemory) {
            Database database = new Database(Database.DatabaseType.CacheBox3);
            Database.createNewInMemoryDB(database, "createNewDB" + Integer.toString(dbCount++));
            return database;
        } else {
            FileHandle dbFiileHandle = Gdx.files.local("testDBfile" + Integer.toString(dbCount++) + ".db3");

            //delete if exist
            dbFiileHandle.delete();

            Database database = new Database(Database.DatabaseType.CacheBox3);
            database.startUp(dbFiileHandle);

            return database;
        }
    }

    public static AbstractView assertAbstractViewSerialation(AbstractView abstractView, Class<?> expectedClazz) {
        de.longri.serializable.BitStore store = abstractView.saveInstanceState();
        byte[] bytes = store.getArray();

        de.longri.serializable.BitStore reader = new de.longri.serializable.BitStore(bytes);

        String className = reader.readString();

        AbstractView newInstanceAbstractView = null;
        Object obj = null;

        try {
            Class clazz = ClassReflection.forName(className);
            Constructor constructor = ClassReflection.getConstructor(clazz, de.longri.serializable.BitStore.class);

            obj = constructor.newInstance(reader);
            newInstanceAbstractView = (AbstractView) obj;

        } catch (ReflectionException e) {
            e.printStackTrace(

            );
        }
        assertNotNull(obj);
        assertNotNull(newInstanceAbstractView);
        assertTrue(expectedClazz.isInstance(obj));
        return newInstanceAbstractView;
    }

}
