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

public class GSA extends NMEASentence {

	@Override
	public String getName() {
		return "GPGSA";
	}

	@Override
	public String getSentence(boolean content) {
		String sentence;
		if (content) {
			StringBuffer buf = new StringBuffer(getName());
			append(buf, GPSData.getStatus().name());
			int ft = 0;
			switch (GPSData.getFixType()) {
			case FIX_NONE:
				ft = 1;
				break;
			case FIX_2D:
				ft = 2;
				break;
			case FIX_3D:
				ft = 3;
				break;
			default:
				throw new IllegalArgumentException();
			}
			append(buf, ft); // 3: 3D-fix, 2: 2D-fix, 1: no-fix
			for (int i = 1; i <= 12; i++) {
				append(buf, getSatelliteNumber(i));
			}
			append(buf, GPSData.getPDOP());
			append(buf, GPSData.getHDOP());
			append(buf, GPSData.getVDOP());
			sentence = buf.toString();
		} else {
			sentence = "GPGSA,V,,,,,,,,,,,,,,,,";
		}
		return sentence;
	}

	private String getSatelliteNumber(int number) {
		if (number <= GPSData.getSatellites()) {
			if (number > 9) {
				return "" + number;
			} else {
				return "0" + number;
			}
		} else {
			return "";
		}
	}
}
