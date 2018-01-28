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

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Language;
import de.longri.cachebox3.translation.SequenceTranslationHandler;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.StringTranslationHandler;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.SoundCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Longri on 02.08.16.
 */
public final class TranslationLoaderTask extends AbstractInitTask {

    private final static Logger log = LoggerFactory.getLogger(TranslationLoaderTask.class);

    private Language loadedLang;

    public TranslationLoaderTask(String name) {
        super(name);
    }

    @Override
    public void runnable() {
//        Translation.translation = new StringTranslationHandler(Gdx.files.internal("lang"), "en-GB");
        Translation.translation = new SequenceTranslationHandler(Gdx.files.internal("lang"), "en-GB");

        EventHandler.fire(new IncrementProgressEvent(10, "Load Translation"));
        loadTranslation();

        // add settings change handler
        Config.localisation.addChangedEventListener(new IChanged() {
            @Override
            public void isChanged() {
                loadTranslation();
            }
        });


        //load sounds
        SoundCache.loadSounds();

    }

    @Override
    public int getProgressMax() {
        return 10;
    }

    private void loadTranslation() {
        if (Config.localisation.getEnumValue() == loadedLang) return;
        try {
            Translation.loadTranslation(Config.localisation.getEnumValue().toString());
            loadedLang = Config.localisation.getEnumValue();
        } catch (Exception e) {
            try {
                log.error("can't load lang: {}", Config.localisation.getEnumValue(), e);
                Translation.loadTranslation(Config.localisation.getEnumDefaultValue().toString());
                loadedLang = Config.localisation.getEnumDefaultValue();
            } catch (IOException e1) {
                log.error("can't load default lang", e1);
            }
        }
        log.debug("Loaded lang: {}", loadedLang);
    }
}