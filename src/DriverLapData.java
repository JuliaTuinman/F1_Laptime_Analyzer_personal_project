import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing lap data for a single driver
 */
public class DriverLapData {
    private final int driverNumber;
    private final String driverName;
    private final List<Double> lapTimes;
    private final List<Integer> lapNumbers;
    private double fastestLapTime;
    private int fastestLapNumber;
    private int finishingPosition;
    
    public DriverLapData(int driverNumber) {
        this.driverNumber = driverNumber;
        this.driverName = getDriverNameByNumber(driverNumber);
        this.lapTimes = new ArrayList<>();
        this.lapNumbers = new ArrayList<>();
        this.fastestLapTime = Double.MAX_VALUE;
        this.fastestLapNumber = 0;
        this.finishingPosition = 999; // Default to end if not set
    }
    
    /**
     * Adds a lap time to this driver's data
     */
    public void addLapTime(double lapTime, int lapNumber) {
        lapTimes.add(lapTime);
        lapNumbers.add(lapNumber);
        
        if (lapTime < fastestLapTime) {
            fastestLapTime = lapTime;
            fastestLapNumber = lapNumber;
        }
    }
    
    /**
     * Calculates average lap time
     */
    public double getAverageLapTime() {
        if (lapTimes.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (double time : lapTimes) {
            sum += time;
        }
        
        return sum / lapTimes.size();
    }
    
    /**
     * Gets the driver name based on their number
     */
    private String getDriverNameByNumber(int number) {
        switch (number) {
            case 1: return "Max Verstappen";
            case 2: return "Logan Sargeant";
            case 3: return "Daniel Ricciardo";
            case 4: return "Lando Norris";
            case 10: return "Pierre Gasly";
            case 11: return "Sergio Perez";
            case 14: return "Fernando Alonso";
            case 16: return "Charles Leclerc";
            case 18: return "Lance Stroll";
            case 20: return "Kevin Magnussen";
            case 21: return "Nyck de Vries";
            case 22: return "Yuki Tsunoda";
            case 23: return "Alexander Albon";
            case 24: return "Zhou Guanyu";
            case 27: return "Nico Hulkenberg";
            case 30: return "Liam Lawson";
            case 31: return "Esteban Ocon";
            case 40: return "Liam Lawson";
            case 43: return "Franco Colapinto";
            case 44: return "Lewis Hamilton";
            case 55: return "Carlos Sainz";
            case 63: return "George Russell";
            case 77: return "Valtteri Bottas";
            case 81: return "Oscar Piastri";
            default: return "Driver #" + number;
        }
    }
    
    // Getters
    public int getDriverNumber() {
        return driverNumber;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public double getFastestLapTime() {
        return fastestLapTime;
    }
    
    public int getFastestLapNumber() {
        return fastestLapNumber;
    }
    
    public int getTotalLaps() {
        return lapTimes.size();
    }
    
    public List<Double> getLapTimes() {
        return new ArrayList<>(lapTimes);
    }
    
    public int getFinishingPosition() {
        return finishingPosition;
    }
    
    public void setFinishingPosition(int position) {
        this.finishingPosition = position;
    }
}