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
package de.longri.cachebox3.utils.json_parser;

import com.badlogic.gdx.utils.JsonStreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 08.10.2017
 */
public class DraftUploadResultParser {

    private final static Logger log = LoggerFactory.getLogger(DraftUploadResultParser.class);


    public static boolean result(String result) throws UnsupportedEncodingException {

        final AtomicBoolean ret = new AtomicBoolean(false);

        InputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));

        JsonStreamParser parser = new JsonStreamParser() {

            @Override
            public void number(String name, long value, String stringValue) {

                if (name.equals("StatusCode")) {
                    if (value == 0) {
                        log.debug("StatusCode are 0 == OK");
                        ret.set(true);
                    } else {
                        log.debug("StatusCode !=0:  {} ", value);
                        ret.set(false);
                    }
                }
            }
        };
        parser.parse(stream);

        return ret.get();
    }


}
