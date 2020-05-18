package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.FloatControl;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.HSV_Color;

public class ColorPicker extends Activity {
    private final FloatControl hue, saturation, value;
    private final CB_Label lblHue, lblSaturation, lblValue;
    private CB_Button resultPresentation;
    private HSV_Color colorValue;
    private ChangeListener onChange;

    private ColorPicker(String title, Drawable icon) {
        super(title, icon);
        lblHue = new CB_Label(Translation.get("ColorHue"));
        hue = new FloatControl(0f, 360f, 3.6f, true, (value, dragged) -> {
            colorValue.setHue(value);
            resultPresentation.setColor(colorValue);
            resultPresentation.setText(colorValue.toString());
        });
        lblSaturation = new CB_Label(Translation.get("ColorSaturation"));
        saturation = new FloatControl(0f, 1f, 0.01f, true, (value, dragged) -> {
            colorValue.setSat(value);
            resultPresentation.setColor(colorValue);
            resultPresentation.setText(colorValue.toString());
        });
        lblValue = new CB_Label(Translation.get("ColorValue"));
        value = new FloatControl(0f, 1f, 0.01f, true, (value, dragged) -> {
            colorValue.setVal(value);
            resultPresentation.setColor(colorValue);
            resultPresentation.setText(colorValue.toString());
            TextButton.TextButtonStyle style = resultPresentation.getStyle();
            if (value < 0.5) {
                style.fontColor = Color.WHITE;
            } else {
                style.fontColor = Color.BLACK;
            }
        });
        resultPresentation = new CB_Button("");
        resultPresentation.setStyle(new TextButton.TextButtonStyle(resultPresentation.getStyle()));
    }

    public static ColorPicker getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new ColorPicker(title, icon);
            activity.top().left();
        }
        return (ColorPicker) activity;
    }

    @Override
    protected void createMainContent() {
        mainContent.addLast(resultPresentation);
        mainContent.addLast(lblHue);
        mainContent.addLast(hue);
        mainContent.addLast(lblSaturation);
        mainContent.addLast(saturation);
        mainContent.addLast(lblValue);
        mainContent.addLast(value);
        mainContent.addLast(new CB_Label(""));
        mainContent.padBottom(CB.scaledSizes.MARGIN).padTop(CB.scaledSizes.MARGIN);
    }

    public void execute(Color color, ChangeListener changeListener) {
        HSV_Color hsv = new HSV_Color(color);
        hue.setValue(hsv.getHue());
        saturation.setValue(hsv.getSat());
        value.setValue(hsv.getVal());
        resultPresentation.setColor(color);
        colorValue = new HSV_Color(color);
        resultPresentation.setText(colorValue.toString());
        onChange = changeListener;
        show();
    }

    public HSV_Color getColorValue() {
        return colorValue;
    }

    @Override
    protected void runAtOk() {
        if (onChange != null) onChange.changed(new ChangeListener.ChangeEvent(), this);
        finish();
    }
}
