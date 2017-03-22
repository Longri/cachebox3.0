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
package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.gui.widgets.Compass;

/**
 * Created by Longri on 24.07.16.
 */
public class CompassView extends AbstractView {

    private final Compass compass;

    public CompassView() {
        super("CompassView");
        compass = new Compass("default");
        this.addChild(compass);

        compass.setBounds(10, 10, this.getWidth() - 20, this.getWidth() - 20);
    }


    @Override
    public void dispose() {

    }

    /**
     * Called when the actor's size has been changed.
     */
    protected void sizeChanged() {
        compass.setBounds(20, 100, this.getWidth() - 20, this.getWidth() - 20);
    }
}
