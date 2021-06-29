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
package net.geoprism.registry.etl.export.dhis2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.Option;
import net.geoprism.dhis2.dhis2adapter.response.model.OptionSet;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.UnexpectedRemoteResponse;

public class DHIS2OptionCache
{
  public static class IntegratedOptionSet
  {
    private OptionSet optionSet;
    
    private SortedSet<Option> options;
    
    public IntegratedOptionSet(OptionSet optionSet)
    {
      this.optionSet = optionSet;
      
      this.options = new TreeSet<Option>();
    }

    public OptionSet getOptionSet()
    {
      return optionSet;
    }

    public void setOptionSet(OptionSet optionSet)
    {
      this.optionSet = optionSet;
    }

    public SortedSet<Option> getOptions()
    {
      return options;
    }

    public void setOptions(SortedSet<Option> options)
    {
      this.options = options;
    }
    
    public void addOption(Option opt)
    {
      this.options.add(opt);
    }
  }
  
  private Map<String, IntegratedOptionSet> optionSets;
  
  private DHIS2TransportServiceIF dhis2;
  
  public DHIS2OptionCache(DHIS2TransportServiceIF dhis2)
  {
    this.dhis2 = dhis2;
    
    init();
  }
  
  private void init()
  {
    try
    {
      MetadataGetResponse<OptionSet> resp = dhis2.<OptionSet>metadataGet(OptionSet.class);
      
      if (!resp.isSuccess())
      {
//        if (resp.hasMessage())
//        {
//          ExportRemoteException ere = new ExportRemoteException();
//          ere.setRemoteError(resp.getMessage());
//          throw ere;
//        }
//        else
//        {
          UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
          throw re;
//        }
      }

      List<OptionSet> objects = resp.getObjects();
      
      this.optionSets = new HashMap<String, IntegratedOptionSet>(objects.size());
      
      for (OptionSet os : objects)
      {
        this.optionSets.put(os.getId(), new IntegratedOptionSet(os));
      }
      
      if (this.optionSets.size() > 0)
      {
        MetadataGetResponse<Option> resp2 = dhis2.<Option>metadataGet(Option.class);
        
        if (!resp2.isSuccess())
        {
//          if (resp.hasMessage())
//          {
//            ExportRemoteException ere = new ExportRemoteException();
//            ere.setRemoteError(resp.getMessage());
//            throw ere;
//          }
//          else
//          {
            UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
            throw re;
//          }
        }
        
        List<Option> options = resp2.getObjects();
        
        for (Option option : options)
        {
          if (option.getOptionSetId() != null && this.optionSets.containsKey(option.getOptionSetId()))
          {
            this.optionSets.get(option.getOptionSetId()).addOption(option);
          }
        }
      }
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }
    catch (BadServerUriException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }
  }

  public Map<String, IntegratedOptionSet> getOptionSets()
  {
    return optionSets;
  }

  public void setOptionSets(Map<String, IntegratedOptionSet> optionSets)
  {
    this.optionSets = optionSets;
  }
  
  public IntegratedOptionSet getOptionSet(String optionSetId)
  {
    return this.optionSets.get(optionSetId);
  }
}
