package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by longri on 09.06.17.
 */
public class IOS_TextInputView extends UIView {

    private final static Logger log = LoggerFactory.getLogger(IOS_TextInputView.class);

    private CGRect previousRect = CGRect.Zero();

    public interface Callback {
        void okClicked(String text);

        void cancelClicked();
    }


    UIViewController mainViewController;
    final UIButton okButton, cancelButton;

    public IOS_TextInputView(UIViewController mainViewController, String text, final Callback callback) {
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


        rect = getSize(text);
        scrollView.setContentSize(rect.getSize());
        final UITextView textView = new UITextView(rect);
        textView.setFont(UIFont.getFont("Helvetica", 15));
        textView.setTextColor(UIColor.black());
        textView.setBackgroundColor(UIColor.white());
        textView.getLayer().setBorderColor(UIColor.lightGray().getCGColor());
        textView.getLayer().setBorderWidth(1.0);
        textView.setText(text);
        textView.setScrollEnabled(false);

        textView.setDelegate(new UITextViewDelegateAdapter() {
            @Override
            public void didChange(UITextView uiTextView) {
                CGRect currentRect = getSize(uiTextView.getText());
                if (previousRect != CGRect.Zero()) {
                    if (currentRect != previousRect) {
                        textView.setFrame(currentRect);
                        scrollView.setContentSize(currentRect.getSize());
                    }
                }
                log.debug("Current Size:{}", currentRect);
                previousRect = currentRect;
            }
        });

        scrollView.addSubview(textView);
        addSubview(scrollView);

        rect = new CGRect(0, 0, width, height);
        this.setFrame(rect);

        this.okButton.addOnTouchUpInsideListener(new UIControl.OnTouchUpInsideListener() {
            @Override
            public void onTouchUpInside(UIControl uiControl, UIEvent uiEvent) {
                callback.okClicked(textView.getText());
                IOS_TextInputView.this.removeFromSuperview();
            }
        });

        this.cancelButton.addOnTouchUpInsideListener(new UIControl.OnTouchUpInsideListener() {
            @Override
            public void onTouchUpInside(UIControl uiControl, UIEvent uiEvent) {
                callback.cancelClicked();
                IOS_TextInputView.this.removeFromSuperview();
            }
        });
    }


    private CGRect getSize(String text) {

        NSString nsString = new NSString(text);
        UIFont uiFont = UIFont.getFont("Helvetica", 15);

        NSAttributedStringAttributes attribs = new NSAttributedStringAttributes();
        attribs.setFont(uiFont);
        CGSize bounding = nsString.getSize(attribs);

        double ext = (uiFont.getLineHeight() * 2);

        return new CGRect(0, 0, bounding.getWidth() + ext, bounding.getHeight() + ext);
    }

}
