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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Lang;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TestView.class);

    public TestView() {
        super("TestView");
    }

    protected void create() {
        // create a Label with name for default
        nameLabel = new VisLabel(this.NAME);
        nameLabel.setAlignment(Align.center);
        nameLabel.setPosition(10, 10);
        nameLabel.setWrap(true);

        colorWidget = new ColorWidget(CB.getSkin().getColor("abstract_background"));
        colorWidget.setBounds(0, 0, this.getWidth(), this.getHeight());

        this.addActor(colorWidget);
        this.addActor(nameLabel);


        final VisTextButton testButton = new VisTextButton(Translation.getLangId());
        testButton.setSize(CB.scaledSizes.BUTTON_WIDTH_WIDE, CB.scaledSizes.BUTTON_HEIGHT);

        testButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                //set next Translation

                ArrayList<Lang> langs = Translation.GetLangs("lang");
                String actLangId = Translation.getLangId();
                int idx = 0;
                for (Lang lang : langs) {
                    if (lang.Name.equals(actLangId)) {
                        break;
                    }
                    idx++;
                }

                if (idx == langs.size()-1) idx = 0;
                else idx++;

                Lang nextLang=langs.get(idx);
                try {
                    Translation.LoadTranslation(nextLang.Path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                testButton.setText(Translation.getLangId());
            }
        });
        this.addActor(testButton);
    }


    @Override
    public void reloadState() {


        StringBuilder sb = new StringBuilder();

        sb.append("LaunchCount:" + Config.AppRaterlaunchCount.getValue());

        nameLabel.setText(sb.toString());


    }

    @Override
    public void saveState() {

    }

    @Override
    public void dispose() {

    }
}
