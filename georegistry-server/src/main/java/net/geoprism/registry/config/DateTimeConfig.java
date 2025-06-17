package net.geoprism.registry.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

@Configuration
public class DateTimeConfig extends WebMvcConfigurationSupport
{

  @Bean
  @Override
  public FormattingConversionService mvcConversionService()
  {
    final FormattingConversionService conversionService = new DefaultFormattingConversionService(false);
    
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(GeoObjectImportConfiguration.DATE_FORMAT);
    dateTimeFormatter.withZone(ZoneId.of(GeoRegistryUtil.SYSTEM_TIMEZONE.getID()));

    DateTimeFormatterRegistrar dateTimeRegistrar = new DateTimeFormatterRegistrar();
    dateTimeRegistrar.setDateFormatter(dateTimeFormatter);
    dateTimeRegistrar.setDateTimeFormatter(dateTimeFormatter);
    dateTimeRegistrar.registerFormatters(conversionService);

    DateFormatter dateFormatter = new DateFormatter(GeoObjectImportConfiguration.DATE_FORMAT);
    dateFormatter.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    DateFormatterRegistrar dateRegistrar = new DateFormatterRegistrar();
    dateRegistrar.setFormatter(dateFormatter);
    dateRegistrar.registerFormatters(conversionService);

    return conversionService;
  }
}