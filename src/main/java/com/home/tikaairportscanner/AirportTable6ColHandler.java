package com.home.tikaairportscanner;

import org.xml.sax.SAXException;

/**
 * Handle a data table with 6 columns of content.<p>
 * Columns are iata code, icao code, airport name and airport location, timezone information, timezone change
 * information. Currently the two timezone informations are not used in further processing.<br>
 * Call the <code>AirportProcessor</code> if valid data are found.
 */
public class AirportTable6ColHandler extends AirportTableColHandler {
    int idx = 0;
    String iata;
    String icao;
    String name;
    String location;
    String time;
    String dst;

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (col) {
            String val = new String(ch, start, length);

            switch (idx) {
                case 0:
                    iata = val.replaceAll("\n", "");
                    break;
                case 1:
                    icao = val.replaceAll("\n", "");
                    break;
                case 2:
                    name = val.replaceAll("\n", "");
                    break;
                case 3:
                    location = val.replaceAll("\n", "");
                    break;
                case 4:
                    time = val.replaceAll("\n", "");
                    break;
                case 5:
                    dst = val.replaceAll("\n", "");
                    break;
                default:
                    throw new IndexOutOfBoundsException("idx is > 5");
            }

            col = false;
            ++idx;
            if (idx == 6) {
                // Only the following is valid
                if (!iata.trim().isEmpty()) {
                    AirportProcessor.processAirport(iata, icao, name, location);
                }
                idx = 0;
            }
        }
    }
}
