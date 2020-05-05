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

    final int bubble_LogTypesIconSize, cacheListViewLogTypesIconSize, logViewLogStylesIconSize, DraftListItemStyleIconSize;

    public Validate_LogTypeIcons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage, LogTypesStyle.class);
        this.bubble_LogTypesIconSize = CB.getScaledInt(12);
        this.cacheListViewLogTypesIconSize = CB.getScaledInt(18);
        this.logViewLogStylesIconSize = CB.getScaledInt(24);
        this.DraftListItemStyleIconSize = CB.getScaledInt(48);
    }

    private int getIconSize(String styleName) {
        switch (styleName) {
            case "bubble_LogTypes":
                return bubble_LogTypesIconSize;
            case "cacheListViewLogTypes":
                return cacheListViewLogTypesIconSize;
            case "logViewLogStyles":
                return logViewLogStylesIconSize;
            default:
                return DraftListItemStyleIconSize;
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
