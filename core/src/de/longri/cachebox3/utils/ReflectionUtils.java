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
package de.longri.cachebox3.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.platform_test.StyleEntry;
import de.longri.cachebox3.types.CacheTypes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Longri on 28.08.2019.
 */
public class ReflectionUtils {

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader...
     *
     * @param classes the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException if something went wrong
     */
    public static List<Class<?>> getClassesInSamePackage(Class<?>... classes) throws ClassNotFoundException {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();
        if (classes == null || classes.length == 0)
            return result;
        // This will hold a list of directories matching the pckgname. There may be more than one if
        // a package is split over multiple jars/paths
        ArrayList<File> directories = new ArrayList<File>();
        HashMap<File, String> packageNames = null;
        String pckgname = null;
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            for (Class<?> clazz : classes) {
                // TODO: this would not work in a non OSGi environment
                String syspath = getBundlePath(clazz);
                if (syspath.endsWith("/")) {
                    syspath = syspath.substring(0, syspath.length() - 1);
                } else if (syspath.endsWith(".jar")) {
                    getClassesInSamePackageFromJar(result, classes, syspath);
                    continue;
                }
                pckgname = clazz.getPackage().getName();
                String path = pckgname.replace('.', '/');
                // Ask for all resources for the path
                Enumeration<URL> resources = cld.getResources(path);
                File directory = null;
                while (resources.hasMoreElements()) {
                    String path2 = resources.nextElement().getPath();
                    if (!path2.contains(syspath)) {
                        // needed to get it working on Eclipse 3.5
                        if (syspath.indexOf("/bin") < 1) {
                            syspath = syspath + "/bin";
                        }
                        directory = new File(URLDecoder.decode(syspath + path2, "UTF-8"));
                    } else
                        directory = new File(URLDecoder.decode(path2, "UTF-8"));
                    directories.add(directory);
                }
                if (packageNames == null)
                    packageNames = new HashMap<File, String>();
                packageNames.put(directory, pckgname);
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        try {
                            // removes the .class extension
                            Class<?> clazz = Class.forName(packageNames.get(directory) + '.' + file.substring(0, file.length() - 6));
                            if (!Modifier.isAbstract(clazz.getModifiers()))
                                result.add(clazz);
                        } catch (Throwable e) {
                            // ignore exception and continue
                        }
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return result;
    }


    /**
     * Returns the list of classes in the same directories as Classes in <code>classes</code>.
     *
     * @param result
     * @param classes
     * @param jarPath
     */
    private static void getClassesInSamePackageFromJar(List<Class<?>> result, Class<?>[] classes, String jarPath) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
            //ClassLoader cld = Thread.currentThread().getContextClassLoader();
            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                String entryName = entry.getName();
                for (Class<?> clazz : classes) {
                    String packageName = clazz.getPackage().getName().replace('.', '/');
                    if (entryName != null && entryName.endsWith(".class") && entryName.startsWith(packageName)) {
                        try {
                            Class<?> entryClass = Class.forName(entryName.substring(0, entryName.length() - 6).replace('/', '.'));
                            if (entryClass != null)
                                result.add(entryClass);
                        } catch (Throwable e) {
                            // do nothing, just continue processing classes
                        }
                    }
                }
            }
        } catch (Exception e) {
            result.addAll(Arrays.asList(classes));
        } finally {
            try {
                if (jarFile != null)
                    jarFile.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns the location of the
     *
     * @param clazz
     * @return
     */
    public static String getBundlePath(Class<?> clazz) {
        ProtectionDomain pd = clazz.getProtectionDomain();
        if (pd == null)
            return null;
        CodeSource cs = pd.getCodeSource();
        if (cs == null)
            return null;
        URL url = cs.getLocation();
        if (url == null)
            return null;
        String result = url.getFile();
        return result;
    }

    public static void getAllStyleCallers(FileHandle src, Array<StyleEntry> caller) {
        for (FileHandle fileHandle : src.list()) {
            if (fileHandle.isDirectory()) {
                getAllStyleCallers(fileHandle, caller);
            } else {
                if (fileHandle.extension().equals("java")) {
                    // read file and search for call of "VisUI.getSkin().get("
                    String fileStr = fileHandle.readString("UTF-8");
                    int pos = -1;
                    while ((pos = 20 + fileStr.indexOf("VisUI.getSkin().get(", pos)) >= 20) {

                        String styleName = "default";

                        int classSearchPos = pos;
                        boolean defaultName = true;
                        if (fileStr.charAt(pos) == '\"' || fileStr.charAt(pos + 1) == '\"') {
                            int endNamePos = fileStr.indexOf('\"', pos + 2);
                            styleName = fileStr.substring(pos, endNamePos).replace('"', ' ').trim();
                            classSearchPos = endNamePos;
                            defaultName = false;
                        }

                        int classNamePos = defaultName ? pos : 1 + fileStr.indexOf(',', classSearchPos);
                        int classNameEndpos = fileStr.indexOf(".class", classNamePos);
                        String className = fileStr.substring(classNamePos, classNameEndpos).trim();
                        String subClassName = "";
                        int dotPos = className.indexOf('.');
                        if (dotPos >= 0) {
                            subClassName = className.substring(dotPos).replace(".", "$");
                            className = className.substring(0, dotPos);
                        }
                        Class clazz = null;

                        try {
                            clazz = Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            // if class not found

                            // try with own package
                            int packageStartPos = fileStr.indexOf("package") + 8;
                            int packageEndPos = fileStr.indexOf(";", packageStartPos);
                            String packageName = fileStr.substring(packageStartPos, packageEndPos).trim();

                            if (fileStr.contains("class " + className + " ")) {
                                try {
                                    clazz = Class.forName(packageName + "." + fileHandle.nameWithoutExtension() + "$" + className);
                                } catch (ClassNotFoundException ex) {
//                                    ex.printStackTrace();
                                }
                            }

                            if (clazz == null) {
                                try {
                                    clazz = Class.forName(packageName + "." + className);
                                } catch (ClassNotFoundException ex) {
                                    // search import for determine Class with imported package
                                    int importEnd = fileStr.indexOf(className) + className.length();
                                    int importStart = 6 + fileStr.lastIndexOf("import", importEnd);

                                    String classNameWithPackage = (fileStr.substring(importStart, importEnd).trim() + subClassName);
                                    try {
                                        clazz = Class.forName(classNameWithPackage);
                                    } catch (ClassNotFoundException exx) {
//                                        exx.printStackTrace();
                                    }
                                }
                            }
                        }

                        if (clazz != null) {

                            if(clazz.equals(MapWayPointItemStyle.class)){
                                StyleEntry entry = new StyleEntry("mapStar", MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);
                                entry = new StyleEntry("mapFound", MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);
                                entry = new StyleEntry("mapSolved", MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);
                                entry = new StyleEntry("mapMultiStartP", MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);
                                entry = new StyleEntry("mapMysteryStartP", MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);
                                entry = new StyleEntry("mapMultiStageStartP", MapWayPointItemStyle.class);
                                if (!caller.contains(entry, false)) caller.add(entry);

                                for (CacheTypes type : CacheTypes.values()) {
                                    entry = new StyleEntry("map" + type.name(), MapWayPointItemStyle.class);
                                    if (!caller.contains(entry, false)) caller.add(entry);
                                }
                                continue;
                            }



                            StyleEntry entry = new StyleEntry(styleName, clazz);
                            if (!caller.contains(entry, false))
                                caller.add(entry);
                        }
                    }
                }
            }
        }
    }

}
