/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.JSONFormatException;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.view.EdgeImportConfigurationView;
import net.geoprism.registry.view.EdgeObjectImportConfigurationDTO;

@Service
public class GraphBusinessService
{
  public EdgeObjectImportConfigurationDTO getJsonImportConfiguration(String fileName, InputStream fileStream, EdgeImportConfigurationView view)
  {
    // Save the file to the file system
    try
    {
      // ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(type);

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (InputStream is = vf.openNewStream())
      {
        EdgeObjectImportConfigurationDTO dto = new EdgeObjectImportConfigurationDTO();
        dto.setGraphTypeClass(view.getGraphTypeClass());
        dto.setGraphTypeCode(view.getGraphTypeCode());
        dto.setSheet(this.getSheetInformationJson(is));
        dto.setVaultFileId(vf.getOid());
        dto.setFileName(fileName);
        dto.setImportStrategy(view.getStrategy());
        dto.setFormatType(FormatImporterType.JSON);
        dto.setObjectType(ObjectImporterFactory.JobHistoryType.EDGE_OBJECT);

        if (view.getDataSource() != null)
        {
          dto.setDataSource(view.getDataSource());
        }
        if (view.getDescription() != null)
        {
          dto.setDescription(view.getDescription());
        }
        if (view.getStartDate() != null)
        {
          dto.setStartDate(view.getStartDate());
        }
        if (view.getEndDate() != null)
        {
          dto.setEndDate(view.getEndDate());
        }

        return dto;
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
   * We're creating a fake 'type' here which really only has the code, in order
   * to be compliant with the needs of GeoObjectImportConfiguration
   * 
   * @param geoObjectType
   * @return
   */
  // private ObjectNode getType(ServerGeoObjectType geoObjectType) {
  // try {
  // final ObjectMapper mapper = new ObjectMapper();
  //
  // for (var name : new String[] { "source", "sourceType", "target",
  // "targetType", }) {
  // ObjectNode attr = (ObjectNode) n;
  // String attributeType = attr.path(AttributeType.JSON_TYPE).asText("");
  // String baseType = GeoObjectImportConfiguration.getBaseType(attributeType);
  // attr.put(GeoObjectImportConfiguration.BASE_TYPE, baseType);
  // }
  //
  // return type;
  // } catch (Exception e) {
  // throw new ProgrammingErrorException(e);
  // }
  // }

  private enum BaseType {
    BOOLEAN, NUMERIC, DATE, TEXT
  }

  /**
   * JSON version of getSheetInformation: - Expects top-level JSON array of
   * objects. - Aggregates all keys and infers a single base type per key from
   * seen values. - Builds: { "name": "json", "attributes": { "boolean": [...],
   * "text": [...], "numeric": [...], "date": [...] } } - Numeric keys are also
   * added to TEXT (to match your original behavior).
   */
  private SheetDTO getSheetInformationJson(InputStream jsonStream)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(jsonStream);

      if (root == null || !root.isArray())
      {
        throw new JSONFormatException("Expected a top-level JSON array of objects.");
      }

      // Track best (most-specific) inferred type per key.
      // Priority order (most specific) BOOLEAN/NUMERIC/DATE over TEXT, but DATE
      // vs NUMERIC/BOOLEAN wins if clearly a date.
      Map<String, BaseType> keyTypes = new HashMap<>();

      for (JsonNode item : root)
      {
        if (!item.isObject())
          continue;

        item.fieldNames().forEachRemaining(field -> {
          JsonNode v = item.get(field);
          BaseType observed = inferBaseType(v);

          // Merge policy: once a field is TEXT, keep TEXT; otherwise prefer
          // non-TEXT over TEXT.
          // DATE beats NUMERIC/BOOLEAN if we detect it; NUMERIC/BOOLEAN beat
          // TEXT.
          BaseType current = keyTypes.get(field);
          if (current == null)
          {
            keyTypes.put(field, observed);
          }
          else
          {
            keyTypes.put(field, mergeTypes(current, observed));
          }
        });
      }

      SheetDTO sheet = new SheetDTO();
      sheet.setName("json"); // analogous to shapefile layer name; adjust if you
                             // have a better source

      // Fill buckets; numeric also goes into text (to mirror your original
      // behavior)
      keyTypes.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
        String key = e.getKey();
        switch (e.getValue())
        {
          case BOOLEAN:
            sheet.put(AttributeBooleanType.TYPE, key);
            break;
          case NUMERIC:
            sheet.put(GeoObjectImportConfiguration.TEXT, key);
            sheet.put(GeoObjectImportConfiguration.NUMERIC, key);
            break;
          case DATE:
            sheet.put(AttributeDateType.TYPE, key);
            break;
          case TEXT:
          default:
            sheet.put(GeoObjectImportConfiguration.TEXT, key);
            break;
        }
      });

      return sheet;

    }
    catch (JsonParseException | JsonMappingException e)
    {
      var ex = new JSONFormatException("Invalid JSON format", e);
      ex.setRootCause(e.getMessage());
      throw ex;
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  /** Decide a base type for a single value. */
  private BaseType inferBaseType(JsonNode v)
  {
    if (v == null || v.isNull())
      return BaseType.TEXT; // nulls don’t pin a type; treat as text-compatible

    if (v.isBoolean())
      return BaseType.BOOLEAN;
    if (v.isNumber())
      return BaseType.NUMERIC;

    // Arrays/objects → default to TEXT (schema-less, safest)
    if (v.isArray() || v.isObject())
      return BaseType.TEXT;

    if (v.isTextual())
    {
      String s = v.asText();
      if (s.isEmpty())
        return BaseType.TEXT;

      // Try date detection; extend patterns as you need.
      if (looksLikeIsoDate(s))
        return BaseType.DATE;

      // Numeric-looking strings? You can enable if desired, but usually plain
      // strings stay TEXT.
      // if (looksNumericString(s)) return BaseType.NUMERIC;

      return BaseType.TEXT;
    }

    return BaseType.TEXT;
  }

  /**
   * Merge two inferred types for the same key. Prefer more specific where
   * sensible.
   */
  private BaseType mergeTypes(BaseType a, BaseType b)
  {
    if (a == b)
      return a;
    // If either is TEXT, prefer the other (BOOLEAN/NUMERIC/DATE) so we keep
    // specificity
    if (a == BaseType.TEXT)
      return b;
    if (b == BaseType.TEXT)
      return a;

    // DATE wins over NUMERIC/BOOLEAN if mixed
    if (a == BaseType.DATE || b == BaseType.DATE)
      return BaseType.DATE;

    // BOOLEAN vs NUMERIC → no clear winner; fall back to TEXT (conservative)
    return BaseType.TEXT;
  }

  /**
   * Basic ISO-8601-ish date checks; extend with your accepted formats if
   * needed.
   */
  private boolean looksLikeIsoDate(String s)
  {
    // Fast paths for common formats
    try
    {
      // yyyy-MM-dd
      LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
      return true;
    }
    catch (DateTimeParseException ignored)
    {
    }

    try
    {
      // 2024-03-10T15:23:01Z / with offsets
      OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return true;
    }
    catch (DateTimeParseException ignored)
    {
    }

    try
    {
      // 2024-03-10T15:23:01 (no zone)
      java.time.LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      return true;
    }
    catch (DateTimeParseException ignored)
    {
    }

    return false;
  }

  /**
   * Optional: numeric-looking string detection if you want to coerce "123.45" →
   * NUMERIC.
   */
  @SuppressWarnings("unused")
  private boolean looksNumericString(String s)
  {
    // Simple, locale-agnostic check
    return s.matches("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
  }
}
