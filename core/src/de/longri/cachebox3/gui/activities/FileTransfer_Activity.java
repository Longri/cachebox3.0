/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.building.utilities.Alignment;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.*;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.socket.filebrowser.FileBrowserServer;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by longri on 02.11.17.
 */
public class FileTransfer_Activity extends ActivityBase {

    private final Logger log = LoggerFactory.getLogger(FileTransfer_Activity.class);

    private final CB_Button closeButton;
    private final FileBrowserServer server;
    private final VisLabel msgLabel;


    public FileTransfer_Activity() {
        super("FileTransfer");

        closeButton = new CB_Button(Translation.get("close"));
        closeButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                CharSequence msg = Translation.get("closeFileTransfer?");
                CharSequence title = Translation.get("closeFileTransfer");
                MessageBox.show(msg, title, MessageBoxButtons.YesNo, MessageBoxIcon.Asterisk,
                        new OnMsgBoxClickListener() {
                            @Override
                            public boolean onClick(int which, Object data) {

                                if (which == ButtonDialog.BUTTON_POSITIVE) {
                                    closeServer();
                                }
                                return true;
                            }
                        });
            }
        });

        server = new FileBrowserServer(Gdx.files.absolute(CB.WorkPath), 9988, new FileBrowserServer.ConectionCloesRemoteReciver() {
            @Override
            public void close() {
                closeServer();
            }
        });
        server.startListening();

        CharSequence message = Translation.get("StartFileTransferConnect", getIpAddress());
        msgLabel = new VisLabel(message);
        msgLabel.setAlignment(Alignment.CENTER.getAlignment());

        this.add(msgLabel);
        this.add(closeButton);
    }

    @Override
    public void layout() {
        super.layout();

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + closeButton.getWidth());
        float y = CB.scaledSizes.MARGIN;

        closeButton.setPosition(x, y);

        float width = this.getWidth() - CB.scaledSizes.MARGINx2;
        float height = this.getHeight() - (y + CB.scaledSizes.MARGINx2);
        y += closeButton.getHeight() + CB.scaledSizes.MARGINx2;
        x = CB.scaledSizes.MARGINx2;

        msgLabel.setBounds(x, y, width, height);
    }

    private String getIpAddress() {
        // The following code loops through the available network interfaces
        // Keep in mind, there can be multiple interfaces per device, for example
        // one per NIC, one per active wireless and the loopback
        // In this case we only care about IPv4 address ( x.x.x.x format )
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(interfaces)) {
                for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                    if (address instanceof Inet4Address) {
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Print the contents of our array to a string.  Yeah, should have used StringBuilder
        String ipAddress = new String("");
        for (String str : addresses) {
            if (!str.equals("127.0.0.1"))
                ipAddress = ipAddress + str + "\n";
        }
        return ipAddress;
    }

    private void closeServer() {
        server.stopListening();
        finish();
    }
}
