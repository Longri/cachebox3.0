package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.skin.styles.IconsStyle;

/**
 * Created by Longri on 29.01.17.
 */
public class Validate_Icons extends Validate_Abstract_Icons {
    public Validate_Icons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage, IconsStyle.class);
    }


    @Override
    public String getName() {
        return "Are all icons available";
    }

    @Override
    protected int getMinWidth() {
        return validationSkin.getIcon.getPrefWidth() - TOLLERANCE;
    }

    @Override
    protected int getMaxWidth() {
        return validationSkin.getIcon.getPrefWidth() + TOLLERANCE;
    }

    @Override
    protected int getMinHeight() {
        return validationSkin.getIcon.getPrefHeight() - TOLLERANCE;
    }

    @Override
    protected int getMaxHeight() {
        return validationSkin.getIcon.getPrefHeight() + TOLLERANCE;
    }
}
