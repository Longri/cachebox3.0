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

import javax.xml.namespace.QName;

/**
 * Created by Longri on 01.04.18.
 */
public class ActiveQName extends QName {

    private boolean active = false;

    public ActiveQName(String localPart) {
        super(localPart);
    }

    public void setActive() {
        active = true;
    }

    public void setInActive() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
