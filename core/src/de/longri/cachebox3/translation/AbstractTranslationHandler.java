package de.longri.cachebox3.translation;

import de.longri.cachebox3.translation.word.CompoundCharSequence;

import java.io.IOException;

/**
 * Created by Longri on 27.10.2017.
 */
public abstract class AbstractTranslationHandler {
    public abstract void loadTranslation(String langPath) throws IOException;

    public abstract CompoundCharSequence getTranslation(String stringId, CharSequence... params);

    public abstract CompoundCharSequence getTranslation(int hashCode, CharSequence... params);

    public abstract boolean isInitial();

    public abstract String getLangNameFromFile(String path) throws IOException;
}

