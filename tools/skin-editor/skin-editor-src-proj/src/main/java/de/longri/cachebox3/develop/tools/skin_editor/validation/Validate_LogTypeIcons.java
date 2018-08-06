package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.skin.styles.IconsStyle;
import de.longri.cachebox3.gui.skin.styles.LogTypesStyle;

/**
 * Created by Longri on 29.01.17.
 */
public class Validate_LogTypeIcons extends Validate_Abstract_Icons {
    
    final int iconSize;
    
    public Validate_LogTypeIcons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage, LogTypesStyle.class);
        this.iconSize= CB.getScaledInt(24);
    }

    @Override
    public String getName() {
        return "Are all LogTypes icons available";
    }

    @Override
    protected int getMinWidth() {
        return iconSize - TOLLERANCE;
    }

    @Override
    protected int getMaxWidth() {
        return iconSize + TOLLERANCE;
    }

    @Override
    protected int getMinHeight() {
        return iconSize - TOLLERANCE;
    }

    @Override
    protected int getMaxHeight() {
        return iconSize + TOLLERANCE;
    }
}
