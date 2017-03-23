package de.longri.cachebox3.locator.events.newT;

import de.longri.cachebox3.locator.CoordinateGPS;

/**
 * Created by Longri on 23.03.2017.
 */
public class GpsEventHelper {

    private double lastLat, lastLon, lastSpeed, lastEle, lastHeading;
    private float accuracy;

    public void newPos(double lat, double lon, boolean isGpsProvided) {
        if (lastLat != lat || lastLon != lon) {
            lastLat = lat;
            lastLon = lon;
            CoordinateGPS newPos = new CoordinateGPS(lat, lon);
            //set additional info's
            newPos.setElevation(lastEle);
            newPos.setSpeed(lastSpeed);
            newPos.setHeading(lastHeading);
            newPos.setIsGpsProvided(isGpsProvided);
            newPos.setAccuracy(accuracy);
            EventHandler.fire(new PositionChangedEvent(newPos));
        }
    }

    public void setElevation(double altitude) {
        lastEle = altitude;
        //TODO add EventHandler
    }

    public void setSpeed(double speed) {
        if (lastSpeed != speed) {
            EventHandler.fire(new SpeedChangedEvent((float) speed));
            lastSpeed = speed;
        }
    }

    public void setCourse(double heading) {
        if (lastHeading != heading) {
            EventHandler.fire(new OrientationChangedEvent((float) heading));
            lastHeading = heading;
        }
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}
