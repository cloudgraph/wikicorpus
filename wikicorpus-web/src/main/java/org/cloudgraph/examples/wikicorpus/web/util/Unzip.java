package org.cloudgraph.examples.wikicorpus.web.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Unzip {
	private static Log log = LogFactory.getLog(Unzip.class);

	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	public static final void unzip(File file, File baseDir) throws IOException {
		Enumeration entries;
		ZipFile zipFile;

		zipFile = new ZipFile(file);

		entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			if (entry.isDirectory()) {
				// Assume directories are stored parents first then
				// children.
				// This is not robust, just for demonstration purposes.
				File dirEntry = new File(baseDir, entry.getName());
				dirEntry.mkdirs();
				if (log.isDebugEnabled())
				     log.debug("Extracted directory: " + dirEntry.getAbsolutePath());
			}
		}
		entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			if (entry.isDirectory()) {
				continue;
			}

			File fileEntry = new File(baseDir, entry.getName());
			fileEntry.getParentFile().mkdirs();
			if (log.isDebugEnabled())
			    log.debug("Extracting file: " + fileEntry.getAbsolutePath());
			InputStream is = zipFile.getInputStream(entry);
			OutputStream os = new BufferedOutputStream(
				new FileOutputStream(fileEntry));
			copyInputStream(is, os);
		}

		zipFile.close();
	}


}