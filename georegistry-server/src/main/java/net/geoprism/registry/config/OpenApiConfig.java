package net.geoprism.registry.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;

@Configuration
public class OpenApiConfig
{

  @Bean
  public OpenAPI customOpenAPI(ServletContext servletContext)
  {

    OpenAPI api = new OpenAPI().info( //
        new Info() //
            .title("CGR") //
            .version("0.0.1") //
            );

//    if (StringUtils.hasText(AppProperties.getOpenApiUrl()))
//    {
//      return api.servers(List.of(new Server().url(AppProperties.getOpenApiUrl())));
//    }

    return api.servers(List.of(new Server().url(servletContext.getContextPath())));
  }
}
