package de.fau.clients.orchestrator.utils;

/**
 * Class representing a version number. The version is divided in a major number, a minor number and
 * a patch level. Best practice is to use the major number for indicating huge changes in the
 * software, the minor number for new features or additional functionality and the patch level for
 * bug fixes and error corrections.
 */
public class VersionNumber {

    protected int major = 0;
    protected int minor = 0;
    protected int patch = 0;

    /**
     * Parses a version string into a <code>VersionNumber</code> object to easily access and compare
     * the number fields.
     *
     * @param versionStr A version string (e.g. "2.1", "v1.2.3", etc.).
     * @return A <code>VersionNumber</code> object.
     */
    public static VersionNumber parseVersionString(final String versionStr) {
        int[] numbers = {0, 0, 0};
        final String[] versionChunks = versionStr.split("[.]", 3);
        int i = 0;
        for (final String chunk : versionChunks) {
            final String[] numberPart = chunk.split("\\D+", 3);
            for (final String num : numberPart) {
                if (!num.isBlank()) {
                    try {
                        numbers[i++] = Integer.parseInt(num);
                    } catch (NumberFormatException ex) {
                    }
                    break;
                }
            }
        }
        return new VersionNumber(numbers[0], numbers[1], numbers[2]);
    }

    public VersionNumber() {
    }

    public VersionNumber(int majorNumber, int minorNumber, int patchLevel) {
        this.major = majorNumber;
        this.minor = minorNumber;
        this.patch = patchLevel;
    }

    public int getMajorNumber() {
        return major;
    }

    public void setMajorNumber(int major) {
        this.major = major;
    }

    public int getMinorNumber() {
        return minor;
    }

    public void setMinorNumber(int minor) {
        this.minor = minor;
    }

    public int getPatchLevel() {
        return patch;
    }

    public void setPatchLevel(int patchLevel) {
        this.patch = patchLevel;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VersionNumber) {
            final VersionNumber versNum = (VersionNumber) obj;
            return (this.major == versNum.major
                    && this.minor == versNum.minor
                    && this.patch == versNum.patch);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.major;
        hash = 19 * hash + this.minor;
        hash = 19 * hash + this.patch;
        return hash;
    }
}
