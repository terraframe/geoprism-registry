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
package net.geoprism.registry.service.request;

import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.CGRApplication;

@Component
public interface RegistryServiceIF
{
  String oauthGetAll(String sessionId, String id);

  String oauthGetPublic(String sessionId, String id);

  JsonObject initHierarchyManager(String sessionId, Boolean publicOnly);

  List<CGRApplication> getApplications(String sessionId);

  JsonObject configuration(String sessionId, String contextPath);

  String getLocalizationMap(String sessionId);

  List<GeoObject> search(String sessionId, String typeCode, String text, Date date);

  CustomSerializer serializer(String sessionId);

  String getCurrentLocale(String sessionId);

  JsonArray getLocales(String sessionId);

  JsonObject serialize(String sessionId, GeoObjectType got);

  JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode, Date date);

  String[] getUIDS(String sessionId, Integer amount);

  JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String parentTypeCode, String hierarchyCode, Date startDate, Date endDate);

}
