package org.reactome.release.verifier;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/20/2024
 */
public class CountUtils {

    /**
     * Checks if the difference between two counts is a reduction of five percent or more.  If the drop is less (or
     * the counts are equal or there is an increase), the method returns false.
     *
     * @param newCount More recent count
     * @param oldCount Less recent count
     * @return <code>true</code> if five percent or more drop;<code>false</code> otherwise
     */
    public static boolean greaterThanOrEqualTo5PercentDrop(int newCount, int oldCount) {
        final int percentage = 5;
        return greaterThanOrEqualToXPercentDrop(newCount, oldCount, percentage);
    }

    /**
     * Checks if the difference between two counts is a reduction of "percentage" percent or more.
     * If the drop is less (or the counts are equal or there is an increase), the method returns false.
     *
     * @param newCount More recent count
     * @param oldCount Less recent count
     * @param percentage Percentage drop
     * @return <code>true</code> if "percentage" or more drop;<code>false</code> otherwise
     */
    public static boolean greaterThanOrEqualToXPercentDrop(int newCount, int oldCount, int percentage) {
        double percentChange = (newCount - oldCount) * 100.0d / oldCount;

        return (percentChange < 0 && Math.abs(percentChange) >= percentage);
    }
}
