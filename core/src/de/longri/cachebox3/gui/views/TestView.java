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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.GlobalCore;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.widgets.ColorWidget;
import de.longri.cachebox3.sqlite.CacheboxDatabase;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 27.07.16.
 */
public class TestView extends AbstractView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(TestView.class);

    public TestView() {
        super("TestView");
    }

    protected void create() {
        // create a Label with name for default
        nameLabel = new VisLabel(this.name);
        nameLabel.setAlignment(Align.center);
        nameLabel.setPosition(10, 10);
        nameLabel.setWrap(true);

        colorWidget = new ColorWidget(CB.getSkin().getColor("abstract_background"));
        colorWidget.setBounds(0, 0, this.getWidth(), this.getHeight());

        this.addActor(colorWidget);
        this.addActor(nameLabel);
    }


    @Override
    public void reloadState() {


        FileHandle dbFile = PlatformConnector.getSandboxFileHandle("CacheBox/testDB.db");

        dbFile.parent().mkdirs();

        Database db = DatabaseFactory.getNewDatabase("CacheBox/testDB.db", 1, null, null);

        try {
            db.openOrCreateDatabase();
        } catch (SQLiteGdxException e) {
            log.error("Create new File", e);
        }

        CacheboxDatabase cbdb = new CacheboxDatabase(CacheboxDatabase.DatabaseType.CacheBox);

        cbdb.StartUp("CB.DB");
        cbdb.Close();

        StringBuilder sb = new StringBuilder();
        sb.append("Create DB on:");
        sb.append(GlobalCore.br);
        sb.append(dbFile.file().getAbsolutePath());
        sb.append(GlobalCore.br);

        sb.append("File exist: ");
        sb.append(dbFile.exists() ? "yes" : "no");
//            if (dbFile.exists()) {
//                sb.append(GlobalCore.br);
//                sb.append("DB version;" + db.getDatabaseSchemeVersion());
//            }

        nameLabel.setText(sb.toString());


    }

    @Override
    public void saveState() {

    }

    @Override
    public void dispose() {

    }
}
