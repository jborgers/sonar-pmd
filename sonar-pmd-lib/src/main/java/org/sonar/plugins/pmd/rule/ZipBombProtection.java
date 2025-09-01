package org.sonar.plugins.pmd.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Provides protective checks against ZIP bomb attacks when scanning JAR files.
 * Extracted from JavaRulePropertyExtractor to isolate security-related logic.
 */
public final class ZipBombProtection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipBombProtection.class);

    // Security thresholds to prevent ZIP bomb attacks
    private static final int THRESHOLD_ENTRIES = 10_000;
    private static final int THRESHOLD_SIZE_BYTES = 10_000_000; // 10 MB
    private static final double THRESHOLD_RATIO = 15; // Increased to accommodate legitimate JAR files

    private ZipBombProtection() {
        // utility
    }

    /**
     * Scans the given jar file and enforces anti ZIP-bomb thresholds.
     * If suspicious conditions are detected, throws PossibleZipBombException.
     *
     * @param jarFile The open JarFile to scan
     * @param jarPath The file path of the jar (used for logging)
     * @throws PossibleZipBombException when thresholds indicate a possible ZIP bomb
     */
    @SuppressWarnings("java:S5042")
    public static void scanJar(JarFile jarFile, File jarPath) throws PossibleZipBombException {
        // Variables to track security thresholds for preventing ZIP bomb attacks
        int totalEntryArchive = 0;
        long totalSizeArchive = 0;

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements() && totalEntryArchive < THRESHOLD_ENTRIES) {
            totalEntryArchive++;
            JarEntry entry = entries.nextElement();

            // Check for ZIP bomb based on compression ratio
            long size = entry.getSize();
            long compressedSize = entry.getCompressedSize();
            if (size > 0 && compressedSize > 0) {
                double compressionRatio = (double) size / (double) compressedSize;
                if (compressionRatio > THRESHOLD_RATIO) {
                    String msg = "Suspicious compression ratio detected in jar file: " + jarPath
                            + ", entry: " + entry.getName() + ", ratio: " + compressionRatio
                            + ". Possible ZIP bomb attack. Skipping rule extraction.";
                    LOGGER.error(msg);
                    throw new PossibleZipBombException(msg);
                }
            }

            // Track total uncompressed size
            if (size > 0) {
                totalSizeArchive += size;
            }
            if (totalSizeArchive > THRESHOLD_SIZE_BYTES) {
                String msg = "Total uncompressed size exceeds threshold in jar file: " + jarPath
                        + ". Possible ZIP bomb attack. Skipping rule extraction.";
                LOGGER.error(msg);
                throw new PossibleZipBombException(msg);
            }
        }

        if (totalEntryArchive >= THRESHOLD_ENTRIES) {
            String msg = "Too many entries in jar file: " + jarPath
                    + ". Possible ZIP bomb attack. Skipping rule extraction.";
            LOGGER.error(msg);
            throw new PossibleZipBombException(msg);
        }
    }

    public static class PossibleZipBombException extends IOException {
        public PossibleZipBombException(String msg) {
            super(msg);
        }
    }
}
