/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.skin.styles.EditTextStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_WidgetGroup;
import de.longri.cachebox3.utils.NamedRunnable;

/**
 * Created by Longri on 18.05.2017.
 */
public class EditTextBox extends Catch_WidgetGroup {

    private boolean singleLine = true;
    private int minLineCount = 1;
    private int maxLineCount = 5;
    private VisLabel textLabel;
    private VisScrollPane scrollPane;
    private Button editButton;
    private CharSequence text;
    private EditTextStyle style;
    private float maxWidth = 0;

    public EditTextBox(boolean multiLine) {
        this(multiLine, "");
    }

    public EditTextBox(boolean multiLine, String text) {
        this.singleLine = !multiLine;
        textLabel = new VisLabel(text);
        scrollPane = new VisScrollPane(textLabel);
        editButton = new Button();
        this.addActor(scrollPane);
        this.addActor(editButton);
        this.setStyle(VisUI.getSkin().get("default", EditTextStyle.class));
        this.setText(text);

        ClickListener clickListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                Input.TextInputListener listener = new Input.TextInputListener() {
                    @Override
                    public void input(final String text) {
                        CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
                            @Override
                            public void run() {
                                setText(text);
                                invalidate();
                            }
                        });
                    }

                    @Override
                    public void canceled() {
                        // do nothing
                    }
                };

                if (singleLine) {
                    PlatformConnector.getSinglelineTextInput(listener, "", EditTextBox.this.text.toString(), "");
                } else {
                    PlatformConnector.getMultilineTextInput(listener, "", EditTextBox.this.text.toString(), "");
                }
            }
        };

        editButton.addListener(clickListener);
        this.addListener(clickListener);
    }

    public void setStyle(EditTextStyle style) {
        this.style = style;
        ScrollPane.ScrollPaneStyle sps = new ScrollPane.ScrollPaneStyle();
        sps.background = style.background;
        this.scrollPane.setStyle(sps);

        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = style.font;
        ls.fontColor = style.fontColor;
        this.textLabel.setStyle(ls);

        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = style.editIcon;
        bs.down = style.editIcon;
        editButton.setStyle(bs);
    }

    public void setMinLineCount(int min) {
        this.minLineCount = min;
    }

    public void setMaxLineCount(int max) {
        this.maxLineCount = max;
    }


    public void setText(CharSequence text) {
        this.text = text;
        textLabel.setText(text);
    }

    @Override
    public void layout() {
        scrollPane.setBounds(0, 0, this.getWidth(), this.getHeight());
        editButton.setBounds(this.getWidth() - style.editIcon.getMinWidth(),
                this.getHeight() - style.editIcon.getMinHeight(),
                style.editIcon.getMinWidth(), style.editIcon.getMinHeight());
    }


    @Override
    public float getMinWidth() {
        float pref = textLabel.getMinWidth() + style.background.getMinWidth();
        return maxWidth > 0 ? Math.min(pref, maxWidth) : pref;
    }

    @Override
    public float getMinHeight() {
        float min = (textLabel.getStyle().font.getLineHeight() * minLineCount) + style.background.getMinHeight();
        float max = (textLabel.getStyle().font.getLineHeight() * maxLineCount) + style.background.getMinHeight();
        if (min > max) return min;
        return min;
    }

    @Override
    public float getPrefWidth() {
        float pref = textLabel.getPrefWidth() + style.background.getMinWidth();
        return maxWidth > 0 ? Math.min(pref, maxWidth) : pref;
    }

    @Override
    public float getPrefHeight() {
        if (textLabel.getPrefHeight() + style.background.getMinHeight() > getMaxHeight()) {
            return getMaxHeight();
        }
        return textLabel.getPrefHeight() + style.background.getMinHeight();
    }

    @Override
    public float getMaxWidth() {
        return maxWidth;
    }


    public void setMaxWidth(float value) {
        this.maxWidth = value;
    }

    @Override
    public float getMaxHeight() {
        return (textLabel.getStyle().font.getLineHeight() * maxLineCount) + style.background.getMinHeight();
    }


    public String getText() {
        return this.text.toString();
    }

    public EditTextStyle getStyle() {
        return this.style;
    }
}
