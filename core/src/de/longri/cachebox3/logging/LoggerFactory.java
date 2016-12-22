package de.longri.cachebox3.logging;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.HashSet;

/**
 * Created by Longri on 22.12.16.
 */
public class LoggerFactory {


    static final HashSet<String> EXCLUDE_LIST = new HashSet<String>();
    static final HashSet<String> INCLUDE_LIST = new HashSet<String>();


    private static final ObjectMap<String, Logger> LOGGER_LIST = new ObjectMap<String, Logger>();
    private static final Logger EMPTY_LOGGER = new EmptyLogger();

    static {
        if (!LoggerSettings.isInit) LoggerSettings.init();
    }


    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {

        // return empty logger if the name on disable list
        if (!INCLUDE_LIST.isEmpty()) {
            if (!INCLUDE_LIST.contains(name)) return EMPTY_LOGGER;
        }

        if (!EXCLUDE_LIST.isEmpty()) {
            if (EXCLUDE_LIST.contains(name)) return EMPTY_LOGGER;
        }

        Logger simpleLogger = LOGGER_LIST.get(name);
        if (simpleLogger != null) {
            return simpleLogger;
        } else {
            Logger newInstance = new GdxLogger(name);
            Logger oldInstance = LOGGER_LIST.put(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
}
