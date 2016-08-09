package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.IconNames;
import de.longri.cachebox3.utils.MesureFontUtil;
import de.longri.cachebox3.utils.SizeF;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 03.08.16.
 */
public class ButtonDialog extends Table {   //VisWindow

    public static final int BUTTON_POSITIVE = 1;
    public static final int BUTTON_NEUTRAL = 2;
    public static final int BUTTON_NEGATIVE = 3;
    static private final Vector2 tmpPosition = new Vector2();
    static private final Vector2 tmpSize = new Vector2();
    public static float FADE_TIME = 0.3f;
    private static float margin, pad;
    private final OnMsgBoxClickListener msgBoxClickListener;
    private final ButtonDialogStyle style;
    protected boolean dontRenderDialogBackground = false;
    protected Object data;
    protected boolean mHasTitle = false;
    protected float mTitleHeight = 0;
    protected float mTitleWidth = CB.getScaledFloat(100);
    protected float mHeaderHeight = CB.getScaledFloat(10f);
    protected float mFooterHeight = CB.getScaledFloat(10f);
    protected float mTitleVersatz = CB.getScaledFloat(5f);
    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };
    private Table titleTable, contentTable, buttonTable;
    private Skin skin;
    private ObjectMap<Actor, Object> values = new ObjectMap();
    private String titleText;
    private Label titleLabel;

    public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
        super();

        this.setDebug(true, true);
        if (title != null) {
            setTitle(title);
        }

        // this.text(msg);


        this.skin = VisUI.getSkin();
        style = skin.get("default", ButtonDialogStyle.class);


        CB_RectF rec = calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None), false).getBounds();
        this.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
        initialize();
        setButtonCaptions(buttons);

        setSkin(skin);


        //TODO add Icon to ButtonDialog
//        SizeF contentSize = getContentSize();
//
//        CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
//
//        if (icon != MessageBoxIcon.None && icon != null) {
//            Image iconImage = new Image(imageRec, "MsgBoxIcon", false);
//            iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
//            addChild(iconImage);
//        }

        msgBoxClickListener = Listener;


    }

    private void setTitle(String title) {
        this.mHasTitle = true;
        this.titleText = title;
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
                icon = CB.getSprite(IconNames.dayGcLiveIcon.name());
                break;
            case 10:
                icon = CB.getSprite(IconNames.dayGcLiveIcon.name());
                break;

            default:
                icon = null;

        }

        return icon;
    }

    public static float calcHeaderHeight(BitmapFont font) {
        return (MesureFontUtil.Measure(font, "T").height) / 2;
    }

    public static float calcFooterHeight(BitmapFont font, boolean hasButtons) {

        if (margin <= 0)
            margin = CB.scaledSizes.MARGIN;

        return hasButtons ? CB.scaledSizes.BUTTON_HEIGHT + margin : calcHeaderHeight(font);
    }

    private void initialize() {

        if (margin <= 0) {
            margin = CB.scaledSizes.MARGIN;
            pad = margin / 2;
        }


//        defaults().space(6);
        if (mHasTitle) {
            add(titleTable = new Table(skin)).expandX().fill();
            // float pad = CB.getScaledFloat(3);
            titleTable.defaults().space(pad).padLeft(pad * 2).padRight(pad).padTop(pad).padBottom(pad);
            row();
        }
        add(contentTable = new Table(skin)).expand().fill();
        row();
        add(buttonTable = new Table(skin));

//        contentTable.defaults().space(2).padLeft(3).padRight(3);
        buttonTable.defaults().space(pad).padBottom(margin).padTop(margin);

        buttonTable.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
            }
        });

        if (mHasTitle) {
            mTitleHeight = 0;
            titleLabel = new Label(titleText, new Label.LabelStyle(style.titleFont, style.titleFontColor));
            titleTable.add(titleLabel).expandX().fillX();

            mTitleHeight = titleLabel.getHeight() + margin;
            mTitleWidth = titleLabel.getWidth() + (4 * margin);

        }


    }

    private SizeF calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon, boolean hasRemember) {
        if (margin <= 0) {
            margin = CB.scaledSizes.MARGIN;
            pad = margin / 2;
        }


        BitmapFont font = this.style.titleFont;

        float MsgWidth = (CB.scaledSizes.WINDOW_WIDTH * 0.95f) - 5 - CB.scaledSizes.BUTTON_HEIGHT;

        float MeasuredTextHeight = MesureFontUtil.MeasureWrapped(font, Text, MsgWidth).height + (margin * 4);

        int Height = (int) (hasIcon ? Math.max(MeasuredTextHeight, CB.scaledSizes.BUTTON_HEIGHT + (margin * 5)) : (int) MeasuredTextHeight);

        if (hasTitle) {
            GlyphLayout titleBounds = MesureFontUtil.Measure(font, "T");
            Height += (titleBounds.height * 3);
            Height += margin * 2;
        }
        Height += calcFooterHeight(font, hasButtons);
        if (hasRemember)
            Height += CB.scaledSizes.CHECK_BOX_HEIGHT;
        Height += calcHeaderHeight(font);

        // min Height festlegen
        Height = (int) Math.max(Height, CB.scaledSizes.BUTTON_HEIGHT * 2.5f);

        // max Height festlegen
        Height = (int) Math.min(Height, Gdx.graphics.getHeight() * 0.95f);

        SizeF ret = new SizeF(CB.scaledSizes.WINDOW_WIDTH, Height);
        return ret;
    }

    @Override
    public void pack() {
        // don't pack


        // see https://github.com/libgdx/libgdx/wiki/Table

    }

    @Override
    public void layout() {
        super.layout();
    }

    public void setButtonCaptions(MessageBoxButtons buttons) {
        float buttonWidth = 0;

        if (buttons == MessageBoxButtons.YesNoRetry) {
            buttonWidth = (this.getWidth() / 3) - margin;
            this.button(Translation.Get("yes"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("no"), buttonWidth, BUTTON_NEGATIVE);
            this.button(Translation.Get("retry"), buttonWidth, BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButtons.AbortRetryIgnore) {
            buttonWidth = (this.getWidth() / 3) - margin;
            this.button(Translation.Get("abort"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("retry"), buttonWidth, BUTTON_NEUTRAL);
            this.button(Translation.Get("ignore"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.OK) {
            buttonWidth = (this.getWidth() / 1) - margin;
            this.button(Translation.Get("ok"), buttonWidth, BUTTON_POSITIVE);
        } else if (buttons == MessageBoxButtons.OKCancel) {
            buttonWidth = (this.getWidth() / 2) - margin;
            this.button(Translation.Get("ok"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.RetryCancel) {
            buttonWidth = (this.getWidth() / 2) - margin;
            this.button(Translation.Get("retry"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.YesNo) {
            buttonWidth = (this.getWidth() / 2) - margin;
            this.button(Translation.Get("yes"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("no"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.YesNoCancel) {
            buttonWidth = (this.getWidth() / 3) - margin;
            this.button(Translation.Get("yes"), buttonWidth, BUTTON_POSITIVE);
            this.button(Translation.Get("no"), buttonWidth, BUTTON_NEGATIVE);
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButtons.Cancel) {
            buttonWidth = (this.getWidth() / 1) - margin;
            this.button(Translation.Get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        }
    }

    /**
     * Adds a text button to the button table. The dialog must have been constructed with a skin to use this method.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public ButtonDialog button(String text, float buttonWidth, Object object) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, buttonWidth, object, skin.get(VisTextButton.VisTextButtonStyle.class));
    }

    /**
     * Adds a text button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public ButtonDialog button(String text, float buttonWidth, Object object, VisTextButton.VisTextButtonStyle buttonStyle) {
        return button(new VisTextButton(text, buttonStyle), buttonWidth, object);
    }

    /**
     * Adds the given button to the button table.
     */
    public ButtonDialog button(Button button, float buttonWidth) {
        return button(button, buttonWidth, null);
    }

    /**
     * Adds the given button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public ButtonDialog button(Button button, float buttonWidth, Object object) {
        buttonTable.add(button).width(buttonWidth).expandX();
        setObject(button, object);
        return this;
    }

    public void setObject(Actor actor, Object object) {
        values.put(actor, object);
    }

    protected void result(Object object) {
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

        if (style.header != null && !dontRenderDialogBackground) {
            style.header.draw(batch, this.getX(), this.getHeight() - (mTitleHeight + mTitleVersatz) + this.getY(), this.getWidth(), mHeaderHeight);
        }
        if (style.footer != null && !dontRenderDialogBackground) {
            style.footer.draw(batch, this.getX(), this.getY(), this.getWidth(), mFooterHeight + 2);
        }
        if (style.center != null && !dontRenderDialogBackground) {
            style.center.draw(batch, this.getX(), mFooterHeight + this.getY(), this.getWidth(),
                    (buttonTable.getHeight()
                            + contentTable.getHeight() - (mFooterHeight + mTitleVersatz)));
        }

        if (mHasTitle) {
            if (mTitleWidth < this.getWidth()) {
                if (style.title != null && !dontRenderDialogBackground) {
                    style.title.draw(batch, this.getX(), this.getY() + titleTable.getY(), mTitleWidth, mTitleHeight);
                }
            } else {
                if (style.header != null && !dontRenderDialogBackground) {
                    style.header.draw(batch, this.getX(), this.getHeight() - mTitleHeight - mTitleVersatz + this.getY(), mTitleWidth, mTitleHeight);
                }
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