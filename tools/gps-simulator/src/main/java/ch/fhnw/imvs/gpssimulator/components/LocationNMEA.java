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
public class LocationNMEA extends JPanel implements GPSDataListener {


	private final JSpinner latitude = new JSpinner();
	private final JSpinner longitude = new JSpinner();
	private final JSpinner speed = new JSpinner();
	private final JSpinner altitude = new JSpinner();
	private final JComboBox<Orientation> ew = new JComboBox<Orientation>();
	private final JComboBox<Orientation> ns = new JComboBox<Orientation>();

	public LocationNMEA() {
		GPSData.addChangeListener(this);

		JPanel labels = new JPanel(new GridLayout(4, 1));
		labels.add(new JLabel("Latitude:", JLabel.RIGHT));
		labels.add(new JLabel("Longitude:", JLabel.RIGHT));
		labels.add(new JLabel("Speed: [kts]", JLabel.RIGHT));
		labels.add(new JLabel("Altitude: [m]", JLabel.RIGHT));

		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

		p1.add(latitude);
		latitude.setPreferredSize(new Dimension(100, 20));
		p1.add(ns);
		ns.setPreferredSize(new Dimension(80, 20));
		{
			JLabel spacer = new JLabel("");
			spacer.setPreferredSize(new Dimension(50, 20));
			p1.add(spacer);
		}

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

		p2.add(longitude);
		longitude.setPreferredSize(new Dimension(100, 20));
		p2.add(ew);
		ew.setPreferredSize(new Dimension(80, 20));
		{
			JLabel spacer = new JLabel("");
			spacer.setPreferredSize(new Dimension(50, 20));
			p2.add(spacer);
		}

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
				GPSData.setLatitude(nmea2degree((Double) latitude.getValue()));
				// log4j.debug("Latitude: " + GPSData.getLatitude());
				// log4j.debug("Latitude (NMEA): " + NMEASentence.getNMEALatitude());
			}
		};

		latitude.setModel(new SpinnerNumberModel(degree2nmea(GPSData.getLatitude()), 0, 9000, 0.002));
		latitude.addChangeListener(latitudeChangeListener);

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
				GPSData.setLongitude(nmea2degree((Double) longitude.getValue()));
				// log4j.debug("Longitude: " + GPSData.getLongitude());
				// log4j.debug("Longitude NMEA: " + NMEASentence.getNMEALongitude());
			}
		};

		longitude.setModel(new SpinnerNumberModel(degree2nmea(GPSData.getLongitude()), 0, 18000, 0.002));
		longitude.addChangeListener(longitudeChangeListener);

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

	@Override
	public void valueChanged() {
		latitude.setValue(degree2nmea(GPSData.getLatitude()));
		longitude.setValue(degree2nmea(GPSData.getLongitude()));

		ns.setSelectedItem(GPSData.getNS());
		ew.setSelectedItem(GPSData.getEW());

		altitude.setValue(GPSData.getAltitude());
		speed.setValue(GPSData.getSpeed());
	}

	private double degree2nmea(double value) {
		double degree = (int) value;
		double minute = (int) (value * 60 - degree * 60);
		double second = value * 60 - (int) (value * 60);

		return (double) Math.round((degree * 100 + minute + second) * 1000) / 1000;
	}

	private double nmea2degree(double value) {
		int degree = (int) value / 100;
		int minute = (int) (value - degree * 100);
		double second = ((value - (int) value) * 60);

		return degree + ((double) minute / 60) + second / 60 / 60;
	}
}
