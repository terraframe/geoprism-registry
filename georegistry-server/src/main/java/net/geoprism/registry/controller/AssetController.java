package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.runwaysdk.Pair;

import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.service.AssetService;

@RestController
@Validated
public class AssetController extends RunwaySpringController
{
  public static final class AssetBody
  {
    private String        oid;

    @NotNull(message = "file requires a value")
    private MultipartFile file;

    public String getOid()
    {
      return oid;
    }

    public void setOid(String oid)
    {
      this.oid = oid;
    }

    public MultipartFile getFile()
    {
      return file;
    }

    public void setFile(MultipartFile file)
    {
      this.file = file;
    }
  }

  @Autowired
  private AssetService service;

  @PostMapping("asset/apply")
  public ResponseEntity<Void> apply(@Valid
  @ModelAttribute AssetBody body) throws IOException
  {
    if (body.getOid() != null && body.getOid().equals("banner"))
    {
      this.service.uploadBannerAndCache(this.getSessionId(), body.getFile().getInputStream(), body.getFile().getOriginalFilename());
    }
    else
    {
      this.service.uploadMiniLogoAndCache(this.getSessionId(), body.getFile().getInputStream(), body.getFile().getOriginalFilename());
    }

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("asset/get-all")
  public ResponseEntity<String> getAll() throws JSONException
  {
    JSONObject banner = new JSONObject();
    banner.put("oid", "banner");
    banner.put("label", "Banner");
    banner.put("custom", this.service.hasBanner(this.getSessionId()));

    JSONObject logo = new JSONObject();
    logo.put("oid", "logo");
    logo.put("label", "Logo");
    logo.put("custom", this.service.hasMiniLogo(this.getSessionId()));

    JSONArray icons = new JSONArray();
    icons.put(banner);
    icons.put(logo);

    JSONObject object = new JSONObject();
    object.put("icons", icons);

    return new ResponseEntity<String>(object.toString(), HttpStatus.OK);
  }

  @GetMapping("asset/view")
  public ResponseEntity<InputStreamResource> view(@RequestParam(required = false) String oid) throws IOException
  {
    Pair<String, InputStream> file;

    if (oid != null && oid.equals("banner"))
    {
      file = this.service.getBannerFileFromCache(this.getSessionId());
    }
    else
    {
      file = this.service.getMiniLogoFileFromCache(this.getSessionId());
    }

    if (file != null)
    {
      String ext = FilenameUtils.getExtension(file.getFirst());

      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.CONTENT_TYPE, "image/" + ext);

      return new ResponseEntity<InputStreamResource>(new InputStreamResource(file.getSecond()), headers, HttpStatus.OK);
    }

    return new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
  }

  @PostMapping("asset/remove")
  public ResponseEntity<Void> remove(@RequestBody OidBody body) throws IOException
  {
    if (body.getOid() != null && body.getOid().equals("banner"))
    {
      this.service.removeBannerFileFromCache(this.getSessionId());
    }
    else
    {
      this.service.removeMiniLogoFileFromCache(this.getSessionId());
    }

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

}