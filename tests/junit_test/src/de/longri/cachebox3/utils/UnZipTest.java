package de.longri.cachebox3.utils;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 29.03.2018.
 */
class UnZipTest {

    static {
        TestUtils.initialGdx();
    }

    @Test
    void extractFolder() throws IOException, InterruptedException {
        FileHandle zipFile = TestUtils.getResourceFileHandle("testsResources/zip/zipTest.zip", true);
        FileHandle extractedFolder = zipFile.parent().child("zipTest");
        FileHandle test1 = extractedFolder.child("text1.txt");
        FileHandle test2 = extractedFolder.child("text2.txt");

        assertThat("Resource zip file must exist", zipFile.exists());
        if (extractedFolder.exists()) {
            if (extractedFolder.isDirectory()) {
                assertThat("target must delete", extractedFolder.deleteDirectory());
            } else {
                assertThat("target must delete", extractedFolder.delete());
            }
        }

        UnZip unZip = new UnZip(extractedFolder);
        final double[] progress = {0};
        FileHandle resultFileHandle = unZip.extractFolder(zipFile, new GenericCallBack<Double>() {
            @Override
            public void callBack(Double value) {
                progress[0] = value;
            }
        });
        assertThat("extracted folder must exist", resultFileHandle != null && resultFileHandle.exists());
        assertThat("extracted folder path must correct", extractedFolder.path().equals(resultFileHandle.path()));

        assertThat("last progress value must be 100.0", progress[0] == 100.0);

        Thread.sleep(500); //wait for close streams;

        assertThat("content must correct", "Hallo CacheBox!".equals(test1.readString()));
        assertThat("content must correct", "Hello Cachebox 3.0!".equals(test2.readString()));


        assertThat("extracted folder must deleted", resultFileHandle.deleteDirectory());

    }
}