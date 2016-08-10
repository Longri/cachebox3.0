package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.IconNames;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 03.08.16.
 */
public class ButtonDialog extends Table {

    // see for layout help ==> https://github.com/libgdx/libgdx/wiki/Table


    static public final int BUTTON_POSITIVE = 1;
    static public final int BUTTON_NEUTRAL = 2;
    static public final int BUTTON_NEGATIVE = 3;

    static private final float FADE_TIME = 0.3f;
    static private final Vector2 tmpPosition = new Vector2();
    static private final Vector2 tmpSize = new Vector2();

    private final OnMsgBoxClickListener msgBoxClickListener;
    private final ButtonDialogStyle style;
    private final Label msgLabel;
    private final Label titleLabel;
    private boolean dontRenderDialogBackground = false;
    private Object data;
    private boolean mHasTitle = false;


    private Table titleTable, contentTable, buttonTable;
    private Skin skin;
    private ObjectMap<Actor, Object> values = new ObjectMap();
    private String titleText;


    private InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
        super();
        this.skin = VisUI.getSkin();
        setSkin(this.skin);
        style = skin.get("default", ButtonDialogStyle.class);
        if (title != null) {
            this.mHasTitle = true;
            this.titleText = title;
            add(titleTable = new Table(skin)).left();
            titleTable.defaults().padLeft(style.title.getLeftWidth()).padRight(style.title.getLeftWidth())
                    .padTop(style.title.getTopHeight()).padBottom(style.title.getBottomHeight());
            row();
            titleLabel = new Label(titleText, new Label.LabelStyle(style.titleFont, style.titleFontColor));
            titleTable.add(titleLabel).left();
        } else {
            titleLabel = null;
        }

        add(contentTable = new Table(skin)).expand().fill().padLeft(CB.scaledSizes.MARGIN).padRight(CB.scaledSizes.MARGIN);
        row();

        add(buttonTable = new Table(skin)).expand().fill().padLeft(CB.scaledSizes.MARGIN).padRight(CB.scaledSizes.MARGIN)
                .padBottom(CB.scaledSizes.MARGIN / 2).padTop(CB.scaledSizes.MARGIN);

        contentTable.defaults().space(2).padLeft(3).padRight(3);
        buttonTable.defaults().padLeft(style.footer.getLeftWidth()).padRight(style.footer.getRightWidth()).padBottom(CB.scaledSizes.MARGIN);

        buttonTable.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
            }
        });

        setButtonCaptions(buttons);

        if (icon != MessageBoxIcon.None && icon != null) {
            Image iconImage = new Image(getIcon(icon));
            contentTable.add(iconImage).width(iconImage.getWidth()).top().pad(CB.scaledSizes.MARGIN);
        }

        msgLabel = new Label(msg, new Label.LabelStyle(style.titleFont, style.titleFontColor));
        msgLabel.setWrap(true);
        contentTable.add(msgLabel).expandX().fillX();
        msgBoxClickListener = Listener;
    }


    private static Sprite getIcon(MessageBoxIcon msgIcon) {
        if (msgIcon == null)
            return null;

        Sprite icon;

        switch (msgIcon.ordinal()) {
            case 0:
                icon = CB.getSprite(IconNames.infoIcon.name());
                break;
            case 1:
                icon = CB.getSprite(IconNames.closeIcon.name());
                break;
            case 2:
                icon = CB.getSprite(IconNames.warningIcon.name());
                break;
            case 3:
                icon = CB.getSprite(IconNames.closeIcon.name());
                break;
            case 4:
                icon = CB.getSprite(IconNames.infoIcon.name());
                break;
            case 5:
                icon = null;
                break;
            case 6:
                icon = CB.getSprite(IconNames.helpIcon.name());
                break;
            case 7:
                icon = CB.getSprite(IconNames.closeIcon.name());
                break;
            case 8:
                icon = CB.getSprite(IconNames.warningIcon.name());
                break;
            case 9:
                icon = CB.getSprite(IconNames.gc_liveIcon.name());
                break;
            case 10:
                icon = CB.getSprite(IconNames.gc_liveIcon.name());
                break;

            default:
                icon = null;

        }

        return icon;
    }

    private void setButtonCaptions(MessageBoxButtons buttons) {
        float buttonWidth = 0;

        float maxWindowWidth = CB.scaledSizes.WINDOW_WIDTH;

        if (buttons == MessageBoxButtons.YesNoRetry) {
            buttonWidth = (maxWindowWidth / 3) - (4 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("yes"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("no"), buttonWidth, BUTTON_NEGATIVE);
            this.button(Translation.Get("retry"), buttonWidth, BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButtons.AbortRetryIgnore) {
            buttonWidth = (maxWindowWidth / 3) - (4 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("abort"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("retry"), buttonWidth, BUTTON_NEUTRAL);
            this.button(Translation.Get("ignore"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.OK) {
            buttonWidth = (maxWindowWidth / 1) - (2 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("ok"), buttonWidth, BUTTON_POSITIVE);
        } else if (buttons == MessageBoxButtons.OKCancel) {
            buttonWidth = (maxWindowWidth / 2) - (3 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("ok"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.RetryCancel) {
            buttonWidth = (maxWindowWidth / 2) - (3 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("retry"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.YesNo) {
            buttonWidth = (maxWindowWidth / 2) - (3 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("yes"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("no"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.YesNoCancel) {
            buttonWidth = (maxWindowWidth / 3) - (4 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("yes"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("no"), buttonWidth, BUTTON_NEGATIVE);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButtons.Cancel) {
            buttonWidth = (maxWindowWidth / 1) - (2 * CB.scaledSizes.MARGIN);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        }
    }

    private void button(String text, float buttonWidth, Object object) {
        VisTextButton.VisTextButtonStyle buttonStyle = skin.get(VisTextButton.VisTextButtonStyle.class);
        VisTextButton button = new VisTextButton(text, buttonStyle);
        buttonTable.add(button).width(buttonWidth);
        values.put(button, object);
    }

    private void result(Object object) {
        if (msgBoxClickListener != null) {
            msgBoxClickListener.onClick((Integer) object, null);
            this.hide();
        }
    }

    public void show() {
        clearActions();
        pack();
        CB.viewmanager.addActor(this);
        CB.viewmanager.setKeyboardFocus(this);
        CB.viewmanager.setScrollFocus(this);
        addAction(sequence(Actions.alpha(0), Actions.fadeIn(FADE_TIME, Interpolation.fade)));
        setPosition(Math.round((CB.viewmanager.getWidth() - getWidth()) / 2), Math.round((CB.viewmanager.getHeight() - getHeight()) / 2));
    }

    public void hide() {
        clearActions();
        addCaptureListener(ignoreTouchDown);
        addAction(sequence(Actions.fadeOut(FADE_TIME, Interpolation.fade), Actions.removeActor()));
    }

    public void draw(Batch batch, float parentAlpha) {
        if (style.stageBackground != null) drawStageBackground(batch, parentAlpha);

        float versatz = !mHasTitle ? 0 : style.title.getBottomHeight();
        if (style.header != null && !dontRenderDialogBackground) {
            style.header.draw(batch, this.getX(), (contentTable.getY() + versatz + contentTable.getHeight() - style.header.getMinHeight()) + this.getY(),
                    this.getWidth(), style.header.getMinHeight() + versatz);
        }
        if (style.footer != null && !dontRenderDialogBackground) {
            style.footer.draw(batch, this.getX(), this.getY(), this.getWidth(), buttonTable.getHeight() + (2 * CB.scaledSizes.MARGIN));
        }
        if (style.center != null && !dontRenderDialogBackground) {
            style.center.draw(batch, this.getX(), contentTable.getY() + this.getY(), this.getWidth(),
                    contentTable.getHeight());
        }

        if (mHasTitle) {
            // TODO handle drawing if Title width to long for window
            if (style.title != null && !dontRenderDialogBackground) {
                style.title.draw(batch, this.getX(), this.getY() + titleTable.getY(), titleTable.getWidth() + style.title.getRightWidth(), titleTable.getHeight());
            }
        }
        super.draw(batch, parentAlpha);
    }

    private void drawStageBackground(Batch batch, float parentAlpha) {
        Stage stage = getStage();
        if (stage.getKeyboardFocus() == null) stage.setKeyboardFocus(this);

        stageToLocalCoordinates(tmpPosition.set(0, 0));
        stageToLocalCoordinates(tmpSize.set(stage.getWidth(), stage.getHeight()));
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        style.stageBackground.draw(batch, getX() + tmpPosition.x, getY() + tmpPosition.y, getX() + tmpSize.x,
                getY() + tmpSize.y);
    }

    public static class ButtonDialogStyle {
        SvgNinePatchDrawable title, header, center, footer;
        Drawable stageBackground;
        BitmapFont titleFont;
        Color titleFontColor;
    }

}