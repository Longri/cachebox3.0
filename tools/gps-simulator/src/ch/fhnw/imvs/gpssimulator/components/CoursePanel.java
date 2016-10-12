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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;

@SuppressWarnings("serial")
public class CoursePanel extends JSlider implements GPSDataListener {

	public CoursePanel() {
		GPSData.addChangeListener(this);
		this.setBorder(BorderFactory.createTitledBorder("Course"));

		this.setMinimum(0);
		this.setMaximum(360);
		this.setValue(GPSData.getCourse());
		this.setMajorTickSpacing(60);
		this.setPaintTicks(true);
		this.setPaintLabels(true);

		this.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				GPSData.setCourse(CoursePanel.this.getValue());
			}
		});
	}

	public void valueChanged() {
		this.setValue(GPSData.getCourse());
	}
}
