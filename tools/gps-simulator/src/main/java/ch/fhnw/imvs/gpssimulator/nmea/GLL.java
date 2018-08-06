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

public class GLL extends NMEASentence {

	@Override
	public String getName() {
		return "GPGLL";
	}

	@Override
	public String getSentence(boolean content) {
		String sentence;
		if (content) {
			String ns = GPSData.getNS().name().substring(0, 1);
			String ew = GPSData.getEW().name().substring(0, 1);

			StringBuffer buf = new StringBuffer(getName());
			append(buf, NMEASentence.getNMEALatitude()); // in degree
			append(buf, ns);
			append(buf, NMEASentence.getNMEALongitude());
			append(buf, ew);
			append(buf, getTimestamp()); // time position is taken in UTC
			append(buf, GPSData.getStatus().name()); // A=OK, V=warning
			append(buf, GPSData.getMode().name().substring(0, 1));
			sentence = buf.toString();
		} else {
			sentence = ("GPGLL,,,,," + getTimestamp() + ",,");
		}
		return sentence;
	}

}
