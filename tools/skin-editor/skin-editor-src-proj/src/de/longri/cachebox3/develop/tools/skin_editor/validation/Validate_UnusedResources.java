/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.develop.tools.skin_editor.validation;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SavableSvgSkin;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;

/**
 * Created by Longri on 25.01.2017.
 */
public class Validate_UnusedResources extends ValidationTask {

    public Validate_UnusedResources(SkinEditorGame game, SavableSvgSkin validationSkin, Stage stage) {
        super(game, validationSkin, stage);
    }

    @Override
    public String getName() {
        return "Search for unused resources";
    }

    @Override
    public void runValidation() {
//TODO serch for unused resources like color, bitmap drawable, fonts.....
    }
}
