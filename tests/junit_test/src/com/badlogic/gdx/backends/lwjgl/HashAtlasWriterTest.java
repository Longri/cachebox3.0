package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.HashAtlasWriter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkinUtil;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.utils.SkinColor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 27.02.2017.
 */
class HashAtlasWriterTest {

    static {
        TestUtils.initialGdx();
    }

    static FileHandle testFolder;
    static FileHandle skinFile;
    static Array<Class> items = new Array<Class>();

    static {
        items.add(SkinColor.class);
        items.add(ScaledSvg.class);
        items.add(VisLabel.LabelStyle.class);
    }


    @BeforeAll
    static void beforeAll() {

        if (EXCLUDE_FROM_TRAVIS.VALUE) return;

        TestUtils.initialGdx();

        testFolder = Gdx.files.local("TestHashWriter");
        testFolder.mkdirs();


        //Create Skin File
        skinFile = testFolder.child("skin.json");

        SvgSkin skin = new SvgSkin("testSkin");


        // some resources
        SkinColor c1 = new SkinColor(Color.CORAL);
        skin.add("Coral", c1);

        FileHandle svgFileHandle = testFolder.child("cursor.svg");
        String svgString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" baseProfile=\"full\" width=\"2\" height=\"31.6151\" viewBox=\"0 0 2.00 31.62\" enable-background=\"new 0 0 2.00 31.62\" xml:space=\"preserve\">\n" +
                "\t<line fill=\"none\" stroke-width=\"2\" stroke-linejoin=\"round\" stroke=\"#000000\" stroke-opacity=\"1\" x1=\"1\" y1=\"0.999939\" x2=\"1\" y2=\"30.6151\"/>\n" +
                "</svg>\n";
        svgFileHandle.writeString(svgString, false);
        ScaledSvg scaledSvg = new ScaledSvg();
        scaledSvg.scale = 0.75f;
        scaledSvg.setRegisterName("cursor");
        scaledSvg.path = "cursor.svg";
        skin.add("cursor", scaledSvg);
        SvgSkinUtil.saveSkin(skin, items, skinFile);

    }

    @AfterAll
    static void afterAll() {
        if (EXCLUDE_FROM_TRAVIS.VALUE) return;
        // delete all test files

        if (!testFolder.deleteDirectory()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    testFolder.deleteDirectory();
                }
            });
            thread.start();
        }

    }


    @Test
    void save() {
        if (EXCLUDE_FROM_TRAVIS.VALUE || EXCLUDE_FROM_TRAVIS.REPAIR) return;

        String testSkinName = "TestSkin";
        SvgSkin testSkin = new SvgSkin(testSkinName);
        testSkin.load(skinFile);

        SkinColor color = testSkin.get("Coral", SkinColor.class);
        assertThat("Color.A must: 1f", color.a == 1f);
        assertThat("Color.R must: 1f", color.r == 1f);
        assertThat("Color.G must: 0.49803922f", color.g == 0.49803922f);
        assertThat("Color.B must: 0.3137255f", color.b == 0.3137255f);

        ScaledSvg scaledSvg = testSkin.get("cursor", ScaledSvg.class);
        assertThat("Scale value must 0.75f", scaledSvg.scale == 0.75f);
        assertThat("name must be: cursor  ", scaledSvg.getRegisterName().equals("cursor"));
        assertThat("Path must be: cursor.svg", scaledSvg.path.equals("cursor.svg"));


        FileHandle cachedTexturatlasFileHandle = Gdx.files.absolute(CB.WorkPath + SvgSkinUtil.TMP_UI_ATLAS_PATH
                + testSkinName + SvgSkinUtil.TMP_UI_ATLAS);

        ArrayList<ScaledSvg> svgs = new ArrayList<ScaledSvg>();
        svgs.add(scaledSvg);
        boolean result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have no changes", result);

        //change color and check for changes
        color.r = 0.56784f;
        testSkin.add("Coral", SkinColor.class);
        SvgSkinUtil.saveSkin(testSkin, items, skinFile);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have changes", !result);

        //reload and check for no changes after load
        testSkin.load(skinFile);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have no changes", result);
        color = testSkin.get("Coral", SkinColor.class);
        assertThat("Color.A must: 1f", color.a == 1f);
        assertThat("Color.R must: 0.56784f", color.r == 0.56784f);
        assertThat("Color.G must: 0.49803922f", color.g == 0.49803922f);
        assertThat("Color.B must: 0.3137255f", color.b == 0.3137255f);

        //change svg content
        FileHandle svgFileHandle = testFolder.child("cursor.svg");
        String svgString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" baseProfile=\"full\" width=\"2\" height=\"31.6151\" viewBox=\"0 0 2.00 31.62\" enable-background=\"new 0 0 2.00 31.62\" xml:space=\"preserve\">\n" +
                "\t<line fill=\"none\" stroke-width=\"3\" stroke-linejoin=\"round\" stroke=\"#004400\" stroke-opacity=\"1\" x1=\"1\" y1=\"0.999939\" x2=\"1\" y2=\"30.6151\"/>\n" +
                "</svg>\n";
        svgFileHandle.writeString(svgString, false);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have changes", !result);

        //reload and check for no changes after load
        testSkin.load(skinFile);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have no changes", result);

        //Change SVG scale factor
        String json = skinFile.readString();
        json = json.replace("\"scale\": 0.75", "\"scale\": 0.95");
        skinFile.writeString(json, false);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have changes", !result);


        //reload and check for no changes after load
        testSkin.load(skinFile);
        svgs.clear();
        scaledSvg = testSkin.get("cursor", ScaledSvg.class);
        svgs.add(scaledSvg);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have no changes", result);
        scaledSvg = testSkin.get("cursor", ScaledSvg.class);
        assertThat("Scale value must 0.95f", scaledSvg.scale == 0.95f);
        assertThat("name must be: cursor  ", scaledSvg.getRegisterName().equals("cursor"));
        assertThat("Path must be: cursor.svg", scaledSvg.path.equals("cursor.svg"));


        //add a new Svg and check for changes
        FileHandle newSvgFileHandle = testFolder.child("cursor2.svg");
        String newSvgString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" baseProfile=\"full\" width=\"2\" height=\"31.6151\" viewBox=\"0 0 2.00 31.62\" enable-background=\"new 0 0 2.00 31.62\" xml:space=\"preserve\">\n" +
                "\t<line fill=\"none\" stroke-width=\"2\" stroke-linejoin=\"round\" stroke=\"#000000\" stroke-opacity=\"1\" x1=\"1\" y1=\"0.999939\" x2=\"1\" y2=\"30.6151\"/>\n" +
                "</svg>\n";
        newSvgFileHandle.writeString(newSvgString, false);
        ScaledSvg newScaledSvg = new ScaledSvg();
        newScaledSvg.scale = 0.35f;
        newScaledSvg.setRegisterName("cursor2");
        newScaledSvg.path = "cursor2.svg";
        testSkin.add("cursor2", newScaledSvg);
        SvgSkinUtil.saveSkin(testSkin, items, skinFile);
        svgs.add(newScaledSvg);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have changes", !result);

        //reload and check for no changes after load
        testSkin.load(skinFile);
        svgs.clear();
        scaledSvg = testSkin.get("cursor", ScaledSvg.class);
        svgs.add(scaledSvg);
        scaledSvg = testSkin.get("cursor2", ScaledSvg.class);
        svgs.add(scaledSvg);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have no changes", result);
        scaledSvg = testSkin.get("cursor2", ScaledSvg.class);
        assertThat("Scale value must 0.95f", scaledSvg.scale == 0.35f);
        assertThat("name must be: cursor2  ", scaledSvg.getRegisterName().equals("cursor2"));
        assertThat("Path must be: cursor2.svg", scaledSvg.path.equals("cursor2.svg"));


        //add a new style
        VisLabel.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.background = testSkin.getDrawable("cursor2");
        assertThat("Label.background must not be NULL", labelStyle.background != null);
        testSkin.add("default", labelStyle);
        SvgSkinUtil.saveSkin(testSkin, items, skinFile);
        result = HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, svgs, skinFile);
        assertThat("Must have changes", !result);

        testSkin.load(skinFile);
        VisLabel.LabelStyle labelStyle2 = testSkin.get(VisLabel.LabelStyle.class);
        assertThat("Label.background must not be NULL", labelStyle2.background != null);
    }


}