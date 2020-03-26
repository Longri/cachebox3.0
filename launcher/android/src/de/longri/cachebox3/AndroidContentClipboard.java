package de.longri.cachebox3;

import android.content.ClipData;
import android.content.ClipboardManager;
import com.badlogic.gdx.utils.Clipboard;

public class AndroidContentClipboard implements Clipboard {
    private final android.content.ClipboardManager clipboardManager;
    private String contents;

    public AndroidContentClipboard(ClipboardManager _clipboardManager) {
        clipboardManager = _clipboardManager;
    }

    @Override
    public String getContents() {
        contents = "";
        if (clipboardManager.hasPrimaryClip()) {
            ClipData cd = clipboardManager.getPrimaryClip();
            if (cd.getItemCount() > 0) {
                CharSequence cs = cd.getItemAt(0).getText();
                if (cs != null)
                    contents = cs.toString();
                else {
                    // maybe it contains a URI
                    // resolveUri(cd.getItemAt(0).getUri())
                }
            }
        }
        return contents;
    }

    @Override
    public void setContents(String contents) {
        this.contents = contents;
        ClipData cd = ClipData.newPlainText("", contents);
        clipboardManager.setPrimaryClip(cd);
    }

}
