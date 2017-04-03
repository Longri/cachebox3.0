package de.longri.cachebox3.locator.events.newT;

import de.longri.cachebox3.locator.CoordinateGPS;

/**
 * Created by Longri on 23.03.2017.
 */
public class GpsEventHelper {

    private double lastLat, lastLon, lastSpeed, lastEle, lastHeading;
    private float lastAccuracy;

    public void newGpsPos(double lat, double lon, boolean isGpsProvided, double elevation,
                          double speed, double bearing, float accuracy) {

        // clamp coordinate to handled precision
        lat = ((int) (lat * 1E6)) / 1E6;
        lon = ((int) (lon * 1E6)) / 1E6;

        short eventID = EventHandler.getId();

        if (lastLat != lat || lastLon != lon) {
            lastLat = lat;
            lastLon = lon;
            CoordinateGPS newPos = new CoordinateGPS(lat, lon);
            //set additional info's
            newPos.setElevation(elevation);
            newPos.setSpeed(speed);
            newPos.setHeading(-bearing);
            newPos.setIsGpsProvided(isGpsProvided);
            newPos.setAccuracy(accuracy);
            EventHandler.fire(new PositionChangedEvent(newPos,eventID));

            setSpeed(speed,eventID);
            setElevation(elevation,eventID);
            setAccuracy(accuracy,eventID);
            setCourse(-bearing,eventID);

        }else{
            // not a new position call other event's only
            setSpeed(speed,eventID);
            setElevation(elevation,eventID);
            setAccuracy(accuracy,eventID);
            setCourse(-bearing,eventID);
        }
    }

    public void setElevation(double altitude) {
       this.setElevation(altitude,EventHandler.getId());
    }

    public void setElevation(double altitude,short id) {
        lastEle = altitude;
        //TODO add EventHandler
    }

    public void setSpeed(double speed) {
        this.setSpeed(speed,EventHandler.getId());
    }

    public void setSpeed(double speed,short id) {
        if (lastSpeed != speed) {
            EventHandler.fire(new SpeedChangedEvent((float) speed,id));
            lastSpeed = speed;
        }
    }

    public void setCourse(double heading) {
        this.setCourse(heading,EventHandler.getId());
    }

    public void setCourse(double heading,short id) {
        if (lastHeading != heading) {
            EventHandler.fire(new OrientationChangedEvent((float) heading,id));
            lastHeading = heading;
        }
    }

    public void setAccuracy(float accuracy) {
        this.setAccuracy(accuracy, EventHandler.getId());
    }

    public void setAccuracy(float accuracy,short id) {
        this.lastAccuracy = accuracy;
    }
}
