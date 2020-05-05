package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.skin.styles.MessageBoxIconStyle;

/**
 * Created by Longri on 29.01.17.
 */
public class Validate_Icons extends Validate_Abstract_Icons {
    public Validate_Icons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage, MessageBoxIconStyle.class);
    }


    @Override
    public String getName() {
        return "Are all icons available";
    }

    @Override
    protected int getMinWidth(String styleName) {
        return validationSkin.messageBoxIcon.getPrefWidth() - TOLERANCE;
    }

    @Override
    protected int getMaxWidth(String styleName) {
        return validationSkin.messageBoxIcon.getPrefWidth() + TOLERANCE;
    }

    @Override
    protected int getMinHeight(String styleName) {
        return validationSkin.messageBoxIcon.getPrefHeight() - TOLERANCE;
    }

    @Override
    protected int getMaxHeight(String styleName) {
        return validationSkin.messageBoxIcon.getPrefHeight() + TOLERANCE;
    }
}
