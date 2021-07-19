package net.geoprism.registry.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;

public class XMLImporter
{
  private ServerGeoObjectTypeConverter service;

  private RegistryAdapter              adapter;

  public XMLImporter()
  {
    this.service = new ServerGeoObjectTypeConverter();
    this.adapter = ServiceFactory.getAdapter();
  }

  @Transaction
  public List<ServerElement> importXMLDefinitions(Organization organization, InputStream istream)
  {
    LinkedList<ServerElement> list = new LinkedList<ServerElement>();

    try
    {

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = factory.newDocumentBuilder();
      Document doc = dBuilder.parse(istream);

      NodeList nList = doc.getElementsByTagName("type");

      for (int i = 0; i < nList.getLength(); i++)
      {
        Node nNode = nList.item(i);

        if (nNode.getNodeType() == Node.ELEMENT_NODE)
        {
          Element elem = (Element) nNode;

          ServerGeoObjectType type = createServerGeoObjectType(organization, elem);

          this.addAttributes(elem, type);

          list.add(type);
        }
      }
    }
    catch (ParserConfigurationException | IOException | SAXException e)
    {
      throw new ProgrammingErrorException(e);
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
        }
      }
    }
  }

  private ServerGeoObjectType createServerGeoObjectType(Organization organization, Element elem)
  {
    String code = elem.getAttribute("code");
    LocalizedValue label = this.getLabel(elem);
    LocalizedValue description = this.getDescription(elem);
    String visibility = elem.getAttribute("visibility");
    GeometryType geometryType = this.getGeometryType(elem);
    boolean isGeometryEditable = this.getIsGeometryEditable(elem);

    GeoObjectType type = new GeoObjectType(code, geometryType, label, description, isGeometryEditable, organization.getCode(), adapter);
    type.setIsPrivate(this.getIsPrivate(visibility));

    return service.create(type);
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
      return GeometryType.POLYGON;
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
