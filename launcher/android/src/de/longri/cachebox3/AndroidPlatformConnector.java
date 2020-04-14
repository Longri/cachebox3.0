/*
 * Copyright (C) 2016 - 2020 team-cachebox.de
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


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.track.TrackRecorder;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.android.canvas.AndroidRealSvgBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Intent.ACTION_VIEW;
import static android.os.Build.VERSION_CODES.N;

/**
 * Created by Longri on 17.07.16.
 * todo recVoice(): create microphoneView;
 */
public class AndroidPlatformConnector extends PlatformConnector {
    final static Logger log = LoggerFactory.getLogger(AndroidPlatformConnector.class);
    private static final int REQUEST_CODE_GET_API_KEY = 987;
    private static final int REQUEST_CAPTURE_IMAGE = 6516;
    private static final int REQUEST_CAPTURE_VIDEO = 6517;
    private static AndroidPlatformConnector androidPlatformConnector;
    private static boolean isVoiceRecordingStarted = false;
    private final AndroidLauncherfragment application;
    private final Handler handle;
    private final Context context;
    private final AndroidFlashLight flashLight;
    public GenericCallBack<String> callBack;
    private AndroidWebView descriptionView;
    private String mediaFileNameWithoutExtension;
    private String tempMediaPath;
    private Uri videoUri;
    private String recordingStartTime;
    private Coordinate recordingStartCoordinate;
    private AndroidEventListener handlingTakePhoto, handlingRecordedVideo;
    private ExtAudioRecorder extAudioRecorder;

    public AndroidPlatformConnector(AndroidLauncherfragment app) {
        application = app;
        context = app.getContext();
        handle = new Handler();
        flashLight = new AndroidFlashLight(context);
        platformConnector = this;
        androidPlatformConnector = this;
        handlingTakePhoto = (requestCode, resultCode, data) -> {
            // application.removeAndroidEventListener(handlingTakePhoto);
            // Intent Result Take Photo
            if (requestCode == REQUEST_CAPTURE_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    log.info("Photo taken");
                    try {
                        // move the photo from temp to UserImageFolder
                        String sourceName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
                        String destinationName = Config.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + ".jpg";
                        if (!sourceName.equals(destinationName)) {
                            FileHandle source = new FileHandle(sourceName);
                            FileHandle destination = new FileHandle(destinationName);
                            if (!source.file().renameTo(destination.file())) {
                                log.error("move from " + sourceName + " to " + destinationName + " failed");
                            }
                        }

                        // todo
                        /*
                                        // for the photo to show within spoilers
                                        if (EventHandler.isSetSelectedCache()) {
                                            EventHandler.getSelectedCache().loadSpoilerRessources();
                                            SpoilerView.getInstance().ForceReload();
                                        }

                                        ViewManager.that.reloadSprites(false);

                         */

                        // track annotation
                        String TrackFolder = Config.TrackFolder.getValue();
                        String relativPath = Utils.getRelativePath(Config.UserImageFolder.getValue(), TrackFolder, "/");
                        Coordinate lastLocation = EventHandler.getMyPosition(); // Locator.getInstance().getLastSavedFineLocation();
                        if (lastLocation == null) {
                            /*
                            lastLocation = Locator.getInstance().Location(Location.ProviderType.any);
                            if (lastLocation == null) {
                                log.info("No (GPS)-r TrackrecordingLocation for.");
                                return;
                            }
                             */
                            log.info("No (GPS)-r Trackrecording Location for photo.");
                            return;
                        }

                        // Da ein Foto eine Momentaufnahme ist, kann hier die Zeit und die Koordinaten nach der Aufnahme verwendet werden.
                        TrackRecorder.getInstance().annotateMedia(mediaFileNameWithoutExtension + ".jpg",
                                relativPath + "/" + mediaFileNameWithoutExtension + ".jpg",
                                lastLocation,
                                getTrackDateTimeString());

                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                } else {
                    log.error("Intent Take Photo resultCode: " + resultCode);
                }
            }
        };
        handlingRecordedVideo = (requestCode, resultCode, data) -> {
            // application.removeAndroidEventListener(handlingRecordedVideo);
            // Intent Result Record Video
            if (requestCode == REQUEST_CAPTURE_VIDEO) {
                if (resultCode == Activity.RESULT_OK) {
                    log.info("Video recorded.");
                    String ext;
                    try {
                        // move Video from temp (recordedVideoFilePath) in UserImageFolder and rename
                        String recordedVideoFilePath = "";
                        // first get the tempfile pathAndName (recordedVideoFilePath)
                        String[] proj = {MediaStore.Images.Media.DATA}; // want to get Path to the file on disk.

                        Cursor cursor = application.getActivity().getContentResolver().query(videoUri, proj, null, null, null); // result set
                        if (cursor != null && cursor.getCount() != 0) {
                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); // my meaning: if only one element index is 0
                            cursor.moveToFirst(); // first row ( here we should have only one row )
                            recordedVideoFilePath = cursor.getString(columnIndex);
                        }
                        if (cursor != null) {
                            cursor.close();
                        }

                        if (recordedVideoFilePath.length() > 0) {
                            ext = Utils.getFileExtension(recordedVideoFilePath);
                            FileHandle source = new FileHandle(recordedVideoFilePath);
                            String destinationName = Config.UserImageFolder.getValue() + "/" + mediaFileNameWithoutExtension + "." + ext;
                            FileHandle destination = new FileHandle(destinationName);
                            if (!source.file().renameTo(destination.file())) {
                                log.error("move from " + recordedVideoFilePath + " to " + destinationName + " failed");
                            } else {
                                log.info("Video saved at " + destinationName);

                                // track annotation
                                String TrackFolder = Config.TrackFolder.getValue();
                                String relativPath = Utils.getRelativePath(Config.UserImageFolder.getValue(), TrackFolder, "/");
                                TrackRecorder.getInstance().annotateMedia(mediaFileNameWithoutExtension + "." + ext,
                                        relativPath + "/" + mediaFileNameWithoutExtension + "." + ext,
                                        recordingStartCoordinate, recordingStartTime);


                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage());
                    }
                } else {
                    log.error("Intent Record Video resultCode: " + resultCode);
                }
            }
        };
    }

    public static AndroidPlatformConnector getInstance(AndroidLauncherfragment app) {
        if (androidPlatformConnector == null) androidPlatformConnector = new AndroidPlatformConnector(app);
        return androidPlatformConnector;
    }

    @Override
    protected String _createThumb(String path, int scaledWidth, String thumbPrefix) {
        String storePath = Utils.getDirectoryName(path) + "/";
        String storeName = Utils.getFileNameWithoutExtension(path);
        String storeExt = Utils.getFileExtension(path).toLowerCase();
        String ThumbPath = storePath + thumbPrefix + Utils.THUMB + storeName + "." + storeExt;

        java.io.File ThumbFile = new java.io.File(ThumbPath);

        if (ThumbFile.exists())
            return ThumbPath;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if (BitmapFactory.decodeFile(path, options) == null) {
            // seems as if decodeFile always returns null (independant from success)
            // todo delete a bad original file (Path)
            // return null;
            // will now perhaps produce bad thumbs
        }

        int oriWidth = options.outWidth;
        int oriHeight = options.outHeight;
        float scalefactor = (float) scaledWidth / (float) oriWidth;

        if (scalefactor >= 1)
            return path; // don't need a thumb, return original path

        int newHeight = (int) (oriHeight * scalefactor);
        int newWidth = (int) (oriWidth * scalefactor);

        final int REQUIRED_WIDTH = newWidth;
        final int REQUIRED_HIGHT = newHeight;
        //Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (oriWidth / scale / 2 >= REQUIRED_WIDTH && oriHeight / scale / 2 >= REQUIRED_HIGHT)
            scale *= 2;

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        android.graphics.Bitmap resized = null;
        try {
            resized = BitmapFactory.decodeStream(new FileInputStream(path), null, o2);
        } catch (FileNotFoundException e1) {

            e1.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(ThumbPath);
            android.graphics.Bitmap.CompressFormat format = android.graphics.Bitmap.CompressFormat.PNG;

            if (storeExt.equals("jpg"))
                format = android.graphics.Bitmap.CompressFormat.JPEG;

            if (out == null || format == null || resized == null) {
                return null;
            }
            resized.compress(format, 80, out);

            resized.recycle();

            return ThumbPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected boolean _isTorchAvailable() {
        return flashLight.available();
    }

    @Override
    protected boolean _isTorchOn() {
        return flashLight.isOn();
    }

    @Override
    protected void _switchTorch() {
        if (flashLight.available()) {
            if (flashLight.isOn()) {
                //switch off
                flashLight.switchOff();
            } else {
                //switch on
                flashLight.switchOn();
            }
        }
    }

    @Override
    public Bitmap getRealScaledSVG(String name, InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {

        AndroidRealSvgBitmap bmp = new AndroidRealSvgBitmap(inputStream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
    }

    @Override
    public FileHandle _getSandBoxFileHandle(String fileName) {
        File dir = this.application.getContext().getFilesDir();
        File file = new File(dir, fileName);
        return Gdx.files.absolute(file.getAbsolutePath());
    }

    @Override
    protected String _getWorkPath() {
        // Internal SD Card
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cachebox3";
    }

    @Override
    protected void getPlatformDescriptionView(final GenericCallBack<PlatformWebView> callBack) {

        this.application.runOnUiThread(() -> {
            if (descriptionView == null)
                descriptionView = new AndroidWebView(AndroidPlatformConnector.this.application.getContext());
            callBack.callBack(descriptionView);

        });
    }

    @Override
    protected void descriptionViewToNull() {
        descriptionView = null;
    }

    @Override
    public void openUrlExtern(String link) {
        try {
            link = link.trim();
            if (link.startsWith("www.")) {
                link = "http://" + link;
            }
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setDataAndType(uri, "text/html");
            if (intent.resolveActivity(application.getContext().getPackageManager()) != null) {
                log.info("Start activity for " + uri.toString());
                application.getActivity().startActivity(intent);
            } else {
                log.error("Activity for " + link + " not installed.");
                CB.viewmanager.toast(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")");
            }
        } catch (Exception exc) {
            log.error(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")", exc);
            CB.viewmanager.toast(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")");
        }
    }

    @Override
    public void _callQuit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            application.getActivity().finishAndRemoveTask();
        } else {
            application.getActivity().finishAffinity();
        }
        Gdx.app.exit();
    }

    @Override
    protected void _postOnMainThread(NamedRunnable runnable) {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(runnable);
    }

    @Override
    protected void _runOnBackGround(final Runnable backgroundTask) {
        CB.postAsync(new NamedRunnable("Run on Background") {
            @Override
            public void run() {
                backgroundTask.run();
            }
        });
    }

    @Override
    protected void _playNotifySound(final FileHandle soundFileHandle) {
        final Sound sound = Gdx.audio.newSound(soundFileHandle);
        //need time for prepare sound
        CB.postAsyncDelayd(100, new NamedRunnable("") {
            @Override
            public void run() {
                sound.play();
            }
        });
    }

    @Override
    public void _getTextInput(boolean singleLine, final Input.TextInputListener listener, int inputType, final String title, final String text,
                              final String hint) {
        this.handle.post(() -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(AndroidPlatformConnector.this.context);

            //set custom title
            TextView myMsg = new TextView(context);
            myMsg.setText(title);
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            myMsg.setTextSize(16);
            alert.setCustomTitle(myMsg);


            Context activity = AndroidPlatformConnector.this.context;
            final EditText input = new EditText(activity);
            input.setHint(hint);
            input.setText(text);
            if (inputType == 0)
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
            else
                input.setInputType(inputType);
            input.setSingleLine(singleLine);
            if (singleLine) {
                input.setLines(1);
                input.setMaxLines(1);
            } else {
                input.setLines(5);
                input.setMaxLines(8);
            }
            input.setGravity(Gravity.LEFT | Gravity.TOP);
            alert.setView(input);
            alert.setPositiveButton(context.getString(17039370),
                    (dialog, whichButton) -> Gdx.app.postRunnable(() -> listener.input(input.getText().toString())));
            alert.setNegativeButton(context.getString(17039360),
                    (dialog, whichButton) -> Gdx.app.postRunnable(() -> listener.canceled()));

            alert.show();
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.postDelayed(() -> {
                input.requestFocus();
                input.setSelection(input.getText().length());
                manager.showSoftInput(input, 0);
            }, 100);

        });
    }

    public void _takePhoto() {
        log.info("takePhoto start " + EventHandler.getSelectedCache());
        if (application.getActivity() == null) return;
        File mediaDir = application.getActivity().getExternalFilesDir("User/Media");
        if (mediaDir == null) return;
        tempMediaPath = mediaDir.getAbsolutePath() + "/"; // oder Environment.DIRECTORY_PICTURES
        if (!Utils.createDirectory(tempMediaPath)) {
            log.error("can't create " + tempMediaPath);
            return;
        }
        try {
            // define the file-name to save photo taken by Camera activity
            String directory = Config.UserImageFolder.getValue();
            if (!Utils.createDirectory(directory)) {
                log.error("can't create " + directory);
                return;
            }
            String cacheName;
            if (EventHandler.isSetSelectedCache()) {
                String validName = Utils.removeInvalidFatChars(EventHandler.getSelectedCache().getGeoCacheCode() + "-" + EventHandler.getSelectedCache().getGeoCacheName());
                cacheName = validName.substring(0, Math.min(validName.length(), 32));
            } else {
                cacheName = "Image";
            }
            mediaFileNameWithoutExtension = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US).format(new Date()) + " " + cacheName;
            String tempMediaPathAndName = tempMediaPath + mediaFileNameWithoutExtension + ".jpg";
            try {
                FileHandle fh = new FileHandle(tempMediaPathAndName);
                if (!fh.file().createNewFile()) log.error("file already exists: " + tempMediaPathAndName);
            } catch (Exception e) {
                log.error("can't create " + tempMediaPathAndName + "\r" + e.toString());
                return;
            }

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= N) {
                uri = FileProvider.getUriForFile(application.getActivity(), "de.longri.cachebox3.fileprovider", new java.io.File(tempMediaPathAndName));
            } else {
                uri = Uri.fromFile(new java.io.File(tempMediaPathAndName));
            }
            log.info(uri.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            if (intent.resolveActivity(application.getActivity().getPackageManager()) != null) {
                // application.addAndroidEventListener(handlingTakePhoto);
                application.getActivity().startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            } else {
                log.error(MediaStore.ACTION_IMAGE_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void _recVideo() {
        try {
            log.info("recVideo start " + EventHandler.getSelectedCache());
            // define the file-name to save video taken by Camera activity
            String directory = Config.UserImageFolder.getValue();
            if (!Utils.createDirectory(directory)) {
                log.error("can't create " + directory);
                return;
            }
            mediaFileNameWithoutExtension = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US).format(new Date());
            String cacheName;
            if (EventHandler.isSetSelectedCache()) {
                String validName = Utils.removeInvalidFatChars(EventHandler.getSelectedCache().getGeoCacheCode() + "-" + EventHandler.getSelectedCache().getGeoCacheName());
                cacheName = validName.substring(0, Math.min(validName.length(), 32));
            } else {
                cacheName = "Video";
            }
            mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;

            // Da ein Video keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
            recordingStartTime = getTrackDateTimeString();
            recordingStartCoordinate = EventHandler.getMyPosition(); // Locator.getInstance().getLocation(Location.ProviderType.GPS);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.TITLE, "");
            videoUri = application.getActivity().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            // log.info(uri.toString());
            final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            // intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, MAXIMUM_VIDEO_SIZE);
            if (intent.resolveActivity(application.getActivity().getPackageManager()) != null) {
                // androidApplication.addAndroidEventListener(handlingRecordedVideo);
                application.getActivity().startActivityForResult(intent, REQUEST_CAPTURE_VIDEO);
            } else {
                log.error(MediaStore.ACTION_VIDEO_CAPTURE + " not installed.");
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private String getTrackDateTimeString() {
        Date timestamp = new Date();
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return datFormat.format(timestamp).replace(" ", "T") + "Z";
    }

    public void _recVoice() {
        // for still todo:         microphoneView = application.getActivity().findViewById(R.id.microphone);
    }
    /*
    public void _recVoice() {
        try {
            if (!isVoiceRecordingStarted) // Voice Recorder starten
            {
                // define the file-name to save voice taken by activity
                String directory = Config.UserImageFolder.getValue();
                if (!Utils.createDirectory(directory)) {
                    log.error("can't create " + directory);
                    return;
                }

                mediaFileNameWithoutExtension = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US).format(new Date());

                String cacheName;
                if (EventHandler.isSetSelectedCache()) {
                    String validName = Utils.removeInvalidFatChars(EventHandler.getSelectedCache().getGeoCacheCode() + "-" + EventHandler.getSelectedCache().getGeoCacheName());
                    cacheName = validName.substring(0, Math.min(validName.length(), 32));
                } else {
                    cacheName = "Voice";
                }

                mediaFileNameWithoutExtension = mediaFileNameWithoutExtension + " " + cacheName;
                extAudioRecorder = ExtAudioRecorder.getInstance(false);
                extAudioRecorder.setOutputFile(directory + "/" + mediaFileNameWithoutExtension + ".wav");
                extAudioRecorder.prepare();
                extAudioRecorder.start();

                String MediaFolder = Config.UserImageFolder.getValue();
                String TrackFolder = Config.TrackFolder.getValue();
                String relativPath = Utils.getRelativePath(MediaFolder, TrackFolder, "/");
                // Da eine Voice keine Momentaufnahme ist, muss die Zeit und die Koordinaten beim Start der Aufnahme verwendet werden.
                TrackRecorder.AnnotateMedia(mediaFileNameWithoutExtension + ".wav", relativPath + "/" + mediaFileNameWithoutExtension + ".wav", Locator.getInstance().getLocation(Location.ProviderType.GPS), getTrackDateTimeString());
                Toast.makeText(application.getActivity(), "Start Voice Recorder", Toast.LENGTH_SHORT).show();

                recordVoice(true);

            } else { // Voice Recorder stoppen
                recordVoice(false);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isVoiceRecordingStarted() {
        return isVoiceRecordingStarted;
    }

    private void recordVoice(boolean value) {
        isVoiceRecordingStarted = value;
        if (isVoiceRecordingStarted) {
            microphoneView.SetOn();
        } else { // Aufnahme stoppen
            microphoneView.SetOff();
            if (extAudioRecorder != null) {
                extAudioRecorder.stop();
                extAudioRecorder.release();
                extAudioRecorder = null;
                Toast.makeText(application.getActivity(), "Stop Voice Recorder", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initalizeMicrophone() {
        microphoneView = application.getActivity().findViewById(R.id.microphone);
        if (microphoneView != null) {
            microphoneView.SetOff();
            microphoneView.setOnClickListener(v -> {
                // Stoppe Aufnahme durch klick auf Mikrofon-Icon
                recordVoice(false);
            });
        }
    }
     */

    public void _shareInfos() {
        String smiley = ((char) new BigInteger("1F604", 16).intValue()) + " ";

        // PackageManager pm = application.getActivity().getPackageManager();
        try {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            /*
            // PackageInfo info =
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code in catch block will be called
            waIntent.setPackage("com.whatsapp");
             */

            AbstractCache cache = EventHandler.getSelectedCache();
            String text = cache.getGeoCacheCode() + " - " + cache.getGeoCacheName() + ("\n" + "https://coord.info/" + cache.getGeoCacheCode());
            if (cache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                text = text + ("\n\n" + "Location (corrected)");
                if (cache.hasCorrectedCoordinates()) {
                    text = text + ("\n" + cache.formatCoordinate());
                } else {
                    text = text + ("\n" + cache.getCorrectedFinal().formatCoordinate());
                }
            } else {
                text = text + ("\n\n" + "Location");
                text = text + ("\n" + cache.formatCoordinate());
            }
            if (getClipboard() != null)
                text = text + ("\n" + getClipboard().getContents());
            text = text + ("\n" + smiley + "AndroidCacheBox");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            application.getActivity().startActivity(Intent.createChooser(shareIntent, Translation.get("ShareWith")));
            //} catch (PackageManager.NameNotFoundException e) {
            //    toast.makeText(application.getActivity(), "WhatsApp not Installed", toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(application.getActivity(), "Share App not installed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void _navigate(Navigation selectedNavi) {
        if (EventHandler.isSetSelectedCache()) {
            double lat;
            double lon;
            String targetName;

            if (EventHandler.getSelectedWayPoint() == null) {
                lat = EventHandler.getSelectedCache().getLatitude();
                lon = EventHandler.getSelectedCache().getLongitude();
                targetName = EventHandler.getSelectedCache().getGeoCacheCode().toString();
            } else {
                lat = EventHandler.getSelectedWayPoint().getLatitude();
                lon = EventHandler.getSelectedWayPoint().getLongitude();
                targetName = EventHandler.getSelectedWayPoint().getGcCode().toString();
            }

            Intent intent = null;
            switch (selectedNavi) {
                case Navigon:
                    intent = getNavigationIntent("android.intent.action.navigon.START_PUBLIC", "");
                    if (intent == null) {
                        intent = getNavigationIntent("", "com.navigon.navigator"); // get the launch-intent from package
                    }
                    if (intent != null) {
                        intent.putExtra("latitude", (float) lat);
                        intent.putExtra("longitude", (float) lon);
                    }
                    break;
                case Orux:
                    intent = getNavigationIntent("com.oruxmaps.VIEW_MAP_ONLINE", "");
                    // from http://www.oruxmaps.com/oruxmapsmanual_en.pdf
                    if (intent != null) {
                        double[] targetLat = {lat};
                        double[] targetLon = {lon};
                        String[] targetNames = {targetName};
                        intent.putExtra("targetLat", targetLat);
                        intent.putExtra("targetLon", targetLon);
                        intent.putExtra("targetName", targetNames);
                        intent.putExtra("navigatetoindex", 1);
                    }
                    break;
                case OsmAnd:
                    intent = getNavigationIntent(ACTION_VIEW, "geo:" + lat + "," + lon);
                    break;
                case OsmAnd2:
                    intent = getNavigationIntent(ACTION_VIEW, "http://download.osmand.net/go?lat=" + lat + "&lon=" + lon + "&z=14");
                    break;
                case Waze:
                    intent = getNavigationIntent(ACTION_VIEW, "waze://?ll=" + lat + "," + lon);
                    break;
                case Sygic:
                    intent = getNavigationIntent(ACTION_VIEW, "com.sygic.aura://coordinate|" + lon + "|" + lat + "|drive");
                    break;
            }
            if (intent == null) {
                // "default" or "no longer existing selection" or "fallback" to google
                intent = getNavigationIntent(ACTION_VIEW, "http://maps.google.com/maps?daddr=" + lat + "," + lon);
            }
            try {
                if (intent != null)
                    application.getActivity().startActivity(intent);
            } catch (Exception e) {
                log.error("Error Start " + selectedNavi, e);
            }
        }
    }

    private Intent getNavigationIntent(String action, String data) {
        Intent intent;
        try {
            if (action.length() > 0) {
                if (data.length() > 0) {
                    intent = new Intent(action, Uri.parse(data));
                } else {
                    intent = new Intent(action);
                }
            } else {
                intent = application.getActivity().getPackageManager().getLaunchIntentForPackage(data);
            }
            if (intent != null) {
                // check if there is an activity that can handle the desired intent
                if (intent.resolveActivity(application.getActivity().getPackageManager()) == null) {
                    intent = null;
                }
            }
            if (intent == null) {
                log.error("No intent for " + action + " , " + data);
            }
        } catch (Exception e) {
            intent = null;
            log.error("Exception: No intent for " + action + " , " + data, e);
        }
        return intent;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE) handlingTakePhoto.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_VIDEO) handlingRecordedVideo.onActivityResult(requestCode, resultCode, data);
    }
}
