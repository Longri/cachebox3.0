package de.longri.cachebox3.develop.tools.skin_editor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.CB_SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.actors.MenuBar;
import de.longri.cachebox3.develop.tools.skin_editor.actors.OptionsPane;
import de.longri.cachebox3.develop.tools.skin_editor.actors.PreviewPane;
import de.longri.cachebox3.develop.tools.skin_editor.actors.WidgetsBar;

public class MainScreen implements Screen {

    private SkinEditorGame game;
    public MenuBar barMenu;
    public WidgetsBar barWidgets;
    public PreviewPane panePreview;
    public OptionsPane paneOptions;
    public Stage stage;
    private String currentProject = "";

    public MainScreen(SkinEditorGame game) {
        this.game = game;

        barMenu = new MenuBar(game);
        barWidgets = new WidgetsBar(game);
        panePreview = new PreviewPane(game, this);
        paneOptions = new OptionsPane(game, panePreview);
        CB_SpriteBatch batch = new CB_SpriteBatch(CB_SpriteBatch.Mode.NORMAL);

        stage = new Stage(new ScreenViewport(),batch);

        Table table = new Table();
        table.setFillParent(true);

        table.top().left().add(barMenu).expandX().fillX().colspan(2).row();
        table.top().left().add(barWidgets).expandX().fillX().colspan(2).row();
        table.top().left().add(paneOptions).width(420).left().fill().expandY();
        ScrollPane scrollPane = new ScrollPane(panePreview);
        table.add(scrollPane).fill().expand();
        stage.addActor(table);
        barWidgets.initializeButtons();


    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        try {
            stage.act(delta);
            stage.draw();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param project
     */
    public void setCurrentProject(String project) {
        currentProject = project;
        barMenu.update(currentProject);
    }

    public String getcurrentProject() {
        return currentProject;
    }

    /**
     *
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

    }

    /**
     *
     */
    @Override
    public void show() {

        if (currentProject == "") {
            Gdx.app.error("MainScreen", "Current project not set!");
            Gdx.app.exit();
        }
        refreshResources();
//
//        barMenu.update(currentProject);
//
//        panePreview.refresh();
//        paneOptions.refresh();
//
//        barWidgets.resetButtonSelection();

        Gdx.input.setInputProcessor(stage);

    }


    @Override
    public void hide() {
    }

    @Override
    public void pause() {
        // Do nothing

    }

    @Override
    public void resume() {
        // Do nothing

    }

    @Override
    public void dispose() {
        // Do nothing

    }

    /**
     * Save everything to skin
     */
    public void saveToSkin() {

        FileHandle projectFolder = Gdx.files.local("projects").child(currentProject);

        FileHandle[] items = projectFolder.child("backups").list();
        Array<String> sortedItems = new Array<String>();
        for (FileHandle item : items) {
            sortedItems.add(item.name());
        }
        sortedItems.sort();

        // Keep only last ten files
        int count = 0;
        for (String item : sortedItems) {
            if (count++ > 8) {
                // remove file
                projectFolder.child("backups").child(item).delete();
            }
        }

        FileHandle projectFile = projectFolder.child("skin.json");
        FileHandle backupFile = projectFolder.child("backups").child("skin_" + (TimeUtils.millis() / 1000) + ".json");
        projectFile.copyTo(backupFile);
        game.skinProject.save(projectFile);

    }


    public void refreshResources() {
        // Load project skin
        if (game.skinProject != null) {
            game.skinProject.dispose();
        }

        //delete temp Cache
        FileHandle cachedTexturatlasFileHandle = Gdx.files.local("null");
        if (cachedTexturatlasFileHandle.exists()) {
            cachedTexturatlasFileHandle.delete();
        }

        FileHandle skinFolder = (Gdx.files.local("projects/" + currentProject));
        game.skinProject = new SavableSvgSkin(true, currentProject, SvgSkin.StorageType.LOCAL, skinFolder);

    }

    public Object getSelectedStyle() {
        return paneOptions.getSelectedStyle();
    }
}
