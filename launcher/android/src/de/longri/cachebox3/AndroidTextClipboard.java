package de.longri.cachebox3;

import android.text.ClipboardManager;
import com.badlogic.gdx.utils.Clipboard;

public class AndroidTextClipboard implements Clipboard {
    private String contents;
    private android.text.ClipboardManager clipboardManager;

    public AndroidTextClipboard(ClipboardManager _cm) {
        clipboardManager = _cm;
    }

    @Override
    public String getContents() {
        contents = (String) clipboardManager.getText();
        return contents;
    }

    @Override
    public void setContents(String _contents) {
        contents = _contents;
        clipboardManager.setText(_contents);
    }
}
