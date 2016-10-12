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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSData.Orientation;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;

@SuppressWarnings("serial")
public class LocationNormal extends JPanel implements GPSDataListener {

	private final JSpinner latitudeDegree = new JSpinner();
	private final JSpinner longitudeDegree = new JSpinner();
	private final JSpinner latitudeMinute = new JSpinner();
	private final JSpinner longitudeMinute = new JSpinner();
	private final JSpinner latitudeSecond = new JSpinner();
	private final JSpinner longitudeSecond = new JSpinner();
	private final JSpinner speed = new JSpinner();
	private final JSpinner altitude = new JSpinner();
	private final JComboBox<Orientation> ew = new JComboBox<Orientation>();
	private final JComboBox<Orientation> ns = new JComboBox<Orientation>();

	public LocationNormal() {
		GPSData.addChangeListener(this);

		JPanel labels = new JPanel(new GridLayout(4, 1));
		labels.add(new JLabel("Latitude:", JLabel.RIGHT));
		labels.add(new JLabel("Longitude:", JLabel.RIGHT));
		labels.add(new JLabel("Speed: [kts]", JLabel.RIGHT));
		labels.add(new JLabel("Altitude: [m]", JLabel.RIGHT));

		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

		latitudeDegree.setPreferredSize(new Dimension(50, 20));
		p1.add(latitudeDegree);
		latitudeDegree.setPreferredSize(new Dimension(50, 20));
		p1.add(latitudeMinute);
		latitudeMinute.setPreferredSize(new Dimension(50, 20));
		p1.add(latitudeSecond);
		latitudeSecond.setPreferredSize(new Dimension(50, 20));
		p1.add(ns);
		ns.setPreferredSize(new Dimension(80, 20));

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

		p2.add(longitudeDegree);
		longitudeDegree.setPreferredSize(new Dimension(50, 20));
		p2.add(longitudeMinute);
		longitudeMinute.setPreferredSize(new Dimension(50, 20));
		p2.add(longitudeSecond);
		longitudeSecond.setPreferredSize(new Dimension(50, 20));
		p2.add(ew);
		ew.setPreferredSize(new Dimension(80, 20));

		JPanel p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));

		p3.add(speed);
		speed.setPreferredSize(new Dimension(100, 20));
		speed.setAlignmentX(Component.RIGHT_ALIGNMENT);
		{
			JLabel spacer = new JLabel("");
			spacer.setPreferredSize(new Dimension(130, 20));
			p3.add(spacer);
		}

		JPanel p4 = new JPanel();
		p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS));

		p4.add(altitude);
		altitude.setPreferredSize(new Dimension(100, 20));
		{
			JLabel spacer = new JLabel("");
			spacer.setPreferredSize(new Dimension(130, 20));
			p4.add(spacer);
		}

		ChangeListener latitudeChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (valueChangedNotification > 0) {
					double latitude = GPSData.getLatitude();
					assert latitude >= 0;
					int lat = (int) Math.round(latitude * 3600);
					latitudeSecond.setValue(lat % 60);
					lat = lat / 60;
					latitudeMinute.setValue(lat % 60);
					lat = lat / 60;
					latitudeDegree.setValue(lat);
				} else {
					Integer degree = (Integer) latitudeDegree.getValue();
					Integer minute = (Integer) latitudeMinute.getValue();
					Integer second = (Integer) latitudeSecond.getValue();

					if (degree == 90) {
						if (minute == -1 && second == 0) {
							latitudeSecond.setValue(59);
						} else if (minute == 0 && second == -1) {
							latitudeMinute.setValue(59);
						} else if (minute == -1 && second == 59) {
							latitudeMinute.setValue(59);
						} else if (minute == 59 && second == -1) {
							latitudeSecond.setValue(59);
						} else if (minute == 59 && second == 59) {
							latitudeDegree.setValue(89);
						} else if (minute == 0 && second == 0) {
							Double tmp = allInOne(degree, minute, second);
							GPSData.setLatitude(tmp);
						} else {
							latitudeSecond.setValue(0);
							latitudeMinute.setValue(0);
						}
					} else if (second == 60) {
						latitudeSecond.setValue(0);
						latitudeMinute.setValue((Integer) latitudeMinute.getValue() + 1);
					} else if (second == -1) {
						if (minute == 0) {
							latitudeSecond.setValue(0);
						} else {
							latitudeSecond.setValue(59);
							latitudeMinute.setValue((Integer) latitudeMinute.getValue() - 1);
						}
					} else if (minute == 60) {
						latitudeMinute.setValue(0);
						latitudeDegree.setValue((Integer) latitudeDegree.getValue() + 1);
					} else if (minute == -1) {
						if (degree == 0) {
							latitudeMinute.setValue(0);
						} else {
							latitudeMinute.setValue(59);
							latitudeDegree.setValue((Integer) latitudeDegree.getValue() - 1);
						}
					} else {
						GPSData.setLatitude(allInOne(degree, minute, second));
					}
					// log4j.debug("Latitude: " + GPSData.getLatitude());
					// log4j.debug("Latitude (NMEA): " + NMEASentence.getNMEALatitude());
				}
			}
		};

		latitudeDegree.setModel(new SpinnerNumberModel(degree(GPSData.getLatitude()), 0, 90, 1));
		latitudeDegree.addChangeListener(latitudeChangeListener);

		latitudeMinute.setModel(new SpinnerNumberModel(minute(GPSData.getLatitude()), -1, 60, 1));
		latitudeMinute.addChangeListener(latitudeChangeListener);

		latitudeSecond.setModel(new SpinnerNumberModel(second(GPSData.getLatitude()), -1, 60, 1));
		latitudeSecond.addChangeListener(latitudeChangeListener);

		ns.addItem(GPSData.Orientation.NORTH);
		ns.addItem(GPSData.Orientation.SOUTH);
		ns.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GPSData.setNS((GPSData.Orientation) ns.getSelectedItem());
			}
		});

		ChangeListener longitudeChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (valueChangedNotification > 0) {
					double longitude = GPSData.getLongitude();
					assert longitude >= 0;
					int lng = (int) Math.round(longitude * 3600);
					longitudeSecond.setValue(lng % 60);
					lng = lng / 60;
					longitudeMinute.setValue(lng % 60);
					lng = lng / 60;
					longitudeDegree.setValue(lng);
				} else {
					Integer degree = (Integer) longitudeDegree.getValue();
					Integer minute = (Integer) longitudeMinute.getValue();
					Integer second = (Integer) longitudeSecond.getValue();

					if (degree == 180) {
						if (minute == -1 && second == 0) {
							longitudeSecond.setValue(59);
						} else if (minute == 0 && second == -1) {
							longitudeMinute.setValue(59);
						} else if (minute == -1 && second == 59) {
							longitudeMinute.setValue(59);
						} else if (minute == 59 && second == -1) {
							longitudeSecond.setValue(59);
						} else if (minute == 59 && second == 59) {
							longitudeDegree.setValue(179);
						} else if (minute == 0 && second == 0) {
							Double tmp = allInOne(degree, minute, second);
							GPSData.setLongitude(tmp);
						} else {
							longitudeSecond.setValue(0);
							longitudeMinute.setValue(0);
						}
					} else if (second == 60) {
						longitudeSecond.setValue(0);
						longitudeMinute.setValue((Integer) longitudeMinute.getValue() + 1);
					} else if (second == -1) {
						if (minute == 0) {
							longitudeSecond.setValue(0);
						} else {
							longitudeSecond.setValue(59);
							longitudeMinute.setValue((Integer) longitudeMinute.getValue() - 1);
						}
					} else if (minute == 60) {
						longitudeMinute.setValue(0);
						longitudeDegree.setValue((Integer) longitudeDegree.getValue() + 1);
					} else if (minute == -1) {
						if (degree == 0) {
							longitudeMinute.setValue(0);
						} else {
							longitudeMinute.setValue(59);
							longitudeDegree.setValue((Integer) longitudeDegree.getValue() - 1);
						}
					} else {
						GPSData.setLongitude(allInOne(degree, minute, second));
					}
					// log4j.debug("Longitude: " + GPSData.getLongitude());
					// log4j.debug("Longitude NMEA: " + NMEASentence.getNMEALongitude());
				}
			}
		};

		longitudeDegree.setModel(new SpinnerNumberModel(degree(GPSData.getLongitude()), 0, 180, 1));
		longitudeDegree.addChangeListener(longitudeChangeListener);

		longitudeMinute.setModel(new SpinnerNumberModel(minute(GPSData.getLongitude()), -1, 60, 1));
		longitudeMinute.addChangeListener(longitudeChangeListener);

		longitudeSecond.setModel(new SpinnerNumberModel(second(GPSData.getLongitude()), -1, 60, 1));
		longitudeSecond.addChangeListener(longitudeChangeListener);

		ew.addItem(GPSData.Orientation.EAST);
		ew.addItem(GPSData.Orientation.WEST);
		ew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GPSData.setEW((GPSData.Orientation) ew.getSelectedItem());
			}
		});

		speed.setModel(new SpinnerNumberModel(GPSData.getSpeed(), 0, 1000, 1));
		speed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				GPSData.setSpeed((Double) speed.getValue());
			}
		});

		altitude.setModel(new SpinnerNumberModel(GPSData.getAltitude(), -100, 10000, 1));
		altitude.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				GPSData.setAltitude((Double) altitude.getValue());
			}
		});

		this.add(labels);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel aroundThis = new JPanel();
		aroundThis.setLayout(new BoxLayout(aroundThis, BoxLayout.Y_AXIS));
		{
			JPanel jPanel = new JPanel();
			jPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			jPanel.add(p1);
			aroundThis.add(jPanel);
		}
		{
			JPanel jPanel = new JPanel();
			jPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			jPanel.add(p2);
			aroundThis.add(jPanel);
		}
		{
			JPanel jPanel = new JPanel();
			jPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			jPanel.add(p3);
			aroundThis.add(jPanel);
		}
		{
			JPanel jPanel = new JPanel();
			jPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			jPanel.add(p4);
			aroundThis.add(jPanel);

		}
		this.add(aroundThis);
	}

	private int valueChangedNotification = 0;

	@Override
	public void valueChanged() {
		valueChangedNotification++;
		latitudeDegree.setValue(degree(GPSData.getLatitude()));
		longitudeDegree.setValue(degree(GPSData.getLongitude()));

		latitudeMinute.setValue(minute(GPSData.getLatitude()));
		longitudeMinute.setValue(minute(GPSData.getLongitude()));

		latitudeSecond.setValue(second(GPSData.getLatitude()));
		longitudeSecond.setValue(second(GPSData.getLongitude()));

		ns.setSelectedItem(GPSData.getNS());
		ew.setSelectedItem(GPSData.getEW());

		altitude.setValue(GPSData.getAltitude());
		speed.setValue(GPSData.getSpeed());
		valueChangedNotification--;
	}

	private double allInOne(int degree, int minute, int second) {
		return degree + ((double) minute / 60) + (double) second / 60 / 60;
	}

	private int degree(double value) {
		return (int) (Math.round(value * 3600) / 3600);
	}

	private int minute(double value) {
		return (int) (Math.round(value * 3600) / 60 % 60);
	}

	private int second(double value) {
		return (int) (Math.round(value * 3600) % 60);
	}

}
