package net.geoprism.dhis2.dhis2adapter.response.model;

import java.awt.Point;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum ValueType {
  
  TEXT( String.class, true ),
  LONG_TEXT( String.class, true ),
  LETTER( String.class, true ),
  PHONE_NUMBER( String.class, false ),
  EMAIL( String.class, false ),
  BOOLEAN( Boolean.class, true ),
  TRUE_ONLY( Boolean.class, true ),
  DATE( LocalDate.class, false ),
  DATETIME( LocalDateTime.class, false ),
  TIME( String.class, false ),
  NUMBER( Double.class, true ),
  UNIT_INTERVAL( Double.class, true ),
  PERCENTAGE( Double.class, true ),
  INTEGER( Integer.class, true ),
  INTEGER_POSITIVE( Integer.class, true ),
  INTEGER_NEGATIVE( Integer.class, true ),
  INTEGER_ZERO_OR_POSITIVE( Integer.class, true ),
  TRACKER_ASSOCIATE( TrackedEntityInstance.class, false ),
  USERNAME( String.class, false ),
  COORDINATE( Point.class, true ),
  ORGANISATION_UNIT( OrganisationUnit.class, false ),
  AGE( Date.class, false ),
  URL( String.class, false ),
  FILE_RESOURCE( String.class, false ),
  IMAGE( String.class, false);
  
  private Class<?> javaClass;
  
  private boolean aggregateable;
  
  ValueType( Class<?> javaClass, boolean aggregateable )
  {
      this.javaClass = javaClass;
      this.aggregateable = aggregateable;
  }

  public Class<?> getJavaClass()
  {
    return javaClass;
  }

  public void setJavaClass(Class<?> javaClass)
  {
    this.javaClass = javaClass;
  }

  public boolean isAggregateable()
  {
    return aggregateable;
  }

  public void setAggregateable(boolean aggregateable)
  {
    this.aggregateable = aggregateable;
  }
  
}
