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

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.ontology.Classifier;

/**
 * Responsible for building {@link Term} objects from Runway {@link Classifier}
 * objects.
 * 
 * @author nathan
 *
 */
public class VertexTermConverter
{
  private VertexObject classification;

  public VertexTermConverter(VertexObject classification)
  {
    this.classification = classification;
  }

  @Request
  public Term build()
  {
    return this.buildTermFromClassifier(this.classification);

  }

  private Term buildTermFromClassifier(VertexObject classification)
  {
    LocalizedValue label = AttributeTypeConverter.convert(classification.getEmbeddedComponent(AbstractClassification.DISPLAYLABEL));

    Term term = new Term(classification.getObjectValue(AbstractClassification.CODE), label, new LocalizedValue(""));

    return term;
  }

  // @Transaction
  // public static VertexObject classification createClassifierFromTerm(String
  // parentTermCode, Term term)
  // {
  // String parentClassifierKey =
  // buildClassifierKeyFromTermCode(parentTermCode);
  //
  // Classifier parent = Classifier.getByKey(parentClassifierKey);
  //
  // enforceTermPermissions(parent, CGRPermissionAction.CREATE);
  //
  // Classifier classifier = new Classifier();
  // classifier.setClassifierId(term.getCode());
  // classifier.setClassifierPackage(parent.getKey());
  // // This will set the value of the display label to the locale of the user
  // // performing the action.
  // LocalizedValueConverter.populate(classifier.getDisplayLabel(),
  // term.getLabel());
  //
  // classifier.apply();
  //
  // classifier.addLink(parent, ClassifierIsARelationship.CLASS);
  //
  // return classifier;
  // }
  //
  // @Transaction
  // public static Classifier updateClassifier(String parentTermCode, String
  // termCode, LocalizedValue value)
  // {
  // String parentClassifierKey =
  // buildClassifierKeyFromTermCode(parentTermCode);
  //
  // Classifier parent = Classifier.getByKey(parentClassifierKey);
  //
  // enforceTermPermissions(parent, CGRPermissionAction.WRITE);
  //
  // String classifierKey = Classifier.buildKey(parent.getKey(), termCode);
  //
  // Classifier classifier = Classifier.getByKey(classifierKey);
  // classifier.lock();
  // classifier.setClassifierId(termCode);
  //
  // LocalizedValueConverter.populate(classifier.getDisplayLabel(), value);
  //
  // classifier.apply();
  //
  // return classifier;
  // }
  //
  // /**
  // * Builds if not exists a {@link Classifier} object as a parent of terms
  // that
  // * pertain to the given {@link MdBusiness}.
  // *
  // * @param mdBusiness
  // * {@link MdBusiness}
  // *
  // * @return {@link Classifier} object as a parent of terms that pertain to
  // the
  // * given {@link MdBusiness}.
  // */
  // public static Classifier buildIfNotExistdMdBusinessClassifier(MdBusiness
  // mdBusiness)
  // {
  // String classTermKey = buildRootClassKey(mdBusiness.getTypeName());
  //
  // Classifier classTerm = null;
  //
  // try
  // {
  // classTerm = Classifier.getByKey(classTermKey);
  // }
  // catch (DataNotFoundException e)
  // {
  //
  // String classifierId = buildRootClassClassifierId(mdBusiness.getTypeName());
  //
  // classTerm = new Classifier();
  // classTerm.setClassifierId(classifierId);
  // classTerm.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
  // // This will set the value of the display label to the locale of the user
  // // performing the action.
  // classTerm.getDisplayLabel().setValue(mdBusiness.getDisplayLabel().getValue());
  // classTerm.getDisplayLabel().setDefaultValue(mdBusiness.getDisplayLabel().getDefaultValue());
  // classTerm.apply();
  //
  // Classifier rootClassTerm =
  // Classifier.getByKey(RegistryConstants.TERM_CLASS);
  //
  // classTerm.addLink(rootClassTerm, ClassifierIsARelationship.CLASS);
  // }
  //
  // return classTerm;
  // }
  //
  // /**
  // * Builds if not exists a {@link Classifier} object as a parent of terms of
  // * the given {@link MdAttributeTerm} or a {@link MdAttributeMultiTerm}.
  // *
  // * @param mdBusiness
  // * {@link MdBusiness}
  // * @param mdAttributeTermOrMultiName
  // * the name of the {@link MdAttributeTerm} or a
  // * {@link MdAttributeMultiTerm}
  // * @param parent
  // *
  // * @return {@link Classifier} object as a parent of terms that pertain to
  // the
  // * given {@link MdBusiness}.
  // */
  // public static Classifier buildIfNotExistAttribute(MdBusiness mdBusiness,
  // String mdAttributeTermOrMultiName, Classifier parent)
  // {
  // String attributeTermKey = buildtAtttributeKey(mdBusiness.getTypeName(),
  // mdAttributeTermOrMultiName);
  //
  // Classifier attributeTerm = null;
  //
  // try
  // {
  // attributeTerm = Classifier.getByKey(attributeTermKey);
  // }
  // catch (DataNotFoundException e)
  // {
  // String classifierId =
  // buildtAtttributeClassifierId(mdBusiness.getTypeName(),
  // mdAttributeTermOrMultiName);
  //
  // attributeTerm = new Classifier();
  // attributeTerm.setClassifierId(classifierId);
  // attributeTerm.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
  // // This will set the value of the display label to the locale of the user
  // // performing the action.
  // attributeTerm.getDisplayLabel().setValue(mdBusiness.getDisplayLabel().getValue());
  // attributeTerm.getDisplayLabel().setDefaultValue(mdBusiness.getDisplayLabel().getDefaultValue());
  // attributeTerm.apply();
  //
  // if (parent != null)
  // {
  // attributeTerm.addLink(parent, ClassifierIsARelationship.CLASS);
  // }
  // }
  //
  // return attributeTerm;
  // }
  //
  // /**
  // * Returns the {@link Classifier} classifier ID of the root term specifying
  // * the values for the given {@link MdBusiness}.
  // *
  // * @param mdBusinessName
  // *
  // * @return {@link Classifier} code/key of the root term specifying the
  // values
  // * for the given {@link MdBusiness}.
  // */
  // public static String buildRootClassClassifierId(String mdBusinessName)
  // {
  // return RegistryConstants.TERM_CLASS + "_" + mdBusinessName;
  // }
  //
  // /**
  // * Returns the {@link Classifier} code/key of the root term specifying the
  // * values for the given {@link MdBusiness}.
  // *
  // * @param mdBusinessName
  // *
  // * @return {@link Classifier} code/key of the root term specifying the
  // values
  // * for the given {@link MdBusiness}.
  // */
  // public static String buildRootClassKey(String mdBusinessName)
  // {
  // return Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE,
  // buildRootClassClassifierId(mdBusinessName));
  // }
  //
  // /**
  // * Returns the {@link Classifier} classifier Id of the root term specifying
  // * the values for an {@link AttributeTermType}.
  // *
  // * @param mdBusiness
  // * @param mdAttributeTermOrMultiName
  // * the name of the {@link MdAttributeTerm} or a
  // * {@link MdAttributeMultiTerm}
  // *
  // * @return {@link Classifier} code/key of the root term specifying the
  // values
  // * for an {@link AttributeTermType}.
  // */
  // public static String buildtAtttributeClassifierId(String mdBusinessName,
  // String mdAttributeTermOrMultiName)
  // {
  // return buildRootClassClassifierId(mdBusinessName) + "_" +
  // mdAttributeTermOrMultiName;
  // }
  //
  // /**
  // * Returns the {@link Classifier} code/key of the root term specifying the
  // * values for an {@link AttributeTermType}.
  // *
  // * @param mdBusinessName
  // * @param mdAttributeTermOrMultiName
  // * the name of the {@link MdAttributeTerm} or a
  // * {@link MdAttributeMultiTerm}
  // *
  // * @return {@link Classifier} code/key of the root term specifying the
  // values
  // * for an {@link AttributeTermType}.
  // */
  // public static String buildtAtttributeKey(String mdBusinessName, String
  // mdAttributeTermOrMultiName)
  // {
  // return Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE,
  // buildRootClassClassifierId(mdBusinessName) + "_" +
  // mdAttributeTermOrMultiName);
  // }
  //
  // /**
  // * Converts the given code from a {@link Term} to a {@link Classifier} key
  // *
  // * @param termCode
  // * @return
  // */
  // public static String buildClassifierKeyFromTermCode(String termCode)
  // {
  // return Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, termCode);
  // }
  //
  // public static void enforceTermPermissions(Classifier parent,
  // CGRPermissionActionIF action)
  // {
  // GeoObjectTypePermissionServiceIF service =
  // ServiceFactory.getGeoObjectTypePermissionService();
  //
  // // Is this a root term for an {@link MdAttributeTerm}
  // try (OIterator<? extends MdAttributeTerm> attrTerm =
  // parent.getAllClassifierTermAttributeRoots())
  // {
  // for (MdAttributeTerm mdAttributeTerm : attrTerm)
  // {
  // MdEntityDAOIF mdEntityDAOIF =
  // MdEntityDAO.get(mdAttributeTerm.getDefiningMdClassId());
  // ServerGeoObjectType geoObjectType =
  // ServerGeoObjectType.get(mdEntityDAOIF.getTypeName());
  // Organization organization = geoObjectType.getOrganization();
  //
  // service.enforceActorHasPermission(organization.getCode(), geoObjectType,
  // geoObjectType.getIsPrivate(), action);
  // }
  // }
  // }
}
