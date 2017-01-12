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
    public void runnable() {
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