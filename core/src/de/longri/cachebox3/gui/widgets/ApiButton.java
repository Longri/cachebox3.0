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
package de.longri.cachebox3.gui.widgets;

import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.ApiButtonStyle;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.apis.GroundspeakAPI.isAccessTokenInvalid;
import static de.longri.cachebox3.apis.GroundspeakAPI.isPremiumMember;

/**
 * Created by Longri on 11.04.2017.
 */
public class ApiButton extends IconButton {

    private final Logger log = LoggerFactory.getLogger(ApiButton.class);
    private final ApiButtonStyle style;

    public ApiButton() {
        super("");
        this.getLabel().setText(Translation.get("getApiKey"));
        this.style = VisUI.getSkin().get("ApiButton", ApiButtonStyle.class);
        TextButtonStyle btnStyle = new VisTextButtonStyle();
        btnStyle.up = style.up;
        btnStyle.down = style.down;
        btnStyle.font = style.font;
        btnStyle.fontColor = style.fontColor;
        this.setStyle(btnStyle);
        preferredHeight = style.unchecked.getMinHeight() + CB.scaledSizes.MARGINx2;
        setIcon();
    }

    @Override
    public void layout() {
        this.getLabel().setHeight(this.getHeight());
        this.getCell(this.getLabel()).spaceLeft(CB.scaledSizes.MARGINx2);
        image.setBounds(this.getWidth() - (style.unchecked.getMinHeight() + CB.scaledSizes.MARGIN), CB.scaledSizes.MARGIN, style.unchecked.getMinHeight(), style.unchecked.getMinHeight());
        super.layout();
    }

    protected void setIcon() {
        boolean Entry = false;
        if (Config.UseTestUrl.getValue()) {
            if (!Config.AccessTokenForTest.getValue().equals(""))
                Entry = true;
        } else {
            if (!Config.AccessToken.getValue().equals(""))
                Entry = true;
        }

        if (Entry) {
            if (isAccessTokenInvalid()) {
                image.setDrawable(style.invalid);
                // image.setDrawable(style.expired);
                // image.setDrawable(style.check);
            } else {
                if (isPremiumMember()) {
                    image.setDrawable(style.unchecked);
                } else {
                    image.setDrawable(style.unchecked);
                }
            }
        } else {
            image.setDrawable(style.unchecked);
        }

    }
}
