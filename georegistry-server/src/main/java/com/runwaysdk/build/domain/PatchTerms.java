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

import java.util.List;

import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.ontology.ClassifierQuery;
import net.geoprism.registry.RegistryConstants;

public class PatchTerms
{
  public static void main(String[] args)
  {
    new PatchTerms().doIt();
  }

  @Transaction
  private void doIt()
  {
    Classifier rootClassTerm = Classifier.getByKey(RegistryConstants.TERM_CLASS);

    ClassifierQuery gQuery = new ClassifierQuery(new QueryFactory());

    try (OIterator<? extends Classifier> it = gQuery.getIterator())
    {
      while (it.hasNext())
      {
        Classifier classifier = it.next();

        try (OIterator<Term> pit = classifier.getDirectAncestors(ClassifierIsARelationship.CLASS))
        {
          List<Term> parents = pit.getAll();

          if (parents.size() > 0)
          {
            Classifier parent = (Classifier) parents.get(0);

            if (!parent.getOid().equals(rootClassTerm.getOid()) && !classifier.getOid().equals(rootClassTerm.getOid()))
            {
              classifier.appLock();
              classifier.setClassifierPackage(parent.getKey());
              classifier.setKeyName(Classifier.buildKey(parent.getKey(), classifier.getClassifierId()));
              classifier.apply();
            }
          }
        }

      }
    }

  }
}
