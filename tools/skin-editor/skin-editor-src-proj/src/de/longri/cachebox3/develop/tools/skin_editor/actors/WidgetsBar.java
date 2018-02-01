/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.longri.cachebox3.develop.tools.skin_editor.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.mobidevelop.maps.editor.ui.utils.Tooltips;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import de.longri.cachebox3.develop.tools.skin_editor.StyleTypes;
import de.longri.cachebox3.utils.SkinColor;


/**
 * A table representing the buttons panel at the top
 *
 * @author Yanick Bourbeau
 */
public class WidgetsBar extends Table {

    private SkinEditorGame game;
    public ButtonGroup group;

    /**
     *
     */
    public WidgetsBar(final SkinEditorGame game) {
        super();
        this.game = game;

        left();
        setBackground(game.skin.getDrawable("default-pane"));

    }

    /**
     *
     */
    public void initializeButtons() {
        float buttonSize = 30;
        group = new ButtonGroup();

        Tooltips.TooltipStyle styleTooltip = new Tooltips.TooltipStyle(game.skin.getFont("default-font"),
                game.skin.getDrawable("default-round"),
                game.skin.get("white", SkinColor.class));

        String[] widgets = SkinEditorGame.widgets;
        for (String widget : widgets) {

            ImageButtonStyle style = new ImageButtonStyle();
            style.checked = game.skin.getDrawable("default-round-down");
            style.down = game.skin.getDrawable("default-round-down");
            style.up = game.skin.getDrawable("default-round");
            style.imageUp = game.skin.getDrawable("widgets/" + widget);
            final ImageButton button = new ImageButton(style);
            button.setUserObject(widget);


            Tooltips tooltip = new Tooltips(styleTooltip, getStage());
            tooltip.registerTooltip(button, (String) button.getUserObject());

            button.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.screenMain.panePreview.refresh();

                    boolean withStyle = true;
                    if (button.getUserObject().equals("Icons"))
                        withStyle = false;

                    if (button.getUserObject().equals("MenuIcons"))
                        withStyle = false;


                    String styleClassName = game.resolveWidgetPackageName((String) button.getUserObject());

                    Class<?> style = null;
                    try {
                        style = Class.forName(styleClassName);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    String widget = button.getUserObject().toString();
                    String widgetStyle = game.resolveWidgetPackageName(widget);
                    game.screenMain.paneOptions.refresh(true, widgetStyle);

                }

            });

            group.add(button);


            add(button).pad(2.5f).width(new Value.Fixed(buttonSize)).height(new Value.Fixed(buttonSize));
        }


        //ComboBox with all Styles
        Array<String> styleNames = new Array();
        for (Class clazz : StyleTypes.items) {
            styleNames.add(clazz.getSimpleName());
        }

        final VisSelectBox<String> selectBox = new VisSelectBox();

        styleNames.sort();

        selectBox.setItems(styleNames);
        add(selectBox).pad(2.5f).height(new Value.Fixed(buttonSize));

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = ((VisSelectBox<String>) actor).getSelected();
                game.screenMain.paneOptions.refresh(true, selected);
            }
        });


    }

    /**
     *
     */
    public void resetButtonSelection() {
        Button button = (Button) group.getButtons().get(0);
        button.setChecked(true);
    }

}
