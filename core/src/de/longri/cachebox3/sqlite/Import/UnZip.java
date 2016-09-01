/*
 * Copyright (C) 2016 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.sqlite.Import;


import java.io.*;
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
	static public String extractFolder(String zipFile) throws ZipException, IOException
	{
		System.out.println("extract => " + zipFile);
		int BUFFER = 2048;
		File file = new File(zipFile);

		ZipFile zip = new ZipFile(file);
		String newPath = zipFile.substring(0, zipFile.length() - 4);

		new File(newPath).mkdir();
		Enumeration<?> zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements())
		{
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath, currentEntry);
			// destFile = new File(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			destinationParent.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

			if (!entry.isDirectory())
			{
				BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1)
				{
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}

			destFile.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

			if (currentEntry.endsWith(".zip"))
			{
				// found a zip file, try to open
				extractFolder(destFile.getAbsolutePath());
			}
		}
		zip.close();

		return newPath;
	}
}
