package net.geoprism.registry.conversion;

import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDecDAOIF;
import com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLocalDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.RelationshipDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.service.ServiceFactory;

public class AttributeTypeConverter extends LocalizedValueConverter
{
  public AttributeType build(MdAttributeConcreteDAOIF mdAttribute)
  {
    Locale locale = Session.getCurrentLocale();

    String attributeName = mdAttribute.definesAttribute();
    LocalizedValue displayLabel = this.convert(mdAttribute.getDisplayLabel(locale), mdAttribute.getDisplayLabels());
    LocalizedValue description = this.convert(mdAttribute.getDescription(locale), mdAttribute.getDescriptions());
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
    else if (mdAttribute instanceof MdAttributeEnumerationDAOIF || mdAttribute instanceof MdAttributeTermDAOIF)
    {
      AttributeTermType attributeType = (AttributeTermType) AttributeType.factory(attributeName, displayLabel, description, AttributeTermType.TYPE, required, unique, isChangeOverTime);

      if (mdAttribute instanceof MdAttributeEnumerationDAOIF && mdAttribute.definesAttribute().equals(DefaultAttribute.STATUS.getName()))
      {
        Term rootStatusTerm = ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.ROOT.code).get();

        attributeType.setRootTerm(rootStatusTerm);
      }
      else if (mdAttribute instanceof MdAttributeTermDAOIF)
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
