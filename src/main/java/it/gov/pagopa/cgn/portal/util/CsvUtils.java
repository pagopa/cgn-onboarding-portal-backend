package it.gov.pagopa.cgn.portal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class CsvUtils {

    private CsvUtils() {
    }

    public static long countCsvLines(InputStream content) throws IOException {
        return CSVFormat.EXCEL.parse(new InputStreamReader(content)).stream().count();
    }

    public static Stream<CSVRecord> getCsvRecordStream(InputStream content) throws IOException {
        return CSVFormat.EXCEL.parse(new InputStreamReader(content)).stream();
    }
}
