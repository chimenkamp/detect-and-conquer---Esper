package ubt.process_analytics.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import ubt.process_analytics.esper.EPPMEventType;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVProvider {

    /**
     * Loads and parses a CSV file containing activity, case_id, and timestamp fields.
     *
     * @param filePath the path to the CSV file as a String.
     * @return a list of EPPMEventType objects containing parsed data.
     * @throws IOException           if there is an error reading the file.
     * @throws CsvValidationException if there is an error parsing the CSV.
     */
    public static List<EPPMEventType> loadCSV(String filePath) throws IOException, CsvValidationException {

        List<EPPMEventType> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
        Map<String, Integer> headerMap = new HashMap<>();

        // Keys
        String KEY_ACTIVITY = EConstants.KEY_ACTIVITY.getValue();
        String KEY_TIMESTAMP = EConstants.KEY_TIMESTAMP.getValue();
        String KEY_CASE_ID = EConstants.KEY_CASE_ID.getValue();

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .build();

        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(parser)
                .build()) {

            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new CsvValidationException("CSV file is empty or has no headers");
            }


            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i], i);
            }


            String[] values;
            while ((values = csvReader.readNext()) != null) {

                if (!(
                        headerMap.containsKey(KEY_ACTIVITY)
                                && headerMap.containsKey(KEY_TIMESTAMP)
                                && headerMap.containsKey(KEY_CASE_ID)
                )) {
                    System.out.println(STR."KEY_ACTIVITY: \{headerMap.containsKey(KEY_ACTIVITY)}");
                    System.out.println(STR."KEY_TIMESTAMP: \{headerMap.containsKey(KEY_TIMESTAMP)}");
                    System.out.println(STR."KEY_CASE_ID: \{headerMap.containsKey(KEY_CASE_ID)}");
                    throw new CsvValidationException("CSV file contains invalid headers");
                }
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(values[headerMap.get(KEY_TIMESTAMP)], formatter);
                LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();

                String activity = values[headerMap.get(KEY_ACTIVITY)];
                String caseId = values[headerMap.get(KEY_CASE_ID)];

                records.add(new EPPMEventType(activity, localDateTime, caseId));
            }
        }

        return records;
    }
}

