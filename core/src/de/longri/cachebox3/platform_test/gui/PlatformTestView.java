/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.platform_test.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Longri on 18.03.2019.
 */
public class PlatformTestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(PlatformTestView.class);

    private VisScrollPane scrollPane;
    private ListView testListView;
    private final Array<PlatformTestViewItem> itemArray = new Array<>();

    public PlatformTestView() {
        super("TestUnitView");

        float contentWidth = (Gdx.graphics.getWidth() - 20);
        VisTable contentTable = new VisTable();
        scrollPane = new VisScrollPane(contentTable);
//        contentTable.setDebug(true);
        contentTable.setRound(false);


        // add button for start Unit-test
        VisTextButton button = new VisTextButton("Start Unit Tests");
        button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                //run on new Thread
                CB.postAsync(new NamedRunnable("Platform Unit Test") {
                    @Override
                    public void run() {
                        for (PlatformTestViewItem item : itemArray) {
                            item.setState(PlatformTestViewItem.State.IN_PROGRESS);
                        }

                        PlatformTestViewItem actContainer = null;
                        boolean annyFaildOnContainer = false;

                        for (PlatformTestViewItem item : itemArray) {
                            if (actContainer == null && item.type == PlatformTestViewItem.Type.CONTAINER) {
                                actContainer = item;
                                continue;
                            }

                            if (item.type == PlatformTestViewItem.Type.CONTAINER) {
                                if (annyFaildOnContainer) {
                                    actContainer.setState(PlatformTestViewItem.State.TEST_FAIL);
                                } else {
                                    actContainer.setState(PlatformTestViewItem.State.TEST_OK);
                                }
                                actContainer = item;
                                annyFaildOnContainer = false;
                                continue;
                            }

                            //reflect test method
                            //TODO stop watch
                            //TODO break test after 100sec
                            try {
                                Class refClass = ClassReflection.forName(actContainer.className);
                                Object instance = ClassReflection.newInstance(refClass);

                                Method method = ClassReflection.getMethod(refClass, item.testName);
                                method.setAccessible(true);
                                method.invoke(instance);
                                item.setState(PlatformTestViewItem.State.TEST_OK);
                            } catch (Exception e) {
                                log.error("TestFailed", e);
                                annyFaildOnContainer = true;
                                item.setState(PlatformTestViewItem.State.TEST_FAIL);
                            }


                        }
                        if (annyFaildOnContainer) {
                            actContainer.setState(PlatformTestViewItem.State.TEST_FAIL);
                        } else {
                            actContainer.setState(PlatformTestViewItem.State.TEST_OK);
                        }
                    }
                });
            }
        });

        contentTable.add(button).width(new Value.Fixed(contentWidth)).pad(20);
        contentTable.row();

        testListView = new ListView(ListViewType.VERTICAL, false);
        testListView.setBackground(VisUI.getSkin().get(ActivityBase.ActivityBaseStyle.class).background);
        testListView.setEmptyString("No Unit Test found");
        testListView.setAdapter(null);
        contentTable.add(testListView).width(new Value.Fixed(contentWidth)).height(new Value.Fixed(contentWidth / 2)).pad(4);
        contentTable.row();

        this.addActor(scrollPane);

        fillTestList();
    }

    private void fillTestList() {

        //read generated tests.json

        FileHandle jsnFile = Gdx.files.internal("platform_test/tests.json");

        JsonReader reader = new JsonReader();

        JsonValue result = reader.parse(jsnFile);
        int idx = 0;
        for (Iterator<JsonValue> it = result.iterator().iterator(); it.hasNext(); ) {
            JsonValue jsonValue = it.next();
            log.debug(jsonValue.name);
            itemArray.add(new PlatformTestViewItem(idx++, PlatformTestViewItem.Type.CONTAINER, jsonValue.name));
            for (int i = 0; i < jsonValue.size; i++) {
                JsonValue child = jsonValue.get(i);
                log.debug("   --" + child.name);
                itemArray.add(new PlatformTestViewItem(idx++, PlatformTestViewItem.Type.TEST, jsonValue.name, child.name));
            }


        }

        testListView.setAdapter(new ListViewAdapter() {
            @Override
            public int getCount() {
                return itemArray.size;
            }

            @Override
            public ListViewItem getView(int index) {
                return itemArray.get(index);
            }

            @Override
            public void update(ListViewItem view) {

            }
        });
    }

    @Override
    public boolean hasContextMenu() {
        return false;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Override
    protected void sizeChanged() {
        scrollPane.setBounds(0, 0, this.getWidth(), this.getHeight());
    }


    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }


}
