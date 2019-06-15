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
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.ios.backend.IOS_RealSvgBitmap;
import org.robovm.apple.avfoundation.AVCaptureDevice;
import org.robovm.apple.avfoundation.AVCaptureTorchMode;
import org.robovm.apple.avfoundation.AVMediaType;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.*;
import org.robovm.objc.block.VoidBlock1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.robovm.apple.dispatch.DispatchQueue.PRIORITY_BACKGROUND;

/**
 * Created by Longri on 17.07.16.
 */
public class IOS_PlatformConnector extends PlatformConnector {
    final static Logger log = LoggerFactory.getLogger(IOS_PlatformConnector.class);

    final IOS_Launcher_BackgroundHandling ios_launcher;

    public IOS_PlatformConnector(IOS_Launcher_BackgroundHandling ios_launcher) {
        super();
        this.ios_launcher = ios_launcher;
    }

    @Override
    protected String _createThumb(String path, int scaledWidth, String thumbPrefix) {

        String ret[] = new String[1];
        AtomicBoolean WAIT = new AtomicBoolean(true);
        CB.postOnMainThread(new NamedRunnable("create thump") {
            @Override
            public void run() {
                try {
                    String storePath = Utils.getDirectoryName(path) + "/";
                    String storeName = Utils.getFileNameWithoutExtension(path);
                    String storeExt = Utils.getFileExtension(path).toLowerCase();
                    String thumbPath = storePath + thumbPrefix + Utils.THUMB + storeName + "." + storeExt;

                    FileHandle thumbFile = new FileHandle(thumbPath);

                    if (thumbFile.exists()) {
                        ret[0] = path;
                        WAIT.set(false);
                        return;
                    }
                    FileHandle orgFile = new FileHandle(path);

                    if (!orgFile.exists() || orgFile.isDirectory()) {
                        ret[0] = path;
                        WAIT.set(false);
                        return;
                    }
                    NSData data = new NSData(toByteArray(orgFile.read()));
                    CGImage image = new UIImage(data).getCGImage();


                    float scalefactor = (float) scaledWidth / (float) image.getWidth();

                    if (scalefactor >= 1) {
                        // don't need a thumb, return original path
                        ret[0] = path;
                        WAIT.set(false);
                        return;
                    }
                    int newHeight = (int) (image.getHeight() * scalefactor);
                    int newWidth = (int) (image.getWidth() * scalefactor);

                    CGBitmapContext cgBitmapContext = CGBitmapContext.create(newWidth, newHeight, 8, 4 * newWidth,
                            CGColorSpace.createDeviceRGB(), CGImageAlphaInfo.PremultipliedLast);

                    cgBitmapContext.drawImage(new CGRect(0, 0, newWidth, newHeight), image);
                    image.dispose();

                    // store
                    UIImage uiImage = new UIImage(cgBitmapContext.toImage());
                    NSData storeData = uiImage.toPNGData();
                    storeData.write(thumbFile.file(), true);
                    storeData.release();
                    uiImage.dispose();
                    cgBitmapContext.close();

                    ret[0] = thumbFile.file().getAbsolutePath();
                    WAIT.set(false);

                } catch (Exception e) {
                    e.printStackTrace();
                    ret[0] = null;
                    WAIT.set(false);
                }
            }
        });
        CB.wait(WAIT);
        log.debug("ready create thumb");
        return ret[0];
    }

    /**
     * Returns a ByteArray from InputStream
     *
     * @param in InputStream
     * @return
     * @throws IOException
     */
    static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[8192];
        while (in.read(buff) > 0) {
            out.write(buff);
        }
        out.close();
        return out.toByteArray();
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


    @Override
    protected String _getWorkPath() {
        return _getSandBoxFileHandle("Cachebox3").file().getAbsolutePath();
    }


    @Override
    protected void generateApiKey(GenericCallBack<String> callBack) {
        log.debug("show WebView for get API key");
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
        log.debug("show WebView as descriptionView");
        try {
            if (descriptionView == null) {
                UIViewController mainViewController = ((IOSApplication) Gdx.app).getUIWindow().getRootViewController();
                descriptionView = new IOS_DescriptionView(mainViewController);
                log.debug("return new DescriptionView");
            } else {
                log.debug("return existing DescriptionView");
            }
            callBack.callBack(descriptionView);
        } catch (Exception e) {
            log.error("show web view", e);
        }

    }

    @Override
    protected void descriptionViewToNull() {
        log.debug("Set description view to NULL");
        descriptionView.disposing();
        descriptionView = null;
    }

    @Override
    public void openUrlExtern(String link) {
        log.debug("Open URL @Safari: {}", link);
        if (link.startsWith("www.")) {
            link = "http://" + link;
        }
        if (!UIApplication.getSharedApplication().openURL(new NSURL(link))) {
            log.error(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")");
            CB.viewmanager.toast(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")");
        }
    }

    @Override
    public void _callQuit() {
        Gdx.app.exit();
    }

    @Override
    protected void _postOnMainThread(NamedRunnable runnable) {
        DispatchQueue.getMainQueue().async(runnable);
    }


    private long UIBackgroundTaskInvalid = UIApplication.getInvalidBackgroundTask();
    private final AtomicLong bgTask = new AtomicLong(UIBackgroundTaskInvalid);

    @Override
    protected void _runOnBackGround(Runnable backgroundTask) {
        long bgTaskId = ios_launcher.application.beginBackgroundTask("BackgroundTask", new Runnable() {
            @Override
            public void run() {
                log.debug("End BGTask");
                ios_launcher.application.endBackgroundTask(bgTask.get());
            }
        });
        bgTask.set(bgTaskId);
        DispatchQueue.getGlobalQueue(PRIORITY_BACKGROUND, 0).async(backgroundTask);
    }

    @Override
    protected void _playNotifySound(final FileHandle soundFileHandle) {
        DispatchQueue.getMainQueue().sync(new Runnable() {
            @Override
            public void run() {
                IOS_BackgroundSound sound = new IOS_BackgroundSound(soundFileHandle);
                sound.play();
            }
        });
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

    private void disposeInputView() {
        textInputView.dispose();
        textInputView = null;
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
