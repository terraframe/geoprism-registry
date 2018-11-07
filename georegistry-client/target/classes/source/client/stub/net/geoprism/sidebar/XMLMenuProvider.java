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
package net.geoprism.sidebar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.RunwayConfigurationException;

public class XMLMenuProvider
{
  private static final String        fileName       = "geoprism/sidebar.xml";

  private static final Object        initializeLock = new Object();

  private static ArrayList<MenuItem> menu;

  private static ArrayList<MenuItem> links;

  public XMLMenuProvider()
  {
    String exMsg = "An exception occurred while reading the geoprism sidebar configuration file.";

    try
    {
      this.readMenu();
    }
    catch (Exception e)
    {
      throw new RunwayConfigurationException(exMsg, e);
    }
  }

  public ArrayList<MenuItem> getMenu()
  {
    return menu;
  }

  public ArrayList<MenuItem> getLinks()
  {
    return links;
  }

  public void readMenu() throws ParserConfigurationException, SAXException, IOException
  {

    synchronized (initializeLock)
    {
      if (menu != null)
      {
        return;
      }

      menu = new ArrayList<MenuItem>();
      links = new ArrayList<MenuItem>();
    }

    InputStream stream = ConfigurationManager.getResourceAsStream(ConfigurationManager.ConfigGroup.ROOT, fileName);

    if (stream != null)
    {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(stream);

      this.populateMenuItems(doc, "link-list", links);
      this.populateMenuItems(doc, "nav-list", menu);
    }
    else
    {
      throw new RunwayConfigurationException("Expected the geoprism sidebar configuration file on the classpath at [" + fileName + "].");
    }
  }

  private void populateMenuItems(Document doc, String tagName, List<MenuItem> collection)
  {
    NodeList roots = doc.getElementsByTagName(tagName);

    for (int j = 0; j < roots.getLength(); j++)
    {
      Node xmlMappings = roots.item(j);

      NodeList children = xmlMappings.getChildNodes();
      for (int i = 0; i < children.getLength(); ++i)
      {
        Node n = children.item(i);

        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
          this.creteMenuItem(collection, (Element) n);
        }
      }
    }
  }

  private void creteMenuItem(List<MenuItem> collection, Element el)
  {
    MenuItem item = this.createMenuItem(el);

    NodeList children = el.getChildNodes();

    for (int i = 0; i < children.getLength(); ++i)
    {
      Node nodeItem = children.item(i);

      if (nodeItem.getNodeType() == Node.ELEMENT_NODE)
      {
        Element childElement = (Element) nodeItem;
        MenuItem child = this.createMenuItem(childElement);

        item.addChild(child);
      }
    }

    collection.add(item);
  }

  private MenuItem createMenuItem(Element el)
  {
    String name = el.getAttribute("name");
    String uri = el.hasAttribute("uri") ? el.getAttribute("uri") : null;
    String roles = el.hasAttribute("roles") ? el.getAttribute("roles") : null;

    MenuItem item = new MenuItem(name, uri, roles);

    if (el.hasAttribute("synch"))
    {
      item.setSynch(el.getAttribute("synch"));
    }

    if (el.hasAttribute("classes"))
    {
      item.setClasses(el.getAttribute("classes"));
    }
    return item;
  }
}
