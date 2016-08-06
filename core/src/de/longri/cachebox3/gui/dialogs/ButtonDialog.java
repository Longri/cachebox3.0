package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.*;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 03.08.16.
 */
public class ButtonDialog extends VisWindow {   //VisWindow

    public static final int BUTTON_POSITIVE = 1;
    public static final int BUTTON_NEUTRAL = 2;
    public static final int BUTTON_NEGATIVE = 3;


    Table contentTable, buttonTable;
    private Skin skin;
    ObjectMap<Actor, Object> values = new ObjectMap();

    private static float margin;
    private final OnMsgBoxClickListener msgBoxClickListener;

    public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
        super(title, "dialog"); // Title, WindowStyleName

        this.setDebug(true, true);

        // this.text(msg);


        this.skin = VisUI.getSkin();
        initialize();
        setButtonCaptions(buttons);
        CB_RectF rec = calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None), false).getBounds();
        this.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());


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

    private void initialize() {
        setModal(true);
        getTitleLabel().setAlignment(VisUI.getDefaultTitleAlign());

        defaults().space(6);
        add(contentTable = new Table(skin)).expand().fill();
        row();
        add(buttonTable = new Table(skin));

        contentTable.defaults().space(2).padLeft(3).padRight(3);
        buttonTable.defaults().space(6).padBottom(3);

        buttonTable.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
            }
        });

        buttonTable.setBackground("drawable_dialog_footer");
    }

    private SizeF calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon, boolean hasRemember) {
        if (margin <= 0)
            margin = UI_Size_Base.that.getMargin();

        float Width = (((UI_Size_Base.that.getButtonWidthWide() + margin) * 3) + margin);
        if (Width * 1.2 < UI_Size_Base.that.getWindowWidth())
            Width *= 1.2f;


        BitmapFont font = this.getStyle().titleFont;

        float MsgWidth = (Width * 0.95f) - 5 - UI_Size_Base.that.getButtonHeight();

        float MeasuredTextHeight = MesureFontUtil.MeasureWrapped(font, Text, MsgWidth).height + (margin * 4);

        int Height = (int) (hasIcon ? Math.max(MeasuredTextHeight, UI_Size_Base.that.getButtonHeight() + (margin * 5)) : (int) MeasuredTextHeight);

        if (hasTitle) {
            GlyphLayout titleBounds = MesureFontUtil.Measure(font, "T");
            Height += (titleBounds.height * 3);
            Height += margin * 2;
        }
        Height += calcFooterHeight(font, hasButtons);
        if (hasRemember)
            Height += UI_Size_Base.that.getChkBoxSize().height;
        Height += calcHeaderHeight(font);

        // min Height festlegen
        Height = (int) Math.max(Height, UI_Size_Base.that.getButtonHeight() * 2.5f);

        // max Height festlegen
        Height = (int) Math.min(Height, UI_Size_Base.that.getWindowHeight() * 0.95f);

        SizeF ret = new SizeF(Width, Height);
        return ret;
    }

    @Override
    public void pack() {
        //TODO remove ContentTable and set own


    }

    public void setButtonCaptions(MessageBoxButtons buttons) {

        if (buttons == MessageBoxButtons.YesNoRetry) {
            this.button(Translation.Get("yes"), BUTTON_POSITIVE);
            this.button(Translation.Get("no"), BUTTON_NEGATIVE);
            this.button(Translation.Get("retry"), BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButtons.AbortRetryIgnore) {
            this.button(Translation.Get("abort"), BUTTON_POSITIVE);
            this.button(Translation.Get("retry"), BUTTON_NEUTRAL);
            this.button(Translation.Get("ignore"), BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.OK) {
            this.button(Translation.Get("ok"), BUTTON_POSITIVE);
        } else if (buttons == MessageBoxButtons.OKCancel) {
            this.button(Translation.Get("ok"), BUTTON_POSITIVE);
            this.button(Translation.Get("cancel"), BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.RetryCancel) {
            this.button(Translation.Get("retry"), BUTTON_POSITIVE);
            this.button(Translation.Get("cancel"), BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.YesNo) {
            this.button(Translation.Get("yes"), BUTTON_POSITIVE);
            this.button(Translation.Get("no"), BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButtons.YesNoCancel) {
            this.button(Translation.Get("yes"), BUTTON_POSITIVE);
            this.button(Translation.Get("no"), BUTTON_NEGATIVE);
            this.button(Translation.Get("cancel"), BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButtons.Cancel) {
            this.button(Translation.Get("cancel"), BUTTON_NEGATIVE);
        }
    }


    /**
     * Adds a text button to the button table. Null will be passed to {@link #result(Object)} if this button is clicked. The dialog
     * must have been constructed with a skin to use this method.
     */
    public ButtonDialog button(String text) {
        return button(text, null);
    }

    /**
     * Adds a text button to the button table. The dialog must have been constructed with a skin to use this method.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public ButtonDialog button(String text, Object object) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, object, skin.get(VisTextButton.VisTextButtonStyle.class));
    }

    /**
     * Adds a text button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public ButtonDialog button(String text, Object object, VisTextButton.VisTextButtonStyle buttonStyle) {
        return button(new VisTextButton(text, buttonStyle), object);
    }

    /**
     * Adds the given button to the button table.
     */
    public ButtonDialog button(Button button) {
        return button(button, null);
    }

    /**
     * Adds the given button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null.
     */
    public ButtonDialog button(Button button, Object object) {
        buttonTable.add(button);
        setObject(button, object);
        return this;
    }

    public void setObject(Actor actor, Object object) {
        values.put(actor, object);
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
            margin = UI_Size_Base.that.getMargin();

        return hasButtons ? UI_Size_Base.that.getButtonHeight() + margin : calcHeaderHeight(font);
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

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

}