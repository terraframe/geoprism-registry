package net.geoprism.registry.service;

import java.util.Locale;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.springframework.stereotype.Component;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

@Component
public class RegistryComponentService
{
  @Request(RequestType.SESSION)
  public CustomSerializer serializer(String sessionId)
  {
    Locale locale = Session.getCurrentLocale();

    return new LocaleSerializer(locale);
  }
}
