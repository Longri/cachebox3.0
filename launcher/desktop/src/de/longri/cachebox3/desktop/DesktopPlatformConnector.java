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
import com.badlogic.gdx.backends.lwjgl.DesktopDescriptionView;
import com.badlogic.gdx.backends.lwjgl.GenerateApiKeyWebView;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.awt.DesktopRealSvgBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
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
    public Bitmap getRealScaledSVG(String name, InputStream stream, SvgScaleType scaleType, float scaleValue) throws IOException {
        DesktopRealSvgBitmap bmp = new DesktopRealSvgBitmap(stream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
    }

    @Override
    public void initialLocationReciver() {

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
