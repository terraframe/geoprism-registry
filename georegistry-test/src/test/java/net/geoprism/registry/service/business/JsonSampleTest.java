/**
 *
 */
package net.geoprism.registry.service.business;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.geoprism.registry.excel.SheetDTO;

/**
 * Tests the import preview sample read from an edge JSON file
 * ({@link GraphBusinessService#getSheetInformationJson}). No database is
 * needed.
 */
public class JsonSampleTest
{
  private static SheetDTO read(String json)
  {
    return new GraphBusinessService().getSheetInformationJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
  }

  private static String edge(int i)
  {
    return "{\"source\":\"DAM-" + i + "\",\"sourceType\":\"Dam\",\"target\":\"DAM-" + ( i + 1 ) + "\",\"targetType\":\"Dam\"}";
  }

  private static String edges(int count)
  {
    List<String> edges = new ArrayList<>();

    for (int i = 1; i <= count; i++)
    {
      edges.add(edge(i));
    }

    return "[" + String.join(",", edges) + "]";
  }

  @Test
  public void testEmptyArray()
  {
    Assertions.assertTrue(read("[]").getSample().isEmpty());
  }

  @Test
  public void testSampleIsCappedAtMaxRows()
  {
    SheetDTO sheet = read(edges(1000));

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals(Map.of("source", "DAM-1", "sourceType", "Dam", "target", "DAM-2", "targetType", "Dam"), sheet.getSample().get(0));
  }

  @Test
  public void testKeyOrderFollowsTheObject()
  {
    SheetDTO sheet = read("[{\"targetType\":\"Dam\",\"source\":\"A\",\"target\":\"B\",\"sourceType\":\"Dam\"}]");

    Assertions.assertEquals(List.of("targetType", "source", "target", "sourceType"), new ArrayList<>(sheet.getSample().get(0).keySet()));
  }

  @Test
  public void testNonObjectEntriesAreSkipped()
  {
    SheetDTO sheet = read("[" + edge(1) + ",42,\"text\",null,[" + edge(2) + "],true," + edge(3) + "]");

    Assertions.assertEquals(2, sheet.getSample().size());
    Assertions.assertEquals("DAM-1", sheet.getSample().get(0).get("source"));
    Assertions.assertEquals("DAM-3", sheet.getSample().get(1).get("source"));
  }

  @Test
  public void testNullValuesAreOmitted()
  {
    SheetDTO sheet = read("[{\"source\":\"A\",\"note\":null}]");

    Assertions.assertEquals(Map.of("source", "A"), sheet.getSample().get(0));
  }

  @Test
  public void testScalarValuesAreText()
  {
    SheetDTO sheet = read("[{\"int\":42,\"decimal\":1234.5,\"flag\":true,\"text\":\"a\\nb\"}]");

    Assertions.assertEquals(Map.of("int", "42", "decimal", "1234.5", "flag", "true", "text", "a\nb"), sheet.getSample().get(0));
  }

  @Test
  public void testKeysOnlyAfterSampleRowsAreStillColumns()
  {
    StringBuilder json = new StringBuilder("[");

    for (int i = 1; i <= 20; i++)
    {
      json.append(i > 1 ? "," : "").append(i > SheetDTO.MAX_SAMPLE_ROWS ? "{\"source\":\"S" + i + "\",\"late\":\"L" + i + "\"}" : "{\"source\":\"S" + i + "\"}");
    }

    SheetDTO sheet = read(json.append("]").toString());

    Assertions.assertTrue(sheet.getAttributes().get("text").contains("late"));
    sheet.getSample().forEach(row -> Assertions.assertFalse(row.containsKey("late")));
  }

  @Test
  public void testNestedValuesAreJson()
  {
    SheetDTO sheet = read("[{\"meta\":{\"a\":1},\"ids\":[1,2,3]}]");

    Assertions.assertEquals("{\"a\":1}", sheet.getSample().get(0).get("meta"));
    Assertions.assertEquals("[1,2,3]", sheet.getSample().get(0).get("ids"));
  }

  @Test
  public void testHugeNestedValueIsTruncated()
  {
    StringBuilder ring = new StringBuilder("[");

    for (int i = 0; i < 100_000; i++)
    {
      ring.append(i > 0 ? "," : "").append("[").append(-100 + i * 1e-5).append(",40]");
    }

    String geometry = "{\"type\":\"Polygon\",\"coordinates\":[" + ring.append("]") + "]}";
    SheetDTO sheet = read("[{\"source\":\"A\",\"geometry\":" + geometry + "}]");

    String value = sheet.getSample().get(0).get("geometry");

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_VALUE_LENGTH + 1, value.length());
    Assertions.assertTrue(value.startsWith("{\"type\":\"Polygon\""));
    Assertions.assertTrue(value.endsWith("…"));
  }

  @Test
  public void testUnusualKeys()
  {
    SheetDTO sheet = read("[{\"人口\":1,\"💧level\":\"x\",\"\":\"blank key\",\"__proto__\":\"proto\",\"constructor\":\"ctor\"}]");

    Map<String, String> row = sheet.getSample().get(0);

    Assertions.assertEquals("1", row.get("人口"));
    Assertions.assertEquals("x", row.get("💧level"));
    Assertions.assertEquals("blank key", row.get(""));
    Assertions.assertEquals("proto", row.get("__proto__"));
    Assertions.assertEquals("ctor", row.get("constructor"));
  }

  @Test
  public void testLongValueWithEmojiAtCut()
  {
    String value = "a".repeat(SheetDTO.MAX_SAMPLE_VALUE_LENGTH - 1) + "😀 tail";

    SheetDTO sheet = read("[{\"note\":\"" + value + "\"}]");

    Assertions.assertEquals("a".repeat(SheetDTO.MAX_SAMPLE_VALUE_LENGTH - 1) + "…", sheet.getSample().get(0).get("note"));
  }

  @Test
  public void testTopLevelObjectIsRejected()
  {
    Assertions.assertThrows(RuntimeException.class, () -> read("{\"edges\":[" + edge(1) + "]}"));
  }

  @Test
  public void testInvalidJsonIsRejected()
  {
    Assertions.assertThrows(RuntimeException.class, () -> read("[{\"source\":\"A\" \"target\":\"B\"}"));
  }
}
