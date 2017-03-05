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

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.tools.XMLData;
import ch.fhnw.imvs.gpssimulator.tools.XMLParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("serial")
public class XMLPanel extends JPanel {

    private JButton openButton;
    private JButton startButton;
    private JButton stopButton;
    private JSlider timeline;

    private File directory;

    private XMLThread xmlThread;

    public XMLPanel() {

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder("XML"));

        final JPanel aroundXML = new JPanel(new FlowLayout());

        openButton = new JButton("Open XML File");

        startButton = new JButton("Start");
        startButton.setEnabled(false);

        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                readWaypointsFile();
            }
        });
        aroundXML.add(openButton);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
                openButton.setEnabled(false);
                stopButton.setEnabled(true);
                if (timeline.getValue() == timeline.getMaximum())
                    timeline.setValue(xmlThread.getStartTime());
                xmlThread.start();
            }
        });
        aroundXML.add(startButton);

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopButton.setEnabled(false);
                startButton.setText("Start");
                xmlThread.interrupt();
            }
        });
        aroundXML.add(stopButton);

        this.add(aroundXML);

        timeline = new JSlider();
        timeline.setBorder(BorderFactory.createTitledBorder("Time Position (secs)"));
        timeline.setEnabled(false);
        timeline.setPaintTicks(true);
        //    	timeline.setPaintLabels(true);
        timeline.setMinimum(0);
        timeline.setMaximum(0);
        timeline.setValue(0);
        timeline.setMajorTickSpacing(10000);
        timeline.setMinorTickSpacing(1000);
        this.add(timeline);
    }

    static class XMLThread extends Thread {

        private volatile boolean running;
        private List<XMLData> waypointList;
        private XMLPanel panel;

        public XMLThread(XMLPanel panel, List<XMLData> waypointList) {
            this.panel = panel;
            this.waypointList = waypointList;
        }

        @Override
        public void interrupt() {
            running = false;
        }

        @Override
        public synchronized void start() {
            panel.timeline.setEnabled(false);
            running = true;

            if (this.isAlive()) {
                this.notify();
            } else {
                super.start();
            }
        }

        private int getStartTime() {
            return waypointList.get(0).getTime();
        }

        private int getEndTime() {
            return waypointList.get(waypointList.size() - 1).getTime();
        }

        private XMLData getData(int time) {
            int starttime = getStartTime();
            if (time < starttime)
                time = starttime;

            int endtime = getEndTime();
            if (time > endtime)
                time = endtime;

            int s = 0;
            int e = waypointList.size() - 1;
            while (s < e - 1) {
                int m = (s + e) / 2;
                if (waypointList.get(m).getTime() <= time)
                    s = m;
                else
                    e = m;
            }

            XMLData start = waypointList.get(s);
            starttime = start.getTime();
            XMLData end = waypointList.get(e);
            endtime = end.getTime();

            XMLData res = new XMLData(time,
                    (end.getLatitude() - start.getLatitude()) / (endtime - starttime) * (time - starttime) + start.getLatitude(),
                    (end.getLongitude() - start.getLongitude()) / (endtime - starttime) * (time - starttime) + start.getLongitude(),
                    (end.getAltitude() - start.getAltitude()) / (endtime - starttime) * (time - starttime) + start.getAltitude(),
                    (end.getCourse() - start.getCourse()) / (endtime - starttime) * (time - starttime) + start.getCourse(),
                    (end.getSpeed() - start.getSpeed()) / (endtime - starttime) * (time - starttime) + start.getSpeed()
            );
            return res;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int timelinePosition = panel.timeline.getValue(); // read the current time from the slider

                    while (true) {

                        XMLData pos = this.getData(timelinePosition);
                        GPSData.setLatitude(pos.getLatitude());
                        GPSData.setLongitude(pos.getLongitude());
                        GPSData.setAltitude(pos.getAltitude());
                        GPSData.setCourse(pos.getCourse());
                        GPSData.setSpeed(pos.getSpeed());

                        if (!running) {
                            break;
                        } else if (timelinePosition >= getEndTime()) {
                            panel.stopButton.setEnabled(false);
                            panel.timeline.setValue(getEndTime());
                            running = false;
                            break;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }

                        // advance timeline by one second
                        timelinePosition = timelinePosition + 1000;
                        if (timelinePosition > getEndTime())
                            timelinePosition = getEndTime();

                        panel.timeline.setValue(timelinePosition);
                        panel.startButton.setText("" + panel.timeline.getValue() / 1000);

                    }

                    synchronized (this) {
                        panel.startButton.setText("Start");
                        panel.startButton.setEnabled(true);
                        panel.openButton.setEnabled(true);
                        panel.timeline.setEnabled(true);
                        this.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void readWaypointsFile() {
        JFileChooser chooser = new JFileChooser(directory);
        chooser.showOpenDialog(chooser);
        File file = chooser.getSelectedFile();

        if (file != null) {
            directory = file.getParentFile();

            try {
                List<XMLData> waypointList = XMLParser.parse(file);

                XMLPanel.this.xmlThread = new XMLThread(XMLPanel.this, waypointList);

                int startTime = waypointList.get(0).getTime();
                int endTime = waypointList.get(waypointList.size() - 1).getTime();

                timeline.setMinimum(startTime);
                timeline.setMaximum(endTime);
                timeline.setValue(startTime);
                timeline.setLabelTable(timeline.createStandardLabels(timeline.getMaximum() / 4));
                timeline.setEnabled(true);

                startButton.setEnabled(true);
            } catch (FileNotFoundException ex) { // TODO do more than ignore
            } catch (IOException ex) { // TODO do more than ignore
            }
        }
    }

    ;

}
