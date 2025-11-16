import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import org.json.*;

/**
 * Service class for interacting with F1 APIs
 * Handles all HTTP requests and JSON parsing
 */
public class F1ApiService {
    private static final String OPENF1_BASE_URL = "https://api.openf1.org/v1";
    
    /**
     * Fetches all race sessions for a given season
     * 
     * @param season Year of the season
     * @return List of RaceInfo objects
     */
    public List<RaceInfo> fetchRaces(int season) throws Exception {
        String endpoint = String.format("%s/sessions?year=%d&session_name=Race", 
            OPENF1_BASE_URL, season);
        String jsonResponse = makeHttpRequest(endpoint);
        
        return parseRaceInfo(jsonResponse);
    }
    
    
    /**
     * Fetches lap data for all drivers in a specific race
     * * @param season Year of the season
     * @param round Race round number
     * @return List of DriverLapData objects sorted by finishing position
     */
    public List<DriverLapData> fetchLapData(int season, int round) throws Exception {
        // First, get the session key for the race
        int sessionKey = getSessionKey(season, round);
        
        if (sessionKey == -1) {
            throw new Exception("Could not find session key for this race");
        }
        
        // Fetch all laps for this session
        String endpoint = String.format("%s/laps?session_key=%d", OPENF1_BASE_URL, sessionKey);
        String jsonResponse = makeHttpRequest(endpoint);
        
        // Parse lap data
        List<DriverLapData> lapData = parseLapData(jsonResponse);
        
        // Fetch and set finishing positions for all drivers in a single batch operation
        fetchFinishingPositions(sessionKey, lapData); 
        
        // Sort by finishing position
        lapData.sort(Comparator.comparingInt(DriverLapData::getFinishingPosition));
        
        return lapData;
    }
    
    /**
     * Fetches and sets finishing positions for all drivers in the race efficiently.
     * This makes ONE API call instead of one per driver.
     * @param sessionKey The session key for the race
     * @param lapData List of DriverLapData to update with positions
     */
    private void fetchFinishingPositions(int sessionKey, List<DriverLapData> lapData) throws Exception {
        // Create a map for quick lookup of DriverLapData by driver number
        Map<Integer, DriverLapData> driverMap = new HashMap<>();
        for (DriverLapData driver : lapData) {
            driverMap.put(driver.getDriverNumber(), driver);
        }
        
        // Fetch all position updates for the session with ONE API call
        // NOTE: The 'position' endpoint can take a long time to return, consider filtering 
        // by 'is_retired' = false to get only the cars that finished, but for comprehensive 
        // results (including retired), this is the better approach.
        String endpoint = String.format("%s/position?session_key=%d", 
            OPENF1_BASE_URL, sessionKey);
        String jsonResponse = makeHttpRequest(endpoint);
        
        JSONArray allPositions = new JSONArray(jsonResponse);
        
        // Map to hold the last recorded position for each driver
        Map<Integer, Integer> finalPositions = new HashMap<>();
        
        // Iterate through all position records to find the *last* one for each driver
        // The array is typically chronological, so the last record is the final one.
        for (int i = 0; i < allPositions.length(); i++) {
            JSONObject positionEntry = allPositions.getJSONObject(i);
            int driverNumber = positionEntry.getInt("driver_number");
            int position = positionEntry.getInt("position");
            
            // Only update if the driver is in our lapData list
            if (driverMap.containsKey(driverNumber)) {
                 finalPositions.put(driverNumber, position);
            }
        }
        
        // Apply the final positions to the DriverLapData objects
        for (Map.Entry<Integer, Integer> entry : finalPositions.entrySet()) {
            int driverNumber = entry.getKey();
            int finalPos = entry.getValue();
            
            DriverLapData driver = driverMap.get(driverNumber);
            if (driver != null) {
                driver.setFinishingPosition(finalPos);
            }
        }
        
        // Any driver not found in the position data is considered to not have finished (DNF/DNS/DSQ)
        // They will keep their default position (999) which is handled in DriverLapData.java
    }
    
    /**
     * Fetches sector times for specific drivers' fastest laps
     * 
     * @param season Year of the season
     * @param round Race round number
     * @param driverNumbers List of driver numbers
     * @return Map of driver number to their sector times
     */
    public Map<Integer, SectorTimes> fetchSectorTimes(int season, int round, List<Integer> driverNumbers) 
            throws Exception {
        int sessionKey = getSessionKey(season, round);
        
        Map<Integer, SectorTimes> sectorTimesMap = new HashMap<>();
        
        for (int driverNum : driverNumbers) {
            String endpoint = String.format("%s/laps?session_key=%d&driver_number=%d", 
                OPENF1_BASE_URL, sessionKey, driverNum);
            String jsonResponse = makeHttpRequest(endpoint);
            
            SectorTimes sectors = parseSectorTimes(jsonResponse);
            if (sectors != null) {
                sectorTimesMap.put(driverNum, sectors);
            }
        }
        
        return sectorTimesMap;
    }
    
    /**
     * Gets the session key for a specific race
     */
    private int getSessionKey(int season, int round) throws Exception {
        // Fetch sessions for the given year
        String endpoint = String.format("%s/sessions?year=%d&session_name=Race", 
            OPENF1_BASE_URL, season);
        String jsonResponse = makeHttpRequest(endpoint);
        
        JSONArray sessions = new JSONArray(jsonResponse);
        
        // Find the race that matches our round (counting races chronologically)
        int raceCount = 0;
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.getJSONObject(i);
            
            // Check if this is a race session
            if (session.getString("session_name").equals("Race")) {
                raceCount++;
                if (raceCount == round) {
                    return session.getInt("session_key");
                }
            }
        }
        
        return -1; // Not found
    }
    
    /**
     * Parses race information from JSON response
     */
    private List<RaceInfo> parseRaceInfo(String jsonResponse) {
        List<RaceInfo> races = new ArrayList<>();
        JSONArray sessions = new JSONArray(jsonResponse);
        
        int roundNumber = 1;
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.getJSONObject(i);
            
            String circuitName = session.optString("circuit_short_name", "Unknown Circuit");
            String countryName = session.optString("country_name", "Unknown Country");
            String dateStart = session.optString("date_start", "Unknown Date");
            int sessionKey = session.getInt("session_key");
            
            // Format date to be more readable (extract just the date part)
            String formattedDate = dateStart.length() >= 10 ? dateStart.substring(0, 10) : dateStart;
            
            races.add(new RaceInfo(roundNumber, circuitName, countryName, formattedDate, sessionKey));
            roundNumber++;
        }
        
        return races;
    }
    
    /**
     * Parses lap data from JSON response
     */
    private List<DriverLapData> parseLapData(String jsonResponse) {
        Map<Integer, DriverLapData> driverMap = new HashMap<>();
        
        JSONArray laps = new JSONArray(jsonResponse);
        
        for (int i = 0; i < laps.length(); i++) {
            JSONObject lap = laps.getJSONObject(i);
            
            int driverNumber = lap.getInt("driver_number");
            
            // Skip invalid laps (pit laps, incomplete laps)
            if (lap.optBoolean("is_pit_out_lap", false) || 
                lap.isNull("lap_duration")) {
                continue;
            }
            
            double lapDuration = lap.getDouble("lap_duration");
            int lapNumber = lap.getInt("lap_number");
            
            // Get or create driver data
            DriverLapData driverData = driverMap.getOrDefault(
                driverNumber, 
                new DriverLapData(driverNumber)
            );
            
            // Add lap time
            driverData.addLapTime(lapDuration, lapNumber);
            
            driverMap.put(driverNumber, driverData);
        }
        
        // Convert map to list (no sorting here, will be sorted after positions are fetched)
        List<DriverLapData> result = new ArrayList<>(driverMap.values());
        
        return result;
    }
    
    /**
     * Parses sector times from JSON response
     * Returns the sector times from the fastest lap
     */
    private SectorTimes parseSectorTimes(String jsonResponse) {
        JSONArray laps = new JSONArray(jsonResponse);
        
        if (laps.length() == 0) {
            return null;
        }
        
        // Find the lap with the fastest lap time
        JSONObject fastestLap = null;
        double fastestTime = Double.MAX_VALUE;
        
        for (int i = 0; i < laps.length(); i++) {
            JSONObject lap = laps.getJSONObject(i);
            
            // Skip invalid laps
            if (lap.optBoolean("is_pit_out_lap", false) || 
                lap.isNull("lap_duration") ||
                lap.isNull("duration_sector_1") ||
                lap.isNull("duration_sector_2") ||
                lap.isNull("duration_sector_3")) {
                continue;
            }
            
            double lapTime = lap.getDouble("lap_duration");
            
            if (lapTime < fastestTime) {
                fastestTime = lapTime;
                fastestLap = lap;
            }
        }
        
        if (fastestLap == null) {
            return null;
        }
        
        return new SectorTimes(
            fastestLap.getDouble("duration_sector_1"),
            fastestLap.getDouble("duration_sector_2"),
            fastestLap.getDouble("duration_sector_3")
        );
    }
    
    /**
     * Makes an HTTP GET request to the specified URL
     */
    private String makeHttpRequest(String urlString) throws Exception {
        URI uri = new URI(urlString); //VSCode gave warning of cast directly to URL (from URI) was depreciated
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP request failed with code: " + responseCode);
        }
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        String inputLine;
        StringBuilder content = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();
        
        return content.toString();
    }
}