package com.example;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.graphhopper.jsprit.core.problem.Location;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class LocationReader {
  public static Location[] getCoordinates(CSVReader reader)
      throws NumberFormatException, CsvValidationException, IOException {
    ArrayList<Location> locations = new ArrayList<Location>();

    String[] line;
    while ((line = reader.readNext()) != null) {
        double latitude = Double.parseDouble(line[0]);
        double longitude = Double.parseDouble(line[1]);

        locations.add(Location.newInstance(latitude, longitude));
    }

    return locations.toArray(new Location[0]);
  }

  public static Location[] getCoordinates(File file)
      throws NumberFormatException, CsvValidationException, IOException {
    CSVReader reader = new CSVReader(new FileReader(file));

    return getCoordinates(reader);
  }
}