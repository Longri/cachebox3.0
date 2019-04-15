package travis;

import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;

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

    static {
        try {
            p.load(new FileInputStream(new File("unittest.properties")));
        } catch (IOException e) {
            try {
                p.load(new FileInputStream(new File("tests/unittest.properties")));
            } catch (IOException e1) {
                readFailer = true;
            }
        }
    }

    public static final boolean VALUE = readFailer || p.getProperty("ExcludeOnTravis", "true").equals("true");
    private static final String GCAPI = p.getProperty("GcAPI", DUMMY_API_KEY);

    public static String GcAPI() {
        return GCAPI;
    }

    static {
        //store api to config
        Config.AccessTokenForTest.setValue("A" + GCAPI);
        Config.AccessToken.setValue("A" + GCAPI);
    }
}
