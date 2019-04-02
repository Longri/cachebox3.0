/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.dialogs;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.NamedRunnable;

/**
 * Created by Longri on 02.09.2017.
 * <p>
 * Static calls of show ButtonDialog
 */
public class MessageBox {

    private final static String NAME = "MessageBox";


    public static void show(CharSequence message) {
        show(message, "", MessageBoxButtons.OK, null, null);
    }

    public static void show(final CharSequence message, final CharSequence title, final MessageBoxButtons buttons, final MessageBoxIcon icon,
                            final OnMsgBoxClickListener clickListener) {

        CB.postOnGlThread(new NamedRunnable("MessageBox") {
            @Override
            public void run() {
                new ButtonDialog(NAME, message, title, buttons, icon, clickListener).show();
            }
        });


    }


}
