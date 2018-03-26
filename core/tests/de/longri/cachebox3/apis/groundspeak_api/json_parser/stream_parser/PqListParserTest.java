package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 26.03.2018.
 */
class PqListParserTest {

    @Test
    void parsePqList() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetPqListResult.json");
        PqListParser parser = new PqListParser(null);

        Array<PocketQuery.PQ> pqList = new Array<>();

        ApiResultState result = parser.parsePqList(stream, pqList);

        assertThat("Result must be ApiResultState.IO", result == ApiResultState.IO);
        assertThat("PQ List count must be 6", pqList.size == 6);

        for (int i = 0; i < pqList.size; i++) {
            PocketQuery.PQ pq = pqList.get(i);
            switch (i) {
                case 0:
                    assertThat("wrong value", pq.guid.equals("a1244aab-f585-4648-8dea-f9b979b533be"));
                    assertThat("wrong value", pq.name.equals("Birkenwerder"));
                    assertThat("wrong value", pq.lastGenerated.equals(new Date(1521969736000L)));
                    assertThat("wrong value", pq.downloadAvailable == false);
                    assertThat("wrong value", pq.sizeMB == 1899174);
                    assertThat("wrong value", pq.cacheCount == 1000);
                    break;
                case 1:
                    assertThat("wrong value", pq.guid.equals("85c3888c-d097-4055-b02e-0f182d71fa9e"));
                    assertThat("wrong value", pq.name.equals("H&#246;now"));
                    assertThat("wrong value", pq.lastGenerated.equals(new Date(1521971482000L)));
                    assertThat("wrong value", pq.downloadAvailable == true);
                    assertThat("wrong value", pq.sizeMB == 1753293);
                    assertThat("wrong value", pq.cacheCount == 1000);
                    break;
                case 5:
                    assertThat("wrong value", pq.guid.equals("e6071d91-9425-4828-94f2-543c7fafc899"));
                    assertThat("wrong value", pq.name.equals("Wandlitz"));
                    assertThat("wrong value", pq.lastGenerated.equals(new Date(1522049839000L)));
                    assertThat("wrong value", pq.downloadAvailable == true);
                    assertThat("wrong value", pq.sizeMB == 1847508);
                    assertThat("wrong value", pq.cacheCount == 1000);
            }
        }
    }
}