/*
 * Copyright (C) 2016 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.gui.stages.initial_tasks;

import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Language;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.IChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Longri on 02.08.16.
 */
public final class TranslationLoaderTask extends AbstractInitTask {

    private final static Logger log = LoggerFactory.getLogger(TranslationLoaderTask.class);

    private Language loadedLang;

    public TranslationLoaderTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void runnable(WorkCallback callback) {
        new Translation("lang");
        callback.taskNameChange("Load Translation");
        loadTranslation();

        // add settings change handler
        Config.localisation.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                loadTranslation();
            }
        });
    }

    private void loadTranslation() {
        if (Config.localisation.getEnumValue() == loadedLang) return;
        try {
            Translation.LoadTranslation(Config.localisation.getEnumValue().toString());
            loadedLang = Config.localisation.getEnumValue();
        } catch (Exception e) {
            try {
                log.error("can't load lang: {}", Config.localisation.getEnumValue(), e);
                Translation.LoadTranslation(Config.localisation.getEnumDefaultValue().toString());
                loadedLang = Config.localisation.getEnumDefaultValue();
            } catch (IOException e1) {
                log.error("can't load default lang", e1);
            }
        }
        log.debug("Loaded lang: {}", loadedLang);
    }
}