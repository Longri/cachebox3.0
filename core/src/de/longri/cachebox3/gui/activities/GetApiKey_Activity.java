package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.skin.styles.ApiButtonStyle;
import de.longri.cachebox3.gui.widgets.WebView;

public class GetApiKey_Activity extends Activity {

    private final WebView webView = new WebView();

    public GetApiKey_Activity() {
        //TODO replace icon with own style for GetApiKey_Activity
        super("getApiKey", VisUI.getSkin().get("ApiButton", ApiButtonStyle.class).check);
        defaults().pad(CB.scaledSizes.MARGIN_HALF);
        this.setDebug(true, true);
    }

    @Override
    protected void createMainContent() {

        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                mainContent.invalidateHierarchy();
                mainContent.layout();
            }
        });

        mainContent.add(webView);
        mainContent.setDebug(true, true);
    }

    @Override
    public void layout() {
        super.layout();
        float calculatedContentHeight = this.getHeight();
        float calculatedContentWidth = this.getWidth();
        calculatedContentWidth -= mainContent.defaults().getPadLeft() * 6;
        calculatedContentHeight -= Math.max(imgTitle.getHeight(), lblTitle.getHeight());
        calculatedContentHeight -= btnCancel.getHeight();
        calculatedContentHeight -= mainContent.defaults().getPadTop() * 14;
        webView.setSize(calculatedContentWidth, calculatedContentHeight);
        mainContent.invalidateHierarchy();
        mainContent.layout();
    }

    /**
     * Draws this actor's debug lines if {@link #getDebug()} is true and, regardless of {@link #getDebug()}, calls
     * {@link Actor#drawDebug(ShapeRenderer)} on each child.
     */
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
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
