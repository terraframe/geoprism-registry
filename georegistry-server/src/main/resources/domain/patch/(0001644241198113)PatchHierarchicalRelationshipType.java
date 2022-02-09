<version xsi:noNamespaceSchemaLocation="classpath:com/runwaysdk/resources/xsd/version.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <doIt>
    <create>
      <mdBusiness name="net.geoprism.registry.HierarchicalRelationshipType" label="Hierarchical Relationship" table="hierarchical_relationship" generateController="false" cacheAlgorithm="everything">
        <attributes>
          <char name="code" size="255" required="true" indexType="unique index" label="Code" />
          <localChar name="displayLabel" label="Label" required="true" />
          <localChar name="description" label="Description" required="false" />        
          <reference name="organization" type="net.geoprism.registry.Organization" required="true" label="Organization" />          
          <reference name="mdTermRelationship" type="com.runwaysdk.system.metadata.MdTermRelationship" required="true" label="MdTermRelationship" />
          <reference name="mdEdge" type="com.runwaysdk.system.metadata.MdEdge" required="true" label="MdTermRelationship" />          
          <text name="abstractDescription" required="false" label="Abstract" />
          <text name="progress" required="false" label="Progress" />
          <text name="acknowledgement" required="false" label="Acknowledgement and disclaimer" />
          <text name="contact" required="false" label="Contact information" />
          <text name="accessConstraints" label="Access Constraints" />
          <text name="useConstraints" label="Use Constraints" />         
          <text name="disclaimer" label="Disclaimer" />
          <text name="phoneNumber" label="Telephone number" />
          <text name="email" label="Email address" />             
        </attributes>
      </mdBusiness>    
    </create>
    <update>
      <mdBusiness name="net.geoprism.registry.InheritedHierarchyAnnotation">
        <attributes>
          <reference name="inheritedHierarchy" required="false" />
          <reference name="forHierarchy" required="false" />
        </attributes>
        <create>
          <attributes>
            <reference name="inheritedHierarchicalRelationshipType" type="net.geoprism.registry.HierarchicalRelationshipType" required="false" label="Inherited Hierarchy" />
            <reference name="forHierarchicalRelationshipType" type="net.geoprism.registry.HierarchicalRelationshipType" required="false" label="For Hierarchy" />        
          </attributes>
        </create>
      </mdBusiness>
    </update>
    <delete>
<!-- 
      <object type="com.runwaysdk.system.metadata.MdBusiness" key="net.geoprism.registry.AttributeHierarchy" />    
 -->    
    </delete>
    
    <permissions>
      <role roleName="PUBLIC">
        <grant>
          <mdBusinessPermission type="net.geoprism.registry.HierarchicalRelationshipType">
            <operation name="ALL" />
          </mdBusinessPermission>
        </grant>
      </role>
      <role roleName="geoprism.admin.Administrator">
        <grant>
          <mdBusinessPermission type="net.geoprism.registry.HierarchicalRelationshipType">
            <operation name="ALL" />
          </mdBusinessPermission>
        </grant>
      </role>
      <role roleName="cgr.RegistryMaintainer">
        <grant>
          <mdBusinessPermission type="net.geoprism.registry.HierarchicalRelationshipType">
            <operation name="ALL" />
          </mdBusinessPermission>
        </grant>
      </role>
      <role roleName="cgr.RegistryContributor">
        <grant>
          <mdBusinessPermission type="net.geoprism.registry.HierarchicalRelationshipType">
            <operation name="READ" />
            <operation name="READ_ALL_ATTRIBUTES" />
          </mdBusinessPermission>
        </grant>
      </role>
      <role roleName="cgr.APIConsumer">
        <grant>
          <mdBusinessPermission type="net.geoprism.registry.HierarchicalRelationshipType">
            <operation name="READ" />
            <operation name="READ_ALL_ATTRIBUTES" />
          </mdBusinessPermission>
        </grant>
      </role>
    </permissions>
    
  </doIt>
  <undoIt>
    <delete>
      <object type="com.runwaysdk.system.metadata.MdAttribute" key="net.geoprism.registry.InheritedHierarchyAnnotation.inheritedHierarchicalRelationshipType" />
      <object type="com.runwaysdk.system.metadata.MdAttribute" key="net.geoprism.registry.InheritedHierarchyAnnotation.forHierarchicalRelationshipType" />
      <object type="com.runwaysdk.system.metadata.MdBusiness" key="net.geoprism.registry.HierarchicalRelationshipType" />
    </delete>
  </undoIt>
</version>