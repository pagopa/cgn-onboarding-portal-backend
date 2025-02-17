package it.gov.pagopa.cgn.portal.util;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvUtils {
    private CsvUtils() {
    }

    public static long countCsvLines(InputStream content)
            throws IOException {
        return CSVFormat.EXCEL.parse(new InputStreamReader(content)).stream().count();
    }

    public static Stream<CSVRecord> getCsvRecordStream(InputStream content)
            throws IOException {
        return CSVFormat.EXCEL.parse(new InputStreamReader(content)).stream();
    }

    public static boolean checkField(String field, InputStream inputStream)
            throws IOException {
        Stream<CSVRecord> stream = getCsvRecordStream(inputStream);
        return stream.anyMatch(line -> line.get(0).contains(field));
    }

    public static <E> List<E> csvToEntityList(InputStream is,
                                              Function<? super CSVRecord, ? extends E> toEntityFunction) {
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CSVParser cp = CSVFormat.DEFAULT.builder()
                                            .setHeader()
                                            .setSkipHeaderRecord(true)
                                            .setNullString("NULL")
                                            .build()
                                            .parse(new InputStreamReader(is));
            return cp.getRecords().stream().map(toEntityFunction).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("CSV data is failed to parse: " + e.getMessage());
        }
    }

    /*
        VALIDATE CSV BUCKET


    private static long countCsvRecord(byte[] content) {
        long recordCount = 0;
        try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
            recordCount = CsvUtils.countCsvLines(contentIs);
        } catch (IOException e) {
            throw new CGNException(e.getMessage());
        }
        return recordCount;
    }

    public static void main (String[] args) {
        try (InputStream inputStream = new FileInputStream("c:\\develop\\test-buckets\\Vouchers_Data_CGN Avvento-2024-KO.csv")){
            byte[] content = inputStream.readAllBytes();
            long csvRecordCount = countCsvRecord(content);
            if (csvRecordCount < 10000) {
                throw new InvalidRequestException(ErrorCodeEnum.CANNOT_LOAD_BUCKET_FOR_NOT_RESPECTED_MINIMUM_BOUND.getValue());
            }
            try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
                Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);
                if (content.length==0) {
                    throw new InternalErrorException(ErrorCodeEnum.CSV_DATA_NOT_VALID.getValue());
                }
                AtomicInteger currentRow = new AtomicInteger(1);
                csvRecordStream.forEach(line -> {
                    if (line.get(0).length() > 20 ||
                        StringUtils.isBlank(line.get(0))) {
                        System.out.println(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue()+" "+ currentRow.get() + " " + line.get(0));
                    }
                    currentRow.incrementAndGet();
                });
            }

            Pattern pDigits = Pattern.compile("\\d"); //[0-9]
            Pattern pAlphab = Pattern.compile("[A-Za-z]");
            Pattern SpChars = Pattern.compile("^(?=.*\\d)[a-zA-Z0-9][-a-zA-Z0-9]+$");

            try (ByteArrayInputStream contentIs = new ByteArrayInputStream(content)) {
                Stream<CSVRecord> csvRecordStream = CsvUtils.getCsvRecordStream(contentIs);

                AtomicInteger currentRow = new AtomicInteger(1);
                csvRecordStream.forEach(line -> {
                    if (line.get(0).length() > 20 ||
                        StringUtils.isBlank(line.get(0))) {
                        System.out.println(ErrorCodeEnum.MAX_ALLOWED_BUCKET_CODE_LENGTH_NOT_RESPECTED.getValue()+" "+ currentRow.get() + " " + line.get(0));
                        currentRow.incrementAndGet();
                        return;
                    }

                    if(!(pDigits.matcher(line.get(0)).find() //at least one digit
                         && pAlphab.matcher(line.get(0)).find())) { //at least on alphab. char)
                        System.out.println(ErrorCodeEnum.BUCKET_CODES_MUST_BE_ALPHANUM_WITH_AT_LEAST_ONE_DIGIT_AND_ONE_CHAR.getValue()+ " "+ currentRow.get() + " " + line.get(0));
                        currentRow.incrementAndGet();
                        return;
                    }

                    if(!(SpChars.matcher(line.get(0)).find())) {
                        System.out.println(ErrorCodeEnum.NOT_ALLOWED_SPECIAL_CHARS.getValue()+" "+ currentRow.get() + " " + line.get(0));
                        currentRow.incrementAndGet();
                        return;
                    }


                });
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

}
