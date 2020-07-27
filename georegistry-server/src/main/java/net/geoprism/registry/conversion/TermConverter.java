/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdEntityDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdEntityDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdAttributeMultiTerm;
import com.runwaysdk.system.metadata.MdAttributeTerm;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.geoobjecttype.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;

/**
 * Responsible for building {@link Term} objects from Runway {@link Classifier}
 * objects.
 * 
 * @author nathan
 *
 */
public class TermConverter
{
  private String rootClassifierKey;

  public TermConverter(String rootClassifierKey)
  {
    this.rootClassifierKey = rootClassifierKey;
  }

  @Request
  public Term build()
  {
    Classifier rootClassifier = Classifier.getByKey(this.rootClassifierKey);

    return this.buildTermFromClassifier(rootClassifier);

  }

  private Term buildTermFromClassifier(Classifier classifier)
  {
    LocalizedValue label = AttributeTypeConverter.convert(classifier.getDisplayLabel());

    Term term = new Term(classifier.getClassifierId(), label, new LocalizedValue(""));

    OIterator<? extends net.geoprism.ontology.Classifier> childClassifiers = classifier.getAllIsAChild();

    childClassifiers.forEach(c -> term.addChild(this.buildTermFromClassifier(c)));

    return term;
  }

  @Transaction
  public static Classifier createClassifierFromTerm(String parentTermCode, Term term)
  {
    String parentClassifierKey = buildClassifierKeyFromTermCode(parentTermCode);

    Classifier parent = Classifier.getByKey(parentClassifierKey);

    enforceTermPermissions(parent, Operation.CREATE);

    Classifier classifier = new Classifier();
    classifier.setClassifierId(term.getCode());
    classifier.setClassifierPackage(parent.getKey());
    // This will set the value of the display label to the locale of the user
    // performing the action.
    LocalizedValueConverter.populate(classifier.getDisplayLabel(), term.getLabel());

    classifier.apply();

    classifier.addLink(parent, ClassifierIsARelationship.CLASS);

    return classifier;
  }

  @Transaction
  public static Classifier updateClassifier(String parentTermCode, String termCode, LocalizedValue value)
  {
    String parentClassifierKey = buildClassifierKeyFromTermCode(parentTermCode);

    Classifier parent = Classifier.getByKey(parentClassifierKey);

    enforceTermPermissions(parent, Operation.WRITE);

    String classifierKey = Classifier.buildKey(parent.getKey(), termCode);

    Classifier classifier = Classifier.getByKey(classifierKey);
    classifier.lock();
    classifier.setClassifierId(termCode);

    LocalizedValueConverter.populate(classifier.getDisplayLabel(), value);

    classifier.apply();

    return classifier;
  }

  /**
   * Builds if not exists a {@link Classifier} object as a parent of terms that
   * pertain to the given {@link MdBusiness}.
   * 
   * @param mdBusiness
   *          {@link MdBusiness}
   * 
   * @return {@link Classifier} object as a parent of terms that pertain to the
   *         given {@link MdBusiness}.
   */
  public static Classifier buildIfNotExistdMdBusinessClassifier(MdBusiness mdBusiness)
  {
    String classTermKey = buildRootClassKey(mdBusiness.getTypeName());

    Classifier classTerm = null;

    try
    {
      classTerm = Classifier.getByKey(classTermKey);
    }
    catch (DataNotFoundException e)
    {

      String classifierId = buildRootClassClassifierId(mdBusiness.getTypeName());

      classTerm = new Classifier();
      classTerm.setClassifierId(classifierId);
      classTerm.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
      // This will set the value of the display label to the locale of the user
      // performing the action.
      classTerm.getDisplayLabel().setValue(mdBusiness.getDisplayLabel().getValue());
      classTerm.getDisplayLabel().setDefaultValue(mdBusiness.getDisplayLabel().getDefaultValue());
      classTerm.apply();

      Classifier rootClassTerm = Classifier.getByKey(RegistryConstants.TERM_CLASS);

      classTerm.addLink(rootClassTerm, ClassifierIsARelationship.CLASS);
    }

    return classTerm;
  }

  /**
   * Builds if not exists a {@link Classifier} object as a parent of terms of
   * the given {@link MdAttributeTerm} or a {@link MdAttributeMultiTerm}.
   * 
   * @param mdBusiness
   *          {@link MdBusiness}
   * @param mdAttributeTermOrMultiName
   *          the name of the {@link MdAttributeTerm} or a
   *          {@link MdAttributeMultiTerm}
   * @param parent
   * 
   * @return {@link Classifier} object as a parent of terms that pertain to the
   *         given {@link MdBusiness}.
   */
  public static Classifier buildIfNotExistAttribute(MdBusiness mdBusiness, String mdAttributeTermOrMultiName, Classifier parent)
  {
    String attributeTermKey = buildtAtttributeKey(mdBusiness.getTypeName(), mdAttributeTermOrMultiName);

    Classifier attributeTerm = null;

    try
    {
      attributeTerm = Classifier.getByKey(attributeTermKey);
    }
    catch (DataNotFoundException e)
    {
      String classifierId = buildtAtttributeClassifierId(mdBusiness.getTypeName(), mdAttributeTermOrMultiName);

      attributeTerm = new Classifier();
      attributeTerm.setClassifierId(classifierId);
      attributeTerm.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
      // This will set the value of the display label to the locale of the user
      // performing the action.
      attributeTerm.getDisplayLabel().setValue(mdBusiness.getDisplayLabel().getValue());
      attributeTerm.getDisplayLabel().setDefaultValue(mdBusiness.getDisplayLabel().getDefaultValue());
      attributeTerm.apply();

      if (parent != null)
      {
        attributeTerm.addLink(parent, ClassifierIsARelationship.CLASS);
      }
    }

    return attributeTerm;
  }

  /**
   * Returns the {@link Classifier} classifier ID of the root term specifying
   * the values for the given {@link MdBusiness}.
   * 
   * @param mdBusinessName
   * 
   * @return {@link Classifier} code/key of the root term specifying the values
   *         for the given {@link MdBusiness}.
   */
  public static String buildRootClassClassifierId(String mdBusinessName)
  {
    return RegistryConstants.TERM_CLASS + "_" + mdBusinessName;
  }

  /**
   * Returns the {@link Classifier} code/key of the root term specifying the
   * values for the given {@link MdBusiness}.
   * 
   * @param mdBusinessName
   * 
   * @return {@link Classifier} code/key of the root term specifying the values
   *         for the given {@link MdBusiness}.
   */
  public static String buildRootClassKey(String mdBusinessName)
  {
    return Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, buildRootClassClassifierId(mdBusinessName));
  }

  /**
   * Returns the {@link Classifier} classifier Id of the root term specifying
   * the values for an {@link AttributeTermType}.
   * 
   * @param mdBusiness
   * @param mdAttributeTermOrMultiName
   *          the name of the {@link MdAttributeTerm} or a
   *          {@link MdAttributeMultiTerm}
   * 
   * @return {@link Classifier} code/key of the root term specifying the values
   *         for an {@link AttributeTermType}.
   */
  public static String buildtAtttributeClassifierId(String mdBusinessName, String mdAttributeTermOrMultiName)
  {
    return buildRootClassClassifierId(mdBusinessName) + "_" + mdAttributeTermOrMultiName;
  }

  /**
   * Returns the {@link Classifier} code/key of the root term specifying the
   * values for an {@link AttributeTermType}.
   * 
   * @param mdBusinessName
   * @param mdAttributeTermOrMultiName
   *          the name of the {@link MdAttributeTerm} or a
   *          {@link MdAttributeMultiTerm}
   * 
   * @return {@link Classifier} code/key of the root term specifying the values
   *         for an {@link AttributeTermType}.
   */
  public static String buildtAtttributeKey(String mdBusinessName, String mdAttributeTermOrMultiName)
  {
    return Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, buildRootClassClassifierId(mdBusinessName) + "_" + mdAttributeTermOrMultiName);
  }

  /**
   * Converts the given code from a {@link Term} to a {@link Classifier} key
   * 
   * @param termCode
   * @return
   */
  public static String buildClassifierKeyFromTermCode(String termCode)
  {
    return Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, termCode);
  }

  public static void enforceTermPermissions(Classifier parent, Operation op)
  {
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      GeoObjectTypePermissionServiceIF service = ServiceFactory.getGeoObjectTypePermissionService();
      SingleActorDAOIF user = Session.getCurrentSession().getUser();

      // Is this a root term for an {@link MdAttributeTerm}
      try (OIterator<? extends MdAttributeTerm> attrTerm = parent.getAllClassifierTermAttributeRoots())
      {
        for (MdAttributeTerm mdAttributeTerm : attrTerm)
        {
          MdEntityDAOIF mdEntityDAOIF = MdEntityDAO.get(mdAttributeTerm.getDefiningMdClassId());
          ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(mdEntityDAOIF.getTypeName());
          Organization organization = geoObjectType.getOrganization();

          service.enforceActorHasPermission(user, organization.getCode(), geoObjectType.getLabel().getValue(), op);
        }
      }
    }
  }
}
