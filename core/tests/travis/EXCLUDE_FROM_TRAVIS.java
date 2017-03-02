package travis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Longri on 27.02.2017.
 */
public class EXCLUDE_FROM_TRAVIS {
    static final Properties p = new Properties();
    static boolean readFailer = false;

    static {
        try {
            p.load(new FileInputStream(new File("unittest.properties")));
        } catch (IOException e) {
            readFailer = true;
        }
    }

    public static final boolean VALUE = readFailer || p.getProperty("ExcludeOnTravis", "true").equals("true");
}
