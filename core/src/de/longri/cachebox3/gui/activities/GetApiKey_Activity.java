package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.WebView;
import de.longri.cachebox3.translation.Translation;

public class GetApiKey_Activity extends Activity {

    private final WebView webView = new WebView();

    public GetApiKey_Activity(String title, Drawable icon) {
        super(title, icon);
    }

    @Override
    protected void createMainContent() {
        webView.setSize(200, 200);
        mainContent.add(webView);
    }

    @Override
    protected void runAtOk() {
        btnOK.setDisabled(true);
//        callBack(edtResult.getText());
        finish();
    }

//    @Override
//    protected void runAtCancel() {
//        callBack("");
//        super.runAtCancel();
//        finish();
//    }
//
//    public void callBack(String result) {
//    }

}
