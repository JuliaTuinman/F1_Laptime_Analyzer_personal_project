/**
 * Utility class for formatting lap times and other data
 */
public class Formatter {
    
    /**
     * Formats a lap time in seconds to MM:SS.mmm format
     * 
     * @param seconds Time in seconds
     * @return Formatted string
     */
    public static String formatLapTime(double seconds) {
        if (seconds == Double.MAX_VALUE || seconds == 0.0) {
            return "N/A";
        }
        
        int minutes = (int) (seconds / 60);
        double remainingSeconds = seconds % 60;
        
        return String.format("%d:%06.3f", minutes, remainingSeconds);
    }
    
    /**
     * Formats a sector time in seconds
     * 
     * @param seconds Time in seconds
     * @return Formatted string
     */
    public static String formatSectorTime(double seconds) {
        if (seconds == 0.0 || Double.isNaN(seconds)) {
            return "N/A";
        }
        
        return String.format("%.3fs", seconds);
    }
    
    /**
     * Formats a time difference with +/- sign
     * 
     * @param difference Time difference in seconds
     * @return Formatted string with sign
     */
    public static String formatDifference(double difference) {
        if (difference == 0.0) {
            return "Equal";
        }
        
        String sign = difference > 0 ? "+" : "";
        return String.format("%s%.3fs", sign, difference);
    }
}