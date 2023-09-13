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
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.ServiceFactory;

public class XMLExporter
{
  private static Logger logger = LoggerFactory.getLogger(XMLExporter.class);

  /**
   * The DOM <code>document</code> that is populated with data from the core.
   */
  private Document      document;

  /**
   * The <code>root</code> element of the DOM document.
   */
  private Element       root;

  private ServerOrganization  orginzation;

  private Set<String>   businessEdgeTypes;

  /**
   * Initializes the <code>document</code>, creates the <code>root</code>
   * element, and parses the <code>schema</code>.
   * 
   */
  public XMLExporter(ServerOrganization orginzation)
  {
    this.orginzation = orginzation;
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
    ServerMetadataCache cache = ServiceFactory.getMetadataCache();
    List<ServerGeoObjectType> types = cache.getAllGeoObjectTypes();

    types.stream().filter(type -> type.getOrganizationCode().equals(this.orginzation.getCode())).forEach(type -> {
      this.exportType(type);
    });

    List<ServerHierarchyType> hierarchies = cache.getAllHierarchyTypes();

    hierarchies.stream().filter(type -> type.getOrganizationCode().equals(this.orginzation.getCode())).forEach(type -> {
      this.exportHierarchy(type);
    });

    BusinessType.getForOrganization(this.orginzation).forEach(type -> {
      this.exportBusinessType(type);
    });

    this.businessEdgeTypes.stream().map(code -> BusinessEdgeType.getByCode(code)).forEach(type -> {
      this.exportBusinessEdgeType(type);
    });

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

  private void exportBusinessType(BusinessType type)
  {
    Element element = document.createElement("business-type");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());

    MdAttribute labelAttribute = type.getLabelAttribute();

    if (labelAttribute != null)
    {
      element.setAttribute("labelAttribute", labelAttribute.getAttributeName());
    }

    this.root.appendChild(element);

    type.getEdgeTypes().forEach(edgeType -> {
      this.businessEdgeTypes.add(edgeType.getCode());
    });
  }

  private void exportBusinessEdgeType(BusinessEdgeType type)
  {
    Element element = document.createElement("business-edge");
    element.setAttribute("code", type.getCode());
    element.setAttribute("label", type.getLabel().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    element.setAttribute("description", type.getDescription().getValue());
    element.setAttribute("parentTypeCode", type.getParent().getCode());
    element.setAttribute("childTypeCode", type.getChild().getCode());

    this.root.appendChild(element);
  }

  private void exportHierarchy(ServerHierarchyType type)
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

    List<HierarchyNode> rootTypes = type.getRootGeoObjectTypes();

    if (rootTypes.size() > 0)
    {
      HierarchyNode rootType = rootTypes.get(0);
      String inheritedHierarchyCode = rootType.getInheritedHierarchyCode();

      if (inheritedHierarchyCode != null && inheritedHierarchyCode.length() > 0)
      {
        element.setAttribute("extends", inheritedHierarchyCode);
      }
    }

    rootTypes.forEach(node -> {
      Element child = this.exportChild(node);

      element.appendChild(child);
    });

    this.root.appendChild(element);
  }

  private Element exportChild(HierarchyNode node)
  {
    Element element = document.createElement("child");
    element.setAttribute("code", node.getGeoObjectType().getCode());

    node.getChildren().forEach(child -> {
      element.appendChild(this.exportChild(child));
    });

    return element;
  }

  private void exportType(ServerGeoObjectType type)
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

      type.getAttributeMap().forEach((attirbuteName, attributeType) -> {

        if (isValid(attributeType))
        {
          Element attribute = this.exportAttribute(attributeType);

          attributes.appendChild(attribute);
        }
      });

      element.appendChild(attributes);

      if (type.getIsAbstract())
      {
        type.getSubtypes().forEach(subtype -> {
          Element groupItem = this.exportGroupItem(subtype);

          element.appendChild(groupItem);
        });

        element.appendChild(attributes);

      }

      this.root.appendChild(element);
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

    attribute.setAttribute("name", attributeType.getName());
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
