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
package de.longri.cachebox3.gui.help;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.utils.CB_RectF;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Longri on 18.08.2016.
 */
public class GestureHelp extends HelpWindow {

    final Drawable buttonDrawable;
    final Sprite gestureRightIcon, gestureUpIcon, gestureLeftIcon, gestureDownIcon;
    final GestureHelpStyle style;


    public GestureHelp(CB_RectF ellipseRectangle, Drawable buttonDrawable, Sprite gestureRightIcon, Sprite gestureUpIcon, Sprite gestureLeftIcon, Sprite gestureDownIcon) {
        super(ellipseRectangle);
        this.gestureRightIcon = gestureRightIcon;
        this.gestureUpIcon = gestureUpIcon;
        this.gestureLeftIcon = gestureLeftIcon;
        this.gestureDownIcon = gestureDownIcon;
        this.buttonDrawable = buttonDrawable;
        this.style = VisUI.getSkin().get("default", GestureHelpStyle.class);
        super.setStyle(this.style);
    }

    @Override
    public void pack() {
        super.pack();

        if (this.hasChildren()) return; // table is created

        int colNum = 5, rowNum = 5;
        final Table table = new Table();

        int cellCount = 0;
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                cellCount++;

                switch (cellCount) {
                    case 3:
                        if (gestureUpIcon != null)
                            table.add(new Image(gestureUpIcon)).size(gestureUpIcon.getWidth(), gestureUpIcon.getHeight());
                        else
                            table.add(new Label("", VisUI.getSkin())).size(30, 30);
                        break;
                    case 11:
                        if (gestureLeftIcon != null)
                            table.add(new Image(gestureLeftIcon)).size(gestureLeftIcon.getWidth(), gestureLeftIcon.getHeight());
                        else
                            table.add(new Label("", VisUI.getSkin())).size(30, 30);
                        break;
                    case 13:
                        table.add(new Image(buttonDrawable)).size(buttonDrawable.getMinWidth(), buttonDrawable.getMinHeight());
                        break;
                    case 15:
                        if (gestureRightIcon != null)
                            table.add(new Image(gestureRightIcon)).size(gestureRightIcon.getWidth(), gestureRightIcon.getHeight());
                        else
                            table.add(new Label("", VisUI.getSkin())).size(30, 30);
                        break;
                    case 23:
                        if (gestureDownIcon != null)
                            table.add(new Image(gestureDownIcon)).size(gestureDownIcon.getWidth(), gestureDownIcon.getHeight());
                        else
                            table.add(new Label("", VisUI.getSkin())).size(30, 30);
                        break;
                    default:
                        table.add(new Label("", VisUI.getSkin())).size(30, 30);
                }
            }
            table.row();
        }

        table.pack();
        table.setPosition((Gdx.graphics.getWidth() - table.getWidth()) / 2, (Gdx.graphics.getHeight() - table.getHeight()) / 2);
        this.addActor(table);
    }

    public void show() {
        super.show();

        // close
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                hide();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, ViewManager.ToastLength.NORMAL.value);
    }


    public static class GestureHelpStyle extends HelpWindowStyle {
        public Drawable arrowUp, arrowRight, arrowDown, arrowLeft;
    }

}
