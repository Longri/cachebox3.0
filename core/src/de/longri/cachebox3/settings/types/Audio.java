/*
 * Copyright (C) 2011-2020 team-cachebox.de
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
package de.longri.cachebox3.settings.types;

public class Audio {

    public Audio(String path, boolean absolute, boolean mute, float volume) {
        super();
        this.Path = path;
        this.Class_Absolute = absolute;
        this.Mute = mute;
        this.Volume = volume;
    }

    public Audio(Audio value) {
        Path = value.Path;
        Volume = value.Volume;
        Mute = value.Mute;
        Class_Absolute = value.Class_Absolute;
    }

    public String Path;
    public float Volume;
    public boolean Mute;
    public boolean Class_Absolute;

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Audio) {
            ret = true;
            Audio aud = (Audio) obj;
            if (!Path.equalsIgnoreCase(aud.Path))
                ret = false;
            if (Class_Absolute != aud.Class_Absolute)
                ret = false;
            if (Mute != aud.Mute)
                ret = false;
            if (Volume != aud.Volume)
                ret = false;
        }

        return ret;
    }
}
