package net.geoprism.registry.service.business;

import static org.apache.spark.sql.functions.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.connect.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.Pair;
import com.runwaysdk.query.OIterator;

import net.geoprism.graph.AttributeBooleanTypeSnapshot;
import net.geoprism.graph.AttributeCharacterTypeSnapshot;
import net.geoprism.graph.AttributeClassificationTypeSnapshot;
import net.geoprism.graph.AttributeDataSourceTypeSnapshot;
import net.geoprism.graph.AttributeDateTypeSnapshot;
import net.geoprism.graph.AttributeDoubleTypeSnapshot;
import net.geoprism.graph.AttributeGeometryTypeSnapshot;
import net.geoprism.graph.AttributeLocalTypeSnapshotBase;
import net.geoprism.graph.AttributeLongTypeSnapshotBase;
import net.geoprism.graph.AttributeTypeSnapshot;
import net.geoprism.graph.AttributeUUIDTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.registry.Commit;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.etl.SparkExportConfig;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;

@Service
public class SparkSynchronizationService
{
  public static final String WKB = "_wkb";

  public static enum SparkColumnType {
    // ---- Null ----
    NULL("void", Category.NULL, DataTypes.NullType),

    // ---- Boolean ----
    BOOLEAN("boolean", Category.BOOLEAN, DataTypes.BooleanType),

    // ---- Integral numerics ----
    BYTE("tinyint", Category.NUMERIC, DataTypes.ByteType), //
    SHORT("smallint", Category.NUMERIC, DataTypes.ShortType), //
    INTEGER("int", Category.NUMERIC, DataTypes.IntegerType), //
    LONG("bigint", Category.NUMERIC, DataTypes.LongType), //

    // ---- Fractional numerics ----
    FLOAT("float", Category.NUMERIC, DataTypes.FloatType), //
    DOUBLE("double", Category.NUMERIC, DataTypes.DoubleType), //
                                                              // scale)

    // ---- Strings ----
    STRING("string", Category.STRING, DataTypes.StringType), //
    CHAR("char", Category.STRING, DataTypes.StringType), // CharType(length)
    VARCHAR("varchar", Category.STRING, DataTypes.StringType), // VarcharType(length)

    // ---- Binary ----
    BINARY("binary", Category.BINARY, DataTypes.BinaryType),

    // ---- Datetime ----
    DATE("date", Category.DATETIME, DataTypes.DateType), // TIMESTAMP("timestamp",
                                                         // Category.DATETIME),
                                                         // // TIMESTAMP_LTZ
    TIMESTAMP_NTZ("timestamp_ntz", Category.DATETIME, DataTypes.TimestampType), // Spark
                                                                                // 3.4+
    TIME("time", Category.DATETIME, DataTypes.DateType), // Spark 4.1+

    // ---- Apache Sedona UDTs (org.apache.spark.sql.sedona_sql.UDT) ----
    GEOMETRY("geometry", Category.SPATIAL, null), // GeometryUDT
    GEOGRAPHY("geography", Category.SPATIAL, null), // GeographyUDT (Sedona
                                                    // 1.7+)
    RASTER("raster", Category.SPATIAL, null), // RasterUDT
    SPATIAL_INDEX("index", Category.SPATIAL, null); // IndexUDT

    public enum Category {
      NULL, BOOLEAN, NUMERIC, STRING, BINARY, DATETIME, INTERVAL, SEMI_STRUCTURED, COMPLEX, SPATIAL, OTHER
    }

    private final String   sqlName;

    private final Category category;

    private DataType       type;

    SparkColumnType(String sqlName, Category category, DataType type)
    {
      this.sqlName = sqlName;
      this.category = category;
      this.type = type;
    }

    public String getSqlName()
    {
      return sqlName;
    }

    public DataType getType()
    {
      return type;
    }

    public Category getCategory()
    {
      return category;
    }

    public boolean isSpatial()
    {
      return category == Category.SPATIAL;
    }

  }

  private static class Type
  {
    String     code;

    StructType schema;

    public Type(String code, StructType schema)
    {
      super();
      this.code = code;
      this.schema = schema;
    }

  }

  private static class ExportData
  {
    final SparkSession        spark;

    final ExportHistory       history;

    final AtomicLong          progress = new AtomicLong(0);

    private Map<String, Type> types  = new HashMap<>();

    public ExportData(SparkSession spark, ExportHistory history, SparkExportConfig config)
    {
      this.spark = spark;
      this.history = history;
    }
  }

  private static Logger                                      logger     = LoggerFactory.getLogger(SparkSynchronizationService.class);

  public static final String                                 GEO        = "http://www.opengis.net/ont/geosparql#";

  public static final String                                 SF         = "http://www.opengis.net/ont/sf#";

  public static final int                                    BATCH_SIZE = 10000;

  @Autowired
  private CommitBusinessServiceIF                            commitService;

  @Autowired
  private PublishBusinessServiceIF                           publishService;

  @Autowired
  private SynchronizationHasProcessedCommitBusinessServiceIF exportService;

  public void execute(SynchronizationConfig synchronization, SparkExportConfig configuration, ExportHistory history)
  {
    // TODO: add ;token=... or;use_ssl=true as needed
    String url = configuration.getSystem().getUrl();

    try (SparkSession spark = SparkSession.builder() //
        .remote(url) //
        .getOrCreate())
    {

      Publish publish = this.publishService.getByUid(configuration.getPublishUid()) //
          .orElseThrow(() -> GeoRegistryUtil.createDataNotFoundException(Publish.CLASS, Publish.UID, configuration.getPublishUid()));

      this.commitService.getLatest(publish).ifPresent(commit -> {

        if (history != null)
        {
          history.appLock();
          history.setWorkTotal(0l);
          history.setWorkProgress(0l);
          history.setExportedRecords(0l);
          history.clearStage();
          history.addStage(ExportStage.EXPORT);
          history.apply();
        }

        ExportData data = new ExportData(spark, history, configuration);

        execute(synchronization, commit, data);

        if (history != null)
        {
          history.appLock();
          history.setWorkProgress(history.getWorkTotal());
          history.setExportedRecords(history.getWorkTotal());
          history.clearStage();
          history.addStage(ExportStage.COMPLETE);
          history.apply();
        }
      });
    }
  }

  protected void execute(SynchronizationConfig synchronization, Commit commit, ExportData data)
  {
    if (!this.exportService.hasBeenPublished(synchronization, commit))
    {
      this.commitService.getDependencies(commit) //
          .stream() //
          .forEach(dependency -> this.execute(synchronization, dependency, data));

      if (data.history != null)
      {
        long count = this.commitService.getEventCount(commit) + 1;

        data.history.appLock();
        data.history.setWorkTotal(data.history.getWorkTotal() + count);
        data.history.apply();
      }

      this.commitService.getTypes(commit).stream() //
          .filter(t -> !t.getIsAbstract() && !t.getIsRoot()) //
          .forEach(type -> {
            createSchema(data, type);
          });

      AtomicReference<Model> model = new AtomicReference<Model>(ModelFactory.createDefaultModel());

      final List<Pair<String, Row>> batch = new ArrayList<>(BATCH_SIZE);

      this.commitService.getRemoteEvents(commit).forEach(event -> {
        if (event instanceof RemoteGeoObjectEvent)
        {
          batch.add(this.handleRemoteGeoObjectApply(commit, (RemoteGeoObjectEvent) event, data, model.get()));
        }

        long cWorkProgress = data.progress.incrementAndGet();

        if (batch.size() == BATCH_SIZE)
        {
          flush(data, batch);
          batch.clear();

          if (data.history != null)
          {
            data.history.appLock();
            data.history.setWorkProgress(cWorkProgress);
            data.history.setExportedRecords(cWorkProgress);
            data.history.apply();
          }
        }
      });

      if (!batch.isEmpty())
      {
        flush(data, batch);
      }

      // Mark the commit as exported
      this.exportService.create(synchronization, commit);

      if (data.history != null)
      {
        data.history.appLock();
        data.history.setWorkProgress(data.progress.get());
        data.history.setExportedRecords(data.progress.get());
        data.history.apply();
      }
    }
    else
    {
      logger.trace("Skipping export of commit [" + commit.getUid() + "] because its already been exported");
    }
  }

  private StructType createSchema(ExportData data, GeoObjectTypeSnapshot type)
  {
    StructType schema = new StructType();

    try (OIterator<? extends AttributeTypeSnapshot> it = type.getAllAttribute())
    {
      List<? extends AttributeTypeSnapshot> attributes = it.getAll();

      Collections.sort(attributes, (a, b) -> a.getCode().compareTo(b.getCode()));

      for (AttributeTypeSnapshot snapshot : attributes)
      {
        SparkColumnType columnType = this.getSedonaType(snapshot);

        if (!columnType.isSpatial())
        {
          schema = schema.add(snapshot.getCode(), columnType.getType());
        }
        else
        {
          schema = schema.add(WKB, DataTypes.BinaryType);
        }
      }
    }

    data.types.put(type.getCode(), new Type(type.getCode(), schema));

    return schema;
  }

  private SparkColumnType getSedonaType(AttributeTypeSnapshot snapshot)
  {
    if (snapshot instanceof AttributeBooleanTypeSnapshot)
    {
      return SparkColumnType.BOOLEAN;
    }
    else if (snapshot instanceof AttributeCharacterTypeSnapshot)
    {
      return SparkColumnType.STRING;
    }
    else if (snapshot instanceof AttributeClassificationTypeSnapshot)
    {
      return SparkColumnType.STRING;
    }
    else if (snapshot instanceof AttributeDataSourceTypeSnapshot)
    {
      return SparkColumnType.STRING;
    }
    else if (snapshot instanceof AttributeDateTypeSnapshot)
    {
      return SparkColumnType.DATE;
    }
    else if (snapshot instanceof AttributeDoubleTypeSnapshot)
    {
      return SparkColumnType.DOUBLE;
    }
    else if (snapshot instanceof AttributeGeometryTypeSnapshot)
    {
      return SparkColumnType.GEOMETRY;
    }
    else if (snapshot instanceof AttributeLocalTypeSnapshotBase)
    {
      return SparkColumnType.STRING;
    }
    else if (snapshot instanceof AttributeLongTypeSnapshotBase)
    {
      return SparkColumnType.LONG;
    }
    else if (snapshot instanceof AttributeUUIDTypeSnapshot)
    {
      return SparkColumnType.STRING;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + snapshot.getType() + "]");
  }

  private static void flush(ExportData data, List<Pair<String, Row>> list)
  {
    list.stream() //
        .collect(Collectors.groupingBy(entry -> entry.getFirst())) //
        .forEach((code, entries) -> {
          Type type = data.types.get(code);
          List<Row> rows = entries.stream().map(entry -> entry.getSecond()).toList();

          Dataset<Row> df = data.spark.createDataFrame(rows, type.schema) //
              .withColumn("geometry", expr("ST_SetSRID(ST_GeomFromWKB(" + WKB + "), 4326)")) //
              .drop(WKB);

          df.write().format("geoparquet") //
              .option("geoparquet.version", "1.1.0") //
              .mode(SaveMode.Append) //
              .saveAsTable(type.code);
        });

  }

  public Pair<String, Row> handleRemoteGeoObjectApply(Commit commit, RemoteGeoObjectEvent event, ExportData data, Model model)
  {
    logger.trace("Spark Projection - Handling remote geo object");

    GeoObject dto = GeoObject.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    Type type = data.types.get(dto.getType().getCode());

    Object[] values = Arrays.stream(type.schema.fieldNames()).map(attributeName -> {

      if (attributeName.equals(WKB))
      {
        Geometry geometry = dto.getGeometry();

        if (geometry != null)
        {
          return new WKBWriter().write(geometry);
        }
      }
      else
      {
        AttributeType attribute = dto.getType().getAttribute(attributeName).orElse(null);

        if (attribute != null)
        {
          Object value = dto.getValue(attributeName);

          if (value != null && value instanceof LocalizedValue)
          {
            return ( (LocalizedValue) value ).getValue();
          }

          return value;
        }
      }

      return null;
    }).toArray();

    return new Pair<String, Row>(dto.getType().getCode(), RowFactory.create(values));
  }

}
