package net.geoprism.registry.graph.transition;

@com.runwaysdk.business.ClassSignature(hash = -1683255043)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to Transition.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class TransitionBase extends com.runwaysdk.business.graph.VertexObject
{
  public final static String CLASS = "net.geoprism.registry.graph.transition.Transition";
  public static java.lang.String EVENT = "event";
  public static java.lang.String IMPACT = "impact";
  public static java.lang.String OID = "oid";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SOURCE = "source";
  public static java.lang.String TARGET = "target";
  public static java.lang.String TRANSITIONTYPE = "transitionType";
  private static final long serialVersionUID = -1683255043;
  
  public TransitionBase()
  {
    super();
  }
  
  public net.geoprism.registry.graph.transition.TransitionEvent getEvent()
  {
    return (net.geoprism.registry.graph.transition.TransitionEvent) this.getObjectValue(EVENT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getEventMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(EVENT);
  }
  
  public void setEvent(net.geoprism.registry.graph.transition.TransitionEvent value)
  {
    this.setValue(EVENT, value);
  }
  
  public String getImpact()
  {
    return (String) this.getObjectValue(IMPACT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getImpactMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(IMPACT);
  }
  
  public void setImpact(String value)
  {
    this.setValue(IMPACT, value);
  }
  
  public String getOid()
  {
    return (String) this.getObjectValue(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public Long getSeq()
  {
    return (Long) this.getObjectValue(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public void setSeq(Long value)
  {
    this.setValue(SEQ, value);
  }
  
  public net.geoprism.registry.graph.GeoVertex getSource()
  {
    return (net.geoprism.registry.graph.GeoVertex) this.getObjectValue(SOURCE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getSourceMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(SOURCE);
  }
  
  public void setSource(net.geoprism.registry.graph.GeoVertex value)
  {
    this.setValue(SOURCE, value);
  }
  
  public net.geoprism.registry.graph.GeoVertex getTarget()
  {
    return (net.geoprism.registry.graph.GeoVertex) this.getObjectValue(TARGET);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getTargetMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(TARGET);
  }
  
  public void setTarget(net.geoprism.registry.graph.GeoVertex value)
  {
    this.setValue(TARGET, value);
  }
  
  public String getTransitionType()
  {
    return (String) this.getObjectValue(TRANSITIONTYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getTransitionTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.transition.Transition.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(TRANSITIONTYPE);
  }
  
  public void setTransitionType(String value)
  {
    this.setValue(TRANSITIONTYPE, value);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static Transition get(String oid)
  {
    return (Transition) com.runwaysdk.business.graph.VertexObject.get(CLASS, oid);
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}