/**
 *
 */
package net.geoprism.registry.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.geoprism.data.etl.excel.ExcelDataFormatter;
import net.geoprism.data.etl.excel.ExcelSheetReader;

/**
 * Tests the import preview sample collected while reading an Excel file
 * ({@link ExcelFieldContentsHandler}). The workbooks are built in memory, and
 * no database is needed.
 */
public class ExcelSampleTest
{
  /**
   * Builds a one sheet workbook from a header and rows. A null value leaves
   * the cell out entirely, as Excel does for an empty cell.
   */
  private static byte[] workbook(List<String> header, List<List<Object>> rows) throws IOException
  {
    return workbook(wb -> fill(wb.createSheet("Sheet1"), header, rows));
  }

  private static byte[] workbook(Consumer<Workbook> builder) throws IOException
  {
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream())
    {
      builder.accept(wb);
      wb.write(out);

      return out.toByteArray();
    }
  }

  private static void fill(Sheet sheet, List<String> header, List<List<Object>> rows)
  {
    Row headerRow = sheet.createRow(0);

    for (int c = 0; c < header.size(); c++)
    {
      if (header.get(c) != null)
      {
        headerRow.createCell(c).setCellValue(header.get(c));
      }
    }

    for (int r = 0; r < rows.size(); r++)
    {
      Row row = sheet.createRow(r + 1);
      List<Object> values = rows.get(r);

      for (int c = 0; c < values.size(); c++)
      {
        setValue(row, c, values.get(c));
      }
    }
  }

  private static void setValue(Row row, int column, Object value)
  {
    if (value == null)
    {
      return;
    }

    Cell cell = row.createCell(column);

    if (value instanceof Number)
    {
      cell.setCellValue( ( (Number) value ).doubleValue());
    }
    else if (value instanceof Boolean)
    {
      cell.setCellValue((Boolean) value);
    }
    else
    {
      cell.setCellValue(value.toString());
    }
  }

  private static List<SheetDTO> read(byte[] bytes) throws Exception
  {
    ExcelFieldContentsHandler handler = new ExcelFieldContentsHandler();
    ExcelSheetReader reader = new ExcelSheetReader(handler, new ExcelDataFormatter());
    reader.process(new ByteArrayInputStream(bytes));

    return handler.getSheets();
  }

  private static SheetDTO readFirst(byte[] bytes) throws Exception
  {
    return read(bytes).get(0);
  }

  private static List<List<Object>> rows(int count, int columns)
  {
    List<List<Object>> rows = new ArrayList<>();

    for (int r = 1; r <= count; r++)
    {
      List<Object> row = new ArrayList<>();
      row.add("CODE-" + r);

      for (int c = 1; c < columns; c++)
      {
        row.add("r" + r + "c" + c);
      }

      rows.add(row);
    }

    return rows;
  }

  private static List<String> header(int columns)
  {
    List<String> header = new ArrayList<>();
    header.add("code");

    for (int c = 1; c < columns; c++)
    {
      header.add("col" + c);
    }

    return header;
  }

  // ------------------------------------------------------------ row counts

  @Test
  public void testHeaderOnly() throws Exception
  {
    SheetDTO sheet = readFirst(workbook(List.of("code", "name"), List.of()));

    Assertions.assertTrue(sheet.getSample().isEmpty());
    Assertions.assertTrue(sheet.getAttributes().get("text").containsAll(List.of("code", "name")));
  }

  @Test
  public void testOneRow() throws Exception
  {
    SheetDTO sheet = readFirst(workbook(List.of("code", "name"), List.of(List.of("ONE", "Only row"))));

    Assertions.assertEquals(List.of(Map.of("code", "ONE", "name", "Only row")), sheet.getSample());
  }

  @Test
  public void testSampleIsCappedAtMaxRows() throws Exception
  {
    SheetDTO sheet = readFirst(workbook(header(3), rows(SheetDTO.MAX_SAMPLE_ROWS + 15, 3)));

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals("CODE-1", sheet.getSample().get(0).get("code"));
    Assertions.assertEquals("CODE-" + SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().get(SheetDTO.MAX_SAMPLE_ROWS - 1).get("code"));
  }

  @Test
  public void testLargeFile() throws Exception
  {
    // Streamed so that building the workbook does not need much memory
    byte[] bytes;

    try (SXSSFWorkbook wb = new SXSSFWorkbook(100); ByteArrayOutputStream out = new ByteArrayOutputStream())
    {
      Sheet sheet = wb.createSheet("Big");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("code");
      header.createCell(1).setCellValue("value");

      for (int r = 1; r <= 100_000; r++)
      {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue("BIG-" + r);
        row.createCell(1).setCellValue(r);
      }

      wb.write(out);
      wb.dispose();
      bytes = out.toByteArray();
    }

    SheetDTO sheet = readFirst(bytes);

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals("BIG-1", sheet.getSample().get(0).get("code"));
  }

  // ------------------------------------------------------------ columns

  @Test
  public void testWideFile() throws Exception
  {
    int columns = 1000;
    SheetDTO sheet = readFirst(workbook(header(columns), rows(12, columns)));

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());

    for (Map<String, String> row : sheet.getSample())
    {
      Assertions.assertEquals(columns, row.size());
    }

    // Column order follows the file, not alphabetical or hash order
    Assertions.assertEquals(header(columns), new ArrayList<>(sheet.getSample().get(0).keySet()));
  }

  @Test
  public void testBlankCellsAreOmitted() throws Exception
  {
    List<Object> row = new ArrayList<>();
    row.add("A");
    row.add(null);
    row.add("C");

    SheetDTO sheet = readFirst(workbook(List.of("first", "second", "third"), List.of(row)));

    Assertions.assertEquals(List.of("first", "third"), new ArrayList<>(sheet.getSample().get(0).keySet()));
  }

  @Test
  public void testWhitespaceOnlyCellsAreOmitted() throws Exception
  {
    SheetDTO sheet = readFirst(workbook(List.of("code", "blank"), List.of(List.of("A", "   "))));

    Assertions.assertFalse(sheet.getSample().get(0).containsKey("blank"));
  }

  @Test
  public void testColumnBlankInEverySampleRow() throws Exception
  {
    // "late" only has values after the sample rows. It has no type, so it is
    // not offered as a column, and it never appears in the sample.
    List<List<Object>> rows = new ArrayList<>();

    for (int r = 1; r <= 15; r++)
    {
      List<Object> row = new ArrayList<>();
      row.add("CODE-" + r);
      row.add(r > SheetDTO.MAX_SAMPLE_ROWS ? "late " + r : null);
      rows.add(row);
    }

    SheetDTO sheet = readFirst(workbook(List.of("code", "late"), rows));

    for (Map<String, String> row : sheet.getSample())
    {
      Assertions.assertFalse(row.containsKey("late"));
    }

    Assertions.assertTrue(sheet.getAttributes().get("text").contains("late"));
  }

  @Test
  public void testJavaScriptPropertyNamesAsColumns() throws Exception
  {
    List<String> header = List.of("code", "constructor", "toString", "valueOf", "hasOwnProperty", "__proto__");
    List<Object> row = new ArrayList<>(List.of("A", "ctor", "ts", "vo", "hop", "proto"));

    SheetDTO sheet = readFirst(workbook(header, List.of(row)));

    Assertions.assertEquals(header, new ArrayList<>(sheet.getSample().get(0).keySet()));
    Assertions.assertEquals("proto", sheet.getSample().get(0).get("__proto__"));
  }

  @Test
  public void testUnusualHeaders() throws Exception
  {
    List<String> header = List.of("code", "Población", "人口", "💧 Water level", "dots.in.name", "<b>html</b>", "{{ angular }}", "line\nbreak", "  spaces  ");
    List<Object> row = new ArrayList<>();

    for (int c = 0; c < header.size(); c++)
    {
      row.add("v" + c);
    }

    SheetDTO sheet = readFirst(workbook(header, List.of(row)));

    Assertions.assertEquals(header.size(), sheet.getSample().get(0).size());
    Assertions.assertEquals("v3", sheet.getSample().get(0).get("💧 Water level"));
  }

  @Test
  public void testBlankHeaderCellIsSkipped() throws Exception
  {
    List<String> header = new ArrayList<>();
    header.add("code");
    header.add(null);
    header.add("name");

    SheetDTO sheet = readFirst(workbook(header, List.of(List.of("A", "orphan", "Name"))));

    // The value under the blank header has no column to belong to
    Assertions.assertEquals(Map.of("code", "A", "name", "Name"), sheet.getSample().get(0));
  }

  // ------------------------------------------------------------ rows

  @Test
  public void testBlankRowsAreSkipped() throws Exception
  {
    byte[] bytes = workbook(wb -> {
      Sheet sheet = wb.createSheet("Sheet1");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("code");

      // Rows 1-2 are missing, row 3 exists but is empty, row 4 has a blank
      // string
      sheet.createRow(3);
      sheet.createRow(4).createCell(0).setCellValue("");

      for (int r = 5; r < 20; r++)
      {
        sheet.createRow(r).createCell(0).setCellValue("ROW-" + r);
      }
    });

    SheetDTO sheet = readFirst(bytes);

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals("ROW-5", sheet.getSample().get(0).get("code"));
  }

  @Test
  public void testEachSheetHasItsOwnSample() throws Exception
  {
    byte[] bytes = workbook(wb -> {
      fill(wb.createSheet("First"), List.of("code", "first"), List.of(List.of("F1", "a"), List.of("F2", "b")));
      fill(wb.createSheet("Second"), List.of("code", "second"), List.of(List.of("S1", "c")));
    });

    List<SheetDTO> sheets = read(bytes);

    Assertions.assertEquals(2, sheets.size());
    Assertions.assertEquals(List.of(Map.of("code", "F1", "first", "a"), Map.of("code", "F2", "first", "b")), sheets.get(0).getSample());
    Assertions.assertEquals(List.of(Map.of("code", "S1", "second", "c")), sheets.get(1).getSample());
  }

  // ------------------------------------------------------------ values

  @Test
  public void testLongValueWithEmojiAtCut() throws Exception
  {
    String value = "a".repeat(SheetDTO.MAX_SAMPLE_VALUE_LENGTH - 1) + "😀 tail";

    SheetDTO sheet = readFirst(workbook(List.of("code", "long"), List.of(List.of("A", value))));

    Assertions.assertEquals("a".repeat(SheetDTO.MAX_SAMPLE_VALUE_LENGTH - 1) + "…", sheet.getSample().get(0).get("long"));
  }

  @Test
  public void testNumbersAndBooleans() throws Exception
  {
    SheetDTO sheet = readFirst(workbook(List.of("int", "decimal", "big", "flag"), List.of(List.of(42, 1234.5, 12345678, true))));

    Map<String, String> row = sheet.getSample().get(0);

    Assertions.assertEquals("42", row.get("int"));
    Assertions.assertEquals("1234.5", row.get("decimal"));
    Assertions.assertEquals("12345678", row.get("big"));
    Assertions.assertEquals("TRUE", row.get("flag"));
  }

  @Test
  public void testNumbersStoredAsTextKeepLeadingZeros() throws Exception
  {
    SheetDTO sheet = readFirst(workbook(List.of("code", "zip"), List.of(List.of("A", "00123"))));

    Assertions.assertEquals("00123", sheet.getSample().get(0).get("zip"));
  }

  @Test
  public void testDateCell() throws Exception
  {
    byte[] bytes = workbook(wb -> {
      Sheet sheet = wb.createSheet("Sheet1");
      sheet.createRow(0).createCell(0).setCellValue("date");

      CellStyle style = wb.createCellStyle();
      style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));

      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(2020, Calendar.FEBRUARY, 18);

      Cell cell = sheet.createRow(1).createCell(0);
      cell.setCellValue(calendar.getTime());
      cell.setCellStyle(style);
    });

    SheetDTO sheet = readFirst(bytes);

    Assertions.assertTrue(sheet.getSample().get(0).get("date").startsWith("2020-02-18"), sheet.getSample().get(0).get("date"));
  }

  @Test
  public void testValuesAreNotInterpreted() throws Exception
  {
    // HTML, Angular syntax and formula-like text are plain strings in the
    // sample; the UI is responsible for escaping them
    List<Object> values = List.of("<script>alert(1)</script>", "{{ 7*7 }}", "=cmd|' /C calc'!A0", "null");

    SheetDTO sheet = readFirst(workbook(List.of("html", "angular", "formula", "nullText"), List.of(values)));

    Assertions.assertEquals(List.of(values.stream().map(Object::toString).toList()), sheet.getSample().stream().map(r -> new ArrayList<>(r.values())).toList());
  }
}
