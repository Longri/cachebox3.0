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

package ch.fhnw.imvs.gpssimulator.tools;

// Immutable
public class XMLData {
	private int time;
	private double latitude;
	private double longitude;
	private double altitude;

	public XMLData(int time, double latitude, double longitude, double altitude) {
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public int getTime() {
		return time;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
}
