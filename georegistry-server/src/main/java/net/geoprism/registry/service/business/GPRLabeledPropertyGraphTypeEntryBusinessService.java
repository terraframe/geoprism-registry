package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.graph.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Service
@Primary
public class GPRLabeledPropertyGraphTypeEntryBusinessService extends LabeledPropertyGraphTypeEntryBusinessService implements LabeledPropertyGraphTypeEntryBusinessServiceIF
{

  @Override
  public LabeledPropertyGraphTypeVersion publish(LabeledPropertyGraphTypeEntry entry)
  {
    LabeledPropertyGraphTypeVersion version = super.publish(entry);

    this.createPublishJob(version);

    return version;
  }

  public void createPublishJob(LabeledPropertyGraphTypeVersion version)
  {
    QueryFactory factory = new QueryFactory();

    PublishLabeledPropertyGraphTypeVersionJobQuery query = new PublishLabeledPropertyGraphTypeVersionJobQuery(factory);
    query.WHERE(query.getVersion().EQ(version));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));
    q.AND(q.job(query));

    if (q.getCount() > 0)
    {
      throw new DuplicateJobException("This version has already been queued for publishing");
    }

    SingleActorDAOIF currentUser = Session.getCurrentSession().getUser();

    PublishLabeledPropertyGraphTypeVersionJob job = new PublishLabeledPropertyGraphTypeVersionJob();
    job.setRunAsUserId(currentUser.getOid());
    job.setVersion(version);
    job.setGraphType(version.getGraphType());
    job.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    job.start();
  }

}
