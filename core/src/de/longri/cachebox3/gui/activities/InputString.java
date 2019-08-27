package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;

public class InputString extends Activity {
    CB_Label lblResult;
    private EditTextField edtResult;
    private String title;

    public InputString(String title, Drawable icon) {
        super(title, icon);
        lblResult = new CB_Label(Translation.get(title));
        edtResult = new EditTextField(true, "");
    }

    @Override
    protected Catch_Table createMainContent() {
        mainContent.addLast(lblResult);
        mainContent.addLast(edtResult);
        return mainContent;
    }

    @Override
    protected void runAtOk() {
        btnOK.setDisabled(true);
        callBack(edtResult.getText());
        finish();
    }

    @Override
    protected void runAtCancel() {
        callBack("");
        finish();
    }

    public void callBack(String result) {
    }

}
