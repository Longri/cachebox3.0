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
package de.longri.cachebox3.gui.utils;


import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.DraftEntry;
import de.longri.cachebox3.types.Trackable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Longri on 02.09.2017.
 */
public class TemplateFormatter {
    public static String ReplaceTemplate(String template, DraftEntry fieldNote) {
        template = template.replace("##finds##", String.valueOf(fieldNote.foundNumber));
        return ReplaceTemplate(template, fieldNote.timestamp);
    }

    public static String ReplaceTemplate(String template, Trackable TB) {
        return ReplaceTemplate(template, new Date());
    }

    private static String ReplaceTemplate(String template, Date timestamp) {
        DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
        String stime = iso8601Format.format(timestamp);
        iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
        String sdate = iso8601Format.format(timestamp);

        template = template.replace("<br>", "\n");
        template = template.replace("##date##", sdate);
        template = template.replace("##time##", stime);
        if (EventHandler.getSelectedCache() != null) {
            template = template.replace("##owner##", EventHandler.getSelectedCache().getOwner());
        } else {
            template = template.replace("##owner##", "????????");
        }

        template = template.replace("##gcusername##", Config.GcLogin.getValue());

        return template;
    }

}
