package de.longri.cachebox3.develop.tools.skin_editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.develop.tools.skin_editor.actors.RangeSelector;
import de.longri.cachebox3.gui.drawables.EmptyDrawable;
import de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class NinePatchEditorDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(NinePatchEditorDialog.class);

    private SkinEditorGame game;
    private TextField textName;
    private TextField textSourceImage;
    private float currentZoom = 1.0f;
    private Table table;
    private Table tableEditor;
    private Table tablePreview;
    private Image image;
    private float imgWidth;
    private float imgHeight;
    private Cell cellImage;
    private RangeSelector rangeTop;
    private RangeSelector rangeBottom;
    private RangeSelector rangeLeft;
    private RangeSelector rangeRight;

    private TextButton buttonPreview1;
    private TextButton buttonPreview2;
    private TextButton buttonPreview3;
    private Cell cellPreview1;
    private Cell cellPreview2;
    private Cell cellPreview3;
    private Pixmap sourcePixmap;
    private ScaledSvg selectedScaledSvg;
    private boolean isChange;

    private FileHandle tmpFile;

    public NinePatchEditorDialog(final SkinEditorGame game) {
        this(game, null);
    }

    public NinePatchEditorDialog(final SkinEditorGame game, ScaledSvg scaledSvg) {
        super(scaledSvg != null ? "Change NinePatch" : "Create NinePatch", game.skin);
        this.game = game;
        isChange = scaledSvg != null;

        tmpFile = new FileHandle(System.getProperty("java.io.tmpdir")).child("skin_ninepatch");
        if (tmpFile.exists() == false) {
            tmpFile.mkdirs();
        }
        tmpFile = tmpFile.child("temp.9.png");


        table = new Table(game.skin);
//		table.debug();
        table.defaults().pad(10);

        table.top();
        table.left();

        textName = new TextField("default_button", game.skin);
        Table tableTop = new Table(game.skin);
        tableTop.add("NinePatch name:").padRight(10);
        tableTop.add(textName).padRight(10);
        tableTop.add("Source Image:").padRight(10);

        textSourceImage = new TextField("", game.skin);
        textSourceImage.setDisabled(true);
        tableTop.add(textSourceImage).expandX().fill().padRight(10);

        TextButton buttonChoose = new TextButton("...", game.skin);
        tableTop.add(buttonChoose).width(32);
        buttonChoose.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DrawablePickerDialog dlg = new DrawablePickerDialog(game, getStage()) {
                    public void selectedSvg(ScaledSvg scaledSvg) {
                        setScaledSvg(scaledSvg);
                    }
                };
                dlg.show(getStage());
            }
        });

        if (!isChange) {
            table.add(tableTop).colspan(2).expandX().fillX();
            table.row();
        }

        rangeTop = new RangeSelector(false, game.skin);
        rangeRight = new RangeSelector(true, game.skin);
        rangeBottom = new RangeSelector(false, game.skin);
        rangeLeft = new RangeSelector(true, game.skin);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshPreview();
            }
        };

        rangeTop.addListener(listener);
        rangeRight.addListener(listener);
        rangeBottom.addListener(listener);
        rangeLeft.addListener(listener);

        tableEditor = new Table(game.skin);
//        tableEditor.debug();
        tableEditor.add();
        tableEditor.add(rangeTop).fillX().height(10);
        tableEditor.row();

        tableEditor.add(rangeLeft).width(10).fillY();

        image = new Image(new EmptyDrawable() {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                // do nothing
            }
        });
        imgWidth = image.getWidth();
        imgHeight = image.getHeight();
        cellImage = tableEditor.add(image);
        cellImage.width(imgWidth * currentZoom).height(imgHeight * currentZoom);

        tableEditor.add(rangeRight).width(10).fillY();
        tableEditor.row();
        tableEditor.add();
        tableEditor.add(rangeBottom).fillX().height(10);
        tableEditor.row();

        ScrollPane scrollPane = new ScrollPane(tableEditor, game.skin);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFlingTime(0);
        table.add(scrollPane).width(500).expand().fill();

        tablePreview = new Table(game.skin);
        reviewTablePreview();

        table.add(new ScrollPane(tablePreview, game.skin)).width(360).expand().fill().row();

        Table tableButtons = new Table(game.skin);
        tableButtons.center();
        TextButton buttonSave = new TextButton("Save NinePatch", game.skin);
        tableButtons.add(buttonSave).padRight(10);

        TextButton buttonZoomIn = new TextButton("+", game.skin);
        tableButtons.add(buttonZoomIn).width(32).padRight(10);

        TextButton buttonZoomOut = new TextButton("-", game.skin);
        tableButtons.add(buttonZoomOut).width(32).padRight(10);


        TextButton buttonCancel = new TextButton("Cancel", game.skin);
        tableButtons.add(buttonCancel);

        table.add(tableButtons).colspan(2).row();

        getContentTable().add(table).width(900).height(640).pad(20);
        getButtonTable().padBottom(15);


        buttonSave.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {


                String saveName = textName.getText();

                if (saveName.isEmpty() == true) {
                    game.showMsgDlg("Error", "Empty name!", getStage());
                    return;

                }
                // if no change check if the name is already in use
                if (!isChange && game.skinProject.has(saveName, SvgNinePatchDrawable.class)) {
                    game.showMsgDlg("Error", "A ninepatch with the same name already exists!", getStage());
                    return;
                }


                // create new SvgNinePatchDrawable and fill values
                SvgNinePatchDrawable SNPD = new SvgNinePatchDrawable();
                SNPD.name = selectedScaledSvg.getRegisterName(); // name of ScaledSvg resource
                SvgNinePatchDrawable.SvgNinePatchDrawableUnScaledValues values = new SvgNinePatchDrawable.SvgNinePatchDrawableUnScaledValues();


                int sourceWidth = sourcePixmap.getWidth();
                values.left = (int) (rangeTop.rangeStart * sourceWidth);
                values.right = (int) (sourceWidth - (rangeTop.rangeStop * sourceWidth));


                int sourceHeight = sourcePixmap.getHeight();
                values.top = (int) (rangeLeft.rangeStart * sourceHeight);
                values.bottom = (int) (sourceHeight - (rangeLeft.rangeStop * sourceHeight));

                SNPD.values = values;

                game.skinProject.add(saveName, SNPD, SvgNinePatchDrawable.class);

                game.screenMain.saveToSkin();
                game.screenMain.refreshResources();

                hide();

            }

        });

        buttonZoomIn.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (currentZoom < 16) {
                    currentZoom *= 2;
                    refreshImage();
                }
            }

        });

        buttonZoomOut.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (currentZoom > 0.5f) {
                    currentZoom /= 2;
                    refreshImage();
                }
            }

        });

        buttonCancel.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                cancel();
                hide();
            }

        });

        key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
        refreshPreview();
        setScaledSvg(scaledSvg);
    }

    private void setScaledSvg(ScaledSvg scaledSvg) {

        if (scaledSvg == null) return;

        log.debug(scaledSvg.toString() + "selected");

        selectedScaledSvg = scaledSvg;

        textName.setText(scaledSvg.getRegisterName() + "_9_Patch");
        textSourceImage.setText(scaledSvg.path);

        //create a Bitmap
        Bitmap bitmap;
        try {
            FileHandle svgFile = game.skinProject.skinFolder.child(scaledSvg.path);
            bitmap = PlatformConnector.getSvg(scaledSvg.getRegisterName(), svgFile.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale);
        } catch (IOException e) {
            return;
        }

        byte[] data = bitmap.getPngEncodedData();
        sourcePixmap = new Pixmap(data, 0, data.length);


        TextureRegionDrawable trd = new TextureRegionDrawable(new TextureRegion(new Texture(sourcePixmap)));
        image = new Image(trd);
        imgWidth = image.getWidth();
        imgHeight = image.getHeight();

        cellImage.clearActor().setActor(image);
        cellImage.width(imgWidth * currentZoom).height(imgHeight * currentZoom);
        image.setDrawable(trd);
        image.setWidth(trd.getMinWidth());
        image.setHeight(trd.getMinHeight());
        refreshPreview();
        refreshImage();
    }

    private void reviewTablePreview() {
        tablePreview.clear();
        //tablePreview.debug();
        tablePreview.center().left();

        TextButtonStyle stylePreview = new TextButton.TextButtonStyle();
        stylePreview.font = game.skin.getFont("default-font");
        buttonPreview1 = new TextButton("1x Button", stylePreview);
        buttonPreview1.setSize(imgWidth, imgHeight);
        cellPreview1 = tablePreview.add(buttonPreview1).width(buttonPreview1.getWidth()).height(buttonPreview1.getHeight()).padBottom(15);
        tablePreview.row();

        buttonPreview2 = new TextButton("2x Button", stylePreview);
        buttonPreview2.setSize(imgWidth * 2.0f, imgHeight * 2.0f);
        cellPreview2 = tablePreview.add(buttonPreview2).width(buttonPreview2.getWidth()).height(buttonPreview2.getHeight()).padBottom(15);
        tablePreview.row();

        buttonPreview3 = new TextButton("3x Button", stylePreview);
        buttonPreview3.setSize(imgWidth * 3.0f, imgHeight * 3.0f);
        cellPreview3 = tablePreview.add(buttonPreview3).width(buttonPreview3.getWidth()).height(buttonPreview3.getHeight()).padBottom(15);
        tablePreview.row();

    }

    private void refreshImage() {

        float newX = imgWidth * currentZoom;
        float newY = imgHeight * currentZoom;

        image.setSize(newX, newY);
        cellImage.width(newX).height(newY);
        tableEditor.setWidth(newX);
        tableEditor.setHeight(newY);

        //set new with and height for range selector
        rangeTop.setWidth(newX);
        rangeBottom.setWidth(newX);
        rangeLeft.setHeight(newY);
        rangeRight.setHeight(newY);

        ScrollPane sp = (ScrollPane) tableEditor.getParent();
        sp.layout();

    }

    public void refreshPreview() {

        Gdx.app.log("NinePatchEditorDialog", "refresh preview.");

        Pixmap pixmapImage = new Pixmap(100, 100, Pixmap.Format.RGBA8888);

        if (sourcePixmap != null) {
            pixmapImage = sourcePixmap;
        }


        Pixmap pixmap = new Pixmap((int) (pixmapImage.getWidth() + 2), (int) (pixmapImage.getHeight() + 2), Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(pixmapImage, 1, 1);

        pixmap.setColor(Color.BLACK);

        // Range left
        int h = pixmapImage.getHeight() + 1;
        pixmap.drawLine(0, (int) (h * rangeLeft.rangeStart), 0, (int) (h * rangeLeft.rangeStop));


        // Range top
        int w = pixmapImage.getWidth() + 1;
        pixmap.drawLine((int) (w * rangeTop.rangeStart), 0, (int) (w * rangeTop.rangeStop), 0);

        // Range right
        h = pixmapImage.getHeight() + 1;
        pixmap.drawLine(pixmapImage.getWidth() + 1, (int) (h * rangeRight.rangeStart), pixmapImage.getWidth() + 1, (int) (h * rangeRight.rangeStop));

        // Range bottom
        w = pixmapImage.getWidth() + 1;
        pixmap.drawLine((int) (w * rangeBottom.rangeStart), pixmap.getHeight() - 1, (int) (w * rangeBottom.rangeStop), pixmap.getHeight() - 1);

        if (sourcePixmap != null) {
            PixmapIO.writePNG(tmpFile, pixmap);

            FileHandle fh = new FileHandle(System.getProperty("java.io.tmpdir")).child("skin_ninepatch");
            TexturePacker.Settings settings = new TexturePacker.Settings();
            TexturePacker.process(settings, fh.path(), fh.path(), "pack");

            TextureAtlas ta = new TextureAtlas(fh.child("pack.atlas"));
            NinePatch np = ta.createPatch("temp");
            SvgNinePatchDrawable drawable = new SvgNinePatchDrawable(np);
            reviewTablePreview();
            buttonPreview1.getStyle().up = drawable;

//            log.debug("Source size:" + selectedScaledSvg.toString());
//            log.debug("source width:" + sourcePixmap.getWidth());
//            log.debug("rangeTop.rangeStart:" + rangeTop.rangeStart);
//            log.debug("rangeTop.rangeStop:" + rangeTop.rangeStop);
//            log.debug("  ");
        }
    }
}
