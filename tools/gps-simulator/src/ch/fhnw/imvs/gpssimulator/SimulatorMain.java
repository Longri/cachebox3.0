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

package ch.fhnw.imvs.gpssimulator;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


import ch.fhnw.imvs.gpssimulator.components.TiltPanel;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.events.location.LocationEvents;
import org.mapsforge.map.swing.view.MapPanel;

import ch.fhnw.imvs.gpssimulator.components.CoursePanel;
import ch.fhnw.imvs.gpssimulator.components.LocationPanel;
import ch.fhnw.imvs.gpssimulator.components.XMLPanel;
import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.nmea.GGA;
import ch.fhnw.imvs.gpssimulator.nmea.GLL;
import ch.fhnw.imvs.gpssimulator.nmea.GSA;
import ch.fhnw.imvs.gpssimulator.nmea.NMEASentence;
import ch.fhnw.imvs.gpssimulator.nmea.RMC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;
import org.slf4j.impl.LoggerInit;

import static com.badlogic.gdx.Application.LOG_DEBUG;

public class SimulatorMain {

    public static Preferences prefs = Preferences.userNodeForPackage(ch.fhnw.imvs.gpssimulator.SimulatorMain.class);


    private static boolean closing = false;


    private static List<NMEASentence> nmeaTypes = new ArrayList<NMEASentence>();

    public static JFrame createFrame() throws IOException {
        JFrame f = new JFrame("GPS Simulator");
        f.setLayout(new BorderLayout());

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    closing = true;
                } catch (NullPointerException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        nmeaTypes.add(new GGA());
        nmeaTypes.add(new GSA());
        nmeaTypes.add(new RMC());
        nmeaTypes.add(new GLL());
        box.add(new LocationPanel()); // add location component
        box.add(new CoursePanel()); // add course component
        box.add(new TiltPanel()); // add course tilt
        box.add(new XMLPanel()); // add XML waypoints component

        JPanel rightbox = new JPanel();
        rightbox.setLayout(new BoxLayout(rightbox, BoxLayout.Y_AXIS));
        MapPanel mapPanel = new MapPanel();

        mapPanel.setVisible(true);
        rightbox.add(mapPanel);

        rightbox.setVisible(true);

        f.add(box, BorderLayout.WEST);
        f.add(rightbox, BorderLayout.EAST);

        JLabel title = new JLabel("GPS Simulator", JLabel.CENTER);
        title.setFont(new Font(null, Font.BOLD, 18));
        f.add(title, BorderLayout.NORTH);

        GPSData.start();
        return f;
    }


    public static void main(String[] args) throws Exception {
        JFrame f = createFrame();
        f.pack();
        f.setResizable(false);
        f.setVisible(true);


        //initial Logger with HeadlessAplication for GDX
        Gdx.app = new HeadlessApplication(new Game() {
            @Override
            public void create() {

            }
        });
        Gdx.net = Gdx.app.getNet();
        Gdx.files =  Gdx.app.getFiles();

        Gdx.app.setLogLevel(LOG_DEBUG);
        LoggerInit.initlogger();


        // add Gps

    }
}
