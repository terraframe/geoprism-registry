package net.geoprism.registry.service.business;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;

import net.geoprism.registry.GeoRegistryUtil;

@Service
public class SchedulerService
{
  @Autowired
  private ETLBusinessService etlService;

  // Runs every day at 2:30 AM
  @Scheduled(cron = "0 30 2 * * ?")
  @Async
  public void runCronTask()
  {
    this.cleanupJobTiles();
  }

  @Request
  public void cleanupJobTiles()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DAY_OF_YEAR, -14);

    this.etlService.deleteJobHistoryTiles(cal.getTime());
  }
}
