/*
 * Copyright (c) 2007 by the University of Applied Sciences Northwestern Switzerland (FHNW)
 * 
 * This program can be redistributed or modified under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 * This program is distributed without any warranty or implied warranty
 * of merchantability or fitness for a particular purpose.
 *
 * See the GNU General Public License for more details.
 */

package ch.fhnw.imvs.gpssimulator.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class LocationPanel extends JPanel implements ActionListener {


    public interface PauseResumeInterface {
        void pause();

        void resume();
    }


    public static PauseResumeInterface pauseResumeInterface;

    Button pauseButton;

    public LocationPanel() {

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.setBorder(BorderFactory.createTitledBorder("Location"));

        this.setPreferredSize(new Dimension(250, 200));

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Normal", new LocationNormal());
        tabs.addTab("NMEA Format", new LocationNMEA());
        tabs.addTab("GPS Format", new LocationGPS());

        this.add(tabs);

        pauseButton = new Button("PAUSE");
        this.add(pauseButton);

        pauseButton.addActionListener(this);

    }


    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("PAUSE")) {
            pauseButton.setLabel("RESUME");
            if (pauseResumeInterface != null) pauseResumeInterface.pause();
        } else if (event.getActionCommand().equals("RESUME")) {
            pauseButton.setLabel("PAUSE");
            if (pauseResumeInterface != null) pauseResumeInterface.resume();
        }
    }
}
