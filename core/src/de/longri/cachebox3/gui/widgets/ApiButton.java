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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.api.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.skin.styles.ApiButtonStyle;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 11.04.2017.
 */
public class ApiButton extends VisTextButton {

    private final Logger log = LoggerFactory.getLogger(ApiButton.class);
    private final ApiButtonStyle style;
    private final com.badlogic.gdx.scenes.scene2d.ui.Image image;
    private final float preferedHight;

    public ApiButton() {
        super(Translation.Get("getApiKey"));
        this.addListener(clickListener);
        this.style = VisUI.getSkin().get("ApiButton", ApiButtonStyle.class);
        TextButtonStyle btnStyle = new VisTextButtonStyle();
        btnStyle.up = style.up;
        btnStyle.down = style.down;
        btnStyle.font = style.font;
        btnStyle.fontColor = style.fontColor;
        this.setStyle(btnStyle);
        this.getLabel().setAlignment(Align.center | Align.left);
        image = new Image(style.unchecked);
        this.addActor(image);
        preferedHight = style.unchecked.getMinHeight() + CB.scaledSizes.MARGINx2;
        setIcon();
    }

    @Override
    public void layout() {
        this.getLabel().setHeight(this.getHeight());
        this.getCell(this.getLabel()).spaceLeft(CB.scaledSizes.MARGINx2);
        image.setBounds(this.getWidth() - (style.unchecked.getMinHeight() + CB.scaledSizes.MARGIN), CB.scaledSizes.MARGIN, style.unchecked.getMinHeight(), style.unchecked.getMinHeight());
        super.layout();
    }

    @Override
    public float getPrefHeight() {
        return this.preferedHight;
    }

    private void setIcon() {
        boolean Entry = false;
        if (Config.StagingAPI.getValue()) {
            if (!Config.GcAPIStaging.getValue().equals(""))
                Entry = true;
        } else {
            if (!Config.GcAPI.getValue().equals(""))
                Entry = true;
        }

        if (Entry) {
            image.setDrawable(style.check);
        } else {
            image.setDrawable(style.unchecked);
        }

        //TODO set icon for invalid and expired

    }


    private final ClickListener clickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            log.debug("Create Api Key clicked");
            PlatformConnector.getApiKey(new GenericCallBack<String>() {
                @Override
                public void callBack(String accessToken) {
                    log.debug("return create ApiKey :{}", accessToken);

                    GroundspeakAPI.CacheStatusValid = false;
                    GroundspeakAPI.CacheStatusLiteValid = false;

                    // store the encrypted AccessToken in the Config file
                    if (Config.StagingAPI.getValue()) {
                        Config.GcAPIStaging.setEncryptedValue(accessToken);
                    } else {
                        Config.GcAPI.setEncryptedValue(accessToken);
                    }

                    Config.AcceptChanges();
                    String act = GroundspeakAPI.GetAccessToken();
                    if (act.length() > 0) {
                        GroundspeakAPI.GetMembershipType(new GenericCallBack<Integer>() {
                            @Override
                            public void callBack(Integer status) {
                                if (status >= 0) {
                                    log.debug("Read User Name/State {}/{}", GroundspeakAPI.MemberName, status);
                                    Config.GcLogin.setValue(GroundspeakAPI.MemberName);
                                    Config.AcceptChanges();
                                    CB.viewmanager.toast("Welcome : " + GroundspeakAPI.MemberName);
                                } else {
                                    CB.viewmanager.toast("Welcome : " + GroundspeakAPI.MemberName);
                                    log.debug("Can't read UserName State: {}", GroundspeakAPI.MemberName, status);
                                }
                            }
                        });
                    }
                }
            });
        }
    };
}