package de.longri.cachebox3.locator.events;

public interface GPS_FallBackEvent {
	public void FallBackToNetworkProvider();

	public void Fix();

}
