package net.geoprism.registry.view;

public class ImportHistoryView
{
  private String historyId;

  public ImportHistoryView()
  {
  }

  public ImportHistoryView(String historyId)
  {
    super();
    this.historyId = historyId;
  }

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
  }

}
