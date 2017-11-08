package travis;

import de.longri.cachebox3.locator.Coordinate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Longri on 27.02.2017.
 */
public class EXCLUDE_FROM_TRAVIS {
    static final Properties p = new Properties();
    public static final boolean REPAIR = true;
    static boolean readFailer = false;
    public static final String DUMMY_API_KEY = "+DummyKEY";
    public static final Coordinate LONGRI_HOME_COORDS = new Coordinate(52.581892, 13.398128);

    static {
        try {
            p.load(new FileInputStream(new File("unittest.properties")));
        } catch (IOException e) {
            try {
                p.load(new FileInputStream(new File("core/unittest.properties")));
            } catch (IOException e1) {
                readFailer = true;
            }
        }
    }

    public static final boolean VALUE = readFailer || p.getProperty("ExcludeOnTravis", "true").equals("true");
    public static final String GcAPI = p.getProperty("GcAPI", DUMMY_API_KEY);
}
