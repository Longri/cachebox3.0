package org.mapsforge.map.swing.view;

import java.awt.event.MouseEvent;
import java.util.prefs.BackingStoreException;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.input.MouseEventListener;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.view.MapView;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import ch.fhnw.imvs.gpssimulator.data.GPSData;

public class GpsSimmulatorMouseEventListener extends MouseEventListener {

    private final Model model;

    public GpsSimmulatorMouseEventListener(AwtMapView mapView) {
        super(mapView);
        this.model = mapView.getModel();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        // Save last Point
        LatLong pos = this.model.mapViewPosition.getCenter();
        int zoom = this.model.mapViewPosition.getZoomLevel();
        SimulatorMain.prefs.putInt("zoom", zoom);
        SimulatorMain.prefs.putDouble("lat", pos.getLatitude());
        SimulatorMain.prefs.putDouble("lon", pos.getLongitude());
        try {
            SimulatorMain.prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        GPSData.setLatitude(pos.getLatitude());
        GPSData.setLongitude(pos.getLongitude());

    }

}
