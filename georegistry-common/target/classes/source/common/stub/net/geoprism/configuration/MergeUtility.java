/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
/**
 * This class performs merge operations between files, directories or input streams. This is designed for use in the build system for custom resource merging.
 * 
 * @author rrowlands
 */
public class MergeUtility
{
  public static void main(String[] args) throws ParseException, IOException
  {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options();
    options.addOption(Option.builder("b").hasArg().argName("base").longOpt("base").desc("The path to the base properties file or directory.").build());
    options.addOption(Option.builder("o").hasArg().argName("override").longOpt("override").desc("The path to the override properties file or directory.").build());
    options.addOption(Option.builder("e").hasArg().argName("export").longOpt("export").desc("The path to the export file or directory. A null value defaults to base.").build());
    CommandLine line = parser.parse( options, args );
    
    String sBase = line.getOptionValue("b");
    String sOverride = line.getOptionValue("o");
    String sExport = line.getOptionValue("e");
    
    if (sExport == null)
    {
      sExport = sBase;
    }
    
    File fBase = new File(sBase);
    File fOverride = new File(sOverride);
    File fExport = new File(sExport);
    if (!fBase.exists() || !fOverride.exists())
    {
      throw new RuntimeException("The base [" + sBase + "] and the override [" + sOverride + "] paths must both exist.");
    }
    
    MergeUtility merger = new MergeUtility();
    merger.mergeFiles(fBase, fOverride, fExport);
  }
  
  public MergeUtility()
  {
    
  }
  
  public void mergeDirectories(File base, File override, File export) throws IOException
  {
    if (!export.exists())
    {
      export.mkdirs();
    }
    
    if (!export.equals(override))
    {
      for (File overrideChild : override.listFiles())
      {
        mergeFiles(new File(base, overrideChild.getName()), overrideChild, new File(export, overrideChild.getName()));
      }
    }
    if (!export.equals(base))
    {
      for (File baseChild : base.listFiles())
      {
        mergeFiles(baseChild, new File(override, baseChild.getName()), new File(export, baseChild.getName()));
      }
    }
  }
  
  public void mergeFiles(File base, File override, File export) throws IOException
  {
    if (base == null || !base.exists())
    {
      if (!override.equals(export))
      {
        if (override.isDirectory())
        {
          mergeDirectories(base, override, export);
        }
        else
        {
          FileUtils.copyFile(override, export);
        }
      }
    }
    else if (override == null || !override.exists())
    {
      if (!base.equals(export))
      {
        if (base.isDirectory())
        {
          mergeDirectories(base, override, export);
        }
        else
        {
          FileUtils.copyFile(base, export);
        }
      }
    }
    else
    {
      if (override.isDirectory())
      {
        mergeDirectories(base, override, export);
      }
      else if (override.getName().endsWith(".properties"))
      {
        if (!export.exists())
        {
          export.createNewFile();
        }
        
        if (!base.exists())
        {
          if (override != export)
          {
            FileUtils.copyFile(override, export);
          }
        }
        else
        {
          Properties baseProps = new Properties();
          baseProps.load(new FileInputStream(base));
          
          Properties overrideProps = new Properties();
          overrideProps.load(new FileInputStream(override));
          
          // When we open a FileOutputStream it wipes the file. Its absolutely required that we've read base and override into memory by now otherwise it screws up the override.
          mergeProperties(baseProps, overrideProps, new FileOutputStream(export));
        }
      }
      else if (!override.equals(export))
      {
        FileUtils.copyFile(override, export);
      }
    }
  }
  
  public void mergeStreams(InputStream base, InputStream override, OutputStream export, String extension) throws IOException
  {
    if (extension.equals("properties"))
    {
      try
      {
        Properties baseProps = new Properties();
        baseProps.load(base);
        
        Properties overrideProps = new Properties();
        overrideProps.load(override);
        
        mergeProperties(baseProps, overrideProps, export);
      }
      finally
      {
        base.close();
        override.close();
      }
    }
    else
    {
      IOUtils.copy(override, export);
    }
  }
  
  public void mergeProperties(Properties base, Properties override, OutputStream export) throws IOException
  {
    try
    {
      Iterator<Object> i = override.keySet().iterator();
      while (i.hasNext())
      {
        String key = (String) i.next();
        
        String value = override.getProperty(key);
        if (value.equals("$REMOVE$"))
        {
          base.remove(key);
        }
        else
        {
          base.setProperty(key, value);
        }
      }
      
      base.store(export, null);
    }
    finally
    {
      export.close();
    }
  }
}
