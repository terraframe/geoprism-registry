package net.geoprism.registry.view;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class HierarchyTypeSnapshotDTO
{
  private String         code;

  private String         orgCode;

  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  @JsonSerialize(using = LocalizedValueSerializer.class)
  private LocalizedValue displayLabel;

  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  @JsonSerialize(using = LocalizedValueSerializer.class)
  private LocalizedValue description;

  private String         progress;

  private String         acknowledgement;

  private String         disclaimer;

  private String         accessContraints;

  private String         useContraints;

  private String         superHierarchy;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getOrgCode()
  {
    return orgCode;
  }

  public void setOrgCode(String orgCode)
  {
    this.orgCode = orgCode;
  }

  public LocalizedValue getDisplayLabel()
  {
    return displayLabel;
  }

  public void setDisplayLabel(LocalizedValue displayLabel)
  {
    this.displayLabel = displayLabel;
  }

  public LocalizedValue getDescription()
  {
    return description;
  }

  public void setDescription(LocalizedValue description)
  {
    this.description = description;
  }

  public String getProgress()
  {
    return progress;
  }

  public void setProgress(String progress)
  {
    this.progress = progress;
  }

  public String getAcknowledgement()
  {
    return acknowledgement;
  }

  public void setAcknowledgement(String acknowledgement)
  {
    this.acknowledgement = acknowledgement;
  }

  public String getDisclaimer()
  {
    return disclaimer;
  }

  public void setDisclaimer(String disclaimer)
  {
    this.disclaimer = disclaimer;
  }

  public String getAccessContraints()
  {
    return accessContraints;
  }

  public void setAccessContraints(String accessContraints)
  {
    this.accessContraints = accessContraints;
  }

  public String getUseContraints()
  {
    return useContraints;
  }

  public void setUseContraints(String useContraints)
  {
    this.useContraints = useContraints;
  }

  public String getSuperHierarchy()
  {
    return superHierarchy;
  }

  public void setSuperHierarchy(String superHierarchy)
  {
    this.superHierarchy = superHierarchy;
  }

}
