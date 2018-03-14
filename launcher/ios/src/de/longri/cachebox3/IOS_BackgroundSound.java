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
package de.longri.cachebox3;

import com.badlogic.gdx.files.FileHandle;
import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.avfoundation.AVAudioPlayer;
import org.robovm.apple.corefoundation.OSStatusException;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.foundation.NSURL;

/**
 * Created by Longri on 02.03.18.
 */
public class IOS_BackgroundSound {

    AVAudioPlayer player;

    public IOS_BackgroundSound(FileHandle fileHandle) {


        NSURL url = new NSURL(fileHandle.file());

        try {
            int soundId = AudioServices.createSystemSoundID(url);
            AudioServices.playAlertSound(soundId);
        } catch (OSStatusException e) {
            e.printStackTrace();
        }



    }

    public void play() {

    }

}
