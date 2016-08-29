package de.longri.cachebox3.sqlite.Import;

import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Longri from => http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 */
public class UnZip {

	/**
	 * Extract the given ZIP-File
	 *
	 * @param zipFile
	 * @return Extracted Folder Path as String
	 * @throws ZipException
	 * @throws IOException
	 */
	static public String extractFolder(String zipFile) throws ZipException, IOException {
		System.out.println("extract => " + zipFile);
		int BUFFER = 2048;
		File file = FileFactory.createFile(zipFile);

		ZipFile zip = new ZipFile(file.getAbsolutePath());
		String newPath = zipFile.substring(0, zipFile.length() - 4);

		FileFactory.createFile(newPath).mkdir();
		Enumeration<?> zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = FileFactory.createFile(newPath, currentEntry);
			// destFile = FileFactory.createFile(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			destinationParent.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

			if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos = destFile.getFileOutputStream();
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}

			destFile.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

			if (currentEntry.endsWith(".zip")) {
				// found a zip file, try to open
				extractFolder(destFile.getAbsolutePath());
			}
		}
		zip.close();

		return newPath;
	}
}
