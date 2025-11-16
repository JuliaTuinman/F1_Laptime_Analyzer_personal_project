/**
 * Model class representing sector times for a lap
 */
public class SectorTimes {
    private final double sector1;
    private final double sector2;
    private final double sector3;
    
    public SectorTimes(double sector1, double sector2, double sector3) {
        this.sector1 = sector1;
        this.sector2 = sector2;
        this.sector3 = sector3;
    }
    
    public double getSector1() {
        return sector1;
    }
    
    public double getSector2() {
        return sector2;
    }
    
    public double getSector3() {
        return sector3;
    }
    
    public double getTotalTime() {
        return sector1 + sector2 + sector3;
    }
    
    @Override
    public String toString() {
        return String.format("S1: %.3fs, S2: %.3fs, S3: %.3fs (Total: %.3fs)",
            sector1, sector2, sector3, getTotalTime());
    }
}