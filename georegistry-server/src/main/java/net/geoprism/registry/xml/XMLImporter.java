/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 * <p>
 * This file is part of Geoprism Registry(tm).
 * <p>
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p>
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
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

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.cache.TransactionCacheFacade;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;

public class XMLImporter {
    private static final String GEO_OBECT_TYPE_PREFIX = "#g_";

    private static final String HIERARCHY_TYPE_PREFIX = "#h_";

    private static final String BUSINESS_TYPE_PREFIX = "#h_";

    private static final String BUSINESS_EDGE_TYPE_PREFIX = "#h_";

    private final RegistryAdapter adapter;

    private final GeoObjectTypeBusinessServiceIF typeService;

    private final HierarchyTypeBusinessServiceIF hierarchyService;

    private final BusinessTypeBusinessServiceIF bTypeService;

    private final BusinessEdgeTypeBusinessServiceIF bEdgeService;

    private final DirectedAcyclicGraphTypeBusinessServiceIF dagService;

    private final UndirectedGraphTypeBusinessServiceIF undirectedService;

    private final Set<String> importedTypes;

    private Document doc;

    private final boolean validate;

    public XMLImporter() {
        this(true);
    }

    public XMLImporter(boolean validate) {
        this.typeService = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);
        this.hierarchyService = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);
        this.bTypeService = ServiceFactory.getBean(BusinessTypeBusinessServiceIF.class);
        this.bEdgeService = ServiceFactory.getBean(BusinessEdgeTypeBusinessServiceIF.class);
        this.dagService = ServiceFactory.getBean(DirectedAcyclicGraphTypeBusinessServiceIF.class);
        this.undirectedService = ServiceFactory.getBean(UndirectedGraphTypeBusinessServiceIF.class);
        this.adapter = ServiceFactory.getAdapter();

        this.validate = validate;
        this.importedTypes = new TreeSet<String>();
    }

    @Transaction
    public List<ServerElement> importXMLDefinitions(ApplicationResource resource, ServerOrganization... organizations) {
        return this.importXMLDefinitions(resource, Arrays.asList(organizations));
    }

    @Transaction
    public List<ServerElement> importXMLDefinitions(ApplicationResource resource, List<ServerOrganization> organizations) {

        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        try (InputStream istream = resource.openNewStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = factory.newDocumentBuilder();
            this.doc = dBuilder.parse(istream);

            NodeList nList = doc.getElementsByTagName("organization");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;
                    String code = elem.getAttribute("code");

                    organizations.stream().filter(org -> org.getCode().equals(code)).forEach(organization -> {
                        list.addAll(this.createTypes(organization, elem));
                        list.addAll(this.createHierarchies(organization, elem));
                        list.addAll(this.createBusinessTypes(organization, elem));
                        list.addAll(this.createBusinessEdgeTypes(organization, elem));
                    });
                }
            }

            list.addAll(this.createDirectedAcyclicGraphTypes(doc));
            list.addAll(this.createUndirectedGraphTypes(doc));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ProgrammingErrorException(e);
        }

        return list;
    }

    private List<ServerElement> createHierarchies(ServerOrganization organization, Element parent) {
        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nList = parent.getElementsByTagName("hierarchy");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                createServerHierarchyType(organization, elem).ifPresent(list::add);
            }
        }

        return list;
    }

    private List<ServerElement> createBusinessEdgeTypes(ServerOrganization organization, Element parent) {
        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nList = parent.getElementsByTagName("business-edge");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                createBusinessEdgeType(organization, elem).ifPresent(list::add);
            }
        }

        return list;
    }

    private List<ServerElement> createDirectedAcyclicGraphTypes(Document doc) {
        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nList = doc.getElementsByTagName("dag");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                list.add(this.createDirectedAcyclicGraphType(elem));
            }
        }

        return list;
    }

    private List<ServerElement> createUndirectedGraphTypes(Document doc) {
        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nList = doc.getElementsByTagName("undirected-graph");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                list.add(this.createUndirectedGraphType(elem));
            }
        }

        return list;
    }

    private List<ServerElement> createTypes(ServerOrganization organization, Element parent) {
        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nodes = parent.getElementsByTagName("type");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;

                list.addAll(this.createServerGeoObjectType(organization, elem));
            }
        }

        return list;
    }

    private List<ServerElement> createBusinessTypes(ServerOrganization organization, Element parent) {
        LinkedList<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nList = parent.getElementsByTagName("business-type");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                createBusinessType(organization, elem).ifPresent(list::add);
            }
        }

        return list;
    }

    private List<ServerElement> addGroupItems(Element root, ServerGeoObjectType superType) {
        List<ServerElement> list = new LinkedList<ServerElement>();

        NodeList nList = root.getElementsByTagName("group-item");

        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;

                String code = elem.getAttribute("code");
                LocalizedValue label = this.getLabel(elem);
                LocalizedValue description = this.getDescription(elem);

                GeoObjectType type = new GeoObjectType(code, superType.getGeometryType(), label, description, superType.isGeometryEditable(), superType.getOrganization().getCode(), adapter);
                type.setSuperTypeCode(superType.getCode());
                type.setIsPrivate(superType.getIsPrivate());

                ServerGeoObjectType result = this.typeService.create(type);

                list.add(result);

                TransactionCacheFacade.put(result);

                this.importedTypes.add(GEO_OBECT_TYPE_PREFIX + code);
            }
        }

        return list;
    }

    private void addAttributes(Element root, ServerGeoObjectType type) {
        NodeList attributeList = root.getElementsByTagName("attributes");

        if (attributeList.getLength() > 0) {
            NodeList nList = attributeList.item(0).getChildNodes();

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;

                    String code = elem.getAttribute("code");
                    LocalizedValue label = this.getLabel(elem);
                    LocalizedValue description = this.getDescription(elem);

                    if (elem.getTagName().equals("text")) {
                        this.typeService.createAttributeType(type, new AttributeCharacterType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("boolean")) {
                        this.typeService.createAttributeType(type, new AttributeBooleanType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("integer")) {
                        this.typeService.createAttributeType(type, new AttributeIntegerType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("decimal")) {
                        AttributeFloatType attributeType = new AttributeFloatType(code, label, description, false, false, false);
                        attributeType.setPrecision(this.getPrecision(elem));
                        attributeType.setScale(this.getScale(elem));

                        this.typeService.createAttributeType(type, attributeType);
                    } else if (elem.getTagName().equals("date")) {
                        this.typeService.createAttributeType(type, new AttributeDateType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("term")) {
                        AttributeTermType attributeType = new AttributeTermType(code, label, description, false, false, false);
                        attributeType = (AttributeTermType) this.typeService.createAttributeType(type, attributeType);

                        Term rootTerm = attributeType.getRootTerm();

                        this.createTermOptions(elem, rootTerm);
                    } else if (elem.getTagName().equals("classification")) {
                        String rootCode = elem.getAttribute("root");
                        String classificationType = elem.getAttribute("classificationType");

                        AttributeClassificationType attributeType = new AttributeClassificationType(code, label, description, false, false, false);
                        attributeType.setRootTerm(new Term(rootCode, new LocalizedValue(""), new LocalizedValue("")));
                        attributeType.setClassificationType(classificationType);

                        attributeType = (AttributeClassificationType) this.typeService.createAttributeType(type, attributeType);
                    }
                }
            }
        }
    }

    private void addAttributes(Element root, BusinessType type) {
        NodeList attributeList = root.getElementsByTagName("attributes");

        if (attributeList.getLength() > 0) {
            NodeList nList = attributeList.item(0).getChildNodes();

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nNode;

                    String code = elem.getAttribute("code");
                    LocalizedValue label = this.getLabel(elem);
                    LocalizedValue description = this.getDescription(elem);

                    if (elem.getTagName().equals("text")) {
                        this.bTypeService.createAttributeType(type, new AttributeCharacterType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("boolean")) {
                        this.bTypeService.createAttributeType(type, new AttributeBooleanType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("integer")) {
                        this.bTypeService.createAttributeType(type, new AttributeIntegerType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("decimal")) {
                        AttributeFloatType attributeType = new AttributeFloatType(code, label, description, false, false, false);
                        attributeType.setPrecision(this.getPrecision(elem));
                        attributeType.setScale(this.getScale(elem));

                        this.bTypeService.createAttributeType(type, attributeType);
                    } else if (elem.getTagName().equals("date")) {
                        this.bTypeService.createAttributeType(type, new AttributeDateType(code, label, description, false, false, false));
                    } else if (elem.getTagName().equals("term")) {
                        AttributeTermType attributeType = new AttributeTermType(code, label, description, false, false, false);
                        attributeType = (AttributeTermType) this.bTypeService.createAttributeType(type, attributeType);

                        Term rootTerm = attributeType.getRootTerm();

                        this.createTermOptions(elem, rootTerm);
                    } else if (elem.getTagName().equals("classification")) {
                        String rootCode = elem.getAttribute("root");
                        String classificationType = elem.getAttribute("classificationType");

                        AttributeClassificationType attributeType = new AttributeClassificationType(code, label, description, false, false, false);
                        attributeType.setRootTerm(new Term(rootCode, new LocalizedValue(""), new LocalizedValue("")));
                        attributeType.setClassificationType(classificationType);

                        attributeType = (AttributeClassificationType) this.bTypeService.createAttributeType(type, attributeType);
                    }
                }
            }
        }
    }

    private void createTermOptions(Element attributeNode, Term root) {
        NodeList attributeList = attributeNode.getElementsByTagName("option");

        for (int i = 0; i < attributeList.getLength(); i++) {
            Node nNode = attributeList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
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

    private void addChildren(ServerHierarchyType hierarchy, ServerGeoObjectType parent, Element root) {
        NodeList childNodes = root.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) childNode;

                String code = elem.getAttribute("code");

                if (!this.importedTypes.contains(GEO_OBECT_TYPE_PREFIX + code)) {
                    // Handle imported missing geo object type
                    findAndCreateGeoObjectType(code);
                }

                ServerGeoObjectType child = ServerGeoObjectType.get(code);

                this.hierarchyService.addToHierarchy(hierarchy, parent, child, this.validate);

                if (root.hasAttribute("extends")) {
                    String inheritedHierarchyCode = root.getAttribute("extends");

                    if (!this.importedTypes.contains(HIERARCHY_TYPE_PREFIX + inheritedHierarchyCode)) {
                        // Handle imported missing hierarchy type
                        findAndCreateHierarchyType(inheritedHierarchyCode);
                    }

                    ServerHierarchyType inheritedHierarchy = ServerHierarchyType.get(inheritedHierarchyCode);

                    this.typeService.setInheritedHierarchy(child, hierarchy, inheritedHierarchy);
                }

                this.addChildren(hierarchy, child, elem);
            }
        }

    }

    protected void findAndCreateGeoObjectType(String code) {
        findAndExecute(code, "type", this::createServerGeoObjectType);
    }

    protected void findAndCreateHierarchyType(String code) {
        findAndExecute(code, "hierarchy", this::createServerHierarchyType);
    }

    protected void findAndExecute(String code, String tagName, BiConsumer<ServerOrganization, Element> consumer) {
        System.out.println("Searching [" + tagName + "] for " + code);

        NodeList elements = this.doc.getElementsByTagName(tagName);

        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);

            if (code.equals(element.getAttribute("code"))) {
                Element orgNode = (Element) element.getParentNode();

                consumer.accept(ServerOrganization.getByCode(orgNode.getAttribute("code")), element);
            }
        }
    }

    private DirectedAcyclicGraphType createDirectedAcyclicGraphType(Element elem) {
        String code = elem.getAttribute("code");
        LocalizedValue label = this.getLabel(elem);
        LocalizedValue description = this.getDescription(elem);

        return this.dagService.create(code, label, description);
    }

    private UndirectedGraphType createUndirectedGraphType(Element elem) {
        String code = elem.getAttribute("code");
        LocalizedValue label = this.getLabel(elem);
        LocalizedValue description = this.getDescription(elem);

        return this.undirectedService.create(code, label, description);
    }

    private Optional<ServerHierarchyType> createServerHierarchyType(ServerOrganization organization, Element elem) {
        String code = elem.getAttribute("code");

        if (!this.importedTypes.contains(HIERARCHY_TYPE_PREFIX + code)) {
            LocalizedValue label = this.getLabel(elem);
            LocalizedValue description = this.getDescription(elem);
            String progress = elem.getAttribute("progress");
            String disclaimer = elem.getAttribute("disclaimer");
            String accessConstraints = elem.getAttribute("accessConstraints");
            String useConstraints = elem.getAttribute("useConstraints");
            String acknowledgement = elem.getAttribute("acknowledgement");

            HierarchyType dto = new HierarchyType(code, label, description, organization.getCode());
            dto.setProgress(progress);
            dto.setDisclaimer(disclaimer);
            dto.setAccessConstraints(accessConstraints);
            dto.setUseConstraints(useConstraints);
            dto.setAcknowledgement(acknowledgement);

            ServiceFactory.getHierarchyPermissionService().enforceCanCreate(organization.getCode());

            ServerHierarchyType type = this.hierarchyService.createHierarchyType(dto);

            TransactionCacheFacade.put(type);
            this.importedTypes.add(HIERARCHY_TYPE_PREFIX + type.getCode());

            this.addChildren(type, RootGeoObjectType.INSTANCE, elem);

            return Optional.of(type);
        }

        return Optional.empty();
    }

    private Optional<BusinessEdgeType> createBusinessEdgeType(ServerOrganization organization, Element elem) {
        String code = elem.getAttribute("code");

        if (!this.importedTypes.contains(BUSINESS_TYPE_PREFIX + code)) {
            LocalizedValue label = this.getLabel(elem);
            LocalizedValue description = this.getDescription(elem);
            String parentTypeCode = elem.getAttribute("parentTypeCode");
            String childTypeCode = elem.getAttribute("childTypeCode");

            ServiceFactory.getHierarchyPermissionService().enforceCanCreate(organization.getCode());

            BusinessEdgeType type = this.bEdgeService.create(organization.getCode(), code, label, description, parentTypeCode, childTypeCode);

            TransactionCacheFacade.put(type);
            this.importedTypes.add(BUSINESS_EDGE_TYPE_PREFIX + type.getCode());

            return Optional.of(type);
        }

        return Optional.empty();
    }

    private List<ServerElement> createServerGeoObjectType(ServerOrganization organization, Element elem) {
        List<ServerElement> list = new LinkedList<ServerElement>();

        String code = elem.getAttribute("code");

        if (!this.importedTypes.contains(GEO_OBECT_TYPE_PREFIX + code)) {
            LocalizedValue label = this.getLabel(elem);
            LocalizedValue description = this.getDescription(elem);
            String visibility = elem.getAttribute("visibility");
            GeometryType geometryType = this.getGeometryType(elem);
            boolean isGeometryEditable = this.getIsGeometryEditable(elem);
            boolean isAbstract = this.getIsGroup(elem) || (elem.getElementsByTagName("group-item").getLength() > 0);

            GeoObjectType dto = new GeoObjectType(code, geometryType, label, description, isGeometryEditable, organization.getCode(), adapter);
            dto.setIsPrivate(this.getIsPrivate(visibility));
            dto.setIsAbstract(isAbstract);

            ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(organization.getCode(), dto.getIsPrivate());

            ServerGeoObjectType type = this.typeService.create(dto);

            TransactionCacheFacade.put(type);

            this.importedTypes.add(GEO_OBECT_TYPE_PREFIX + type.getCode());

            this.addAttributes(elem, type);

            list.add(type);
            list.addAll(this.addGroupItems(elem, type));
        }

        return list;
    }

    private Optional<BusinessType> createBusinessType(ServerOrganization organization, Element elem) {
        String code = elem.getAttribute("code");

        if (!this.importedTypes.contains(BUSINESS_TYPE_PREFIX + code)) {
            LocalizedValue label = this.getLabel(elem);

            ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(organization.getCode(), false);

            JsonObject object = new JsonObject();
            object.addProperty(BusinessType.CODE, code);
            object.addProperty(BusinessType.ORGANIZATION, organization.getCode());
            object.add(BusinessType.DISPLAYLABEL, label.toJSON());

            BusinessType type = this.bTypeService.apply(object);

            TransactionCacheFacade.put(type);

            this.importedTypes.add(BUSINESS_TYPE_PREFIX + type.getCode());

            this.addAttributes(elem, type);

            this.updateBusinessType(type, elem);

            return Optional.of(type);
        }

        return Optional.empty();
    }

    private void updateBusinessType(BusinessType type, Element elem) {
        String labelAttribute = elem.getAttribute("labelAttribute");

        if (!StringUtils.isBlank(labelAttribute)) {
            type.appLock();
            type.setLabelAttribute(labelAttribute);
            type.apply();
        }
    }

    private LocalizedValue getDescription(Element elem) {
        String description = elem.getAttribute("description");

        LocalizedValue value = new LocalizedValue(description);
        value.setValue(LocalizedValue.DEFAULT_LOCALE, description);
        return value;
    }

    private LocalizedValue getLabel(Element elem) {
        String label = elem.getAttribute("label");

        LocalizedValue value = new LocalizedValue(label);
        value.setValue(LocalizedValue.DEFAULT_LOCALE, label);
        return value;
    }

    private boolean getIsGroup(Element elem) {
        String isGroup = elem.getAttribute("isGroup");

        return isGroup != null && isGroup.equalsIgnoreCase("true");
    }

    private boolean getIsGeometryEditable(Element elem) {
        String isGeometryEditable = elem.getAttribute("isGeometryEditable");

        return isGeometryEditable != null && isGeometryEditable.equalsIgnoreCase("true");
    }

    private GeometryType getGeometryType(Element elem) {
        String geometryType = elem.getAttribute("geometryType");

        if (geometryType.equalsIgnoreCase("POINT")) {
            return GeometryType.MULTIPOINT;
        } else if (geometryType.equalsIgnoreCase("LINE")) {
            return GeometryType.MULTILINE;
        } else if (geometryType.equalsIgnoreCase("POLYGON")) {
            return GeometryType.MULTIPOLYGON;
        } else if (geometryType.equalsIgnoreCase("MIXED")) {
            return GeometryType.MIXED;
        }

        throw new ProgrammingErrorException("Unknown geometry type [" + geometryType + "]");
    }

    private Boolean getIsPrivate(String visibility) {
        return visibility != null && visibility.equalsIgnoreCase("private");
    }

    private int getPrecision(Element elem) {
        String precision = elem.getAttribute("precision");

        return Integer.parseInt(precision);
    }

    private int getScale(Element elem) {
        String scale = elem.getAttribute("scale");

        return Integer.parseInt(scale);
    }

}
