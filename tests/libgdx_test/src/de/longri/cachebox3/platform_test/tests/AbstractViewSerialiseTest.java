

//  Don't modify this file, it's created by tool 'extract_libgdx_test

/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.gui.views.*;

import com.badlogic.gdx.utils.reflect.*;
import de.longri.cachebox3.TestUtils;
import de.longri.serializable.BitStore;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertNotNull;
import static de.longri.cachebox3.platform_test.Assert.assertTrue;

/**
 * Created by Longri on 25.03.2019.
 */
public class AbstractViewSerialiseTest {


    @Test
    public void AboutViewTest() throws de.longri.serializable.NotImplementedException, PlatformAssertionError {
        TestUtils.initialVisUI();

        AboutView aboutView = new AboutView();
        AboutView newAboutView = (AboutView) TestUtils.assertAbstractViewSerialation(aboutView, AboutView.class);

        // About view have no member! check only if not null!


    }




}
