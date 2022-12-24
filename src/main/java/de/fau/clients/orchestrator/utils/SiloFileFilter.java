package de.fau.clients.orchestrator.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A file filter used in file selection dialogues to load/save only *.silo-files.
 */
public final class SiloFileFilter extends FileFilter {

    private final static String SILO_SUFFIX = "silo";

    /*
     * Get the extension of a file.
     */
    private static String getExtension(final File file) {
        String ext = null;
        final String fileName = file.getName();
        int idx = fileName.lastIndexOf('.');

        if (idx > 0 && idx < fileName.length() - 1) {
            ext = fileName.substring(idx + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Accept all directories and all *.silo files.
     *
     * @param file The file to check.
     * @return <code>true</code> if file filter matches, otherwise <code>false</code>.
     */
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        final String ext = getExtension(file);
        if (ext != null) {
            if (ext.equals(SILO_SUFFIX)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return The description of this filter.
     */
    @Override
    public String getDescription() {
        return "SiLA Orchestrator File (*.silo)";
    }
}
