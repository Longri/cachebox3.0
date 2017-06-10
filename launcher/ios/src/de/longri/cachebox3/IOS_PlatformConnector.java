/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.locator.geocluster.ClusterRunnable;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import org.oscim.backend.canvas.Bitmap;
import org.robovm.apple.avfoundation.AVCaptureDevice;
import org.robovm.apple.avfoundation.AVCaptureTorchMode;
import org.robovm.apple.avfoundation.AVMediaType;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.*;
import org.robovm.objc.block.VoidBlock1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class IOS_PlatformConnector extends PlatformConnector {
    final static Logger log = LoggerFactory.getLogger(IOS_PlatformConnector.class);

    final IOS_Launcher ios_launcher;

    public IOS_PlatformConnector(IOS_Launcher ios_launcher) {
        super();
        this.ios_launcher = ios_launcher;
    }

    @Override
    protected boolean _isTorchAvailable() {
        AVCaptureDevice device = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);
        return device.hasTorch();
    }

    @Override
    protected boolean _isTorchOn() {
        AVCaptureDevice device = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);
        return (device.getTorchMode() == AVCaptureTorchMode.On);
    }

    @Override
    protected void _switchTorch() {
        AVCaptureDevice device = AVCaptureDevice.getDefaultDeviceForMediaType(AVMediaType.Video);

        try {
            device.lockForConfiguration();
            if (device.getTorchMode() == AVCaptureTorchMode.Off) {
                log.debug("Switch torch on");
                device.setTorchMode(AVCaptureTorchMode.On);
            } else {
                log.debug("Switch torch off");
                device.setTorchMode(AVCaptureTorchMode.Off);
            }
        } catch (NSErrorException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Bitmap getRealScaledSVG(String name, InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {

        IOS_RealSvgBitmap bmp = new IOS_RealSvgBitmap(inputStream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
    }


    IOS_LocationListener locationManager;

    @Override
    public void initialLocationReciver() {
        Gdx.app.log("step", "1");
        locationManager = new IOS_LocationListener();
        locationManager.createLocationManager();
    }

    @Override
    protected String _getWorkPath() {
        return _getSandBoxFileHandle("Cachebox3").file().getAbsolutePath();
    }


    @Override
    protected void generateApiKey(GenericCallBack<String> callBack) {
        log.debug("Show WebView for get API key");
        try {
            UIViewController mainViewController = ((IOSApplication) Gdx.app).getUIWindow().getRootViewController();
            GenerateApiKeyWebViewController controller = new GenerateApiKeyWebViewController(callBack, mainViewController);
            ((IOSApplication) Gdx.app).getUIWindow().setRootViewController(controller);
            ((IOSApplication) Gdx.app).getUIWindow().makeKeyAndVisible();
        } catch (Exception e) {
            log.error("show web view", e);
        }
    }

    IOS_DescriptionView descriptionView;

    @Override
    protected void getPlatformDescriptionView(final GenericCallBack<PlatformDescriptionView> callBack) {
        log.debug("Show WebView as descriptionView");
        try {
            if (descriptionView == null) {
                UIViewController mainViewController = ((IOSApplication) Gdx.app).getUIWindow().getRootViewController();
                descriptionView = new IOS_DescriptionView(mainViewController);
            }
            callBack.callBack(descriptionView);

        } catch (Exception e) {
            log.error("show web view", e);
        }

    }

    @Override
    protected void descriptionViewToNull() {
//TODO set descriptionViewToNull
    }

    @Override
    public void openUrlExtern(String link) {
        log.debug("Open URL @Safari: {}", link);
        if (link.startsWith("www.")) {
            link = "http://" + link;
        }
        if (!UIApplication.getSharedApplication().openURL(new NSURL(link))) {
            log.error(Translation.Get("Cann_not_open_cache_browser") + " (" + link + ")");
            CB.viewmanager.toast(Translation.Get("Cann_not_open_cache_browser") + " (" + link + ")");
        }
    }

    @Override
    public FileHandle _getSandBoxFileHandle(String fileName) {
        return new FileHandle(new File(System.getenv("HOME"), "Library/local/" + fileName).getAbsolutePath());
    }

    IOS_TextInputView textInputView;

    @Override
    public void _getMultilineTextInput(final Input.TextInputListener listener, String title, String text, String hint) {

        textInputView = new IOS_TextInputView(((IOSApplication) Gdx.app).getUIWindow()
                .getRootViewController(), text, new IOS_TextInputView.Callback() {
            @Override
            public void okClicked(String text) {
                listener.input(text);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        disposeInputView();
                    }
                });
            }

            @Override
            public void cancelClicked() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        disposeInputView();
                    }
                });
            }
        });


        // buildUIAlertView(listener, title, text, hint);
    }

    private void disposeInputView(){
        textInputView.dispose();
        textInputView=null;
    }

    // Issue 773 indicates this may solve a premature GC issue
    UIAlertViewDelegate delegate;

    /**
     * Builds an {@link UIAlertView} with an added {@link UITextField} for inputting text.
     *
     * @param listener Text input listener
     * @param title    Dialog title
     * @param text     Text for text field
     * @return UiAlertView
     */
    private UIAlertView buildUIAlertView(final Input.TextInputListener listener, String title, String text, String placeholder) {


        UIAlertController allertControler = new UIAlertController(title, "\n\n\n\n\n", UIAlertControllerStyle.Alert);


        allertControler.addTextField(new VoidBlock1<UITextField>() {
            @Override
            public void invoke(UITextField uiTextField) {
                uiTextField.setText("fake");
            }
        });

        CGRect rect = new CGRect(0, 50, 270, 130);
        final UITextView textView = new UITextView(rect);

        textView.setFont(UIFont.getFont("Helvetica", 15));
        textView.setTextColor(UIColor.black());
        textView.setBackgroundColor(UIColor.white());
        textView.getLayer().setBorderColor(UIColor.lightGray().getCGColor());
        textView.getLayer().setBorderWidth(1.0);
        textView.setText(text);
        textView.setUserInteractionEnabled(true);

//        allertControler.setModalPresentationStyle(UIModalPresentationStyle.FullScreen);
        allertControler.getView().addSubview(textView);


        UIAlertAction cancel = new UIAlertAction("Cancel", UIAlertActionStyle.Cancel, null);
        UIAlertAction action = new UIAlertAction("Ok", UIAlertActionStyle.Default, new VoidBlock1<UIAlertAction>() {
            @Override
            public void invoke(UIAlertAction uiAlertAction) {
                listener.input(textView.getText());
                CB.requestRendering();
            }
        });


        allertControler.addAction(cancel);
        allertControler.addAction(action);


//        ((IOSApplication) Gdx.app).getUIWindow().makeKeyAndVisible();


//        NSLayoutConstraint constraint=new NSLayoutConstraint(allertControler.getView(),NSLayoutAttribute.Height,NSLayoutRelation.Equal,null,NSLayoutAttribute.NotAnAttribute,1,10);
//        allertControler.getView().addConstraint(constraint);


        allertControler.setPreferredContentSize(new CGSize(Gdx.graphics.getWidth(), 400));


        ((IOSApplication) Gdx.app).getUIWindow().getRootViewController().presentViewController(allertControler, false, new Runnable() {
            @Override
            public void run() {

            }
        });
//
//        allertControler.setPreferredContentSize(new CGSize(100, 400));

//        CGRect allertRect = new CGRect(15, 50, 240, 100);
//        allertControler.getView().setFrame(allertRect);


//        delegate = new UIAlertViewDelegateAdapter() {
//            @Override
//            public void clicked(UIAlertView view, long clicked) {
//                if (clicked == 0) {
//                    // user clicked "Cancel" button
//                    listener.canceled();
//                } else if (clicked == 1) {
//                    // user clicked "Ok" button
//                    UITextField textField = view.getTextField(0);
//                    listener.input(textField.getText());
//                }
//                delegate = null;
//            }
//
//            @Override
//            public void cancel(UIAlertView view) {
//                listener.canceled();
//                delegate = null;
//            }
//        };
//
//        // build the view
//        final UIAlertView uiAlertView = new UIAlertView();
//        uiAlertView.setTitle(title);
//        uiAlertView.addButton("Cancel");
//        uiAlertView.addButton("Ok");
//        uiAlertView.setAlertViewStyle(UIAlertViewStyle.PlainTextInput);
//        uiAlertView.setDelegate(delegate);
//
//
//        UITextField textField = uiAlertView.getTextField(0);
//        textField.setPlaceholder(placeholder);
//        textField.setText(text);
//
//        uiAlertView.show();
//        CGRect allertRect = new CGRect(15, 50, 240, 500);
//        uiAlertView.setBounds(allertRect);

        return null;
    }
}
