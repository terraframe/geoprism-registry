package com.runwaysdk.build.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdAttribute;
import com.runwaysdk.system.metadata.MdAttributeLocalText;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListQuery;

public class LocalizeListMetadataFieldsPatch
{
  private static final Logger logger = LoggerFactory.getLogger(LocalizeListMetadataFieldsPatch.class);
  
  private MdBusiness masterlistMd = (MdBusiness) MdBusiness.getMdBusiness(MasterList.CLASS);
  
  // Attributes on MasterList which were deleted and replaced
  public static java.lang.String LISTABSTRACT = "listAbstract";
  public static java.lang.String PROCESS = "process";
  public static java.lang.String PROGRESS = "progress";
  public static java.lang.String ACCESSCONSTRAINTS = "accessConstraints";
  public static java.lang.String USECONSTRAINTS = "useConstraints";
  public static java.lang.String ACKNOWLEDGEMENTS = "acknowledgements";
  public static java.lang.String DISCLAIMER = "disclaimer";
  
  public static final String[] newAttrs = new String[] {
    MasterList.DESCRIPTIONLOCAL, MasterList.PROCESSLOCAL, MasterList.PROGRESSLOCAL, MasterList.ACCESSCONSTRAINTSLOCAL,
    MasterList.USECONSTRAINTSLOCAL, MasterList.ACKNOWLEDGEMENTSLOCAL, MasterList.DISCLAIMERLOCAL
  };
  
  public static final String[] oldAttrs = new String[] {
      LISTABSTRACT, PROCESS, PROGRESS, ACCESSCONSTRAINTS, USECONSTRAINTS, ACKNOWLEDGEMENTS, DISCLAIMER
  };
  
  public static void main(String[] args)
  {
    new LocalizeListMetadataFieldsPatch().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    dotItInTrans();
  }

  @Transaction
  private void dotItInTrans()
  {
    // First, define the new attributes
    logger.info("Defining new localized attributes for MasterList metadata.");
    defineAttributes();

    // Then, patch all existing data by copying the data from the old attribute to the new attribute
    migrateExistingLists();
    
    // Finally, we can delete the old attributes.
    logger.info("Deleting unlocalized attributes for MasterList metadata.");
    deleteAttributes();
  }
  
  private void migrateExistingLists()
  {
    MasterListQuery mlq = new MasterListQuery(new QueryFactory());
    
    logger.info("Migrating metadata for " + mlq.getCount() + " existing lists.");
    
    try (OIterator<? extends MasterList> it = mlq.getIterator())
    {
      while (it.hasNext())
      {
        MasterList list = it.next();
        BusinessDAO listDAO = (BusinessDAO) BusinessFacade.getEntityDAO(list);
        
        list.getDescriptionLocal().setDefaultValue(listDAO.getValue(LISTABSTRACT));
        list.getProcessLocal().setDefaultValue(listDAO.getValue(PROCESS));
        list.getProgressLocal().setDefaultValue(listDAO.getValue(PROGRESS));
        list.getAccessConstraintsLocal().setDefaultValue(listDAO.getValue(ACCESSCONSTRAINTS));
        list.getUseConstraintsLocal().setDefaultValue(listDAO.getValue(USECONSTRAINTS));
        list.getAcknowledgementsLocal().setDefaultValue(listDAO.getValue(ACKNOWLEDGEMENTS));
        list.getDisclaimerLocal().setDefaultValue(listDAO.getValue(DISCLAIMER));
        
        list.apply();
      }
    }
  }
  
  private void defineAttributes()
  {
    for (int i = 0; i < newAttrs.length; ++i)
    {
      String newAttrName = newAttrs[i];
      MdAttributeConcreteDAO oldMdAttr = (MdAttributeConcreteDAO) MdAttributeConcreteDAO.getByKey(MasterList.CLASS + "." + oldAttrs[i]);
      
      MdAttributeLocalText mdAttr = new MdAttributeLocalText();
      mdAttr.setDefiningMdClass(masterlistMd);
      mdAttr.setAttributeName(newAttrName);
      mdAttr.getDisplayLabel().setLocaleMap(oldMdAttr.getDisplayLabels());
      mdAttr.apply();
    }
  }
  
  private void deleteAttributes()
  {
    for (String name : oldAttrs)
    {
      MdAttributeConcreteDAO attr = (MdAttributeConcreteDAO) MdAttributeConcreteDAO.getByKey(MasterList.CLASS + "." + name);
      attr.delete();
    }
  }
}
