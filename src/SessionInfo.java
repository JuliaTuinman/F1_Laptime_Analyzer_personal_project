/**
 * Model class for session information
 */
public class SessionInfo {
    private final int season;
    private final int round;
    
    public SessionInfo(int season, int round) {
        this.season = season;
        this.round = round;
    }
    
    public int getSeason() {
        return season;
    }
    
    public int getRound() {
        return round;
    }
    
    @Override
    public String toString() {
        return String.format("Season %d, Round %d", season, round);
    }
}