/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.Commit;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.OrganizationBusinessService;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.service.business.RemoteClientBuilderServiceIF;
import net.geoprism.registry.service.business.RemoteClientIF;
import net.geoprism.registry.service.business.RemoteCommitService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RemoteCommitServiceTest implements InstanceTestClassListener
{
  @Autowired
  private RemoteClientBuilderServiceIF           builder;

  @Autowired
  private PublishBusinessServiceIF               pService;

  @Autowired
  private CommitBusinessServiceIF                commitService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF gSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hSnapshotService;

  @Autowired
  private PublishBusinessServiceIF               publishService;

  @Autowired
  private OrganizationBusinessServiceIF          organizationService;

  @Autowired
  private RemoteCommitService                    service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    ServerOrganization org = ServerOrganization.getByCode("MOHA", false);

    if (org == null)
    {
      this.organizationService.create(new OrganizationDTO("MOHA", new LocalizedValue("MOHA"), new LocalizedValue("MOHA")));
    }

    // Delete the existing commit
    try (RemoteClientIF client = builder.open(""))
    {
      client.getPublish("").ifPresent(dto -> {
        this.publishService.getByUid(dto.getUid()).ifPresent(publish -> {
          // Ensure that the commit has not already been pulled
          this.commitService.getCommit(publish, 1).ifPresent(commit -> {
            this.commitService.delete(commit);
          });
        });
      });
    }
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Before
  @Request
  public void after()
  {
  }

  @Test
  @Request
  public void testPull() throws InterruptedException
  {
    Commit commit = this.service.pull("test", "mock", 1);

    Assert.assertNotNull(commit);
  }
}
