package net.geoprism.registry.etl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Date;

import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.shapefile.GeoObjectShapefileImporter;
import net.geoprism.registry.shapefile.NullLogger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

public class GeoObjectShapefileImportJob extends GeoObjectShapefileImportJobBase
{
  private static final long serialVersionUID = 1742592504;
  
  private Logger logger = LoggerFactory.getLogger(GeoObjectShapefileImportJob.class);
  
  public GeoObjectShapefileImportJob()
  {
    super();
  }
  
  @Override
  public synchronized JobHistory start()
  {
    throw new UnsupportedOperationException();
  }
  
  public synchronized ImportHistory start(GeoObjectImportConfiguration configuration)
  {
    return executableJobStart(configuration);
  }
  
  private ImportHistory executableJobStart(GeoObjectImportConfiguration configuration)
  {
    ImportHistory history = (ImportHistory) this.createNewHistory();
    
    history.appLock();
    history.setConfigJson(configuration.toJson().toString());
    history.apply();
    
    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();

    this.getQuartzJob().start(record);

    return history;
  }

  @Override
  public void execute(ExecutionContext executionContext) throws MalformedURLException, InvocationTargetException
  {
    ImportHistory history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();
    ImportStage stage = history.getStage().get(0);
    GeoObjectImportConfiguration config = GeoObjectImportConfiguration.parse(history.getConfigJson(), false);
    
    File shpFile = config.getShpVaultFile();
    
    process(executionContext, history, stage, config, shpFile);
  }

  private void process(ExecutionContext executionContext, ImportHistory history, ImportStage stage, GeoObjectImportConfiguration config, File shpFile) throws MalformedURLException, InvocationTargetException
  {
    logger.info("Shapefile Importer entering import stage [" + stage.getEnumName() + "] with file [" + shpFile.getName() + "].");
    
    // TODO : We should have a single transaction where we do all the history configuration upfront, that way the job is either fully configured (and resumable) or it isn't (no in-between)
    config.setHistoryId(history.getOid());
    
    if (stage.equals(ImportStage.SYNONYM_CHECK))
    {
      history.appLock();
      history.setWorkProgress(0);
      history.setImportedRecords(0);
      history.setConfigJson(config.toJson().toString());
      history.apply();
      
      GeoObjectShapefileImporter importer = new GeoObjectShapefileImporter(shpFile, config);
      importer.setImportHistory(history);
      importer.run(stage, new NullLogger());
      
      if (config.hasProblems())
      {
        executionContext.setStatus(AllJobStatus.FEEDBACK);
        
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.SYNONYM_RESOLVE);
        history.setConfigJson(config.toJson().toString()); // TODO : Do we intend to be saving the import problems here?
        history.apply();
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT);
        history.setConfigJson(config.toJson().toString());
        history.apply();
        
        this.process(executionContext, history, ImportStage.IMPORT, config, shpFile);
      }
    }
    else if (stage.equals(ImportStage.IMPORT))
    {
      history.appLock();
      history.setWorkProgress(0);
      history.setImportedRecords(0);
      history.setConfigJson(config.toJson().toString());
      history.apply();
      
      GeoObjectShapefileImporter importer = new GeoObjectShapefileImporter(shpFile, config);
      importer.setImportHistory(history);
      importer.run(stage, new NullLogger());
      
      if (config.hasProblems())
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT_RESOLVE);
        history.setConfigJson(config.toJson().toString());
        history.apply();
        
        executionContext.setStatus(AllJobStatus.FEEDBACK);
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.COMPLETE);
        history.setConfigJson(config.toJson().toString());
        history.apply();
      }
    }
    else if (stage.equals(ImportStage.RESUME_IMPORT))
    {
      history.appLock();
      history.setConfigJson(config.toJson().toString());
      history.apply();
      
      GeoObjectShapefileImporter importer = new GeoObjectShapefileImporter(shpFile, config);
      importer.setImportHistory(history);
      importer.run(stage, new NullLogger());
      
      if (config.hasProblems())
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT_RESOLVE);
        history.setConfigJson(config.toJson().toString());
        history.apply();
        
        executionContext.setStatus(AllJobStatus.FEEDBACK);
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.COMPLETE);
        history.setConfigJson(config.toJson().toString());
        history.apply();
      }
    }
    else
    {
      String msg = "Invalid import stage [" + stage.getEnumName() + "].";
      logger.error(msg);
      throw new ProgrammingErrorException(msg);
    }
  }
  
  @Request
  public void afterJobExecute(JobHistory history)
  {
    AllJobStatus finalStatus = history.getStatus().get(0);
    
    if (finalStatus.equals(AllJobStatus.SUCCESS) || finalStatus.equals(AllJobStatus.FAILURE))
    {
      this.delete();
      
      GeoObjectImportConfiguration config = GeoObjectImportConfiguration.parse(((ImportHistory) history).getConfigJson(), false);
      
      File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), config.getDirectory());
      try
      {
        FileUtils.deleteDirectory(root);
      }
      catch (IOException e)
      {
        logger.error("Error deleting shapefile directory in vault. Directory still exists [" + root.getAbsolutePath() + "].");
      }
    }
  }
  
  @Override
  public synchronized void resume(JobHistoryRecord jhr)
  {
    ImportHistory hist = (ImportHistory) jhr.getChild();
    hist.appLock();
    hist.clearStage();
    hist.addStage(ImportStage.RESUME_IMPORT);
    hist.apply();
    
    super.resume(jhr);
  }
  
  @Override
  protected JobHistory createNewHistory()
  {
    ImportHistory history = new ImportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ImportStage.SYNONYM_CHECK); 
    history.apply();
    
    return history;
  }
  
  public boolean canResume()
  {
    return true;
  }
  
  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }
}
