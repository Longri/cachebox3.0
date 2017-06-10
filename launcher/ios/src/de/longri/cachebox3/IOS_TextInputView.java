package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSURL;
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


        
//        final UITextViewDelegate delegate = textView.getDelegate();
//
//        textView.setDelegate(new UITextViewDelegate() {
//            @Override
//            public boolean shouldBeginEditing(UITextView uiTextView) {
//                return delegate.shouldBeginEditing(uiTextView);
//            }
//
//            @Override
//            public boolean shouldEndEditing(UITextView uiTextView) {
//                return delegate.shouldEndEditing(uiTextView);
//            }
//
//            @Override
//            public void didBeginEditing(UITextView uiTextView) {
//                delegate.didBeginEditing(uiTextView);
//            }
//
//            @Override
//            public void didEndEditing(UITextView uiTextView) {
//                delegate.didEndEditing(uiTextView);
//            }
//
//            @Override
//            public boolean shouldChangeCharacters(UITextView uiTextView, NSRange nsRange, String s) {
//                return delegate.shouldChangeCharacters(uiTextView, nsRange, s);
//            }
//
//            @Override
//            public void didChange(UITextView uiTextView) {
//                delegate.didChange(uiTextView);
//                UITextPosition pos = uiTextView.getEndOfDocument();
//                CGRect currentRect = uiTextView.getCaretRect(pos);
//                if (previousRect != CGRect.Zero()) {
//                    if (currentRect.getOrigin().getY() > previousRect.getOrigin().getY()) {
//                        log.debug("Line wrap");
//                    }
//                }
//                previousRect = currentRect;
//            }
//
//            @Override
//            public void didChangeSelection(UITextView uiTextView) {
//                delegate.didChangeSelection(uiTextView);
//            }
//
//            @Override
//            public boolean shouldInteractWithURL(UITextView uiTextView, NSURL nsurl, NSRange nsRange, UITextItemInteraction uiTextItemInteraction) {
//                return delegate.shouldInteractWithURL(uiTextView, nsurl, nsRange, uiTextItemInteraction);
//            }
//
//            @Override
//            public boolean shouldInteractWithTextAttachment(UITextView uiTextView, NSTextAttachment nsTextAttachment, NSRange nsRange, UITextItemInteraction uiTextItemInteraction) {
//                return delegate.shouldInteractWithTextAttachment(uiTextView, nsTextAttachment, nsRange, uiTextItemInteraction);
//            }
//
//            @Override
//            public boolean shouldInteractWithURL(UITextView uiTextView, NSURL nsurl, NSRange nsRange) {
//                return delegate.shouldInteractWithURL(uiTextView, nsurl, nsRange);
//            }
//
//            @Override
//            public boolean shouldInteractWithTextAttachment(UITextView uiTextView, NSTextAttachment nsTextAttachment, NSRange nsRange) {
//                return delegate.shouldInteractWithTextAttachment(uiTextView, nsTextAttachment, nsRange);
//            }
//
//            @Override
//            public void didScroll(UIScrollView uiScrollView) {
//                delegate.didScroll(uiScrollView);
//            }
//
//            @Override
//            public void didZoom(UIScrollView uiScrollView) {
//                delegate.didZoom(uiScrollView);
//            }
//
//            @Override
//            public void willBeginDragging(UIScrollView uiScrollView) {
//                delegate.willBeginDragging(uiScrollView);
//            }
//
//            @Override
//            public void willEndDragging(UIScrollView uiScrollView, CGPoint cgPoint, CGPoint cgPoint1) {
//                delegate.willEndDragging(uiScrollView, cgPoint, cgPoint1);
//            }
//
//            @Override
//            public void didEndDragging(UIScrollView uiScrollView, boolean b) {
//                delegate.didEndDragging(uiScrollView, b);
//            }
//
//            @Override
//            public void willBeginDecelerating(UIScrollView uiScrollView) {
//                delegate.willBeginDecelerating(uiScrollView);
//            }
//
//            @Override
//            public void didEndDecelerating(UIScrollView uiScrollView) {
//                delegate.didEndDecelerating(uiScrollView);
//            }
//
//            @Override
//            public void didEndScrollingAnimation(UIScrollView uiScrollView) {
//                delegate.didEndScrollingAnimation(uiScrollView);
//            }
//
//            @Override
//            public UIView getViewForZooming(UIScrollView uiScrollView) {
//                return delegate.getViewForZooming(uiScrollView);
//            }
//
//            @Override
//            public void willBeginZooming(UIScrollView uiScrollView, UIView uiView) {
//                delegate.willBeginZooming(uiScrollView, uiView);
//            }
//
//            @Override
//            public void didEndZooming(UIScrollView uiScrollView, UIView uiView, double v) {
//                delegate.didEndZooming(uiScrollView, uiView, v);
//            }
//
//            @Override
//            public boolean shouldScrollToTop(UIScrollView uiScrollView) {
//                return delegate.shouldScrollToTop(uiScrollView);
//            }
//
//            @Override
//            public void didScrollToTop(UIScrollView uiScrollView) {
//                delegate.didScrollToTop(uiScrollView);
//            }
//        });

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


}
