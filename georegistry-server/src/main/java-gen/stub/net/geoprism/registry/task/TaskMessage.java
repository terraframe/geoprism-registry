package net.geoprism.registry.task;

public class TaskMessage extends TaskMessageBase
{
  private static final long serialVersionUID = -291980366;
  
  public TaskMessage()
  {
    super();
  }
  
  public TaskMessage(com.runwaysdk.business.MutableWithStructs entity, String structName)
  {
    super(entity, structName);
  }
  
}
