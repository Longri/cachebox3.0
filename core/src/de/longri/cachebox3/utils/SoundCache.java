/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingsAudio;

public class SoundCache {
    final static Logger log = LoggerFactory.getLogger(SoundCache.class);

    public enum Sounds {
        GPS_lose, GPS_fix, Approach, AutoResortSound, Global
    }

    private static Music GlobalVolumeSound;
    private static Music GPS_lose;
    private static Music GPS_fix;
    private static Music Approach;
    private static Music AutoResort;

    public static void play(Sounds sound) {
        play(sound, false);
    }

    public static void play(Sounds sound, boolean ignoreMute) {
        if (Config.GlobalVolume.getValue().Mute && !ignoreMute)
            return;

        switch (sound) {
            case GPS_lose:
                if ((ignoreMute || !Config.GPS_lose.getValue().Mute) && GPS_lose != null)
                    GPS_lose.play();
                break;
            case GPS_fix:
                if ((ignoreMute || !Config.GPS_fix.getValue().Mute) && GPS_fix != null)
                    GPS_fix.play();
                break;
            case Approach:
                if ((ignoreMute || !Config.Approach.getValue().Mute) && Approach != null)
                    Approach.play();
                break;
            case AutoResortSound:
                if ((ignoreMute || !Config.AutoResortSound.getValue().Mute) && AutoResort != null)
                    AutoResort.play();
                break;
            case Global:
                if ((ignoreMute || !Config.GlobalVolume.getValue().Mute) && GlobalVolumeSound != null)
                    GlobalVolumeSound.play();
                break;
        }
    }

    public static void loadSounds() {

        GlobalVolumeSound = getMusikFromSetting(Config.GlobalVolume);
        Approach = getMusikFromSetting(Config.Approach);
        GPS_fix = getMusikFromSetting(Config.GPS_fix);
        GPS_lose = getMusikFromSetting(Config.GPS_lose);
        AutoResort = getMusikFromSetting(Config.AutoResortSound);

        Config.GlobalVolume.addChangedEventListener(changedListener);
        Config.Approach.addChangedEventListener(changedListener);
        Config.GPS_fix.addChangedEventListener(changedListener);
        Config.GPS_lose.addChangedEventListener(changedListener);
        Config.AutoResortSound.addChangedEventListener(changedListener);

        setVolumes();
    }

    public static void setVolumes() {

        // calc volume Global and own
        float GlobalVolume = Config.GlobalVolume.getValue().Volume;

        if (GlobalVolumeSound != null)
            GlobalVolumeSound.setVolume(GlobalVolume);
        if (Approach != null)
            Approach.setVolume(Config.Approach.getValue().Volume * GlobalVolume);
        if (GPS_fix != null)
            GPS_fix.setVolume(Config.GPS_fix.getValue().Volume * GlobalVolume);
        if (GPS_lose != null)
            GPS_lose.setVolume(Config.GPS_lose.getValue().Volume * GlobalVolume);
        if (AutoResort != null)
            AutoResort.setVolume(Config.AutoResortSound.getValue().Volume * GlobalVolume);
    }

    private static IChanged changedListener = new IChanged() {

        @Override
        public void isChanged() {
            setVolumes();
        }

    };

    private static Music getMusikFromSetting(SettingsAudio set) {
        String path = set.getValue().Path;

        FileHandle handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : Gdx.files.internal(path);

        if (handle == null || !handle.exists() || handle.isDirectory() || path.length() == 0) {
            path = set.getDefaultValue().Path;
            handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : Gdx.files.internal(path);
            if (handle != null && handle.exists()) {
                set.loadDefault();
                set.setDirty();
            }
        }

        if (handle == null || !handle.exists()) {
            log.error("LoadSound: " + set.getValue().Path);
            return null;
        }

        Music ret;
        try {
            ret = Gdx.audio.newMusic(handle);
        } catch (Exception e) {
            log.error("LoadSound: " + set.getValue().Path);
            return null;
        }
        return ret;
    }

}
