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
package net.geoprism.registry.conversion;

import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDecDAOIF;
import com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLocalDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.RelationshipDAOIF;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.graph.VertexObjectDAOIF;
import com.runwaysdk.session.Session;

public class AttributeTypeConverter extends LocalizedValueConverter
{
  public AttributeType build(MdAttributeConcreteDAOIF mdAttribute)
  {
    Locale locale = Session.getCurrentLocale();

    String attributeName = mdAttribute.definesAttribute();
    LocalizedValue displayLabel = AttributeTypeConverter.convert(mdAttribute.getDisplayLabel(locale), mdAttribute.getDisplayLabels());
    LocalizedValue description = AttributeTypeConverter.convert(mdAttribute.getDescription(locale), mdAttribute.getDescriptions());
    boolean required = mdAttribute.isRequired();
    boolean unique = mdAttribute.isUnique();

    boolean isChangeOverTime = true;
    DefaultAttribute defaultAttr = DefaultAttribute.getByAttributeName(attributeName);
    if (defaultAttr != null)
    {
      isChangeOverTime = defaultAttr.isChangeOverTime();
    }

    if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeBooleanType.TYPE, required, unique, isChangeOverTime);
    }
    else if (mdAttribute instanceof MdAttributeLocalDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeLocalType.TYPE, required, unique, isChangeOverTime);
    }
    else if (mdAttribute instanceof MdAttributeCharacterDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeCharacterType.TYPE, required, unique, isChangeOverTime);
    }
    else if (mdAttribute instanceof MdAttributeDateDAOIF || mdAttribute instanceof MdAttributeDateTimeDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeDateType.TYPE, required, unique, isChangeOverTime);
    }
    else if (mdAttribute instanceof MdAttributeDecDAOIF)
    {
      MdAttributeDecDAOIF mdAttributeDec = (MdAttributeDecDAOIF) mdAttribute;

      AttributeFloatType attributeType = (AttributeFloatType) AttributeType.factory(attributeName, displayLabel, description, AttributeFloatType.TYPE, required, unique, isChangeOverTime);
      attributeType.setPrecision(Integer.parseInt(mdAttributeDec.getLength()));
      attributeType.setScale(Integer.parseInt(mdAttributeDec.getDecimal()));

      return attributeType;
    }
    else if (mdAttribute instanceof MdAttributeIntegerDAOIF || mdAttribute instanceof MdAttributeLongDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeIntegerType.TYPE, required, unique, isChangeOverTime);
    }
    else if (mdAttribute instanceof MdAttributeClassificationDAOIF)
    {
      MdClassificationDAOIF mdClassification = ( (MdAttributeClassificationDAOIF) mdAttribute ).getMdClassificationDAOIF();

      AttributeClassificationType attributeType = (AttributeClassificationType) AttributeType.factory(attributeName, displayLabel, description, AttributeClassificationType.TYPE, required, unique, isChangeOverTime);
      attributeType.setClassificationType(mdClassification.definesType());

      VertexObjectDAOIF root = ( (MdAttributeClassificationDAOIF) mdAttribute ).getRoot();

      if (root != null)
      {
        VertexObject classification = VertexObject.instantiate((VertexObjectDAO) root);
        VertexTermConverter termBuilder = new VertexTermConverter(classification);
        Term adapterTerm = termBuilder.build();

        attributeType.setRootTerm(adapterTerm);
      }

      return attributeType;
    }
    else if (mdAttribute instanceof MdAttributeEnumerationDAOIF || mdAttribute instanceof MdAttributeTermDAOIF)
    {
      AttributeTermType attributeType = (AttributeTermType) AttributeType.factory(attributeName, displayLabel, description, AttributeTermType.TYPE, required, unique, isChangeOverTime);

      if (mdAttribute instanceof MdAttributeTermDAOIF)
      {
        List<RelationshipDAOIF> rels = ( (MdAttributeTermDAOIF) mdAttribute ).getAllAttributeRoots();

        if (rels.size() > 0)
        {
          RelationshipDAOIF rel = rels.get(0);

          BusinessDAO classy = (BusinessDAO) rel.getChild();

          TermConverter termBuilder = new TermConverter(classy.getKey());
          Term adapterTerm = termBuilder.build();

          attributeType.setRootTerm(adapterTerm);
        }
        else
        {
          throw new ProgrammingErrorException("Expected an attribute root on MdAttribute [" + mdAttribute.getKey() + "].");
        }
      }
      else
      {
        throw new ProgrammingErrorException("Enum attributes are not supported at this time.");
      }

      return attributeType;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + mdAttribute.getClass().getSimpleName() + "]");
  }

}
