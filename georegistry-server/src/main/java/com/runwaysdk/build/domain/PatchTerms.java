/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package com.runwaysdk.build.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.ontology.ClassifierQuery;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.RegistryConstants;

public class PatchTerms
{
  private Logger logger = LoggerFactory.getLogger(PatchTerms.class);

  public static void main(String[] args)
  {
    new PatchTerms().doIt();
  }

  @Transaction
  private void doIt()
  {
    ClassifierQuery gQuery = new ClassifierQuery(new QueryFactory());

    Classifier root = Classifier.getByKey("net.geoprism.registry.ROOT");

    try (OIterator<? extends Classifier> it = gQuery.getIterator())
    {
      while (it.hasNext())
      {
        Classifier classifier = it.next();

        LinkedList<Term> parents = new LinkedList<>(GeoEntityUtil.getOrderedAncestors(root, classifier, ClassifierIsARelationship.CLASS));
        Collections.reverse(parents);

//        logger.error("[" + classifier.getClassifierId() + "]: " + parents.size());
//        System.out.println("[" + classifier.getClassifierId() + "]: " + parents.size());

        // Option attributes should have 2 parents
        // Root -> Class Root -> Class -> Attribute -> Option
        if (parents.size() == 4)
        {
          Iterator<Term> pit = parents.iterator();

//          while (pit.hasNext())
//          {
//            Classifier p = (Classifier) pit.next();
//            
////            logger.error("[" + p.getClassifierId() + "]: ");
//            System.out.println(" Parent [" + p.getClassifierId() + "]: ");
//          }

          pit = parents.iterator();
          pit.next();

          Classifier parent = (Classifier) pit.next();

          classifier.appLock();
          classifier.setClassifierPackage(parent.getKey());
          classifier.setKeyName(Classifier.buildKey(parent.getKey(), classifier.getClassifierId()));
          classifier.apply();
        }
        else
        {
          classifier.appLock();
          classifier.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
          classifier.setKeyName(Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, classifier.getClassifierId()));
          classifier.apply();
        }

      }
    }

  }
}
