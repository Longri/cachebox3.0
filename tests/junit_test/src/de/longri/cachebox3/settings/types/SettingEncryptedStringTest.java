package de.longri.cachebox3.settings.types;

import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.settings.Config;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SettingEncryptedStringTest {


    static {
        TestUtils.initialGdx();
    }

    private static final SettingBool testBool = (SettingBool) Config.settingsList.addSetting(new SettingBool("testBool"
            , SettingCategory.RememberAsk, SettingMode.Normal, false, SettingStoreType.Global,
            SettingUsage.ACB, true));

    private static final SettingsBlob testBlob = (SettingsBlob) Config.settingsList.addSetting(new SettingsBlob("testBlob"
            , SettingCategory.RememberAsk, SettingMode.Normal, SettingStoreType.Global,
            SettingUsage.ACB, false, new byte[]{}));


    @Test
    void desiredTimeTest() throws SQLiteGdxException {

        // call => https://gc-oauth.longri.de/index.php?Version=ACB

        String key = "+m7yOBxXFVJCXv98M1co5DWLN2n3+cKilgHdMhQ=";

        SettingEncryptedString set = new SettingEncryptedString("test", SettingCategory.Internal, SettingMode.Normal, "", SettingStoreType.Local, SettingUsage.ALL);
        set.setEncryptedValue(key);

        String keydecrypted = set.getValue();

        assertEquals(keydecrypted, "AdhBPdTK5ew6Cw0ErHlIVAWPzLrE=");

    }

}
