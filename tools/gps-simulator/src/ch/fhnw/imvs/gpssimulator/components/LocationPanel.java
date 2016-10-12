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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class LocationPanel extends JPanel {

	public LocationPanel() {

		this.setBorder(BorderFactory.createTitledBorder("Location"));

		JTabbedPane tabs = new JTabbedPane();

		tabs.addTab("Normal", new LocationNormal());
		tabs.addTab("NMEA Format", new LocationNMEA());
		tabs.addTab("GPS Format", new LocationGPS());

		this.add(tabs);
	}
}
