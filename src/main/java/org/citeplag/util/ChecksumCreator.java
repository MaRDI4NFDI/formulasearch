package org.citeplag.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Creating checksums for files and comparing folder contents on
 * base of checksums.
 * @author Johannes Stegmüller
 */
public class ChecksumCreator {

    private static final Logger LOG = LogManager.getLogger(ChecksumCreator.class.getName());

    /**
     * Create a hashmap of filepaths as keys and corresponding checksums as values
     * from the files in a specified directory.
     * @param folderPath specified directory
     * @param type checksum type SHA, MD2, MD5, SHA-256, SHA-384...
     * @return hashmap(filepaths,checksums)
     * @throws NoSuchAlgorithmException on wrong type checksum algorithm specified
     * @throws IOException wrong directory path specified
     */
    public Map<String, String> createChecksumsForFolder(String folderPath, String type)
            throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(type); //SHA, MD2, MD5, SHA-256, SHA-384...
        Map<String, String> filesAndChecksums = new HashMap<>();

        List<File>  listOfFiles = listAllFiles(folderPath);

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String checksumFile = checksum(file.getPath(), md);
                filesAndChecksums.put(file.getName(), checksumFile);
            }
        }
        return filesAndChecksums;
    }

    /**
     * Get a list of all filepaths in specified directory including subfolders.
     * @param directoryName path to specified directory
     * @return list of Files in directory ( also contains subfolders as entries)
     */
    public List<File> listAllFiles(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<File>();

        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList != null && fList.length > 0) {
            resultList.addAll(Arrays.asList(fList));
            for (File file : fList) {
                if (file.isFile()) {
                    continue;
                } else if (file.isDirectory()) {
                    resultList.addAll(listAllFiles(file.getAbsolutePath()));
                }
            }
        } else {
            LOG.warn("The directory for storing files is empty.");
        }
        return resultList;
    }

    /**
     * Comparing the values of two hasmaps with checksums, sorting the value-lists before.
     * @param filesAndChecksums first hashmap
     * @param filesAndChecksums2 second hashmap
     * @return true if same entries and values, false if not
     */
    public boolean compareChecksums(Map<String, String> filesAndChecksums, Map<String, String> filesAndChecksums2) {
        // Handling comparisons including null-values.
        if (filesAndChecksums == null && filesAndChecksums2 == null) {
            return true;
        }
        if (filesAndChecksums == null || filesAndChecksums2 == null) {
            return false;
        }
        // For simplicity’s sake just comparing the hashes here. If files are moved in harvestDirectory,
        // result will be here true still.
        List<String> checksumsOne = new ArrayList<String>(filesAndChecksums.values());
        List<String> checksumsTwo = new ArrayList<String>(filesAndChecksums2.values());
        Collections.sort(checksumsOne);
        Collections.sort(checksumsTwo);
        return checksumsOne.equals(checksumsTwo);
    }

    /**
     * Creating a checksum for a file specified by path.
     * @param filepath filepath for file
     * @param md Message digest which specifies hash algorithm
     * @return checksum as string.
     * @throws IOException if file is not found.
     */
    @SuppressWarnings("checkstyle:EmptyBlock")
    private String checksum(String filepath, MessageDigest md) throws IOException {

        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) { }; //empty loop to clear the data
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
