package de.longri.cachebox3.translation;

import java.io.IOException;

/**
 * Created by Longri on 27.10.2017.
 */
public abstract class AbstractTranslationHandler {
    public abstract void loadTranslation(String langPath) throws IOException;

    public abstract String getTranslation(String stringId, String... params);

    public abstract String getTranslation(int hashCode, String... params);

    public abstract boolean isInitial();

    public abstract String getLangNameFromFile(String path) throws IOException;
}

