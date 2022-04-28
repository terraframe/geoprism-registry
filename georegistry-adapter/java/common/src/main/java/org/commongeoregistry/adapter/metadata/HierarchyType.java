/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Metadata that describes a hierarchy type, such as Geopolitical or health
 * administrative.
 *
 */
public class HierarchyType implements Serializable
{
  /**
   * 
   */
  private static final long   serialVersionUID           = -1947163248569170534L;

  public static final String  JSON_CODE                  = "code";

  public static final String  JSON_INHERITED_HIER_CODE   = "inheritedHierarchyCode";

  public static final String  JSON_LOCALIZED_LABEL       = "label";

  public static final String  JSON_LOCALIZED_DESCRIPTION = "description";

  public static final String  JSON_ABSTRACT_DESCRIPTION  = "abstractDescription";

  public static final String  JSON_PROGRESS              = "progress";

  public static final String  JSON_ACKNOWLEDGEMENT       = "acknowledgement";

  public static final String  JSON_DISCLAIMER            = "disclaimer";

  public static final String  JSON_ACCESS_CONSTRAINTS    = "accessConstraints";

  public static final String  JSON_USE_CONSTRAINTS       = "useConstraints";

  public static final String  JSON_CONTACT               = "contact";

  public static final String  JSON_PHONE_NUMBER          = "phoneNumber";

  public static final String  JSON_EMAIL                 = "email";

  public static final String  JSON_ROOT_GEOOBJECTTYPES   = "rootGeoObjectTypes";

  public static final String  JSON_GEOOBJECTTYPE         = "geoObjectType";

  public static final String  JSON_CHILDREN              = "children";

  /**
   * Invariant: Is either an empty string or is the code of a valid
   * {@link OrganizationDTO}.
   */
  public static final String  JSON_ORGANIZARION_CODE     = "organizationCode";

  /**
   * Unique identifier but also human readable.
   */
  private String              code;

  /**
   * The localized label of the hierarchy type for the presentation tier.
   */
  private LocalizedValue      label;

  /**
   * The localized description of the hierarchy type for the presentation tier.
   */
  private LocalizedValue      description;

  /**
   * The organization responsible for this {@link HierarchyType}. This can be
   * null.
   */
  private String              organizationCode;

  private String              abstractDescription;

  private String              progress;

  private String              acknowledgement;

  private String              disclaimer;

  private String              accessConstraints;

  private String              useConstraints;

  private String              contact;

  private String              phoneNumber;

  private String              email;

  private List<HierarchyNode> rootGeoObjectTypes;

  public HierarchyType(String code, LocalizedValue label, LocalizedValue description, String organizationCode)
  {
    this.code = code;
    this.label = label;
    this.description = description;
    this.organizationCode = organizationCode;
    this.rootGeoObjectTypes = Collections.synchronizedList(new LinkedList<HierarchyNode>());
  }

  public String getCode()
  {
    return this.code;
  }

  public LocalizedValue getLabel()
  {
    return this.label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  public LocalizedValue getDescription()
  {
    return this.description;
  }

  public void setDescription(LocalizedValue description)
  {
    this.description = description;
  }

  /**
   * 
   * @return the code of the {@link OrganizationDTO} (Optional) that manages
   *         this {@link HierarchyType}, or NULL if not managed by an
   *         {@link OrganizationDTO}.
   */
  public String getOrganizationCode()
  {
    return this.organizationCode;
  }

  /**
   * Sets the {@link OrganizationDTO} (Optional) that manages this
   * {@link HierarchyType}.
   * 
   * Precondition: The organization code is valid
   * 
   * @param organizationCode
   *          code of the {@link OrganizationDTO} that manages this
   *          {@link HierarchyType}, or NULL if none.
   */
  public void setOrganizationCode(String organizationCode)
  {
    this.organizationCode = organizationCode;
  }

  public String getAbstractDescription()
  {
    return abstractDescription;
  }

  public void setAbstractDescription(String abstractDescription)
  {
    this.abstractDescription = abstractDescription;
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

  public String getContact()
  {
    return contact;
  }

  public void setContact(String contact)
  {
    this.contact = contact;
  }

  public String getAccessConstraints()
  {
    return accessConstraints;
  }

  public void setAccessConstraints(String accessConstraints)
  {
    this.accessConstraints = accessConstraints;
  }

  public String getUseConstraints()
  {
    return useConstraints;
  }

  public void setUseConstraints(String useConstraints)
  {
    this.useConstraints = useConstraints;
  }

  public String getDisclaimer()
  {
    return disclaimer;
  }

  public void setDisclaimer(String disclaimer)
  {
    this.disclaimer = disclaimer;
  }

  public String getPhoneNumber()
  {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber)
  {
    this.phoneNumber = phoneNumber;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public List<HierarchyNode> getRootGeoObjectTypes()
  {
    return this.rootGeoObjectTypes;
  }
  
  public Iterator<HierarchyNode> getAllNodesIterator()
  {
    return this.getAllNodes().iterator(); // TODO : There's definitely a better way to do this but I don't really have time
  }
  
  public List<HierarchyNode> getAllNodes()
  {
    ArrayList<HierarchyNode> descends = new ArrayList<HierarchyNode>();
    
    for (HierarchyNode child : this.getRootGeoObjectTypes())
    {
      descends.add(child);
      
      descends.addAll(child.getAllDescendants());
    }
    
    return descends;
  }

  /**
   * Adds root {@link GeoObjectType} objects to the root of the hierarchy type.
   * 
   * @param hierarchyNode
   */
  public void addRootGeoObjects(HierarchyNode hierarchyNode)
  {
    this.rootGeoObjectTypes.add(hierarchyNode);
  }
  
  public void clearRootGeoObjectTypes()
  {
    this.rootGeoObjectTypes.clear();
  }

  public JsonObject toJSON()
  {
    return toJSON(new DefaultSerializer());
  }

  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject jsonObj = new JsonObject();

    jsonObj.addProperty(JSON_CODE, this.getCode());

    jsonObj.add(JSON_LOCALIZED_LABEL, this.getLabel().toJSON(serializer));

    jsonObj.add(JSON_LOCALIZED_DESCRIPTION, this.getDescription().toJSON(serializer));

    jsonObj.addProperty(JSON_ORGANIZARION_CODE, this.organizationCode == null ? "" : this.organizationCode);
    jsonObj.addProperty(JSON_ABSTRACT_DESCRIPTION, this.abstractDescription == null ? "" : this.abstractDescription);
    jsonObj.addProperty(JSON_PROGRESS, this.progress == null ? "" : this.progress);
    jsonObj.addProperty(JSON_ACKNOWLEDGEMENT, this.acknowledgement == null ? "" : this.acknowledgement);
    jsonObj.addProperty(JSON_DISCLAIMER, this.disclaimer == null ? "" : this.disclaimer);
    jsonObj.addProperty(JSON_CONTACT, this.contact == null ? "" : this.contact);
    jsonObj.addProperty(JSON_PHONE_NUMBER, this.phoneNumber == null ? "" : this.phoneNumber);
    jsonObj.addProperty(JSON_EMAIL, this.email == null ? "" : this.email);
    jsonObj.addProperty(JSON_ACCESS_CONSTRAINTS, this.accessConstraints == null ? "" : this.accessConstraints);
    jsonObj.addProperty(JSON_USE_CONSTRAINTS, this.useConstraints == null ? "" : this.useConstraints);

    JsonArray jaRoots = new JsonArray();
    for (int i = 0; i < rootGeoObjectTypes.size(); ++i)
    {
      HierarchyNode hnode = rootGeoObjectTypes.get(i);

      jaRoots.add(hnode.toJSON());
    }

    jsonObj.add(JSON_ROOT_GEOOBJECTTYPES, jaRoots);

    serializer.configure(this, jsonObj);

    return jsonObj;
  }

  /**
   * Constructs a {@link HierarchyType} from the given JSON.
   * 
   * @param _sJson
   * @param _registry
   * @return
   */
  public static HierarchyType fromJSON(String _sJson, RegistryAdapter _registry)
  {
    JsonParser parser = new JsonParser();

    JsonObject oJson = parser.parse(_sJson).getAsJsonObject();

    String code = oJson.get(JSON_CODE).getAsString();
    LocalizedValue label = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_LABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_DESCRIPTION).getAsJsonObject());

    String abstractDescription = oJson.has(JSON_ABSTRACT_DESCRIPTION) ? oJson.get(JSON_ABSTRACT_DESCRIPTION).getAsString() : null;
    String progress = oJson.has(JSON_PROGRESS) ? oJson.get(JSON_PROGRESS).getAsString() : null;
    String acknowledgement = oJson.has(JSON_ACKNOWLEDGEMENT) ? oJson.get(JSON_ACKNOWLEDGEMENT).getAsString() : null;
    String disclaimer = oJson.has(JSON_DISCLAIMER) ? oJson.get(JSON_DISCLAIMER).getAsString() : null;
    String contact = oJson.has(JSON_CONTACT) ? oJson.get(JSON_CONTACT).getAsString() : null;
    String phoneNumber = oJson.has(JSON_PHONE_NUMBER) ? oJson.get(JSON_PHONE_NUMBER).getAsString() : null;
    String email = oJson.has(JSON_EMAIL) ? oJson.get(JSON_EMAIL).getAsString() : null;
    String accessConstraints = oJson.has(JSON_ACCESS_CONSTRAINTS) ? oJson.get(JSON_ACCESS_CONSTRAINTS).getAsString() : null;
    String useConstraints = oJson.has(JSON_USE_CONSTRAINTS) ? oJson.get(JSON_USE_CONSTRAINTS).getAsString() : null;

    String organizationCode = null;

    JsonElement jsonOrganization = oJson.get(JSON_ORGANIZARION_CODE);
    if (jsonOrganization != null)
    {
      organizationCode = jsonOrganization.getAsString();
    }

    HierarchyType ht = new HierarchyType(code, label, description, organizationCode);
    ht.setAbstractDescription(abstractDescription);
    ht.setProgress(progress);
    ht.setAcknowledgement(acknowledgement);
    ht.setDisclaimer(disclaimer);
    ht.setContact(contact);
    ht.setPhoneNumber(phoneNumber);
    ht.setEmail(email);
    ht.setAccessConstraints(accessConstraints);
    ht.setUseConstraints(useConstraints);

    JsonArray rootGeoObjectTypes = oJson.getAsJsonArray(JSON_ROOT_GEOOBJECTTYPES);
    if (rootGeoObjectTypes != null)
    {
      for (int i = 0; i < rootGeoObjectTypes.size(); ++i)
      {
        HierarchyNode node = HierarchyNode.fromJSON(rootGeoObjectTypes.get(i).getAsJsonObject().toString(), _registry);

        ht.addRootGeoObjects(node);
      }
    }

    return ht;
  }

  public static HierarchyType[] fromJSONArray(String saJson, RegistryAdapter adapter)
  {
    JsonParser parser = new JsonParser();

    JsonArray jaHts = parser.parse(saJson).getAsJsonArray();
    HierarchyType[] hts = new HierarchyType[jaHts.size()];
    for (int i = 0; i < jaHts.size(); ++i)
    {
      HierarchyType ht = HierarchyType.fromJSON(jaHts.get(i).toString(), adapter);
      hts[i] = ht;
    }

    return hts;
  }
}
