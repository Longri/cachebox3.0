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

import java.util.Calendar;
import java.util.TimeZone;

import ch.fhnw.imvs.gpssimulator.data.GPSData;

public abstract class NMEASentence {

	private final char[] hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public abstract String getName();

	private boolean linePrinted;

	public boolean isLinePrinted() {
		return linePrinted;
	}

	public void setLinePrinted(boolean linePrinted) {
		this.linePrinted = linePrinted;
	}

	private boolean printContent;

	public void setPrintContent(boolean printContent) {
		this.printContent = printContent;
	}

	private boolean printChecksum;

	public void setPrintChecksum(boolean printChecksum) {
		this.printChecksum = printChecksum;
	}

	protected void append(StringBuffer buf, double arg) {
		buf.append("," + arg);
	}

	protected void append(StringBuffer buf, int arg) {
		buf.append("," + arg);
	}

	protected void append(StringBuffer buf, String arg) {
		buf.append(",");
		buf.append(arg);
	}

	protected abstract String getSentence(boolean content);

	public final String getSentence() {
		String s = getSentence(printContent);
		if (printChecksum)
			s = addCheckSum(s);
		return s;
	}

	private String addCheckSum(String string) {
		byte[] buf = string.getBytes();
		int sum = buf[0];
		for (int i = 1; i < buf.length; i++) {
			sum ^= buf[i];
		}
		return string + "*" + hex[sum / 16] + hex[sum % 16];
	}

	public String getTimestamp() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		int hour_of_day = calendar.get(Calendar.HOUR_OF_DAY);
		String cookedHour_of_day;
		if (hour_of_day < 10) {
			cookedHour_of_day = "0" + hour_of_day;
		} else {
			cookedHour_of_day = "" + hour_of_day;
		}

		int minutes = calendar.get(Calendar.MINUTE);
		String cookedMinutes;
		if (minutes < 10) {
			cookedMinutes = "0" + minutes;
		} else {
			cookedMinutes = "" + minutes;
		}

		int seconds = calendar.get(Calendar.SECOND);
		String cookedSeconds;
		if (seconds < 10) {
			cookedSeconds = "0" + seconds;
		} else {
			cookedSeconds = "" + seconds;
		}

		int milliseconds = calendar.get(Calendar.MILLISECOND);
		String cookedMilliseconds;
		if (milliseconds > 99) {
			cookedMilliseconds = ("" + milliseconds).substring(0, 2);
		} else {
			cookedMilliseconds = "" + milliseconds;
		}

		return cookedHour_of_day + cookedMinutes + cookedSeconds + "." + cookedMilliseconds;
	}

	public String getDatestamp() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);

		String cookedDay_of_month;
		if (day_of_month < 10) {
			cookedDay_of_month = "" + 0 + day_of_month;
		} else {
			cookedDay_of_month = "" + day_of_month;
		}

		int month = calendar.get(Calendar.MONTH) + 1;
		String cookedMonth;
		if (month < 10) {
			cookedMonth = "" + 0 + month;
		} else {
			cookedMonth = "" + month;
		}
		return "" + cookedDay_of_month + cookedMonth + ("" + calendar.get(Calendar.YEAR)).substring(2, 4);
	}

	public synchronized static double getNMEALatitude() {
		double degree = (int) GPSData.getLatitude();
		double minute = (int) (GPSData.getLatitude() * 60 - degree * 60);
		double second = GPSData.getLatitude() * 60 - (int) (GPSData.getLatitude() * 60);

		return (double) Math.round((degree * 100 + minute + second) * 100) / 100;
	}

	public synchronized static double getNMEALongitude() {
		double degree = (int) GPSData.getLongitude();
		double minute = (int) (GPSData.getLongitude() * 60 - degree * 60);
		double second = GPSData.getLongitude() * 60 - (int) (GPSData.getLongitude() * 60);

		return (double) Math.round((degree * 100 + minute + second) * 100) / 100;
	}
}
