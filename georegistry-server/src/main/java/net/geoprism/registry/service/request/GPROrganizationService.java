package net.geoprism.registry.service.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.GPROrganizationBusinessService;

@Service
@Primary
public class GPROrganizationService extends OrganizationService implements OrganizationServiceIF
{
  @Autowired
  private CacheProviderIF                provider;

  @Autowired
  private GPROrganizationBusinessService service;

  /**
   * Updates the given {@link OrganizationDTO} represented as JSON.
   * 
   * @pre given {@link OrganizationDTO} must already exist.
   * 
   * @param sessionId
   * @param json
   *          JSON of the {@link OrganizationDTO} to be updated.
   * @return updated {@link OrganizationDTO}
   */
  @Override
  public OrganizationDTO updateOrganization(String sessionId, String json)
  {
    OrganizationDTO dto = super.updateOrganization(sessionId, json);

    SerializedListTypeCache.getInstance().clear();

    return dto;
  }

  /**
   * Deletes the {@link OrganizationDTO} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link OrganizationDTO} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteOrganization(String sessionId, String code)
  {
    super.deleteOrganization(sessionId, code);

    SerializedListTypeCache.getInstance().clear();
  }

  @Request(RequestType.SESSION)
  public void importFile(String sessionId, MultipartFile file) throws IOException
  {
    try (InputStream istream = file.getInputStream())
    {
      try (InputStreamReader reader = new InputStreamReader(istream))
      {
        JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

        this.service.importJsonTree(array);
      }
    }

    provider.getServerCache().refresh();
  }

}
