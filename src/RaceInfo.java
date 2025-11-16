/**
 * Model class for race information
 */
public class RaceInfo {
    private final int round;
    private final String circuitName;
    private final String countryName;
    private final String date;
    private final int sessionKey;
    
    public RaceInfo(int round, String circuitName, String countryName, String date, int sessionKey) {
        this.round = round;
        this.circuitName = circuitName;
        this.countryName = countryName;
        this.date = date;
        this.sessionKey = sessionKey;
    }
    
    public int getRound() {
        return round;
    }
    
    public String getCircuitName() {
        return circuitName;
    }
    
    public String getCountryName() {
        return countryName;
    }
    
    public String getDate() {
        return date;
    }
    
    public int getSessionKey() {
        return sessionKey;
    }
    
    @Override
    public String toString() {
        return String.format("Round %d: %s (%s) - %s", round, circuitName, countryName, date);
    }
}