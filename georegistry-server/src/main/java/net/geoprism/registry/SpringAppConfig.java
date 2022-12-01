package net.geoprism.registry;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.geoprism.registry.io.GeoObjectImportConfiguration;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "net.geoprism.registry.controller", "net.geoprism.registry.service", "net.geoprism.registry.spring" })
public class SpringAppConfig extends WebMvcConfigurationSupport
{

  @Bean
  public InternalResourceViewResolver getInternalResourceViewResolver()
  {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/"); // TODO : Security hole??
    resolver.setSuffix(".jsp");
    return resolver;
  }

  @Bean(name = "multipartResolver")
  public CommonsMultipartResolver multipartResolver()
  {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    multipartResolver.setMaxUploadSize(-1);
    return multipartResolver;
  }

  @Bean
  @Override
  public FormattingConversionService mvcConversionService()
  {
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);

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

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
  {
    GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    msgConverter.setGson(gson);
    converters.add(msgConverter);
  }
}
