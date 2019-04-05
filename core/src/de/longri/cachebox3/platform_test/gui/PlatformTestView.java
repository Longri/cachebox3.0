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
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.skin.styles.ButtonDialogStyle;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 18.03.2019.
 */
public class PlatformTestView extends AbstractView {
    final static Logger log = LoggerFactory.getLogger(PlatformTestView.class);

    private VisTable contentTable = new VisTable();
    private ListView testListView;
    private final Array<PlatformTestViewItem> itemArray = new Array<>();
    private final TestButton button;

    public PlatformTestView() {
        super("TestUnitView");

        float contentWidth = (Gdx.graphics.getWidth() - 20);

        fillTestList();

        int count = 0;
        for (PlatformTestViewItem item : itemArray) {
            if (item.testName != null) count++;
        }

        button = new TestButton(count);

        // add button for start Unit-test
        button.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                //run on new Thread
                CB.postAsync(new NamedRunnable("Platform Unit Test") {
                    @Override
                    public void run() {
                        button.beginnTest();
                        boolean anyTestFaild = false;

                        for (PlatformTestViewItem item : itemArray) {
                            item.setState(PlatformTestViewItem.State.IN_PROGRESS);
                        }

                        PlatformTestViewItem actContainer = null;
                        AtomicBoolean annyFaildOnContainer = new AtomicBoolean(false);

                        int count = 0;
                        int countIO = 0;
                        for (PlatformTestViewItem item : itemArray) {
                            item.start();
                            if (item.testName != null) button.setActTestName(item.testName);
                            if (actContainer == null && item.type == PlatformTestViewItem.Type.CONTAINER) {
                                actContainer = item;

                                //maybe call before
                                if (actContainer.beforeAllName != null) {
                                    try {
                                        Class refClass = ClassReflection.forName(actContainer.className);
                                        refClass.getMethod(actContainer.beforeAllName).invoke(null);
                                    } catch (ReflectionException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                                continue;
                            }

                            if (item.type == PlatformTestViewItem.Type.CONTAINER) {
                                if (annyFaildOnContainer.get()) {
                                    actContainer.setState(PlatformTestViewItem.State.TEST_FAIL);
                                    anyTestFaild = true;
                                } else {
                                    actContainer.setState(PlatformTestViewItem.State.TEST_OK);
                                }
                                //maybe call after
                                if (actContainer.afterAllName != null) {
                                    try {
                                        Class refClass = ClassReflection.forName(actContainer.className);
                                        refClass.getMethod(actContainer.afterAllName).invoke(null);
                                    } catch (ReflectionException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                                actContainer.stop();
                                actContainer = item;
                                //maybe call before
                                if (actContainer.beforeAllName != null) {
                                    try {
                                        Class refClass = ClassReflection.forName(actContainer.className);
                                        refClass.getMethod(actContainer.beforeAllName).invoke(null);
                                    } catch (ReflectionException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                                annyFaildOnContainer.set(false);
                                continue;
                            }
                            button.setreadyTestCount(++count);
                            try {
                                Class refClass = ClassReflection.forName(actContainer.className);
                                Object instance = ClassReflection.newInstance(refClass);

                                Method method = ClassReflection.getMethod(refClass, item.testName);
                                method.setAccessible(true);

                                if (item.runOnGL) {
                                    CB.postOnGlThread(new NamedRunnable("PlatformTestOnGL") {
                                        @Override
                                        public void run() {
                                            try {
                                                method.invoke(instance);
                                                item.setState(PlatformTestViewItem.State.TEST_OK);
                                            } catch (ReflectionException e) {
                                                log.error("TestFailed", e);
                                                annyFaildOnContainer.set(true);
                                                item.setState(PlatformTestViewItem.State.TEST_FAIL, printStackTrace(e));
                                            }

                                        }
                                    }, true);
                                } else {
                                    method.invoke(instance);
                                    item.setState(PlatformTestViewItem.State.TEST_OK);
                                }
                                button.setreadyTestCountIO(countIO++);
                            } catch (Exception e) {
                                log.error("TestFailed", e);
                                annyFaildOnContainer.set(true);
                                item.setState(PlatformTestViewItem.State.TEST_FAIL, printStackTrace(e));
                            }
                            item.stop();
                        }
                        actContainer.stop();
                        //maybe call after
                        if (actContainer.afterAllName != null) {
                            try {
                                Class refClass = ClassReflection.forName(actContainer.className);
                                refClass.getMethod(actContainer.afterAllName).invoke(null);
                            } catch (ReflectionException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                        if (annyFaildOnContainer.get()) {
                            actContainer.setState(PlatformTestViewItem.State.TEST_FAIL);
                            anyTestFaild = true;
                        } else {
                            actContainer.setState(PlatformTestViewItem.State.TEST_OK);
                        }
                        button.testFinish(anyTestFaild);
                    }

                });
            }
        });

        contentTable.add(button).width(new Value.Fixed(contentWidth));
        contentTable.row();

        testListView = new ListView(ListViewType.VERTICAL, false);
        testListView.setBackground(VisUI.getSkin().get(ActivityBase.ActivityBaseStyle.class).background);
        testListView.setEmptyString("No Unit Test found");
        testListView.setSelectable(SelectableType.SINGLE);
        testListView.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                PlatformTestViewItem item = (PlatformTestViewItem) testListView.getSelectedItem();
                if (item == null) return;
                final String msg = item.getMsg();
                if (msg == null) return;
                CB.postOnNextGlThread(new NamedRunnable("showMsgBox") {
                    @Override
                    public void run() {
                        if (msg != null) {

                            //post msg with smaller font
                            ButtonDialogStyle buttonDialogStyle = new ButtonDialogStyle(VisUI.getSkin().get("default", ButtonDialogStyle.class));
                            buttonDialogStyle.titleFont = CB.getSkin().getFont("AboutInfo");
                            ButtonDialog dialog = new ButtonDialog("PlatformTestMassageDialog",
                                    ButtonDialog.getMsgContentTable(msg, null, buttonDialogStyle), null,
                                    MessageBoxButtons.OK, null, buttonDialogStyle);
                            dialog.show();
                        }
                    }
                });
                item.setSelected(false);
                testListView.getSelectedItems().clear();
            }
        });
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


        contentTable.add(testListView).width(new Value.Fixed(contentWidth)).expandY().fillY();
        contentTable.row();

        this.addActor(contentTable);
    }

    private String printStackTrace(Throwable t) {
        Throwable printTrowable = null;
        for (Throwable e = t.getCause(); e != null; e = e.getCause()) {
            printTrowable = e;
        }

        if (printTrowable instanceof PlatformAssertionError) {
            StringBuilder sb = new StringBuilder();
            sb.appendLine(printTrowable.getMessage());
            sb.append(" @ ");
            StackTraceElement ele = printTrowable.getStackTrace()[1];
            sb.append(ele.getFileName());
            sb.append(':');
            sb.append(ele.getLineNumber());
            return sb.toString();
        }
        StringBuffer buf = new StringBuffer(500);
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            printTrowable.printStackTrace(pw);

            buf.append(sw.toString()); // stack trace as a string

            sw.close();
            pw.close();
        } catch (Exception e) {
            t.printStackTrace();
        }
        String result = buf.toString();
        if (result.length() > 500) {
            result = result.substring(0, 500);
        }
        return result;
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
            JsonValue beforeNameValue = jsonValue.get("BeforeAllName");
            JsonValue afterNameValue = jsonValue.get("AfterAllName");
            String beforeName = beforeNameValue != null ? beforeNameValue.asString() : null;
            String afterName = afterNameValue != null ? afterNameValue.asString() : null;
            itemArray.add(new PlatformTestViewItem(idx++, PlatformTestViewItem.Type.CONTAINER, jsonValue.name,
                    null, false, beforeName, afterName));
            for (int i = 0; i < jsonValue.size; i++) {
                JsonValue child = jsonValue.get(i);
                if (child.name.equals("BeforeAllName") || child.name.equals("AfterAllName")) continue;
                log.debug("   --" + child.name);
                itemArray.add(new PlatformTestViewItem(idx++, PlatformTestViewItem.Type.TEST, jsonValue.name,
                        child.name, (child.child() != null && child.child().asString().equals("RunOnGL"))));
            }
        }
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
        contentTable.setBounds(0, 0, this.getWidth(), this.getHeight());
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
