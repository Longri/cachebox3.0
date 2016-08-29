package de.longri.cachebox3.sqlite.Import;

public class CacheInfo {
	public long id;
	public int ListingCheckSum;
	public boolean ListingChanged = true;
	public boolean ImagesUpdated = false;
	public boolean DescriptionImagesUpdated = false;
	public boolean Found = false;
	public boolean CorrectedCoordinates = false;
	public double Latitude = 0;
	public double Longitude = 0;
	public boolean favorite = false;
	public long GpxFilename_Id = 0;

	public CacheInfo(long ID, long GpxFilename_Id) {
		this.id = ID;
		this.GpxFilename_Id = GpxFilename_Id;
	}

	public CacheInfo() {

	}
}