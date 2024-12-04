package it.gov.pagopa.cgn.portal.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvUtils {
	private CsvUtils() {
	}

	public static long countCsvLines(InputStream content) throws IOException {
		return CSVFormat.EXCEL.parse(new InputStreamReader(content)).stream().count();
	}

	public static Stream<CSVRecord> getCsvRecordStream(InputStream content) throws IOException {
		return CSVFormat.EXCEL.parse(new InputStreamReader(content)).stream();
	}

	public static <E> List<E> csvToEntityList(InputStream is, Function<? super CSVRecord, ? extends E> toEntityFunction) {
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			CSVParser cp = CSVFormat.DEFAULT.builder().setHeader()
									.setSkipHeaderRecord(true)
									.setNullString("NULL")
									.build()
									.parse(new InputStreamReader(is));
			return cp.getRecords().stream().map(toEntityFunction).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("CSV data is failed to parse: " + e.getMessage());
		}
	}

}
