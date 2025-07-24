/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.io.XMLException;
import com.runwaysdk.system.metadata.MdAttribute;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.cache.ServerMetadataCache;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.service.business.ServiceFactory;

public class XMLExporter
{
  private static Logger                     logger = LoggerFactory.getLogger(XMLExporter.class);

  /**
   * The DOM <code>document</code> that is populated with data from the core.
   */
  private Document                          document;

  /**
   * The <code>root</code> element of the DOM document.
   */
  private Element                           root;

  private List<ServerOrganization>          organizations;

  private Set<String>                       businessEdgeTypes;

  private GeoObjectTypeBusinessServiceIF    typeService;

  private HierarchyTypeBusinessServiceIF    hierarchyService;

  private BusinessTypeBusinessServiceIF     bTypeService;

  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  /**
   * Initializes the <code>document</code>, creates the <code>root</code>
   * element, and parses the <code>schema</code>.
   * 
   */
  public XMLExporter(ServerOrganization... organizations)
  {
    this(Arrays.asList(organizations));
  }

  public XMLExporter(List<ServerOrganization> organizations)
  {
    this.typeService = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);
    this.hierarchyService = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);
    this.bTypeService = ServiceFactory.getBean(BusinessTypeBusinessServiceIF.class);
    this.bEdgeService = ServiceFactory.getBean(BusinessEdgeTypeBusinessServiceIF.class);

    this.organizations = organizations;
    this.businessEdgeTypes = new TreeSet<>();

    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);

      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.newDocument();

      root = document.createElement("CGR");
      document.appendChild(root);
    }
    catch (ParserConfigurationException e)
    {
      throw new XMLException(e);
    }
  }

  public Document getDocument()
  {
    return document;
  }

  public Element getRoot()
  {
    return root;
  }

  public void build()
  {
    for (ServerOrganization organization : organizations)
    {
      exportOrganization(organization);
    }

    if (new RolePermissionService().isSRA())
    {
      UndirectedGraphType.getAll().forEach(type -> {
        this.exportUndirectedGraphType(type);
      });

      DirectedAcyclicGraphType.getAll().forEach(type -> {
        this.exportDAG(type);
      });
    }
  }

  protected void exportOrganization(ServerOrganization organization)
  {
    this.businessEdgeTypes.clear();

    Element element = document.createElement("organization");
    element.setAttribute("code", organization.getCode());

    ServerMetadataCache cache = ServiceFactory.getMetadataCache();
    List<ServerGeoObjectType> types = cache.getAllGeoObjectTypes();

    types.stream().filter(type -> type.getOrganizationCode().equals(organization.getCode())).forEach(type -> {
      this.exportType(element, type);
    });

    List<ServerHierarchyType> hierarchies = cache.getAllHierarchyTypes();

    hierarchies.stream().filter(type -> type.getOrganizationCode().equals(organization.getCode())).forEach(type -> {
      this.exportHierarchy(element, type);
    });

    this.bTypeService.getForOrganization(organization).forEach(type -> {
      this.exportBusinessType(element, type);
    });

    this.businessEdgeTypes.stream().map(code -> this.bEdgeService.getByCodeOrThrow(code)).forEach(type -> {
      this.exportBusinessEdgeType(element, type);
    });

    this.root.appendChild(element);
  }

  private void exportUndirectedGraphType(UndirectedGraphType type)
  {
    Element element = document.createElement("undirected-graph");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    this.root.appendChild(element);
  }

  private void exportDAG(DirectedAcyclicGraphType type)
  {
    Element element = document.createElement("dag");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    this.root.appendChild(element);
  }

  private void exportBusinessType(Element parent, BusinessType type)
  {
    Element element = document.createElement("business-type");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());

    MdAttribute labelAttribute = type.getLabelAttribute();

    if (labelAttribute != null)
    {
      element.setAttribute("labelAttribute", labelAttribute.getAttributeName());
    }

    parent.appendChild(element);

    this.bTypeService.getEdgeTypes(type).forEach(edgeType -> {
      this.businessEdgeTypes.add(edgeType.getCode());
    });
  }

  private void exportBusinessEdgeType(Element parent, BusinessEdgeType type)
  {
    Element element = document.createElement("business-edge");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    element.setAttribute("parentTypeCode", this.bEdgeService.getParent(type).getCode());
    element.setAttribute("childTypeCode", this.bEdgeService.getChild(type).getCode());

    parent.appendChild(element);
  }

  private void exportHierarchy(Element parent, ServerHierarchyType type)
  {
    Element element = document.createElement("hierarchy");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    element.setAttribute("progress", type.getProgress());
    element.setAttribute("acknowledgement", type.getAcknowledgement());
    element.setAttribute("disclaimer", type.getDisclaimer());
    element.setAttribute("accessConstraints", type.getAccessConstraints());
    element.setAttribute("useConstraints", type.getUseConstraints());

    List<HierarchyNode> rootTypes = this.hierarchyService.getRootGeoObjectTypes(type);

    if (rootTypes.size() > 0)
    {
      HierarchyNode rootType = rootTypes.get(0);
      String inheritedHierarchyCode = rootType.getInheritedHierarchyCode();

      if (inheritedHierarchyCode != null && inheritedHierarchyCode.length() > 0)
      {
        element.setAttribute("extends", inheritedHierarchyCode);
      }
    }

    List<ServerGeoObjectType> nodes = this.hierarchyService.getDirectRootNodes(type);

    nodes.forEach(node -> {
      Element child = this.exportChild(type, node);

      element.appendChild(child);
    });

    parent.appendChild(element);
  }

  private Element exportChild(ServerHierarchyType type, ServerGeoObjectType node)
  {
    Element element = document.createElement("child");
    element.setAttribute("code", node.getCode());

    List<ServerGeoObjectType> children = this.hierarchyService.getChildren(type, node);

    children.forEach(child -> {
      element.appendChild(this.exportChild(type, child));
    });

    return element;
  }

  private void exportType(Element parent, ServerGeoObjectType type)
  {
    ServerGeoObjectType superType = type.getSuperType();

    if (superType == null)
    {
      Element element = document.createElement("type");
      element.setAttribute("code", type.getCode());
      element.setAttribute("label", type.getLabel().getValue());
      element.setAttribute("description", type.getDescription().getValue());
      element.setAttribute("visibility", type.getIsPrivate() ? "PRIVATE" : "PUBLIC");
      element.setAttribute("geometryType", this.getGeometryType(type));
      element.setAttribute("isGeometryEditable", Boolean.toString(type.isGeometryEditable()));
      element.setAttribute("isGroup", Boolean.toString(type.getIsAbstract()));

      Element attributes = document.createElement("attributes");

      type.toDTO().getAttributeMap().forEach((attirbuteName, attributeType) -> {

        if (isValid(attributeType))
        {
          Element attribute = this.exportAttribute(attributeType);

          attributes.appendChild(attribute);
        }
      });

      element.appendChild(attributes);

      if (type.getIsAbstract())
      {
        this.typeService.getSubtypes(type).forEach(subtype -> {
          Element groupItem = this.exportGroupItem(subtype);

          element.appendChild(groupItem);
        });

        element.appendChild(attributes);

      }

      parent.appendChild(element);
    }
  }

  private boolean isValid(AttributeType attributeType)
  {
    if (attributeType instanceof AttributeLocalType)
    {
      return false;
    }

    List<String> attributeNames = Arrays.asList(DefaultAttribute.values()).stream().map(attr -> attr.getName()).collect(Collectors.toList());

    return !attributeNames.contains(attributeType.getName());
  }

  private Element exportGroupItem(ServerGeoObjectType subtype)
  {
    Element element = document.createElement("group-item");
    element.setAttribute("code", subtype.getCode());
    element.setAttribute("label", subtype.getLabel().getValue());
    element.setAttribute("description", subtype.getDescription().getValue());

    return element;
  }

  private Element exportAttribute(AttributeType attributeType)
  {
    Element attribute = null;

    if (attributeType instanceof AttributeBooleanType)
    {
      attribute = document.createElement("boolean");
    }
    else if (attributeType instanceof AttributeCharacterType)
    {
      attribute = document.createElement("text");
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      attribute = document.createElement("integer");
    }
    else if (attributeType instanceof AttributeDateType)
    {
      attribute = document.createElement("date");
    }
    else if (attributeType instanceof AttributeFloatType)
    {
      AttributeFloatType type = (AttributeFloatType) attributeType;

      attribute = document.createElement("decimal");
      attribute.setAttribute("precision", Integer.toString(type.getPrecision()));
      attribute.setAttribute("scale", Integer.toString(type.getScale()));
    }
    else if (attributeType instanceof AttributeTermType)
    {
      AttributeTermType type = (AttributeTermType) attributeType;

      attribute = document.createElement("term");

      List<Term> terms = type.getTerms();

      for (Term term : terms)
      {
        Element element = this.exportTerm(term);

        attribute.appendChild(element);
      }
    }
    else if (attributeType instanceof AttributeClassificationType)
    {
      AttributeClassificationType type = (AttributeClassificationType) attributeType;

      attribute = document.createElement("classification");
      attribute.setAttribute("root", type.getRootTerm().getCode());
      attribute.setAttribute("classificationType", type.getClassificationType());
    }

    attribute.setAttribute("code", attributeType.getName());
    attribute.setAttribute("label", attributeType.getLabel().getValue());
    attribute.setAttribute("description", attributeType.getDescription().getValue());

    return attribute;
  }

  private Element exportTerm(Term term)
  {
    Element element = document.createElement("option");
    element.setAttribute("code", term.getCode());
    element.setAttribute("label", term.getLabel().getValue());
    element.setAttribute("description", term.getDescription().getValue());

    return element;
  }

  private String getGeometryType(ServerGeoObjectType type)
  {
    GeometryType geometryType = type.getGeometryType();

    if (geometryType.equals(GeometryType.LINE) || geometryType.equals(GeometryType.MULTILINE))
    {
      return "LINE";
    }
    else if (geometryType.equals(GeometryType.POINT) || geometryType.equals(GeometryType.MULTIPOINT))
    {
      return "POINT";
    }
    else if (geometryType.equals(GeometryType.POLYGON) || geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return "POLYGON";
    }
    else if (geometryType.equals(GeometryType.MIXED))
    {
      return "MIXED";
    }

    throw new UnsupportedOperationException();
  }

  public void write(File file)
  {
    try
    {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      DOMSource source = new DOMSource(document);

      try (FileWriter writer = new FileWriter(file))
      {
        StreamResult result = new StreamResult(writer);

        transformer.transform(source, result);
      }
    }
    catch (TransformerException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public InputStream write()
  {
    try
    {
      // Zip up the entire contents of the file
      final PipedOutputStream pos = new PipedOutputStream();
      final PipedInputStream pis = new PipedInputStream(pos);

      Thread t = new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {

            try
            {
              TransformerFactory transformerFactory = TransformerFactory.newInstance();

              Transformer transformer = transformerFactory.newTransformer();
              transformer.setOutputProperty(OutputKeys.INDENT, "yes");
              transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

              DOMSource source = new DOMSource(document);

              StreamResult result = new StreamResult(pos);

              transformer.transform(source, result);
            }
            finally
            {
              pos.close();
            }
          }
          catch (TransformerException | IOException e)
          {
            logger.error("Error while writing the XML file", e);
          }
        }
      });
      t.setDaemon(true);
      t.start();

      return pis;

    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
}
