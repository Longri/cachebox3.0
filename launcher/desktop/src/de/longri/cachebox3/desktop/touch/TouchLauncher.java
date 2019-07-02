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
package de.longri.cachebox3.desktop.touch;

/**
 * Created by Longri on 02.07.2019.
 */
public class TouchLauncher {
    public static void main(String[] args) {
        //redirect to new Lwjgl3 version on namespace com.badlogic.gdx.backends.lwjgl3
        com.badlogic.gdx.backends.lwjgl3.TouchLwjgl3Launcher.main(args);
    }
}
