/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.animations.actor_animations.Blink;
import de.longri.cachebox3.gui.animations.actor_animations.GestureHelpAnimation;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.ActionButton;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.CB_RectF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 18.08.2016.
 */
public class GestureHelp extends HelpWindow {
    final static Logger log = LoggerFactory.getLogger(GestureHelp.class);

    final Drawable buttonDrawable;
    final Drawable gestureRightIcon, gestureUpIcon, gestureLeftIcon, gestureDownIcon;
    final GestureHelpStyle style;
    final CharSequence GESTURE_MSG = Translation.get("gestureHelp");
    final CharSequence DONT_SHOW_AGAIN_MSG = Translation.get("DontShowHelp");
    final Table table = new Table();
    private boolean isShowing = false;

    private Actor arrowRight, arrowDown, arrowLeft, arrowUp;

    public GestureHelp(CB_RectF ellipseRectangle, Drawable buttonDrawable, Drawable gestureRightIcon, Drawable gestureUpIcon, Drawable gestureLeftIcon, Drawable gestureDownIcon) {
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


        int cellCount = 0;

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
                        if (gestureUpIcon != null) {
                            arrowUp = getArrowImageRotated(0);
                            table.add(arrowUp);
                        } else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 11:
                        if (gestureLeftIcon != null)
                            table.add(new Image(gestureLeftIcon));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 12:
                        if (gestureLeftIcon != null) {
                            arrowLeft = getArrowImageRotated(90);
                            table.add(arrowLeft);
                        } else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 13:
                        table.add(new Image(buttonDrawable));
                        break;
                    case 14:
                        if (gestureRightIcon != null) {
                            arrowRight = getArrowImageRotated(-90);
                            table.add(arrowRight);
                        } else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 15:
                        if (gestureRightIcon != null)
                            table.add(new Image(gestureRightIcon));
                        else
                            table.add(new Label("", VisUI.getSkin()));
                        break;
                    case 18:
                        if (gestureDownIcon != null) {
                            arrowDown = getArrowImageRotated(180);
                            table.add(arrowDown);
                        } else
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


        VisTextButton.VisTextButtonStyle buttonStyle = new VisTextButton.VisTextButtonStyle();
        buttonStyle.font = this.style.font;
        buttonStyle.fontColor = this.style.fontColor;
        buttonStyle.up = new ColorDrawable(this.style.backgroundColor);

        CB_Button button = new CB_Button(DONT_SHOW_AGAIN_MSG, buttonStyle);
        button.setPosition(CB.scaledSizes.MARGIN, this.ellipseRectangle.getMaxY() + CB.scaledSizes.MARGINx2);
        button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                log.debug("click on don't show again");
                Config.showGestureHelp.setValue(false);
                Config.AcceptChanges();

                //close directly
                hide();

            }
        });
        this.addActor(button);
    }

    private Image getArrowImageRotated(int angle) {
        Image image = new Image(style.arrowDrawable);

        if (angle == 90 || angle == -90) {
            image = new Image(style.arrowDrawable90);
            angle += 90;
        }

        image.pack();
        image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
        image.rotateBy(angle);
        image.pack();
        return image;
    }

    public void show() {
        super.show();

        isShowing = true;

        // close
        new com.badlogic.gdx.utils.Timer().scheduleTask(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                hide();
            }
        }, ViewManager.ToastLength.LONG.value);
    }

    public void hide() {
        if (!isShowing) return;
        super.hide();

        isShowing = false;

        // remove Blink action
        if (arrowRight != null) arrowRight.clearActions();
        if (arrowDown != null) arrowDown.clearActions();
        if (arrowLeft != null) arrowLeft.clearActions();
        if (arrowUp != null) arrowUp.clearActions();
    }

    public void show(ActionButton.GestureDirection gestureDirection) {
        this.show();

        Vector2 start = new Vector2();

        table.getCells().get(12).getActor().localToStageCoordinates(start);

        Vector2 end = new Vector2();


        switch (gestureDirection) {

            case Right:
                if (arrowRight != null) arrowRight.addAction(new Blink());
                table.getCells().get(14).getActor().localToStageCoordinates(end);
                break;
            case Up:
                if (arrowUp != null) arrowUp.addAction(new Blink());
                table.getCells().get(2).getActor().localToStageCoordinates(end);
                break;
            case Left:
                if (arrowLeft != null) arrowLeft.addAction(new Blink());
                table.getCells().get(10).getActor().localToStageCoordinates(end);
                break;
            case Down:
                if (arrowDown != null) arrowDown.addAction(new Blink());
                table.getCells().get(22).getActor().localToStageCoordinates(end);
                break;
        }


        this.addAction(new GestureHelpAnimation(start, end));
    }


    public static class GestureHelpStyle extends HelpWindowStyle {
        public Drawable arrowDrawable;
        public Drawable arrowDrawable90;
    }

}
