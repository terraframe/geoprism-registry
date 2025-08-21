package net.geoprism.registry.service.business;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.JSONFormatException;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.upload.EdgeObjectImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

@Service
public class GraphBusinessService
{
  public ObjectNode getJsonImportConfiguration(String graphTypeClass, String graphTypeCode, Date startDate, Date endDate, String source, String fileName, InputStream fileStream, ImportStrategy strategy)
  {
    // Save the file to the file system
    try
    {
//      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(type);

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (InputStream is = vf.openNewStream())
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
        
        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(is);

        ObjectNode obj = mapper.createObjectNode();
        obj.put(EdgeObjectImportConfiguration.GRAPH_TYPE_CLASS, graphTypeClass);
        obj.put(EdgeObjectImportConfiguration.GRAPH_TYPE_CODE, graphTypeCode);
        obj.set(GeoObjectImportConfiguration.SHEET, this.getSheetInformationJson(is));
        obj.put(ImportConfiguration.VAULT_FILE_ID, vf.getOid());
        obj.put(ImportConfiguration.FILE_NAME, fileName);
        obj.put(ImportConfiguration.IMPORT_STRATEGY, strategy.name());
        obj.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.JSON.name());
        obj.put(ImportConfiguration.OBJECT_TYPE, ObjectImporterFactory.ObjectImportType.EDGE_OBJECT.name());

        if (source != null) {
          obj.put(GeoObjectImportConfiguration.DATA_SOURCE, source);
        }
        if (startDate != null) {
          obj.put(GeoObjectImportConfiguration.START_DATE, format.format(startDate));
        }
        if (endDate != null) {
          obj.put(GeoObjectImportConfiguration.END_DATE, format.format(endDate));
        }

        return obj;
      }
    }
    catch (RunwayException | SmartException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  /**
   * We're creating a fake 'type' here which really only has the code, in order to be compliant with the needs of GeoObjectImportConfiguration
   * 
   * @param geoObjectType
   * @return
   */
//  private ObjectNode getType(ServerGeoObjectType geoObjectType) {
//    try {
//      final ObjectMapper mapper = new ObjectMapper();
//
//      for (var name : new String[] { "source", "sourceType", "target", "targetType",  }) {
//        ObjectNode attr = (ObjectNode) n;
//        String attributeType = attr.path(AttributeType.JSON_TYPE).asText("");
//        String baseType = GeoObjectImportConfiguration.getBaseType(attributeType);
//        attr.put(GeoObjectImportConfiguration.BASE_TYPE, baseType);
//      }
//
//      return type;
//    } catch (Exception e) {
//      throw new ProgrammingErrorException(e);
//    }
//  }
  
  
  
  
  private enum BaseType { BOOLEAN, NUMERIC, DATE, TEXT }

  /**
   * JSON version of getSheetInformation:
   * - Expects top-level JSON array of objects.
   * - Aggregates all keys and infers a single base type per key from seen values.
   * - Builds:
   *   {
   *     "name": "json",
   *     "attributes": {
   *        "boolean": [...],
   *        "text": [...],
   *        "numeric": [...],
   *        "date": [...]
   *     }
   *   }
   * - Numeric keys are also added to TEXT (to match your original behavior).
   */
  private ObjectNode getSheetInformationJson(InputStream jsonStream) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(jsonStream);

      if (root == null || !root.isArray()) {
        throw new JSONFormatException("Expected a top-level JSON array of objects.");
      }

      // Track best (most-specific) inferred type per key.
      // Priority order (most specific) BOOLEAN/NUMERIC/DATE over TEXT, but DATE vs NUMERIC/BOOLEAN wins if clearly a date.
      Map<String, BaseType> keyTypes = new HashMap<>();

      for (JsonNode item : root) {
        if (!item.isObject()) continue;

        item.fieldNames().forEachRemaining(field -> {
          JsonNode v = item.get(field);
          BaseType observed = inferBaseType(v);

          // Merge policy: once a field is TEXT, keep TEXT; otherwise prefer non-TEXT over TEXT.
          // DATE beats NUMERIC/BOOLEAN if we detect it; NUMERIC/BOOLEAN beat TEXT.
          BaseType current = keyTypes.get(field);
          if (current == null) {
            keyTypes.put(field, observed);
          } else {
            keyTypes.put(field, mergeTypes(current, observed));
          }
        });
      }

      // Build attributes buckets
      ObjectNode attributes = mapper.createObjectNode();
      ArrayNode boolArr = attributes.putArray(AttributeBooleanType.TYPE);          // "boolean"
      ArrayNode textArr = attributes.putArray(GeoObjectImportConfiguration.TEXT);  // "text"
      ArrayNode numArr  = attributes.putArray(GeoObjectImportConfiguration.NUMERIC); // "numeric"
      ArrayNode dateArr = attributes.putArray(AttributeDateType.TYPE);             // "date"

      // Fill buckets; numeric also goes into text (to mirror your original behavior)
      keyTypes.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(e -> {
            String key = e.getKey();
            switch (e.getValue()) {
              case BOOLEAN:
                boolArr.add(key);
                break;
              case NUMERIC:
                numArr.add(key);
                textArr.add(key); // include numeric in text as well
                break;
              case DATE:
                dateArr.add(key);
                break;
              case TEXT:
              default:
                textArr.add(key);
                break;
            }
          });

      ObjectNode sheet = mapper.createObjectNode();
      sheet.put("name", "json"); // analogous to shapefile layer name; adjust if you have a better source
      sheet.set("attributes", attributes);
      return sheet;

    } catch (JsonParseException | JsonMappingException e) {
      var ex = new JSONFormatException("Invalid JSON format", e);
      ex.setRootCause(e.getMessage());
      throw ex;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProgrammingErrorException(e);
    }
  }

  /** Decide a base type for a single value. */
  private BaseType inferBaseType(JsonNode v) {
    if (v == null || v.isNull()) return BaseType.TEXT; // nulls don’t pin a type; treat as text-compatible

    if (v.isBoolean()) return BaseType.BOOLEAN;
    if (v.isNumber())  return BaseType.NUMERIC;

    // Arrays/objects → default to TEXT (schema-less, safest)
    if (v.isArray() || v.isObject()) return BaseType.TEXT;

    if (v.isTextual()) {
      String s = v.asText();
      if (s.isEmpty()) return BaseType.TEXT;

      // Try date detection; extend patterns as you need.
      if (looksLikeIsoDate(s)) return BaseType.DATE;

      // Numeric-looking strings? You can enable if desired, but usually plain strings stay TEXT.
      // if (looksNumericString(s)) return BaseType.NUMERIC;

      return BaseType.TEXT;
    }

    return BaseType.TEXT;
  }

  /** Merge two inferred types for the same key. Prefer more specific where sensible. */
  private BaseType mergeTypes(BaseType a, BaseType b) {
    if (a == b) return a;
    // If either is TEXT, prefer the other (BOOLEAN/NUMERIC/DATE) so we keep specificity
    if (a == BaseType.TEXT) return b;
    if (b == BaseType.TEXT) return a;

    // DATE wins over NUMERIC/BOOLEAN if mixed
    if (a == BaseType.DATE || b == BaseType.DATE) return BaseType.DATE;

    // BOOLEAN vs NUMERIC → no clear winner; fall back to TEXT (conservative)
    return BaseType.TEXT;
  }

  /** Basic ISO-8601-ish date checks; extend with your accepted formats if needed. */
  private boolean looksLikeIsoDate(String s) {
    // Fast paths for common formats
    try {
      // yyyy-MM-dd
      LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
      return true;
    } catch (DateTimeParseException ignored) {}

    try {
      // 2024-03-10T15:23:01Z / with offsets
      OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return true;
    } catch (DateTimeParseException ignored) {}

    try {
      // 2024-03-10T15:23:01 (no zone)
      java.time.LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      return true;
    } catch (DateTimeParseException ignored) {}

    return false;
  }

  /** Optional: numeric-looking string detection if you want to coerce "123.45" → NUMERIC. */
  @SuppressWarnings("unused")
  private boolean looksNumericString(String s) {
    // Simple, locale-agnostic check
    return s.matches("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
  }
}
