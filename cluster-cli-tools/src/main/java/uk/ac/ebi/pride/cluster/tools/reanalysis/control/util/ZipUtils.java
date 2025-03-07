
package uk.ac.ebi.pride.cluster.tools.reanalysis.control.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Kenneth Verheggen
 */
public class ZipUtils {

    /**
     * the Logging instance
     */
    private static Logger LOGGER = Logger.getLogger(ZipUtils.class);
    /**
     * boolean indicating if the file is unzipped
     */
    private static boolean unzipped = false;


    /**
     * Unzips a file to the specified folder
     *
     * @param archive the zipped folder
     * @param outputDir the destination folder
     */
    public static void unzipArchive(File archive, File outputDir) {
        try (ZipFile zipfile = new ZipFile(archive)) {
            for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
            unzipped = true;
            zipfile.close();
        } catch (Exception e) {
            LOGGER.error("Error while extracting file " + archive, e);
        } finally {
            if (unzipped) {
                archive.delete();
            }
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        LOGGER.debug("Extracting: " + entry);

        try (BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry)); BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            IOUtils.copy(inputStream, outputStream);
            outputStream.flush();
        }
    }

    private static void createDir(File dir) {
        LOGGER.debug("Creating dir " + dir.getName());
        if (!dir.mkdirs() & !dir.exists()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    /**
     * Zips a single file to the specified folder
     */
    public static void zipFile(File inputFile, File zipFile) {

        try (FileInputStream fileInputStream = new FileInputStream(inputFile);
                FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            ZipEntry zipEntry = new ZipEntry(inputFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
            byte[] buf = new byte[1024];
            int bytesRead;

            // Read the input file by chucks of 1024 bytes
            // and write the read bytes to the zip stream
            while ((bytesRead = fileInputStream.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, bytesRead);
            }

            // close ZipEntry to store the stream to the file
            zipOutputStream.closeEntry();
            LOGGER.debug("Regular file :" + inputFile.getCanonicalPath() + " is zipped to archive :" + zipFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error(e);
        }

    }

    /**
     * Zips an entire folder in one go
     */
    static public void zipFolder(File inputFolder, File zipFile) throws UnspecifiedException, IOException {
        if (zipFile.exists()) {
            zipFile.delete();
        }
        zipFile.getParentFile().mkdirs();
        zipFile.createNewFile();
        try (FileOutputStream fileWriter = new FileOutputStream(zipFile); ZipOutputStream zip = new ZipOutputStream(fileWriter)) {
            addFolderToZip("", inputFolder.getAbsolutePath(), zip);
            zip.flush();
        }
    }

    static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
            throws UnspecifiedException, FileNotFoundException, IOException {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
            throws UnspecifiedException, IOException {
        File folder = new File(srcFolder);

        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
            }
        }
    }
}
