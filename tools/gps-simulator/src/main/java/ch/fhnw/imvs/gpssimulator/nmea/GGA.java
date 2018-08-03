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

package ch.fhnw.imvs.gpssimulator.nmea;

import ch.fhnw.imvs.gpssimulator.data.GPSData;

public class GGA extends NMEASentence {

	@Override
	public String getName() {
		return "GPGGA";
	}

	@Override
	public String getSentence(boolean content) {
		String sentence;
		if (content) {
			String ns = GPSData.getNS().name().substring(0, 1);
			String ew = GPSData.getEW().name().substring(0, 1);

			StringBuffer buf = new StringBuffer(getName());
			append(buf, getTimestamp()); // time position is taken in UTC [hhmmss.ss]
			append(buf, NMEASentence.getNMEALatitude()); // latitude [ddmm.mmmm]
			append(buf, ns); // [N|S]
			append(buf, NMEASentence.getNMEALongitude()); // longitude [dddmm.mmmm]
			append(buf, ew); // [E|W]
			append(buf, GPSData.getQuality()); // GPS Quality: 
												// 0 fix not available or invalid
												// 1 GPS mode, fix valid
												// 2 DGPS, fix valid
												// 3 PPS, 4 RTK, 5 Float RTK, 6 Estimated, 7 Manual, 8 Simulator
			append(buf, GPSData.getSatellites()); // Number of satellites being tracked [<=12]
			append(buf, GPSData.getHDOP()); // Horizontal dilution of position [d.d]
			append(buf, GPSData.getAltitude()); // Altitude, above mean sea level
			append(buf, "M"); // Altitude unit
			append(buf, "0"); // Height of geoid (mean sea level) above WGS84 ellipsoid
			append(buf, "M"); // Height of geoid unit
			append(buf, ""); // time in seconds since last DGPS update
			append(buf, ""); // DGPS station ID number [0000-1023]
			sentence = buf.toString();
		} else {
			sentence = "GPGGA," + getTimestamp() + ",,,,,,,,,,,,,";
		}
		return sentence;
	}

}
