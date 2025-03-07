/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.pride.cluster.tools.reanalysis.control.util;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.pride.cluster.tools.reanalysis.exception.UnspecifiedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Kenneth
 */
public class JarLookupService {

    /**
     * This method looks for the appropriate jar in the given folder (excludes the lib folder)
     * @param regex the regex to match the file
     * @param searchRoot the root directory to start te search
     * @return a file matching the regex
     * @throws UnspecifiedException
     */
    public static File lookupFile(String regex, File searchRoot) throws UnspecifiedException {
        if (!searchRoot.isDirectory()) {
            throw new IllegalArgumentException(searchRoot + " is no directory.");
        }
        final Pattern p = Pattern.compile(regex);
        Iterator iterateFiles = FileUtils.iterateFiles(searchRoot, new String[]{"jar"}, true);
        List<File> matchingFiles = new ArrayList<>();
        while (iterateFiles.hasNext()) {
            File file = (File) iterateFiles.next();
            if (p.matcher(file.getName()).matches() & !file.getParent().equalsIgnoreCase("lib")) {
                matchingFiles.add(file);
            }
        }
        if (matchingFiles.size() > 1) {
            throw new UnspecifiedException("There are multiple file candidates (" + matchingFiles.size() + "), please ensure only a single version is present and try again");
        } else if (matchingFiles.isEmpty()) {
            throw new UnspecifiedException("There are no matching files present");
        }
        return matchingFiles.get(0);
    }

}
