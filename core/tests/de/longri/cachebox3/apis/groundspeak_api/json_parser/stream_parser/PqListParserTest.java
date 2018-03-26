package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 26.03.2018.
 */
class PqListParserTest {

    @Test
    void parsePqList() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetPqListResult.json");
        PqListParser parser = new PqListParser();

        Array<PocketQuery.PQ> pqList = new Array<>();

        ApiResultState result = parser.parsePqList(stream, pqList);

        assertThat("Result must be ApiResultState.IO", result == ApiResultState.IO);
        assertThat("PQ List count must be 10", pqList.size == 10);
    }
}