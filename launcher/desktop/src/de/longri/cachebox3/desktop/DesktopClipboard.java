package de.longri.cachebox3.desktop;

import com.badlogic.gdx.utils.Clipboard;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class DesktopClipboard implements Clipboard, ClipboardOwner {
    @Override
    public String getContents() {
        String result = "";
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                // doh...
            } catch (IOException ex) {
                // doh...
            }
        }
        return result;
    }

    @Override
    public void setContents(String content) {
        StringSelection stringSelection = new StringSelection(content);
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    @Override
    public void lostOwnership(java.awt.datatransfer.Clipboard arg0, Transferable arg1) {
    }
}
