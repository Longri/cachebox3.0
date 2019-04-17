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
package de.longri.cachebox3;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StringBuilder;
import org.apache.commons.cli.*;

import java.io.StringWriter;

import static java.lang.System.exit;

/**
 * Created by Longri on 19.03.2019.
 */
public class main {

    public static void main(String[] args) throws Exception {

        CommandLine cmd = getCommandLine(args);

        //initial mock Gdx for using FileHandle
        Gdx.app = new HeadlessApplication(new Game() {
            @Override
            public void create() {

            }
        });
        Gdx.files = Gdx.app.getFiles();
        Gdx.net = Gdx.app.getNet();

        junitSrcDir = Gdx.files.absolute(TEST_SRC_DIR);
        libgdxTestSrcDir = Gdx.files.absolute(TEST_TARGET_DIR);

        if (cmd.hasOption("e")) {
            long start = System.currentTimeMillis();
            System.out.println("Start enabling Platform tests !");
            enable();
            long duration = System.currentTimeMillis() - start;
            System.out.println("Platform tests enabled! (" + duration / 1000 + " sec)");
        } else if (cmd.hasOption('d')) {
            long start = System.currentTimeMillis();
            System.out.println("Start disabling Platform tests !");
            disable();
            long duration = System.currentTimeMillis() - start;
            System.out.println("Platform tests disabled! (" + duration / 1000 + " sec)");
        }

        exit(0);
    }

    private static Array<String> ignoredDirs = new Array<>();
    private static Array<String> ignoredFiles = new Array<>();
    private static Array<FileHandle> sourceFilesToCopy = new Array<>();
    private static FileHandle junitSrcDir;
    private static FileHandle libgdxTestSrcDir;
    private static final String ASSET_DIR = "./launcher/android/assets/platform_test/";
    private static final String TEST_JSON = "tests.json";
    private static final String TEST_SRC_DIR = "./tests/junit_test/src";
    private static final String TEST_RESOURCES_DIR = "./tests/junit_test/testsResources";
    private static final String TEST_TARGET_DIR = "./tests/libgdx_test/src/de/longri/cachebox3/platform_test/tests";
    private static final String TARGET_PACKAGE_LINE = "package de.longri.cachebox3.platform_test.tests;";
    private static final String GENERATE = "\n\n//  Don't modify this file, it's created by tool 'extract_libgdx_test\n\n";
    private static final String IMPORT = "import";
    private static final String JUPITER_TEST = "org.junit.jupiter.api.Test";
    private static final String AFTER_ALL = "AfterAll";
    private static final String BEFORE_ALL = "BeforeAll";
    private static final String BEFORE_EACH = "BeforeEach";
    private static final String IMPORT_AFTER_ALL = "import de.longri.cachebox3.platform_test.AfterAll;";
    private static final String IMPORT_BEFORE_ALL = "import de.longri.cachebox3.platform_test.BeforeAll;";
    private static final String IMPORT_BEFORE_EACH = "import de.longri.cachebox3.platform_test.BeforeEach;";

    private static final String IMPORT_TEST_ANNOTATION = "import de.longri.cachebox3.platform_test.PlatformAssertionError;\n" +
            "import de.longri.cachebox3.platform_test.Test;";
    private static final String ASSERT_THAT = "Assert.assertThat;";
    private static final String ASSERT_THAT_LINE = "import static de.longri.cachebox3.platform_test.Assert.assertThat;";
    private static final String ASSERT_EQUALS = "Assertions.assertEquals;";
    private static final String ASSERT_EQUALS_LINE = "import static de.longri.cachebox3.platform_test.Assert.assertEquals;";
    private static final String ASSERT_TRUE = "Assertions.assertTrue;";
    private static final String ASSERT_TRUE_LINE = "import static de.longri.cachebox3.platform_test.Assert.assertTrue;";
    private static final String ASSERT_FALSE = "Assertions.assertFalse;";
    private static final String ASSERT_FALSE_LINE = "import static de.longri.cachebox3.platform_test.Assert.assertFalse;";
    private static final String ASSERT_NOT_NULL = "Assertions.assertNotNull;";
    private static final String ASSERT_NOT_NULL_LINE = "import static de.longri.cachebox3.platform_test.Assert.assertNotNull;";
    private static final String TEST_CACHE_NAME_SPACE = "import de.longri.cachebox3.types.test_caches.";


    private static final String CLASS = "class ";
    private static final String VOID = "void ";
    private static final String PUBLIC = "public ";
    private static final String THROWS = ") throws ";
    private static final String ASSERTATION_ERROR = "PlatformAssertionError";
    private static final String TRAVIS = "travis.EXCLUDE_FROM_TRAVIS";
    private static final String IMPORT_TRAVIS = "import de.longri.cachebox3.platform_test.EXCLUDE_FROM_TRAVIS;";

    private static boolean onlyFlagSet = false;
    private static boolean mustCopyTestCacheNameSpace = false;

    private static void enable() {
        readIgnoreFile();
        fillSourceFileList();

        // create asset dir
        FileHandle assetDir = Gdx.files.absolute(ASSET_DIR);
        FileHandle testJsonFile = assetDir.child(TEST_JSON);

        if (!assetDir.isDirectory()) {
            assetDir.mkdirs();
            if (!assetDir.isDirectory()) {
                throw new RuntimeException("Can't create Asset directory: " + assetDir.file().getAbsolutePath());
            }
        }

        //copy resources
        FileHandle testResourcesDir = Gdx.files.absolute(TEST_RESOURCES_DIR);
        testResourcesDir.copyTo(assetDir);

        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setWriter(writer);
        json.writeObjectStart();

        for (FileHandle source : sourceFilesToCopy) {
            FileHandle targetFileHandle = libgdxTestSrcDir.child(source.name());
            if (targetFileHandle.exists()) {
                if (!targetFileHandle.delete()) {
                    throw new RuntimeException("Can't generate/(delete) target file:" + source.name());
                }
            }
            targetFileHandle.writeString(generateTestFile(source, json), false);
        }
        json.writeObjectEnd();
        String jsonString = stringWriter.toString();
        testJsonFile.writeString(jsonString, false);

        if (mustCopyTestCacheNameSpace) {
            FileHandle testCacheSourceDir = junitSrcDir.child("de/longri/cachebox3/types/test_caches");
            FileHandle testCacheTargetDir = libgdxTestSrcDir.child("../../types");
            testCacheSourceDir.copyTo(testCacheTargetDir);

            // must replace 'Assertions.assertEquals;' on AbstractTestCache
            FileHandle AbstractTestCache_Java = testCacheTargetDir.child("test_caches/AbstractTestCache.java");
            String strAbstractTestCache = AbstractTestCache_Java.readString();
            strAbstractTestCache = strAbstractTestCache.replace("import static org.junit.jupiter.api.Assertions.assertEquals;", "import static de.longri.cachebox3.platform_test.Assert.assertEquals;");
            strAbstractTestCache = strAbstractTestCache.replace("import static org.junit.jupiter.api.Assertions.assertTrue;", "import static de.longri.cachebox3.platform_test.Assert.assertTrue;\n" +
                    "import de.longri.cachebox3.platform_test.PlatformAssertionError;");
            strAbstractTestCache = strAbstractTestCache.replace(" public void assertCache(AbstractCache other, Database database) {", " public void assertCache(AbstractCache other, Database database) throws PlatformAssertionError {");
            strAbstractTestCache = strAbstractTestCache.replace(" private void assertLogs(Database database) {", " private void assertLogs(Database database) throws PlatformAssertionError {");
            strAbstractTestCache = strAbstractTestCache.replace(" private void assetCacheAttributes(AbstractCache abstractCache, Database database) {", "  private void assetCacheAttributes(AbstractCache abstractCache, Database database) throws PlatformAssertionError {");
            strAbstractTestCache = strAbstractTestCache.replace(" private void assertWaypoints(AbstractCache other, Database database) {", " private void assertWaypoints(AbstractCache other, Database database) throws PlatformAssertionError {");
            strAbstractTestCache = strAbstractTestCache.replace(" private boolean fullWaypointEquals(AbstractWaypoint wp1, AbstractWaypoint wp2, Database database) {", " private boolean fullWaypointEquals(AbstractWaypoint wp1, AbstractWaypoint wp2, Database database) throws PlatformAssertionError {");
            strAbstractTestCache = strAbstractTestCache.replace(" protected boolean fullLogEntryEquals(LogEntry log1, LogEntry log2, Database database) {", "  protected boolean fullLogEntryEquals(LogEntry log1, LogEntry log2, Database database) throws PlatformAssertionError {");


            AbstractTestCache_Java.writeString(strAbstractTestCache, false);
        }

    }

    private static void disable() {
        // delete asset dir for Android
        FileHandle assetDir = Gdx.files.absolute(ASSET_DIR);
        assetDir.deleteDirectory();

        // delete asset dir for Desktop
        FileHandle assetDirDesk = Gdx.files.absolute("./launcher/desktop/workingDir/platform_test");
        assetDirDesk.deleteDirectory();


    }

    private static void readIgnoreFile() {
        FileHandle ignoreFile = junitSrcDir.child("libgdx_test.ignore");
        String[] lines = ignoreFile.readString().split("\n");

        onlyFlagSet = false;

        for (String line : lines) {
            line = line.replace("\r", "");

            if (line.isEmpty()) continue;

            if (line.startsWith("only ")) {
                onlyFlagSet = true;
            }

            if (onlyFlagSet && !line.startsWith("only ")) continue;
            if (onlyFlagSet) line = line.replace("only ", "");

            if (line.endsWith("/")) {
                ignoredDirs.add(line.replace(".", "/"));
            } else {
                int cnt = line.lastIndexOf(".");
                String ext = line.substring(cnt);
                line = line.replace(ext, "");
                ignoredFiles.add(line.replace(".", "/") + ext);
            }
        }
    }

    private static void fillSourceFileList() {
        Array<FileHandle> listAll = new Array<>();
        listFile(junitSrcDir, listAll);

        for (FileHandle file : listAll) {
            boolean isIgnored = false;

            if (onlyFlagSet) {
                for (String name : ignoredFiles) {
                    if (file.path().endsWith(name)) {
                        isIgnored = true;
                        break;
                    }
                }
                if (!isIgnored) continue;
                sourceFilesToCopy.add(file);
                continue;
            }


            for (String dir : ignoredDirs) {
                if (file.path().contains(dir)) {
                    isIgnored = true;
                    break;
                }
            }
            if (isIgnored) continue;
            for (String name : ignoredFiles) {
                if (file.path().endsWith(name)) {
                    isIgnored = true;
                    break;
                }
            }
            if (isIgnored) continue;
            sourceFilesToCopy.add(file);
        }

    }

    private static void listFile(FileHandle fileHandle, Array<FileHandle> list) {
        FileHandle[] dir = fileHandle.list();
        for (FileHandle file : dir) {
            if (file.isDirectory()) {
                listFile(file, list);
            } else {
                list.add(file);
            }
        }
    }

    private static String generateTestFile(FileHandle fileHandle, Json json) {
        StringBuilder sb = new StringBuilder(GENERATE);
        String source = fileHandle.readString();
        String[] lines = source.split("\n");

        boolean packageReplace = false;
        boolean travisReplace = false;
        boolean jupiterTestReplace = false;
        boolean assertThatReplace = false;
        boolean assertEqualseReplace = false;
        boolean assertTrueReplace = false;
        boolean assertFalseReplace = false;
        boolean assertNotNullReplace = false;
        boolean publicClassReplace = false;
        boolean fileObjStartWritten = false;
        boolean beforeAllReplace = false;
        boolean beforeEachReplace = false;
        boolean afterReplace = false;

        //search Before/After All methode if exist
        String beforeMethodeName = null;
        String afterMethodeName = null;
        if (source.contains("@AfterAll") || source.contains("@BeforeAll")) {
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                line = line.replace("\r", "");
                if (line.contains("@AfterAll")) {
                    try {
                        int pos = lines[i + 1].indexOf(VOID) + VOID.length();
                        int nameEnd = lines[i + 1].indexOf("(", pos);
                        afterMethodeName = lines[i + 1].substring(pos, nameEnd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (line.contains("@BeforeAll")) {
                    try {
                        int pos = lines[i + 1].indexOf(VOID) + VOID.length();
                        int nameEnd = lines[i + 1].indexOf("(", pos);
                        beforeMethodeName = lines[i + 1].substring(pos, nameEnd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            line = line.replace("\r", "");

            if (!mustCopyTestCacheNameSpace && line.contains(TEST_CACHE_NAME_SPACE))
                mustCopyTestCacheNameSpace = true;

            if (!packageReplace && (line.startsWith("package") || line.contains(" package "))) {
                packageReplace = true;

                sb.appendLine(TARGET_PACKAGE_LINE);

                //import package if not empty
                String packageName = line.replace("package", "").replace(";", "").trim();
                try {
                    Class[] list = ReflectionHelper.getClasses(packageName);

                    if (list.length > 0) {
                        sb.appendLine("");
                        sb.append("import ");
                        sb.appendLine(packageName + ".*;");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            } else if (!beforeAllReplace && line.startsWith(IMPORT) && line.contains(BEFORE_ALL)) {
                beforeAllReplace = true;
                sb.appendLine(IMPORT_BEFORE_ALL);
                continue;
            } else if (!beforeEachReplace && line.startsWith(IMPORT) && line.contains(BEFORE_EACH)) {
                beforeEachReplace = true;
                sb.appendLine(IMPORT_BEFORE_EACH);
                continue;
            } else if (!afterReplace && line.startsWith(IMPORT) && line.contains(AFTER_ALL)) {
                afterReplace = true;
                sb.appendLine(IMPORT_AFTER_ALL);
                continue;
            } else if (!jupiterTestReplace && line.startsWith(IMPORT) && line.contains(JUPITER_TEST)) {
                jupiterTestReplace = true;
                sb.appendLine(IMPORT_TEST_ANNOTATION);
                continue;
            } else if (!travisReplace && line.startsWith(IMPORT) && line.contains(TRAVIS)) {
                travisReplace = true;
                sb.appendLine(IMPORT_TRAVIS);
                continue;
            } else if (!assertEqualseReplace && line.endsWith(ASSERT_EQUALS)) {
                assertEqualseReplace = true;
                sb.appendLine(ASSERT_EQUALS_LINE);
                continue;
            } else if (!assertThatReplace && line.endsWith(ASSERT_THAT)) {
                assertThatReplace = true;
                sb.appendLine(ASSERT_THAT_LINE);
                continue;
            } else if (!assertTrueReplace && line.endsWith(ASSERT_TRUE)) {
                assertTrueReplace = true;
                sb.appendLine(ASSERT_TRUE_LINE);
                continue;
            } else if (!assertFalseReplace && line.endsWith(ASSERT_FALSE)) {
                assertFalseReplace = true;
                sb.appendLine(ASSERT_FALSE_LINE);
                continue;
            } else if (!assertNotNullReplace && line.endsWith(ASSERT_NOT_NULL)) {
                assertNotNullReplace = true;
                sb.appendLine(ASSERT_NOT_NULL_LINE);
                continue;
            } else if (!publicClassReplace && line.contains(CLASS)) {
                publicClassReplace = true;
                //maybe class is public
                if (line.contains(PUBLIC + CLASS)) {
                    sb.appendLine(line);
                    continue;
                }
                sb.appendLine(line.replace(CLASS, PUBLIC + CLASS));
                continue;
            }

            if (line.contains(VOID)) {
                //check public have @Test annotation
                if (lines[i - 1].contains("@Test")) {
                    if (!line.contains(PUBLIC + VOID)) {
                        line = line.replace(VOID, PUBLIC + VOID);
                    }

                    // write test to json
                    int pos = line.indexOf(VOID) + VOID.length();
                    int nameEnd = line.indexOf("(", pos);
                    String methodName = line.substring(pos, nameEnd);

                    if (!fileObjStartWritten) {
                        json.writeObjectStart("de.longri.cachebox3.platform_test.tests." + fileHandle.nameWithoutExtension());

                        //write Before/After methodeNames
                        if (beforeMethodeName != null)
                            json.writeValue("BeforeAllName", beforeMethodeName);
                        if (afterMethodeName != null)
                            json.writeValue("AfterAllName", afterMethodeName);

                        fileObjStartWritten = true;
                    }

                    json.writeObjectStart(methodName);
                    if (lines[i - 1].contains("@RunOnGL")) {
                        json.writeValue("@", "RunOnGL");
                    }
                    json.writeObjectEnd();
                }


                // add throws, if method contains any assertion call
                boolean hasAssertCall = false;
                int braceCnt = 1;
                for (int j = i + 1; j < lines.length; j++) {
                    if (lines[j].contains("assertThat(") ||
                            lines[j].contains("assertEquals(") ||
                            lines[j].contains("assertTrue(") ||
                            lines[j].contains("assertFalse(") ||
                            lines[j].contains("assertNotNull(") ||
                            lines[j].contains("assertRecursiveDir(") ||
                            lines[j].contains("assertCache(") ||
                            lines[j].contains("assertAbstractViewSerialation(")) {
                        hasAssertCall = true;
                        break;
                    }
                    if (lines[j].contains("{")) braceCnt++;
                    if (lines[j].contains("}")) braceCnt--;
                    if (braceCnt == 0) {
                        break;
                    }
                }

                if (hasAssertCall) {
                    if (line.contains(THROWS)) {
                        line = line.replace(" {", ", " + ASSERTATION_ERROR + " {");
                    } else {
                        line = line.replace(")", THROWS + ASSERTATION_ERROR);
                    }
                }

                sb.appendLine(line);
                continue;
            }

            sb.appendLine(line);
        }

        if (fileObjStartWritten) json.writeObjectEnd();

        return sb.toString();
    }

    private static CommandLine getCommandLine(String[] args) {
        Options options = new Options();

        Option enable = new Option("e", "enable", false, "enable Platform Test");
        enable.setRequired(false);
        options.addOption(enable);

        Option disable = new Option("d", "disable", false, "disable Platform Test");
        disable.setRequired(false);
        options.addOption(disable);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Extract Platform Test need one of two options 'enable' or 'disable' !", options);

            System.exit(1);
            return null;
        }

        if (cmd.getOptions() == null || cmd.getOptions().length == 0) {
            formatter.printHelp("Extract Platform Test need one of two options," +
                    "\n 'enable' or 'disable' !\n\n", options);

            System.exit(1);
            return null;
        }

        return cmd;
    }
}
