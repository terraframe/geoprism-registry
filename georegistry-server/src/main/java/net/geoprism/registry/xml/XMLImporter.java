/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.dataaccess.transaction.TransactionState;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class XMLImporter
{
  private RegistryAdapter            adapter;

  private Map<String, ServerElement> cache;

  public XMLImporter()
  {
    this.adapter = ServiceFactory.getAdapter();
    this.cache = new HashMap<String, ServerElement>();
  }

  @Transaction
  public List<ServerElement> importXMLDefinitions(Organization organization, InputStream istream)
  {
    TransactionState state = TransactionState.getCurrentTransactionState();
    state.putTransactionObject("transaction-state", this.cache);

    LinkedList<ServerElement> list = new LinkedList<ServerElement>();

    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = factory.newDocumentBuilder();
      Document doc = dBuilder.parse(istream);

      list.addAll(this.createTypes(organization, doc));
      list.addAll(this.createHierarchies(organization, doc));
      list.addAll(this.createDirectedAcyclicGraphTypes(doc));
      list.addAll(this.createUndirectedGraphTypes(doc));
    }
    catch (ParserConfigurationException | IOException | SAXException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return list;
  }

  private List<ServerElement> createHierarchies(Organization organization, Document doc)
  {
    LinkedList<ServerElement> list = new LinkedList<ServerElement>();

    NodeList nList = doc.getElementsByTagName("hierarchy");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node nNode = nList.item(i);

      if (nNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) nNode;

        ServerHierarchyType type = createServerHierarchyType(organization, elem);
        list.add(type);
        this.cache.put(type.getCode(), type);

        this.addChildren(type, RootGeoObjectType.INSTANCE, elem);
      }
    }

    return list;
  }

  private List<ServerElement> createDirectedAcyclicGraphTypes(Document doc)
  {
    LinkedList<ServerElement> list = new LinkedList<ServerElement>();

    NodeList nList = doc.getElementsByTagName("dag");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node nNode = nList.item(i);

      if (nNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) nNode;

        list.add(this.createDirectedAcyclicGraphType(elem));
      }
    }

    return list;
  }

  private List<ServerElement> createUndirectedGraphTypes(Document doc)
  {
    LinkedList<ServerElement> list = new LinkedList<ServerElement>();

    NodeList nList = doc.getElementsByTagName("undirected-graph");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node nNode = nList.item(i);

      if (nNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) nNode;

        list.add(this.createUndirectedGraphType(elem));
      }
    }

    return list;
  }

  private List<ServerElement> createTypes(Organization organization, Document doc)
  {
    LinkedList<ServerElement> list = new LinkedList<ServerElement>();

    NodeList nList = doc.getElementsByTagName("type");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node nNode = nList.item(i);

      if (nNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) nNode;

        ServerGeoObjectType type = createServerGeoObjectType(organization, elem);
        list.add(type);
        this.cache.put(type.getCode(), type);

        this.addAttributes(elem, type);

        list.addAll(this.addGroupItems(elem, type));
      }
    }

    return list;
  }

  private List<ServerElement> addGroupItems(Element root, ServerGeoObjectType superType)
  {
    List<ServerElement> list = new LinkedList<ServerElement>();

    NodeList nList = root.getElementsByTagName("group-item");

    for (int i = 0; i < nList.getLength(); i++)
    {
      Node nNode = nList.item(i);

      if (nNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) nNode;

        String code = elem.getAttribute("code");
        LocalizedValue label = this.getLabel(elem);
        LocalizedValue description = this.getDescription(elem);

        GeoObjectType type = new GeoObjectType(code, superType.getGeometryType(), label, description, superType.isGeometryEditable(), superType.getOrganization().getCode(), adapter);
        type.setSuperTypeCode(superType.getCode());
        type.setIsPrivate(superType.getIsPrivate());

        ServerGeoObjectType result = new ServerGeoObjectTypeConverter().create(type);

        list.add(result);
        this.cache.put(code, result);
      }
    }

    return list;
  }

  private void addAttributes(Element root, ServerGeoObjectType type)
  {
    NodeList attributeList = root.getElementsByTagName("attributes");

    if (attributeList.getLength() > 0)
    {
      NodeList nList = attributeList.item(0).getChildNodes();

      for (int i = 0; i < nList.getLength(); i++)
      {
        Node nNode = nList.item(i);

        if (nNode.getNodeType() == Node.ELEMENT_NODE)
        {
          Element elem = (Element) nNode;

          String code = elem.getAttribute("code");
          LocalizedValue label = this.getLabel(elem);
          LocalizedValue description = this.getDescription(elem);

          if (elem.getTagName().equals("text"))
          {
            type.createAttributeType(new AttributeCharacterType(code, label, description, false, false, false));
          }
          else if (elem.getTagName().equals("boolean"))
          {
            type.createAttributeType(new AttributeBooleanType(code, label, description, false, false, false));
          }
          else if (elem.getTagName().equals("integer"))
          {
            type.createAttributeType(new AttributeIntegerType(code, label, description, false, false, false));
          }
          else if (elem.getTagName().equals("decimal"))
          {
            AttributeFloatType attributeType = new AttributeFloatType(code, label, description, false, false, false);
            attributeType.setPrecision(this.getPrecision(elem));
            attributeType.setScale(this.getScale(elem));

            type.createAttributeType(attributeType);
          }
          else if (elem.getTagName().equals("date"))
          {
            type.createAttributeType(new AttributeDateType(code, label, description, false, false, false));
          }
          else if (elem.getTagName().equals("term"))
          {
            AttributeTermType attributeType = new AttributeTermType(code, label, description, false, false, false);
            attributeType = (AttributeTermType) type.createAttributeType(attributeType);

            Term rootTerm = attributeType.getRootTerm();

            this.createTermOptions(elem, rootTerm);
          }
          else if (elem.getTagName().equals("classification"))
          {
            String rootCode = elem.getAttribute("root");
            String classificationType = elem.getAttribute("classificationType");

            AttributeClassificationType attributeType = new AttributeClassificationType(code, label, description, false, false, false);
            attributeType.setRootTerm(new Term(rootCode, new LocalizedValue(""), new LocalizedValue("")));
            attributeType.setClassificationType(classificationType);

            attributeType = (AttributeClassificationType) type.createAttributeType(attributeType);
          }
        }
      }
    }
  }

  private void createTermOptions(Element attributeNode, Term root)
  {
    NodeList attributeList = attributeNode.getElementsByTagName("option");

    for (int i = 0; i < attributeList.getLength(); i++)
    {
      Node nNode = attributeList.item(i);

      if (nNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) nNode;

        String code = elem.getAttribute("code");
        LocalizedValue label = this.getLabel(elem);
        LocalizedValue description = this.getDescription(elem);

        Term term = new Term(code, label, description);

        Classifier classifier = TermConverter.createClassifierFromTerm(root.getCode(), term);

        TermConverter termBuilder = new TermConverter(classifier.getKeyName());

        termBuilder.build();
      }
    }
  }

  private void addChildren(ServerHierarchyType hierarchy, ServerGeoObjectType parent, Element root)
  {
    NodeList childNodes = root.getChildNodes();

    for (int i = 0; i < childNodes.getLength(); i++)
    {
      Node childNode = childNodes.item(i);

      if (childNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element elem = (Element) childNode;

        String code = elem.getAttribute("code");

        ServerGeoObjectType child = ServerGeoObjectType.get(code);

        hierarchy.addToHierarchy(parent, child, false);

        if (root.hasAttribute("extends"))
        {
          String inheritedHierarchyCode = root.getAttribute("extends");
          ServerHierarchyType inheritedHierarchy = ServerHierarchyType.get(inheritedHierarchyCode);

          child.setInheritedHierarchy(hierarchy, inheritedHierarchy);
        }

        this.addChildren(hierarchy, child, elem);
      }
    }
  }

  private DirectedAcyclicGraphType createDirectedAcyclicGraphType(Element elem)
  {
    String code = elem.getAttribute("code");
    LocalizedValue label = this.getLabel(elem);
    LocalizedValue description = this.getDescription(elem);

    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.create(code, label, description);

    return type;
  }

  private UndirectedGraphType createUndirectedGraphType(Element elem)
  {
    String code = elem.getAttribute("code");
    LocalizedValue label = this.getLabel(elem);
    LocalizedValue description = this.getDescription(elem);

    UndirectedGraphType type = UndirectedGraphType.create(code, label, description);

    return type;
  }

  private ServerHierarchyType createServerHierarchyType(Organization organization, Element elem)
  {
    String code = elem.getAttribute("code");
    LocalizedValue label = this.getLabel(elem);
    LocalizedValue description = this.getDescription(elem);
    String progress = elem.getAttribute("progress");
    String disclaimer = elem.getAttribute("disclaimer");
    String accessConstraints = elem.getAttribute("accessConstraints");
    String useConstraints = elem.getAttribute("useConstraints");
    String acknowledgement = elem.getAttribute("acknowledgement");

    HierarchyType type = new HierarchyType(code, label, description, organization.getCode());
    type.setProgress(progress);
    type.setDisclaimer(disclaimer);
    type.setAccessConstraints(accessConstraints);
    type.setUseConstraints(useConstraints);
    type.setAcknowledgement(acknowledgement);

    ServiceFactory.getHierarchyPermissionService().enforceCanCreate(organization.getCode());

    return new ServerHierarchyTypeBuilder().createHierarchyType(type);
  }

  private ServerGeoObjectType createServerGeoObjectType(Organization organization, Element elem)
  {
    String code = elem.getAttribute("code");
    LocalizedValue label = this.getLabel(elem);
    LocalizedValue description = this.getDescription(elem);
    String visibility = elem.getAttribute("visibility");
    GeometryType geometryType = this.getGeometryType(elem);
    boolean isGeometryEditable = this.getIsGeometryEditable(elem);
    boolean isAbstract = this.getIsGroup(elem) || ( elem.getElementsByTagName("group-item").getLength() > 0 );

    GeoObjectType type = new GeoObjectType(code, geometryType, label, description, isGeometryEditable, organization.getCode(), adapter);
    type.setIsPrivate(this.getIsPrivate(visibility));
    type.setIsAbstract(isAbstract);

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(organization.getCode(), type.getIsPrivate());

    return new ServerGeoObjectTypeConverter().create(type);
  }

  private LocalizedValue getDescription(Element elem)
  {
    String description = elem.getAttribute("description");

    LocalizedValue value = new LocalizedValue(description);
    value.setValue(LocalizedValue.DEFAULT_LOCALE, description);
    return value;
  }

  private LocalizedValue getLabel(Element elem)
  {
    String label = elem.getAttribute("label");

    LocalizedValue value = new LocalizedValue(label);
    value.setValue(LocalizedValue.DEFAULT_LOCALE, label);
    return value;
  }

  private boolean getIsGroup(Element elem)
  {
    String isGroup = elem.getAttribute("isGroup");

    return isGroup != null && isGroup.equalsIgnoreCase("true");
  }

  private boolean getIsGeometryEditable(Element elem)
  {
    String isGeometryEditable = elem.getAttribute("isGeometryEditable");

    return isGeometryEditable != null && isGeometryEditable.equalsIgnoreCase("true");
  }

  private GeometryType getGeometryType(Element elem)
  {
    String geometryType = elem.getAttribute("geometryType");

    if (geometryType.equalsIgnoreCase("POINT"))
    {
      return GeometryType.MULTIPOINT;
    }
    else if (geometryType.equalsIgnoreCase("LINE"))
    {
      return GeometryType.MULTILINE;
    }
    else if (geometryType.equalsIgnoreCase("POLYGON"))
    {
      return GeometryType.MULTIPOLYGON;
    }
    else if (geometryType.equalsIgnoreCase("MIXED"))
    {
      return GeometryType.MIXED;
    }

    throw new ProgrammingErrorException("Unknown geometry type [" + geometryType + "]");
  }

  private Boolean getIsPrivate(String visibility)
  {
    return visibility != null && visibility.equalsIgnoreCase("private");
  }

  private int getPrecision(Element elem)
  {
    String precision = elem.getAttribute("precision");

    return Integer.parseInt(precision);
  }

  private int getScale(Element elem)
  {
    String scale = elem.getAttribute("scale");

    return Integer.parseInt(scale);
  }

}
