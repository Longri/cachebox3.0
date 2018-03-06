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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.SettingsAudio;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundCache {
    final static Logger log = LoggerFactory.getLogger(SoundCache.class);

    public enum Sounds {
        GPS_lose, GPS_fix, Approach, AutoResortSound, Global
    }

    private static Sound GlobalVolumeSound;
    private static Sound GPS_lose;
    private static Sound GPS_fix;
    private static Sound Approach;
    private static Sound AutoResort;
    private static float globalVolume;
    private static float approachVolume;
    private static float gpsFixVolume;
    private static float gpsLoseVolume;
    private static float autoResortVolume;


    public static void play(Sounds sound) {
        play(sound, false);
    }

    public static void play(Sounds sound, boolean ignoreMute) {

        //if ignoreMute, the command comes from the settings, so let's turn the volume up again!
        if (ignoreMute) setVolumes();

        log.debug("play Sound: {} ", sound.name());

        if (Config.GlobalVolume.getValue().Mute && !ignoreMute) {
            log.debug("Global volume is muted! Don't play sound");
            return;
        }


        switch (sound) {
            case GPS_lose:
                if (GPS_lose == null)
                    log.warn("Sound {} not loaded, can't play", sound.name());

                if ((ignoreMute || !Config.GPS_lose.getValue().Mute) && GPS_lose != null) {
                    GPS_lose.stop();
                    GPS_lose.play(gpsLoseVolume);
                }
                break;
            case GPS_fix:
                if (GPS_fix == null)
                    log.warn("Sound {} not loaded, can't play", sound.name());

                if ((ignoreMute || !Config.GPS_fix.getValue().Mute) && GPS_fix != null) {
                    GPS_fix.stop();
                    GPS_fix.play(gpsFixVolume);
                }
                break;
            case Approach:
                if (Approach == null)
                    log.warn("Sound {} not loaded, can't play", sound.name());

                if ((ignoreMute || !Config.Approach.getValue().Mute) && Approach != null) {
                    Approach.stop();
                    Approach.play(approachVolume);
                } else {
                    log.debug("Approach sound not played while {} ",
                            Approach == null ? "Approach sound are NULL" : "Approach sound are muted");
                }
                break;
            case AutoResortSound:
                if (AutoResort == null)
                    log.warn("Sound {} not loaded, can't play", sound.name());

                if ((ignoreMute || !Config.AutoResortSound.getValue().Mute) && AutoResort != null) {
                    AutoResort.stop();
                    AutoResort.play(autoResortVolume);
                }
                break;
            case Global:
                if (GlobalVolumeSound == null)
                    log.warn("Sound {} not loaded, can't play", sound.name());

                if ((ignoreMute || !Config.GlobalVolume.getValue().Mute) && GlobalVolumeSound != null) {
                    GlobalVolumeSound.stop();
                    GlobalVolumeSound.play(globalVolume);
                }
                break;
        }
    }

    public static void loadSounds() {

        log.debug("Load Sounds");

        //on iOS we must copy musik files to tmp folder
        if (CanvasAdapter.platform == Platform.IOS) {
            FileHandle dataFileHandle = Gdx.files.absolute(CB.WorkPath + "/data");
            dataFileHandle.mkdirs();
            FileHandle soundFolder = Gdx.files.internal("sound");
            soundFolder.copyTo(dataFileHandle);
        }


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

        log.debug("set Volumes");

        globalVolume = Config.GlobalVolume.getValue().Volume;
        approachVolume = Config.Approach.getValue().Volume * globalVolume;
        gpsFixVolume = Config.GPS_fix.getValue().Volume * globalVolume;
        gpsLoseVolume = Config.GPS_lose.getValue().Volume * globalVolume;
        autoResortVolume = Config.AutoResortSound.getValue().Volume * globalVolume;

    }

    private static IChanged changedListener = new IChanged() {

        @Override
        public void isChanged() {
            setVolumes();
        }

    };

    private static Sound getMusikFromSetting(SettingsAudio set) {
        String path = set.getValue().Path;
        FileHandle handle;
        if (CanvasAdapter.platform == Platform.IOS) {
            handle = Gdx.files.absolute(CB.WorkPath + "/data/" + path);
        } else {
            handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : Gdx.files.internal(path);
        }

        if (handle == null || !handle.exists() || handle.isDirectory() || path.length() == 0) {
            path = set.getDefaultValue().Path;
            handle = set.getValue().Class_Absolute ? Gdx.files.absolute(path) : Gdx.files.internal(path);
            if (handle != null && handle.exists()) {
                set.loadDefault();
            }
        }

        if (handle == null || !handle.exists()) {
            log.error("LoadSound: " + set.getValue().Path);
            return null;
        }


        Sound ret;
        try {
            ret = Gdx.audio.newSound(handle);
        } catch (Exception e) {
            log.error("LoadSound: " + set.getValue().Path, e);
            return null;
        }
        return ret;
    }

}
