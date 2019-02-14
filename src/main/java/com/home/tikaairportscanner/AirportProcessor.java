package com.home.tikaairportscanner;

import com.home.util.DbParameters;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

/**
 * Airport data processor.
 */
public class AirportProcessor {
    private static final Logger LOG = Logger.getLogger(AirportProcessor.class.getName());
    private static final String UPDTUSER = "Scanner";
    private static final DbParameters DB_PARAMS = new DbParameters("AirportScanner.properties");
    private static int unchanged = 0;
    private static int inserted = 0;
    private static int updated = 0;

    /**
     * Insert or update the database using the given airport values.
     *
     * @param iata     the iata code
     * @param icao     the icao code
     * @param name     the airports name
     * @param location the airports location
     */
    public static void processAirport(String iata, String icao, String name, String location) {
        // Can happen
        if (iata.trim().isEmpty()) {
            throw new IllegalArgumentException("IATA CODE is EMPTY");
        }

        String schema = (DB_PARAMS.getSchema().trim().isEmpty() ? "" : (DB_PARAMS.getSchema() + "."));
        String table = DB_PARAMS.getCheckTable();
        String selectAirport = "SELECT ICAOCODE, DESCR, VERSION from " + schema + table + " WHERE IATACODE = ?";

        // Valid parameters to process
        try {
            PreparedStatement preparedStatement = TikaAirportLoader.getConnection().prepareStatement(selectAirport,
                                                                                                     ResultSet.TYPE_SCROLL_SENSITIVE,
                                                                                                     ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, iata);
            ResultSet rs = preparedStatement.executeQuery();

            // Check if the result set is empty
            if (!rs.isBeforeFirst()) {
                // Insert a new airport
                String insertAirport = "INSERT INTO " + schema + table
                        + " (IATACODE, ICAOCODE, DESCR, CREATED, CREAUSER, UPDATED, UPDTUSER, VERSION)"
                        + " VALUES (?,?,?,?,?,?,?,?)";
                preparedStatement = TikaAirportLoader.getConnection().prepareStatement(insertAirport);
                preparedStatement.setString(1, iata);
                preparedStatement.setString(2, icao);
                preparedStatement.setString(3, name + ", " + location);
                preparedStatement.setTimestamp(4, getCurrentTimeStamp());
                preparedStatement.setString(5, UPDTUSER);
                preparedStatement.setTimestamp(6, getCurrentTimeStamp());
                preparedStatement.setString(7, UPDTUSER);
                preparedStatement.setInt(8, 0);

                preparedStatement.executeUpdate();

                ++inserted;
            }
            else {
                // Update the current row in case values are empty
                while (rs.next()) {
                    String icaocode = rs.getString("ICAOCODE");
                    String descr = rs.getString("DESCR");
                    int version = rs.getInt("VERSION");

                    boolean updateNeeded = false;

                    if (icaocode.isEmpty()) {
                        icaocode = icao;
                        updateNeeded = true;
                    }

                    if (descr.isEmpty()) {
                        descr = name + ", " + location;
                        updateNeeded = true;
                    }

                    if (updateNeeded) {
                        ++version;
                        String updateAirport = "UPDATE " + schema + table + " SET ICAOCODE = ?, DESCR = ?, UPDATED = ?, UPDTUSER = ?, VERSION = ? WHERE IATACODE = ?";
                        preparedStatement = TikaAirportLoader.getConnection().prepareStatement(updateAirport);
                        preparedStatement.setString(1, icaocode);
                        preparedStatement.setString(2, descr);
                        preparedStatement.setTimestamp(3, getCurrentTimeStamp());
                        preparedStatement.setString(4, UPDTUSER);
                        preparedStatement.setInt(5, version);
                        preparedStatement.setString(6, iata);

                        preparedStatement.executeUpdate();

                        ++updated;
                    }
                    else {
                        ++unchanged;
                    }
                }
            }
        }
        catch (SQLException ex) {
            LOG.severe(ex.getMessage());
        }
    }

    /**
     * Create the current Timestamp value.
     *
     * @return the current Timestamp value
     */
    private static Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }

    /**
     * Get the number of inserted rows.
     *
     * @return the number of inserts
     */
    public static int getInserted() {
        return inserted;
    }

    /**
     * Get the number of updated rows.
     *
     * @return the number of updates
     */
    public static int getUpdated() {
        return updated;
    }

    /**
     * Get the number of unchanged rows.
     *
     * @return the number of unchanged rows
     */
    public static int getUnchanged() {
        return unchanged;
    }
}
