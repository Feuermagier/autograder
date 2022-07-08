package de.firemage.autograder.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    public static void unzip(File input, File destination) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream in = new ZipInputStream(new FileInputStream(input));
        ZipEntry entry = in.getNextEntry();
        while (entry != null) {
            File newFile = createFileFromZipEntry(destination, entry);
            if (entry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            entry = in.getNextEntry();
        }
        in.closeEntry();
        in.close();
    }

    private static File createFileFromZipEntry(File destination, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destination, zipEntry.getName());

        String destDirPath = destination.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
