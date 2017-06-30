package de.longri.cachebox3.apis.groundspeak_api.json_parser.string_parser;

import com.badlogic.gdx.utils.JsonReader;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 30.06.2017.
 */
public class ApiStatusResultParser {

    public int get(String resultString){
        final AtomicInteger st = new AtomicInteger(-1);
        try {
            (new JsonReader() {
                public void number(String name, long value, String stringValue) {
                    super.number(name, value, stringValue);
                    if (name.equals("StatusCode")) {
                        st.set((int) value);
                    }
                }
            }).parse(resultString);
        } catch (Exception e) {
            LoggerFactory.getLogger(ApiStatusResultParser.class).error("Ask for Api state! Result:{}", resultString);
            e.printStackTrace();
        }
        return st.get();
    }

}
