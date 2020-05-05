package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.gui.skin.styles.MenuIconStyle;

/**
 * Created by Longri on 29.01.17.
 */
public class Validate_MenuIcons extends Validate_Abstract_Icons {
    public Validate_MenuIcons(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage, MenuIconStyle.class);
    }

    @Override
    public String getName() {
        return "Are all menu icons available";
    }


    @Override
    protected int getMinWidth() {
        return validationSkin.menuIcon.getPrefWidth() - TOLERANCE;
    }

    @Override
    protected int getMaxWidth() {
        return validationSkin.menuIcon.getPrefWidth() + TOLERANCE;
    }

    @Override
    protected int getMinHeight() {
        return validationSkin.menuIcon.getPrefHeight() - TOLERANCE;
    }

    @Override
    protected int getMaxHeight() {
        return validationSkin.menuIcon.getPrefHeight() + TOLERANCE;
    }
}
