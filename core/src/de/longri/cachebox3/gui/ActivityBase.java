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
package de.longri.cachebox3.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.Showable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper class to bring the CB2 Activities to CB3
 * Created by Longri on 23.08.2016.
 */
public class ActivityBase extends Window implements Showable {


    protected final ActivityBaseStyle style;
    protected boolean needsLayout = true;
    private AtomicBoolean isDisposed = new AtomicBoolean(false);

    public ActivityBase() {
        this("nameless", VisUI.getSkin().get("default", ActivityBaseStyle.class));
    }

    public ActivityBase(String name) {
        this(name, VisUI.getSkin().get("default", ActivityBaseStyle.class));
    }

    public ActivityBase(String name, ActivityBaseStyle style) {
        super(name);
        if (!CB.isGlThread()) {
            throw new RuntimeException("Don't instance a ActivityBase on non GL Thread");
        }
        this.style = style;
        this.setBackground(style.background);
        this.setStageBackground(style.stageBackground);
    }


    public void finish() {
        CB.postOnGlThread(new NamedRunnable("Finish ActivityBase") {
            @Override
            public void run() {
                ActivityBase.super.hide();
            }
        });
    }

    public void onShow() {

    }

    public void onHide() {

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void show() {
        CB.postOnGlThread(new NamedRunnable("ActivityBase") {
            @Override
            public void run() {
                ActivityBase.super.show();
                //set to full screen
                ActivityBase.this.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });
    }

    @Override
    public void dispose() {
        isDisposed.set(true);
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public float getPrefWidth() {
        return Gdx.graphics.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return Gdx.graphics.getHeight();
    }

    public static class ActivityBaseStyle {
        public Drawable background, stageBackground;
    }

}
