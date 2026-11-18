/**
 *
 */
package net.geoprism.registry.excel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for the import preview sample kept in {@link SheetDTO}. These do
 * not need a database.
 */
public class SheetDTOSampleTest
{
  private static final int    MAX      = SheetDTO.MAX_SAMPLE_VALUE_LENGTH;

  private static final String ELLIPSIS = "…";

  /**
   * Adds a single value to a new sheet and returns how it appears in the
   * sample
   */
  private static String sampleOf(Object value)
  {
    SheetDTO sheet = new SheetDTO();
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("column", value);
    sheet.addSampleRow(row);

    return sheet.getSample().get(0).get("column");
  }

  private static void assertNoBrokenSurrogates(String value)
  {
    for (int i = 0; i < value.length(); i++)
    {
      char c = value.charAt(i);

      if (Character.isHighSurrogate(c))
      {
        Assertions.assertTrue(i + 1 < value.length() && Character.isLowSurrogate(value.charAt(i + 1)), "Unpaired high surrogate at " + i + " in " + value);
        i++;
      }
      else
      {
        Assertions.assertFalse(Character.isLowSurrogate(c), "Unpaired low surrogate at " + i + " in " + value);
      }
    }
  }

  // ------------------------------------------------------------ truncation

  @Test
  public void testValueAtLimitIsNotTruncated()
  {
    String value = "b".repeat(MAX);

    Assertions.assertEquals(value, sampleOf(value));
  }

  @Test
  public void testValueOverLimitIsTruncated()
  {
    Assertions.assertEquals("c".repeat(MAX) + ELLIPSIS, sampleOf("c".repeat(MAX + 1)));
  }

  @Test
  public void testEmojiStraddlingLimitIsNotSplit()
  {
    // The emoji starts at UTF-16 index MAX - 1, so a plain substring(0, MAX)
    // would keep only its high surrogate
    for (String emoji : new String[] { "😀", "🌊", "🏗️", "🇺🇸", "👨‍👩‍👧" })
    {
      String value = "a".repeat(MAX - 1) + emoji + " tail";
      String sample = sampleOf(value);

      assertNoBrokenSurrogates(sample);
      Assertions.assertEquals("a".repeat(MAX - 1) + ELLIPSIS, sample, "Emoji " + emoji);
    }
  }

  @Test
  public void testEmojiEndingAtLimitIsKept()
  {
    String value = "d".repeat(MAX - 2) + "😀" + "tail";

    Assertions.assertEquals("d".repeat(MAX - 2) + "😀" + ELLIPSIS, sampleOf(value));
  }

  @Test
  public void testEmojiStartingAtLimitIsDropped()
  {
    Assertions.assertEquals("e".repeat(MAX) + ELLIPSIS, sampleOf("e".repeat(MAX) + "😀"));
  }

  @Test
  public void testAllEmojiValue()
  {
    String sample = sampleOf("🌊".repeat(150));

    assertNoBrokenSurrogates(sample);
    Assertions.assertTrue(sample.endsWith(ELLIPSIS));
  }

  @Test
  public void testAccentedValue()
  {
    Assertions.assertEquals("é".repeat(MAX) + ELLIPSIS, sampleOf("é".repeat(250)));
  }

  @Test
  public void testMultilineValueIsKept()
  {
    Assertions.assertEquals("line one\nline two", sampleOf("line one\nline two"));
  }

  // ------------------------------------------------------------ numbers

  @Test
  public void testDoublesUsePlainNotation()
  {
    Assertions.assertEquals("1234.5", sampleOf(1234.5));
    Assertions.assertEquals("9999999", sampleOf(9999999.0));
    Assertions.assertEquals("10000000", sampleOf(10000000.0));
    Assertions.assertEquals("12345678", sampleOf(12345678.0));
    Assertions.assertEquals("987654321.25", sampleOf(987654321.25));
    Assertions.assertEquals("1000000000000", sampleOf(1e12));
    Assertions.assertEquals("0.001", sampleOf(0.001));
    Assertions.assertEquals("0.0009", sampleOf(0.0009));
    Assertions.assertEquals("0.0000123", sampleOf(0.0000123));
    Assertions.assertEquals("-45000000.5", sampleOf(-45000000.5));
    Assertions.assertEquals("-0.00005", sampleOf(-0.00005));
    Assertions.assertEquals("0", sampleOf(0.0));
  }

  @Test
  public void testFloatDoesNotGainDigits()
  {
    Assertions.assertEquals("0.1", sampleOf(0.1f));
    Assertions.assertEquals("12345678", sampleOf(1.2345678E7f));
  }

  @Test
  public void testNonFiniteDoubles()
  {
    Assertions.assertEquals("NaN", sampleOf(Double.NaN));
    Assertions.assertEquals("Infinity", sampleOf(Double.POSITIVE_INFINITY));
  }

  @Test
  public void testOtherNumbers()
  {
    Assertions.assertEquals("1000", sampleOf(new BigDecimal("1E+3")));
    Assertions.assertEquals("42", sampleOf(42L));
    Assertions.assertEquals("7", sampleOf(7));
  }

  @Test
  public void testBooleans()
  {
    Assertions.assertEquals("true", sampleOf(true));
  }

  // ------------------------------------------------------------ dates

  @Test
  public void testDateIsFormattedInUtc()
  {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.clear();
    calendar.set(2020, Calendar.FEBRUARY, 18);

    Assertions.assertEquals("2020-02-18", sampleOf(calendar.getTime()));
  }

  // ------------------------------------------------------------ rows

  @Test
  public void testNullValuesAreOmitted()
  {
    SheetDTO sheet = new SheetDTO();
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("a", "1");
    row.put("b", null);
    row.put("c", "3");
    sheet.addSampleRow(row);

    Assertions.assertEquals(List.of("a", "c"), new ArrayList<>(sheet.getSample().get(0).keySet()));
  }

  @Test
  public void testColumnOrderIsPreserved()
  {
    SheetDTO sheet = new SheetDTO();
    Map<String, Object> row = new LinkedHashMap<>();

    for (String column : new String[] { "zeta", "alpha", "mu", "constructor", "__proto__" })
    {
      row.put(column, column);
    }

    sheet.addSampleRow(row);

    Assertions.assertEquals(List.of("zeta", "alpha", "mu", "constructor", "__proto__"), new ArrayList<>(sheet.getSample().get(0).keySet()));
  }

  @Test
  public void testSampleStopsAtMaxRows()
  {
    SheetDTO sheet = new SheetDTO();

    for (int i = 0; i < SheetDTO.MAX_SAMPLE_ROWS + 5; i++)
    {
      Assertions.assertEquals(i >= SheetDTO.MAX_SAMPLE_ROWS, sheet.sampleFull());

      sheet.addSampleRow(Map.of("i", i));
    }

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals("0", sheet.getSample().get(0).get("i"));
    Assertions.assertEquals(String.valueOf(SheetDTO.MAX_SAMPLE_ROWS - 1), sheet.getSample().get(SheetDTO.MAX_SAMPLE_ROWS - 1).get("i"));
  }

  // ------------------------------------------------------------ JSON

  @Test
  public void testJsonRoundTrip()
  {
    SheetDTO sheet = new SheetDTO();
    sheet.setName("Sheet1");
    sheet.put("text", "name");
    sheet.addSampleRow(Map.of("name", "a".repeat(MAX - 1) + "😀"));

    JsonMapper mapper = JsonMapper.shared();
    String json = mapper.writeValueAsString(sheet);

    // sampleFull() is not a bean property, so it must not be serialized
    Assertions.assertFalse(json.contains("sampleFull"), json);

    SheetDTO copy = mapper.readValue(json, SheetDTO.class);

    Assertions.assertEquals(sheet.getSample(), copy.getSample());
  }

  @Test
  public void testJsonWithoutSample()
  {
    // Import records saved before the preview existed have no sample
    SheetDTO sheet = JsonMapper.shared().readValue("{\"name\":\"Sheet1\",\"attributes\":{\"text\":[\"name\"]}}", SheetDTO.class);

    Assertions.assertNotNull(sheet.getSample());
    Assertions.assertTrue(sheet.getSample().isEmpty());
  }
}
