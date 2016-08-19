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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.ColorDrawable;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.CB_RectF;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Longri on 18.08.2016.
 */
public class GestureHelp extends HelpWindow {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GestureHelp.class);

    final Drawable buttonDrawable;
    final Sprite gestureRightIcon, gestureUpIcon, gestureLeftIcon, gestureDownIcon;
    final GestureHelpStyle style;
    final String GESTURE_MSG = Translation.Get("gestureHelp"); // "You can also use this gesture to call this function"
    final String DONT_SHOW_AGAIN_MSG = Translation.Get("DontShowHelp"); // "Don't show help Msg again!"


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
        float m = CB.scaledSizes.MARGIN;
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                cellCount++;

                switch (cellCount) {
                    case 3:
                        if (gestureUpIcon != null)
                            table.add(new Image(gestureUpIcon));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 8:
                        if (gestureUpIcon != null)
                            table.add(getArrowImageRotated(0));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 11:
                        if (gestureLeftIcon != null)
                            table.add(new Image(gestureLeftIcon));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 12:
                        if (gestureLeftIcon != null)
                            table.add(getArrowImageRotated(90));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 13:
                        table.add(new Image(buttonDrawable));
                        break;
                    case 14:
                        if (gestureRightIcon != null)
                            table.add(getArrowImageRotated(-90));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 15:
                        if (gestureRightIcon != null)
                            table.add(new Image(gestureRightIcon));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 18:
                        if (gestureUpIcon != null)
                            table.add(getArrowImageRotated(180));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 23:
                        if (gestureDownIcon != null)
                            table.add(new Image(gestureDownIcon));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    default:
                        table.add(new Label("", VisUI.getSkin()));
                }
            }
            table.row();
        }

        table.pack();
        table.setPosition((Gdx.graphics.getWidth() - table.getWidth()) / 2, (Gdx.graphics.getHeight() - table.getHeight()) / 2);
        this.addActor(table);

        // add label

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = this.style.font;
        labelStyle.fontColor = this.style.fontColor;
        Label label = new Label("", labelStyle);

        label.setWrap(true);
        label.setAlignment(Align.center, Align.center);
        float width = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2;
        label.setWidth(width);

        GlyphLayout bounds = label.getStyle().font.newFontCache().setText(GESTURE_MSG, 0, 0, width, 0, true);

        label.setText(GESTURE_MSG);
        label.setPosition(CB.scaledSizes.MARGIN, Gdx.graphics.getHeight() - (bounds.height + CB.scaledSizes.MARGINx2));
        this.addActor(label);


        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = this.style.font;
        buttonStyle.fontColor = this.style.fontColor;
        buttonStyle.up = new ColorDrawable(this.style.backgroundColor);

        TextButton button = new TextButton(DONT_SHOW_AGAIN_MSG, buttonStyle);
        button.setPosition(CB.scaledSizes.MARGIN, this.ellipseRectangle.getMaxY() + CB.scaledSizes.MARGINx2);
        button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // todo Handle don't show help again
                log.debug("click on don't show again, but not handled now!");
            }
        });
        this.addActor(button);
    }

    private Image getArrowImageRotated(int angle) {
        Image image = new Image(style.arrowDrawable);
        image.pack();
        image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
        image.rotateBy(angle);
        return image;
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
        timer.schedule(task, ViewManager.ToastLength.LONG.value);
    }


    public static class GestureHelpStyle extends HelpWindowStyle {
        public Drawable arrowDrawable;
    }

}
