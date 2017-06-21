package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.gamecontroller.GCControllerDirectionPad;
import org.robovm.apple.uikit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public IOS_TextInputView(UIViewController mainViewController, final String text, final Callback callback) {
        this.mainViewController = mainViewController;
        ((IOSApplication) Gdx.app).getUIWindow().addSubview(this);


        this.setBackgroundColor(UIColor.fromRGBA(0.75, 0.75, 0.8, 1.0));

        this.okButton = new UIButton(UIButtonType.System);
        this.cancelButton = new UIButton(UIButtonType.RoundedRect);


        UIColor buttonColor = UIColor.fromRGBA(0.55, 0.55, 0.57, 1.0);

        this.okButton.setBackgroundColor(buttonColor);
        this.cancelButton.setBackgroundColor(buttonColor);

        this.okButton.setTitleColor(UIColor.black(), UIControlState.Normal);
        this.cancelButton.setTitleColor(UIColor.black(), UIControlState.Normal);


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
                CGRect currentRect = setContentSize(textView, scrollView);
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

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setContentSize(textView, scrollView);
            }
        });

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        textView.setSelectedRange(new NSRange(text.length(), 0));
                        textView.becomeFirstResponder();
                    }
                });
            }
        });
    }

    private CGRect setContentSize(UITextView textView, UIScrollView scrollView) {
        CGRect currentRect = getSize(textView.getText());
        if (previousRect != CGRect.Zero()) {
            if (currentRect != previousRect) {
                textView.setFrame(currentRect);
                scrollView.setContentSize(currentRect.getSize());
            }
        }
        return currentRect;
    }


    private CGRect getSize(String text) {

        NSString nsString = new NSString(text);
        UIFont uiFont = UIFont.getFont("Helvetica", 15);

        NSAttributedStringAttributes attribs = new NSAttributedStringAttributes();
        attribs.setFont(uiFont);
        CGSize bounding = nsString.getSize(attribs);

        double ext = (uiFont.getLineHeight() * 2);

        double height = Math.max(bounding.getHeight() + ext, this.getBounds().getHeight() - this.okButton.getBounds().getHeight());
        double width = Math.max(bounding.getWidth() + ext, this.getBounds().getWidth());
        return new CGRect(0, 0, width, height);
    }

}
