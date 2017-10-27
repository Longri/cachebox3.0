package de.longri.cachebox3.translation;

import java.io.IOException;

/**
 * Created by Longri on 27.10.2017.
 */
public class Translation {

    // Initial on TranslationLoaderTask with showing Splash
    public static AbstractTranslationHandler translation;

    public static void loadTranslation(String LangPath) throws IOException {
        translation.loadTranslation(LangPath);
    }

    public static CharSequence get(String StringId, String... params) {
        return translation.getTranslation(StringId, params);
    }

    public static CharSequence get(int hashCode, String... params) {
        return translation.getTranslation(hashCode, params);
    }

    public static boolean isInitial() {
        return translation.isInitial();
    }

    public static String getLangNameFromFile(String path) throws IOException {
        return translation.getLangNameFromFile(path);
    }
}
