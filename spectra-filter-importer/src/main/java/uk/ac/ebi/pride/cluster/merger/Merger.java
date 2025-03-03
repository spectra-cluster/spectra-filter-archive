package uk.ac.ebi.pride.cluster.merger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Merge a group of MGF files
 *
 * 1. Read a group of MGF files in parallel
 * 2. Write spectra to equal size MGF files
 *
 * @author Rui Wang
 * @version $Id$
 */
public class Merger {

    private static final Logger logger = LoggerFactory.getLogger(Merger.class);

    public static void main(String[] args) throws InterruptedException {

        if (args.length != 4) {
            System.err.println("Usage: [Input MGF folder] [Output MGF folder] [Output file size in Gb] [Number of batches to process]");
            System.exit(1);
        }

        // get all the mgf files to be processed
        Set<File> mgfFileQueue = getMgfFiles(args[0]);

        logger.info("Found {} mgf files", mgfFileQueue.size());

        // max file size
        long maxOutputFileSizeInByte = getMaxFileSize(args[2]);

        // output folder
        File outputFolder = new File(args[1]);

        // number of processing threads
        int numberOfBatches = Integer.parseInt(args[3]);

        // split mgf files into groups for process
        FileGroup[] mgfFileGroups = splitMgfFilesIntoGroups(mgfFileQueue, numberOfBatches);

        logger.info("Merging {} mgf files in {} batches", mgfFileQueue.size(), numberOfBatches);
        logger.info("Maximum output mgf file size is {} bytes", maxOutputFileSizeInByte);

        // init count down latch
        CountDownLatch countDown = new CountDownLatch(numberOfBatches);

        // thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        int groupCount = 0;
        for (FileGroup mgfFileGroup : mgfFileGroups) {
            groupCount++;
            executorService.submit(new MgfBySizeSplitter("group-" + groupCount, mgfFileGroup.getFiles(), maxOutputFileSizeInByte, outputFolder, countDown));
        }

        // wait for finish
        try {
            countDown.await();
        } finally {
            executorService.shutdown();
        }

        logger.info("Merging of mgf files have finished successfully");
    }

    private static long getMaxFileSize(String arg) {
        double outputFileSizeInGB = Double.parseDouble(arg);
        return (long) (outputFileSizeInGB * 1024 * 1024 * 1024);
    }

    private static Set<File> getMgfFiles(String arg) {
        Set<File> mgfFileQueue;
        File inputFolder = new File(arg);
        mgfFileQueue = Arrays.stream(inputFolder.listFiles()).filter(file -> file.getName().endsWith("mgf")).collect(Collectors.toCollection(LinkedHashSet::new));
        return mgfFileQueue;
    }

    private static FileGroup[] splitMgfFilesIntoGroups(Set<File> mgfFileQueue, int numberOfBatch) {

        FileGroup[] fileGroups = IntStream.range(0, numberOfBatch).mapToObj(i -> new FileGroup()).toArray(FileGroup[]::new);

        // init file groups
        for (File mgfFile : mgfFileQueue) {
            FileGroup selectedFileGroup = null;

            long fileSize = -1;
            for (FileGroup fileGroup : fileGroups) {
                if (fileSize == -1 || fileSize >= fileGroup.getSumOfFileSize()) {
                    selectedFileGroup = fileGroup;
                    fileSize = selectedFileGroup.getSumOfFileSize();
                }
            }

            selectedFileGroup.addFile(mgfFile);
        }

        return fileGroups;
    }

    private static class FileGroup {
        private final Set<File> files = new LinkedHashSet<>();
        private long sumOfFileSize = 0l;

        void addFile(File file) {
            files.add(file);
            sumOfFileSize += file.length();
        }

        Set<File> getFiles() {
            return files;
        }

        long getSumOfFileSize() {
            return sumOfFileSize;
        }
    }

}
