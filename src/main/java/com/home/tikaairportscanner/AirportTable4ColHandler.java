package com.home.tikaairportscanner;

import org.xml.sax.SAXException;

/**
 * Handle a data table with 4 columns of content.<p>
 * Columns are iata code, icao code, airport name and airport location.<br>
 * Call the <code>AirportProcessor</code> if valid data are found.
 */
public class AirportTable4ColHandler extends AirportTableColHandler {
    int idx = 0;
    String iata;
    String icao;
    String name;
    String location;

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
                default:
                    throw new IndexOutOfBoundsException("idx is > 3");
            }

            col = false;
            ++idx;
            if (idx == 4) {
                // Only the following is valid
                if (!iata.trim().isEmpty()) {
                    AirportProcessor.processAirport(iata, icao, name, location);
                }
                idx = 0;
            }
        }
    }
}
