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
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by Longri on 19.03.2019.
 */
public class main {
    public static void main(String[] args) throws Exception {

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

        readIgnoreFile();
        fillSourceFileList();

        for (FileHandle source : sourceFilesToCopy) {
            FileHandle targetFileHandle = libgdxTestSrcDir.child(source.name());
            if (targetFileHandle.exists()) {
                if (!targetFileHandle.delete()) {
                    throw new RuntimeException("Can't generate/(delete) target file:" + source.name());
                }
            }
            targetFileHandle.writeString(generateTestFile(source), false, "utf-8");
        }

    }

    private static Array<String> ignoredDirs = new Array<>();
    private static Array<String> ignoredFiles = new Array<>();
    private static Array<FileHandle> sourceFilesToCopy = new Array<>();
    private static FileHandle junitSrcDir;
    private static FileHandle libgdxTestSrcDir;
    private static final String TEST_SRC_DIR = "./tests/junit_test/src";
    private static final String TEST_TARGET_DIR = "./tests/libgdx_test/src/de/longri/cachebox3/platform_test/tests";
    private static final String TARGET_PACKAGE_LINE = "package de.longri.cachebox3.platform_test.tests;";
    private static final String GENERATE = "\n\n//  Don't modify this file, it's created by tool 'extract_libgdx_test\n\n";
    private static final String IMPORT = "import";
    private static final String JUPITER_TEST = "org.junit.jupiter.api.Test";
    private static final String IMPORT_TEST_ANNOTATION = "import de.longri.cachebox3.platform_test.PlatformAssertionError;\n" +
            "import de.longri.cachebox3.platform_test.Test;";

    private static void readIgnoreFile() {
        FileHandle ignoreFile = junitSrcDir.child("libgdx_test.ignore");
        String[] lines = ignoreFile.readString().split("\n");
        for (String line : lines) {
            line = line.replace("\r", "");

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

    private static String generateTestFile(FileHandle fileHandle) {
        StringBuilder sb = new StringBuilder(GENERATE);
        String source = fileHandle.readString();
        String[] lines = source.split("\n");

        boolean packageReplace = false;
        boolean jupiterTestReplace = false;

        for (String line : lines) {
            line = line.replace("\r", "");

            if (!packageReplace && (line.startsWith("package") || line.contains(" package "))) {
                packageReplace = true;
                sb.appendLine(TARGET_PACKAGE_LINE);
                continue;
            } else if (!jupiterTestReplace && line.startsWith(IMPORT) && line.contains(JUPITER_TEST)) {
                jupiterTestReplace = true;
                sb.appendLine(IMPORT_TEST_ANNOTATION);
                continue;
            }

            sb.appendLine(line);
        }


        return sb.toString();
    }

}
