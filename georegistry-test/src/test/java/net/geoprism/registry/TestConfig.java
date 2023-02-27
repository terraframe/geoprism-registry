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
package net.geoprism.registry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.geoprism.registry.test.MockHttpServletRequest;
import net.geoprism.registry.test.MockHttpServletResponse;

@Configuration
@ComponentScan(basePackages = { "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.spring", "net.geoprism.registry.test" })
public class TestConfig
{

  
  @Bean
  HttpServletRequest request(){
    return new MockHttpServletRequest();
  }
  
  @Bean
  HttpServletResponse response(){
    return new MockHttpServletResponse();
  }
}
