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
    static {
        try {
            p.load(new FileInputStream(new File("unittest.properties")));
        } catch (IOException e) {

        }
    }

    public static final boolean VALUE = p.getProperty("ExcludeOnTravis", "true").equals("true");
}
