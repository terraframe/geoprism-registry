/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.report;

@com.runwaysdk.business.ClassSignature(hash = -716278884)
public abstract class ReportItemViewDTOBase extends com.runwaysdk.business.ViewDTO 
{
  public final static String CLASS = "net.geoprism.report.ReportItemView";
  private static final long serialVersionUID = -716278884;
  
  protected ReportItemViewDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DASHBOARDLABEL = "dashboardLabel";
  public static java.lang.String OID = "oid";
  public static java.lang.String REPORTID = "reportId";
  public static java.lang.String REPORTLABEL = "reportLabel";
  public static java.lang.String REPORTNAME = "reportName";
  public String getDashboardLabel()
  {
    return getValue(DASHBOARDLABEL);
  }
  
  public void setDashboardLabel(String value)
  {
    if(value == null)
    {
      setValue(DASHBOARDLABEL, "");
    }
    else
    {
      setValue(DASHBOARDLABEL, value);
    }
  }
  
  public boolean isDashboardLabelWritable()
  {
    return isWritable(DASHBOARDLABEL);
  }
  
  public boolean isDashboardLabelReadable()
  {
    return isReadable(DASHBOARDLABEL);
  }
  
  public boolean isDashboardLabelModified()
  {
    return isModified(DASHBOARDLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getDashboardLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(DASHBOARDLABEL).getAttributeMdDTO();
  }
  
  public String getReportId()
  {
    return getValue(REPORTID);
  }
  
  public void setReportId(String value)
  {
    if(value == null)
    {
      setValue(REPORTID, "");
    }
    else
    {
      setValue(REPORTID, value);
    }
  }
  
  public boolean isReportIdWritable()
  {
    return isWritable(REPORTID);
  }
  
  public boolean isReportIdReadable()
  {
    return isReadable(REPORTID);
  }
  
  public boolean isReportIdModified()
  {
    return isModified(REPORTID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getReportIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(REPORTID).getAttributeMdDTO();
  }
  
  public String getReportLabel()
  {
    return getValue(REPORTLABEL);
  }
  
  public void setReportLabel(String value)
  {
    if(value == null)
    {
      setValue(REPORTLABEL, "");
    }
    else
    {
      setValue(REPORTLABEL, value);
    }
  }
  
  public boolean isReportLabelWritable()
  {
    return isWritable(REPORTLABEL);
  }
  
  public boolean isReportLabelReadable()
  {
    return isReadable(REPORTLABEL);
  }
  
  public boolean isReportLabelModified()
  {
    return isModified(REPORTLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getReportLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(REPORTLABEL).getAttributeMdDTO();
  }
  
  public String getReportName()
  {
    return getValue(REPORTNAME);
  }
  
  public void setReportName(String value)
  {
    if(value == null)
    {
      setValue(REPORTNAME, "");
    }
    else
    {
      setValue(REPORTNAME, value);
    }
  }
  
  public boolean isReportNameWritable()
  {
    return isWritable(REPORTNAME);
  }
  
  public boolean isReportNameReadable()
  {
    return isReadable(REPORTNAME);
  }
  
  public boolean isReportNameModified()
  {
    return isModified(REPORTNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getReportNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(REPORTNAME).getAttributeMdDTO();
  }
  
  public final void remove()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.report.ReportItemViewDTO.CLASS, "remove", _declaredTypes);
    getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final void remove(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.report.ReportItemViewDTO.CLASS, "remove", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static ReportItemViewDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.ViewDTO dto = (com.runwaysdk.business.ViewDTO)clientRequest.get(oid);
    
    return (ReportItemViewDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createSessionComponent(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
}
