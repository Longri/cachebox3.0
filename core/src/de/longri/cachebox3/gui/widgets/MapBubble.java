/*
 * Copyright (C) 2018 team-cachebox.de
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

import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.gui.skin.styles.MapBubbleStyle;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;

/**
 * Created by Longri on 31.01.2018.
 */
public class MapBubble extends VisTable {

    private final MapBubbleStyle style;
    private final AbstractCache cache;
    private final AbstractWaypoint waypoint;

    private float leftPatch;

    public MapBubble() {
        this(null, null);
    }

    public MapBubble(AbstractCache cache) {
        this(cache, null);
    }

    public MapBubble(AbstractWaypoint waypoint) {
        this(null, waypoint);
    }

    public MapBubble(AbstractCache cache, AbstractWaypoint waypoint) {
        this.cache = cache;
        this.waypoint = waypoint;
        style = VisUI.getSkin().get("bubble", MapBubbleStyle.class);
        this.setBackground(style.background);
    }

    public float getOffsetX() {
        return style.offsetX;
    }

    public float getOffsetY() {
        return style.offsetY;
    }

}
