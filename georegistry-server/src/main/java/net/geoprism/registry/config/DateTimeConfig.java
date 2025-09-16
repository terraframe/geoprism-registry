package net.geoprism.registry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

@Configuration
public class DateTimeConfig implements WebMvcConfigurer
{
  @Override
  public void addFormatters(FormatterRegistry registry)
  {
    DateFormatter dateFormatter = new DateFormatter(GeoObjectImportConfiguration.DATE_FORMAT);
    dateFormatter.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    registry.addFormatter(dateFormatter);
  }
}