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
import com.badlogic.gdx.utils.Align;
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
    public String selectedStyle;

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


        Tooltips.TooltipStyle styleTooltip = new Tooltips.TooltipStyle(game.skin.getFont("default-font"),
                game.skin.getDrawable("default-round"),
                game.skin.get("white", SkinColor.class));

        //ComboBox with all Styles
        Array<String> styleNames = new Array();
        for (Class clazz : StyleTypes.items) {
            styleNames.add(clazz.getSimpleName());
        }

        final VisSelectBox<String> selectBox = new VisSelectBox();

        styleNames.sort();

        selectBox.setItems(styleNames);
        add((Actor) null).expandX().fillX();
        add(selectBox).pad(2.5f).height(new Value.Fixed(buttonSize)).align(Align.right);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedStyle = ((VisSelectBox<String>) actor).getSelected();
                game.screenMain.paneOptions.refresh(true, selectedStyle);
            }
        });


    }
}
