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
package de.longri.cachebox3.gui.map;

import org.oscim.theme.ExternalRenderTheme;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.XmlRenderThemeMenuCallback;

/**
 * Created by Longri on 21.03.2018.
 */
public class NamedExternalRenderTheme extends ExternalRenderTheme {

    public final String name;
    public final String path;

    public NamedExternalRenderTheme(String name, String fileName) throws IRenderTheme.ThemeException {
        super(fileName);
        this.name = name;
        this.path = fileName;
    }

    public NamedExternalRenderTheme(String name, String fileName, XmlRenderThemeMenuCallback menuCallback) throws IRenderTheme.ThemeException {
        super(fileName, menuCallback);
        this.name = name;
        this.path = fileName;
    }

    @Override
    public String toString() {
        return "External Render Theme: " + name;
    }
}
