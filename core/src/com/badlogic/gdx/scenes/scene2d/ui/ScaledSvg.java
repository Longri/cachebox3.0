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
package com.badlogic.gdx.scenes.scene2d.ui;


/**
 * Created by Longri on 16.08.2016.
 */
public class ScaledSvg {
    public String path;
    public float scale = 1;
    private String registerName;

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public String getRegisterName() {
        return registerName;
    }
}
