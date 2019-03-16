package ch.fhnw.imvs.gpssimulator.components;

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class TiltPanel extends JSlider implements GPSDataListener {

    public TiltPanel() {
        GPSData.addChangeListener(this);
        this.setBorder(BorderFactory.createTitledBorder("Tilt"));

        this.setMinimum(0);
        this.setMaximum(70);
        this.setValue(0);
        this.setMajorTickSpacing(10);
        this.setPaintTicks(true);
        this.setPaintLabels(true);

        this.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                GPSData.setTilt(TiltPanel.this.getValue());
            }
        });
    }

    public void valueChanged() {
        this.setValue((int) GPSData.getTilt());
    }
}
