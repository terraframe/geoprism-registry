package net.geoprism.registry.graph;

@com.runwaysdk.business.ClassSignature(hash = -625138547)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to TransitionEvent.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class TransitionEventBase extends com.runwaysdk.business.graph.VertexObject
{
  public final static String CLASS = "net.geoprism.registry.graph.TransitionEvent";
  public static java.lang.String DESCRIPTION = "description";
  public static java.lang.String EVENTDATE = "eventDate";
  public static java.lang.String OID = "oid";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String TYPECODE = "typeCode";
  private static final long serialVersionUID = -625138547;
  
  public TransitionEventBase()
  {
    super();
  }
  
  public com.runwaysdk.ComponentIF getDescription()
  {
    return (com.runwaysdk.ComponentIF) this.getObjectValue(DESCRIPTION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDAOIF getDescriptionMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.TransitionEvent.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDAOIF)mdClassIF.definesAttribute(DESCRIPTION);
  }
  
  public void setDescription(com.runwaysdk.ComponentIF value)
  {
    this.setValue(DESCRIPTION, value);
  }
  
  public java.util.Date getEventDate()
  {
    return (java.util.Date) this.getObjectValue(EVENTDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateDAOIF getEventDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.TransitionEvent.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateDAOIF)mdClassIF.definesAttribute(EVENTDATE);
  }
  
  public void setEventDate(java.util.Date value)
  {
    this.setValue(EVENTDATE, value);
  }
  
  public String getOid()
  {
    return (String) this.getObjectValue(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.TransitionEvent.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public Long getSeq()
  {
    return (Long) this.getObjectValue(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.TransitionEvent.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public void setSeq(Long value)
  {
    this.setValue(SEQ, value);
  }
  
  public String getTypeCode()
  {
    return (String) this.getObjectValue(TYPECODE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getTypeCodeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.TransitionEvent.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(TYPECODE);
  }
  
  public void setTypeCode(String value)
  {
    this.setValue(TYPECODE, value);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public com.runwaysdk.business.graph.EdgeObject addTransitionAssignmentChild(net.geoprism.registry.graph.Transition transition)
  {
    return super.addChild(transition, "net.geoprism.registry.graph.TransitionAssignment");
  }
  
  public void removeTransitionAssignmentChild(net.geoprism.registry.graph.Transition transition)
  {
    super.removeChild(transition, "net.geoprism.registry.graph.TransitionAssignment");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<net.geoprism.registry.graph.Transition> getTransitionAssignmentChildTransitions()
  {
    return super.getChildren("net.geoprism.registry.graph.TransitionAssignment",net.geoprism.registry.graph.Transition.class);
  }
  
  public static TransitionEvent get(String oid)
  {
    return (TransitionEvent) com.runwaysdk.business.graph.VertexObject.get(CLASS, oid);
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
