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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.SizeF;

/**
 * Created by Longri on 05.08.16.
 */
public abstract class CB_View_Base extends WidgetGroup {

    public final String NAME;

    public CB_View_Base(float x, float y, float width, float height, String name) {
        this.NAME = name;
        this.setBounds(x, y, width, height);
    }

    public CB_View_Base(String name) {
        this.NAME = name;
    }

    public CB_View_Base(CB_RectF rec, String name) {
        this.NAME = name;
        this.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
    }

    public CB_View_Base(SizeF size, String name){
        this.NAME = name;
        this.setBounds(0, 0, size.width, size.height);
    }

    public abstract void dispose();

    public void addChild(Actor actor) {
        this.addActor(actor);
    }

    public void removeChild(Actor actor) {
        this.removeActor(actor, true);
    }

    public void setRec(CB_RectF rec) {
        this.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
    }


    //TODO catch onShow and onHide and call

    public void onShow() {
        SnapshotArray<Actor> childs = this.getChildren();
        if (childs != null && childs.size > 0) {
            try {
                for (int i = 0, n = childs.size; i < n; i++) {
                    // alle renderChilds() der in dieser CB_View_Base
                    // enthaltenen Childs auf rufen.
                    if (childs.get(i) instanceof CB_View_Base) {
                        CB_View_Base view = (CB_View_Base) childs.get(i);
                        if (view != null)
                            view.onShow();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void onHide() {
        SnapshotArray<Actor> childs = this.getChildren();
        if (childs != null && childs.size > 0) {
            try {
                for (int i = 0, n = childs.size; i < n; i++) {
                    // alle renderChilds() der in dieser GL_View_Base
                    // enthaltenen Childs auf rufen.
                    if (childs.get(i) instanceof CB_View_Base) {
                        CB_View_Base view = (CB_View_Base) childs.get(i);
                        if (view != null)
                            view.onHide();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
