package de.longri.cachebox3.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 27.06.2017.
 */
class DownloaderTest {

    static {
        TestUtils.initialGdx();
    }

    @Test
    void run() throws MalformedURLException {

        FileHandle testFolder = Gdx.files.local("TestFileDownloader");
        testFolder.mkdirs();
        FileHandle testFile = testFolder.child("test.jpg");
        if (testFile.exists())
            testFile.delete();

        URL url = new URL("http://imgcdn.geocaching.com/cache/43b4dc3a-bb2d-41f1-a142-4f312dbb913a.jpg?rnd=0.9582919");
        Downloader downloader = new Downloader(url, testFile);
        downloader.run();
        assertTrue(testFile.exists());
        assertTrue(testFile.length() > 0);

        testFolder.deleteDirectory();

    }

}