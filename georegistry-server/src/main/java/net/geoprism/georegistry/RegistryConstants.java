package net.geoprism.georegistry;

public interface RegistryConstants
{
  public static final String REGISTRY_PACKAGE                 = "net.geoprism.registry";

  public static final String UNIVERSAL_MDBUSINESS_PACKAGE     = REGISTRY_PACKAGE + ".universal";

  public static final String UNIVERSAL_RELATIONSHIP_POST      = "Metadata";

  public static final String GEO_ENTITY_ATTRIBUTE_NAME        = "geoEntity";

  public static final String GEO_ENTITY_ATTRIBUTE_LABEL       = "GeoEntity";

  public static final String UUID                             = "uuid";

  public static final String UUID_LABEL                       = "UID";

  public static final String GEOMETRY_ATTRIBUTE_NAME          = "geom";

  public static final String GEO_POINT_ATTRIBUTE_LABEL        = "Geo Point";

  public static final String GEO_LINE_ATTRIBUTE_LABEL         = "Geo Line";

  public static final String GEO_POLYGON_ATTRIBUTE_LABEL      = "Geo Polygon";

  public static final String GEO_MULTIPOINT_ATTRIBUTE_LABEL   = "Geo MultiPoint";

  public static final String GEO_MULTILINE_ATTRIBUTE_LABEL    = "Geo MultiLine";

  public static final String GEO_MULTIPOLYGON_ATTRIBUTE_LABEL = "Geo MultiPolygon";

  public static final String REGISTRY_ROLE_PREFIX             = "commongeoregistry.";

  public static final String REGISTRY_MAINTAINER_PREFIX       = REGISTRY_ROLE_PREFIX + "registry.maintainer.";

  public static final String REGISTRY_ADMIN_ROLE              = REGISTRY_ROLE_PREFIX + "RegistryAdministrator";

  public static final String REGISTRY_MAINTAINER_ROLE         = REGISTRY_ROLE_PREFIX + "RegistryMaintainer";

  public static final String REGISTRY_CONTRIBUTOR_ROLE        = REGISTRY_ROLE_PREFIX + "RegistryContributor";

  public static final String API_CONSUMER_ROLE                = REGISTRY_ROLE_PREFIX + "APIConsumer";

  public final static String TERM_CLASS                       = "CLASS";
}
