package de.longri.cachebox3.gui.stages.initial_tasks;

import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.IChanged;

import java.io.IOException;

/**
 * Created by Longri on 02.08.16.
 */
public final class TranslationLoaderTask extends AbstractInitTask {

    public TranslationLoaderTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void RUNABLE() {
        new Translation("lang");

        loadTranslation();

        // add settings change handler
        Config.Sel_LanguagePath.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                loadTranslation();
            }
        });
    }

    private void loadTranslation() {
        try {
            Translation.LoadTranslation(Config.Sel_LanguagePath.getValue());
        } catch (Exception e) {
            try {
                Translation.LoadTranslation(Config.Sel_LanguagePath.getDefaultValue());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}