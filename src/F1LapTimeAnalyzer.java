import java.util.*;

/**
 * Main class for F1 Lap Time Analyzer
 * Entry point for the application
 */
public class F1LapTimeAnalyzer {
    private static final Scanner scanner = new Scanner(System.in);
    private static final F1ApiService apiService = new F1ApiService();
    
    public static void main(String[] args) {
        System.out.println("=== F1 Lap Time Analyzer ===\n");
        
        try {
            // Get session information from user
            SessionInfo sessionInfo = getUserInput();
            
            // Fetch lap data
            System.out.println("\nFetching race data...");
            List<DriverLapData> lapData = apiService.fetchLapData(
                sessionInfo.getSeason(), 
                sessionInfo.getRound()
            );
            
            if (lapData.isEmpty()) {
                System.out.println("No data available for this race.");
                return;
            }
            
            // Display results
            displayResults(lapData);
            
            // Main menu loop
            boolean running = true;
            while (running) {
                running = displayMenu(lapData, sessionInfo);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } 
    }
    
    private static SessionInfo getUserInput() {
        System.out.print("Enter season (e.g., 2023 or 2024): ");
        int season = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        try {
            // Fetch and display available races
            System.out.println("\nFetching available races for " + season + "...");
            List<RaceInfo> races = apiService.fetchRaces(season);
            
            if (races.isEmpty()) {
                System.out.println("No races found for this season.");
                System.out.print("Enter race round number manually: ");
                int round = scanner.nextInt();
                scanner.nextLine();
                return new SessionInfo(season, round);
            }
            
            // Display races
            System.out.println("\n=== Available Races for " + season + " ===");
            System.out.printf("%-5s %-30s %-20s%n", "Round", "Circuit", "Date");
            System.out.println("-".repeat(55));
            
            for (RaceInfo race : races) {
                System.out.printf("%-5d %-30s %-20s%n",
                    race.getRound(),
                    race.getCircuitName(),
                    race.getDate()
                );
            }
            
            System.out.print("\nEnter race round number: ");
            int round = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            return new SessionInfo(season, round);
            
        } catch (Exception e) {
            System.out.println("Error fetching races: " + e.getMessage());
            System.out.print("Enter race round number manually: ");
            int round = scanner.nextInt();
            scanner.nextLine();
            return new SessionInfo(season, round);
        }
    }
    
    private static void displayResults(List<DriverLapData> lapData) {
        System.out.println("\n=== Race Results (Final Positions) ===");
        // UPDATED: Only Pos, No., and Driver Name columns
        System.out.printf("%-5s %-5s %-30s%n", 
            "Pos", "No.", "Driver");
        System.out.println("-".repeat(35)); // Adjusted length of separator
        
        for (DriverLapData driver : lapData) {
            // Only display drivers with valid finishing positions
            if (driver.getFinishingPosition() < 999) {
                // UPDATED: Only showing position, number, and name
                System.out.printf("%-5d %-5d %-30s%n",
                    driver.getFinishingPosition(),
                    driver.getDriverNumber(),
                    driver.getDriverName()
                );
            }
        }
    }
    
    private static boolean displayMenu(List<DriverLapData> lapData, SessionInfo sessionInfo) {
        System.out.println("\n=== Analysis Menu ===");
        System.out.println("1. Display Top 3 Fastest Laps");
        System.out.println("2. View Average Lap Times for Specific Driver");
        System.out.println("3. Compare Sector Times Between Two Drivers");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        switch (choice) {
            case 1:
                displayTop3FastestLaps(lapData);
                break;
            case 2:
                displayDriverAverageLapTime(lapData);
                break;
            case 3:
                compareSectorTimes(lapData, sessionInfo);
                break;
            case 4:
                System.out.println("Exiting...");
                return false;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        
        return true;
    }
    
    private static void displayTop3FastestLaps(List<DriverLapData> lapData) {
        System.out.println("\n=== Top 3 Fastest Laps ===");
        
        List<DriverLapData> sorted = new ArrayList<>(lapData);
        sorted.sort(Comparator.comparingDouble(DriverLapData::getFastestLapTime));
        
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            DriverLapData driver = sorted.get(i);
            System.out.printf("%d. %s - %s (Lap %d)%n",
                (i + 1),
                driver.getDriverName(),
                Formatter.formatLapTime(driver.getFastestLapTime()),
                driver.getFastestLapNumber()
            );
        }
    }
    
    private static void displayDriverAverageLapTime(List<DriverLapData> lapData) {
        System.out.print("\nEnter driver number (e.g., 1 for Verstappen): ");
        int driverNumber = scanner.nextInt();
        scanner.nextLine();
        
        Optional<DriverLapData> driver = lapData.stream()
            .filter(d -> d.getDriverNumber() == driverNumber)
            .findFirst();
        
        if (driver.isPresent()) {
            DriverLapData d = driver.get();
            System.out.println("\n=== Driver Statistics ===");
            System.out.println("Driver: " + d.getDriverName());
            System.out.println("Number: " + d.getDriverNumber());
            System.out.println("Total Laps: " + d.getTotalLaps());
            System.out.println("Fastest Lap: " + Formatter.formatLapTime(d.getFastestLapTime()) 
                + " (Lap " + d.getFastestLapNumber() + ")");
            System.out.println("Average Lap Time: " + Formatter.formatLapTime(d.getAverageLapTime()));
        } else {
            System.out.println("Driver not found.");
        }
    }
    
    private static void compareSectorTimes(List<DriverLapData> lapData, SessionInfo sessionInfo) {
        System.out.print("\nEnter first driver number: ");
        int driver1Num = scanner.nextInt();
        
        System.out.print("Enter second driver number: ");
        int driver2Num = scanner.nextInt();
        scanner.nextLine();
        
        try {
            Map<Integer, SectorTimes> sectorData = apiService.fetchSectorTimes(
                sessionInfo.getSeason(), 
                sessionInfo.getRound(),
                Arrays.asList(driver1Num, driver2Num)
            );
            
            SectorTimes sectors1 = sectorData.get(driver1Num);
            SectorTimes sectors2 = sectorData.get(driver2Num);
            
            if (sectors1 == null || sectors2 == null) {
                System.out.println("Sector data not available for one or both drivers.");
                return;
            }
            
            Optional<DriverLapData> d1 = lapData.stream()
                .filter(d -> d.getDriverNumber() == driver1Num)
                .findFirst();
            Optional<DriverLapData> d2 = lapData.stream()
                .filter(d -> d.getDriverNumber() == driver2Num)
                .findFirst();
            
            System.out.println("\n=== Fastest Lap Sector Comparison ===");
            System.out.printf("%-20s %-15s %-15s %-15s%n", 
                "Driver", "Sector 1", "Sector 2", "Sector 3");
            System.out.println("-".repeat(65));
            
            d1.ifPresent(driver -> {
                System.out.printf("%-20s %-15s %-15s %-15s%n",
                    driver.getDriverName(),
                    Formatter.formatSectorTime(sectors1.getSector1()),
                    Formatter.formatSectorTime(sectors1.getSector2()),
                    Formatter.formatSectorTime(sectors1.getSector3())
                );
            });
            
            d2.ifPresent(driver -> {
                System.out.printf("%-20s %-15s %-15s %-15s%n",
                    driver.getDriverName(),
                    Formatter.formatSectorTime(sectors2.getSector1()),
                    Formatter.formatSectorTime(sectors2.getSector2()),
                    Formatter.formatSectorTime(sectors2.getSector3())
                );
            });
            
            // Show differences
            System.out.println("\n=== Sector Differences (Driver 1 - Driver 2) ===");
            double diff1 = sectors1.getSector1() - sectors2.getSector1();
            double diff2 = sectors1.getSector2() - sectors2.getSector2();
            double diff3 = sectors1.getSector3() - sectors2.getSector3();
            
            System.out.printf("Sector 1: %s%n", Formatter.formatDifference(diff1));
            System.out.printf("Sector 2: %s%n", Formatter.formatDifference(diff2));
            System.out.printf("Sector 3: %s%n", Formatter.formatDifference(diff3));
            
        } catch (Exception e) {
            System.out.println("Error fetching sector times: " + e.getMessage());
        }
    }
}