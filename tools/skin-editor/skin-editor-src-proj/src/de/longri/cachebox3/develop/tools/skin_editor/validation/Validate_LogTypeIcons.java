package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.skin.styles.LogTypesStyle;

/**
 * Created by Longri on 29.01.17.
 */
public class Validate_LogTypeIcons extends Validate_Abstract_Icons {

    final int scaled12, scaled18, scaled48, scaled24;

    public Validate_LogTypeIcons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage, LogTypesStyle.class);
        this.scaled12 = CB.getScaledInt(12);
        this.scaled18 = CB.getScaledInt(18);
        this.scaled24 = CB.getScaledInt(24);
        this.scaled48 = CB.getScaledInt(48);
    }

    private int getIconSize(String styleName) {
        switch (styleName) {
            case "LogTypesSize12":
                return scaled12;
            case "LogTypesSize18":
                return scaled18;
            case "LogTypesSize48":
                return scaled48;
            case "LogTypesSize24":
                return scaled24;
            default:
                return -1;
        }
    }

    @Override
    public String getName() {
        return "Are all LogTypes icons available";
    }

    @Override
    protected int getMinWidth(String styleName) {
        return getIconSize(styleName) - TOLERANCE;
    }

    @Override
    protected int getMaxWidth(String styleName) {
        return getIconSize(styleName) + TOLERANCE;
    }

    @Override
    protected int getMinHeight(String styleName) {
        return getIconSize(styleName) - TOLERANCE;
    }

    @Override
    protected int getMaxHeight(String styleName) {
        return getIconSize(styleName) + TOLERANCE;
    }
}
