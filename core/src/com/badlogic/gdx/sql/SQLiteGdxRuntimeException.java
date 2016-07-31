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
package com.badlogic.gdx.sql;

import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Unchecked runtime exception for SQLite used in this extension.
 *
 * @author M Rafay Aleem (2014)-(https://github.com/mrafayaleem/gdx-sqlite)
 * @author Longri (2016)
 */
public class SQLiteGdxRuntimeException extends GdxRuntimeException {
    private static final long serialVersionUID = 5859319081184266132L;

    public SQLiteGdxRuntimeException(String message) {
        super(message);
    }

    public SQLiteGdxRuntimeException(Throwable t) {
        super(t);
    }

    public SQLiteGdxRuntimeException(String message, Throwable t) {
        super(message, t);
    }

}
