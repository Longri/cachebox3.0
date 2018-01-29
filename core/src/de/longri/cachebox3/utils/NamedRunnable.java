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
package de.longri.cachebox3.utils;

/**
 * Created by Longri on 18.01.2018.
 */
public abstract class NamedRunnable implements Runnable {

    public final String name;

    public NamedRunnable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NamedRunnable: " + name;
    }

}
