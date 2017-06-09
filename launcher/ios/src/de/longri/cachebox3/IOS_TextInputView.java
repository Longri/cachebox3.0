package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.*;

/**
 * Created by longri on 09.06.17.
 */
public class IOS_TextInputView extends UIView {

    UIViewController mainViewController;
    final UIButton okButton, cancelButton;

    public IOS_TextInputView(UIViewController mainViewController, String text) {
        this.mainViewController = mainViewController;
        ((IOSApplication) Gdx.app).getUIWindow().addSubview(this);

        this.setBackgroundColor(UIColor.red());

        this.okButton = new UIButton(UIButtonType.System);
        this.cancelButton = new UIButton(UIButtonType.RoundedRect);

        this.okButton.setBackgroundColor(UIColor.lightGray());
        this.cancelButton.setBackgroundColor(UIColor.lightGray());


        this.okButton.setTitle("ok", UIControlState.Normal);
        this.cancelButton.setTitle("cancel", UIControlState.Normal);

        CGRect bounds = mainViewController.getView().getBounds();
        float width = (float) bounds.getWidth();
        float height = (float) (bounds.getHeight() / 1.8);
        float buttonWidth = 100;
        float buttonHeight = 50;

        CGRect rect = new CGRect(width - buttonWidth, height - buttonHeight, buttonWidth, buttonHeight);
        this.okButton.setFrame(rect);
        rect = new CGRect(0, height - buttonHeight, buttonWidth, buttonHeight);
        this.cancelButton.setFrame(rect);

        addSubview(this.okButton);
        addSubview(this.cancelButton);


        rect = new CGRect(0, 0, width, height - buttonHeight);
        final UIScrollView scrollView = new UIScrollView(rect);
        scrollView.setShowsHorizontalScrollIndicator(true);
        scrollView.setShowsVerticalScrollIndicator(true);
        scrollView.setCanCancelContentTouches(false);
        scrollView.setClipsToBounds(true);
        scrollView.setIndicatorStyle(UIScrollViewIndicatorStyle.White);
        scrollView.setScrollEnabled(true);


        rect = new CGRect(0, 0, width * 2, height - buttonHeight);
        scrollView.setContentSize(rect.getSize());
        final UITextView textView = new UITextView(rect);
        textView.setFont(UIFont.getFont("Helvetica", 15));
        textView.setTextColor(UIColor.black());
        textView.setBackgroundColor(UIColor.white());
        textView.getLayer().setBorderColor(UIColor.lightGray().getCGColor());
        textView.getLayer().setBorderWidth(1.0);
        textView.setText(text);
        textView.setScrollEnabled(false);

        scrollView.addSubview(textView);
        addSubview(scrollView);

        rect = new CGRect(0, 0, width, height);
        this.setFrame(rect);
    }


}
