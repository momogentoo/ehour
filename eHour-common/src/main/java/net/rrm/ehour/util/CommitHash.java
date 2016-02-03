package net.rrm.ehour.util;

/**
 * Automatically updated commit hash
 */
public final class CommitHash {
    public final static String COMMIT_HASH = "9361c2c";
    private final static int LENGTH_OF_SHORT_COMMIT_HASH = 7;

    /**
     * Get short 7-byte commit hash
     * @return
     */
    public final static String getShortCommitHash() {
        if (COMMIT_HASH.length() <= LENGTH_OF_SHORT_COMMIT_HASH) {
            return COMMIT_HASH;
        }
        else {
            return COMMIT_HASH.substring(0, LENGTH_OF_SHORT_COMMIT_HASH);
        }
    }
}
