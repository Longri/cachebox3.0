package de.longri.cachebox3.locator.events;

import java.util.ArrayList;

public class GPS_FallBackEventList {
	public static ArrayList<GPS_FallBackEvent> list = new ArrayList<GPS_FallBackEvent>();

	public static void Add(GPS_FallBackEvent event) {
		if (!list.contains(event))
			list.add(event);
	}

	public static void Remove(GPS_FallBackEvent event) {
		list.remove(event);
	}

	public static void CallFallBack() {

		for (GPS_FallBackEvent event : list) {
			try {
				event.FallBackToNetworkProvider();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void CallFix() {

		for (GPS_FallBackEvent event : list) {
			try {
				event.Fix();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
