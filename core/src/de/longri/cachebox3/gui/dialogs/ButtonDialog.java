package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.kotcrab.vis.ui.widget.VisDialog;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.*;

/**
 * Created by Longri on 03.08.16.
 */
public class ButtonDialog extends VisDialog {

    public static final int BUTTON_POSITIVE = 1;
    public static final int BUTTON_NEUTRAL = 2;
    public static final int BUTTON_NEGATIVE = 3;

    private static float margin;
    private final OnMsgBoxClickListener msgBoxClickListener;

    public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {

        super(title, "dialog"); // Title, WindowStyleName
        this.text(msg);
        setButtonCaptions(buttons);
        CB_RectF rec = calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None), false).getBounds();

        this.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());

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

    public void show() {
        this.show(CB.viewmanager);
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


    public void setButtonCaptions(MessageBoxButtons buttons) {

        this.button(Translation.Get("yes"), 1);

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

    /**
     * Called when a button is clicked. The dialog will be hidden after this method returns unless {@link #cancel()} is called.
     * @param object The object specified when the button was added.
     */
    protected void result (Object object) {

        if(msgBoxClickListener!=null){
            msgBoxClickListener.onClick((Integer)object,null);
            this.hide();
            this.remove();
        }

    }


}