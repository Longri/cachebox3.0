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
package de.longri.cachebox3.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.DesktopDescriptionView;
import com.badlogic.gdx.backends.lwjgl.GenerateApiKeyWebView;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.exceptions.NotImplementedException;
import org.oscim.awt.DesktopRealSvgBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class DesktopPlatformConnector extends PlatformConnector {

    private final static Logger log = LoggerFactory.getLogger(DesktopPlatformConnector.class);

    @Override
    protected boolean _isTorchAvailable() {
        return false;
    }

    @Override
    protected boolean _isTorchOn() {
        return false;
    }

    @Override
    protected void _switchTorch() {
        // is not implemented, do nothing
    }

    @Override
    protected String _createThumb(String path, int scaledWidth, String thumbPrefix) {
        try {
            String storePath = Utils.getDirectoryName(path) + "/";
            String storeName = Utils.getFileNameWithoutExtension(path);
            String storeExt = Utils.getFileExtension(path).toLowerCase();
            String ThumbPath = storePath + thumbPrefix + Utils.THUMB + storeName + "." + storeExt;

            java.io.File ThumbFile = new java.io.File(ThumbPath);

            if (ThumbFile.exists())
                return ThumbPath;

            java.io.File orgFile = new java.io.File(path);
            if (orgFile.exists()) {
                BufferedImage ori = ImageIO.read(orgFile);
                if (ori == null) {
                    orgFile.delete();
                    return null;
                }
                float scalefactor = (float) scaledWidth / (float) ori.getWidth();

                if (scalefactor >= 1)
                    return path; // don't need a thumb, return original path

                int newHeight = (int) (ori.getHeight() * scalefactor);
                int newWidth = (int) (ori.getWidth() * scalefactor);

                Image scaled = ori.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                BufferedImage img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                img.createGraphics().drawImage(scaled, 0, 0, null);
                ImageIO.write(img, storeExt, ThumbFile);

                img.flush();
                ori.flush();
                scaled.flush();

                img = null;
                ori = null;
                scaled = null;

                return ThumbPath;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public Bitmap getRealScaledSVG(String name, InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        DesktopRealSvgBitmap bmp = new DesktopRealSvgBitmap(stream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
    }


    @Override
    public FileHandle _getSandBoxFileHandle(String fileName) {
        return Gdx.files.local(fileName);
    }

    @Override
    protected String _getWorkPath() {
        return _getSandBoxFileHandle("Cachebox3").file().getAbsolutePath();
    }

    @Override
    protected void generateApiKey(GenericCallBack<String> callBack) {
        GenerateApiKeyWebView webView = new GenerateApiKeyWebView(callBack);
    }

    private DesktopDescriptionView descriptionView;

    @Override
    protected void getPlatformDescriptionView(final GenericCallBack<PlatformDescriptionView> callBack) {
        if (descriptionView == null) descriptionView = new DesktopDescriptionView();
        callBack.callBack(descriptionView);
    }

    @Override
    protected void descriptionViewToNull() {
        descriptionView.close();
        descriptionView = null;
    }

    @Override
    public void openUrlExtern(String link) {
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {

            System.err.println("Desktop doesn't support the browse action (fatal)");
            System.exit(1);
        }

        try {
            java.net.URI uri = null;
            if (link.startsWith("file://")) {
                File f = new File(link.replace("file://", ""));
                uri = f.toURI();
            } else {
                uri = new java.net.URI(link);
            }

            desktop.browse(uri);

        } catch (Exception e) {

            System.err.println(e.getMessage());
        }
    }

    @Override
    public void _callQuit() {
        Gdx.app.exit();
    }

    @Override
    protected void _postOnMainThread(NamedRunnable runnable) {
        SwingUtilities.invokeLater(runnable);
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
    public void _getMultilineTextInput(final Input.TextInputListener listener, final String title,
                                       final String text, final String hint) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JPanel panel = new JPanel(new FlowLayout());

                JPanel textPanel = new JPanel() {
                    public boolean isOptimizedDrawingEnabled() {
                        return false;
                    }
                };

                textPanel.setLayout(new OverlayLayout(textPanel));

                final JScrollPane scrollPane = new JScrollPane(textPanel);
                panel.add(scrollPane);

                final JTextArea textField = new JTextArea(30, 40);
                textField.setText(text);
                textField.setAlignmentX(0.0f);
                textPanel.add(textField);

                final JLabel placeholderLabel = new JLabel(hint);
                placeholderLabel.setForeground(Color.GRAY);
                placeholderLabel.setAlignmentX(0.0f);
                textPanel.add(placeholderLabel, 0);

                textField.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void removeUpdate(DocumentEvent arg0) {
                        this.updated();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent arg0) {
                        this.updated();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent arg0) {
                        this.updated();
                    }

                    private void updated() {
                        if (textField.getText().length() == 0)
                            placeholderLabel.setVisible(true);
                        else
                            placeholderLabel.setVisible(false);
                    }
                });

                JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null,
                        null);

                pane.setInitialValue(null);
                pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());

                Border border = textField.getBorder();
                placeholderLabel.setBorder(new EmptyBorder(border.getBorderInsets(textField)));

                JDialog dialog = pane.createDialog(null, title);
                pane.selectInitialValue();

                dialog.addWindowFocusListener(new WindowFocusListener() {

                    @Override
                    public void windowLostFocus(WindowEvent arg0) {
                    }

                    @Override
                    public void windowGainedFocus(WindowEvent arg0) {
                        textField.requestFocusInWindow();
                    }
                });

                dialog.setVisible(true);
                dialog.dispose();

                Object selectedValue = pane.getValue();

                if (selectedValue != null && (selectedValue instanceof Integer)
                        && ((Integer) selectedValue).intValue() == JOptionPane.OK_OPTION) {
                    listener.input(textField.getText());
                } else {
                    listener.canceled();
                }

            }
        });
    }
}
