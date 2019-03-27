package de.longri.cachebox3.translation;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.translation.word.CompoundCharSequence;

import java.io.IOException;

/**
 * Created by Longri on 27.10.2017.
 */
public abstract class AbstractTranslationHandler {

    protected final FileHandle workPath;
    protected final FileHandle defaultLang;

    protected AbstractTranslationHandler(FileHandle workPath, String defaultLang) {
        this.workPath = workPath;
        this.defaultLang = workPath == null ? null : workPath.child(defaultLang + "/strings.ini");
    }


    public abstract void loadTranslation(String langPath) throws IOException;

    public abstract CompoundCharSequence getTranslation(String stringId, CharSequence... params);

    public abstract CompoundCharSequence getTranslation(int hashCode, CharSequence... params);

    public abstract boolean isInitial();

    public abstract String getLangNameFromFile(String path) throws IOException;
}

