package net.geoprism.registry.config;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.runwaysdk.constants.DeployProperties;

import net.geoprism.PluginUtil;

@SpringBootApplication
public class Application extends SpringBootServletInitializer
{

  public static void main(String[] args)
  {
    SpringApplication.run(Application.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
  {
    buildDb();
    
    return application.sources(SpringAppConfig.class);
  }
  
  private void buildDb()
  {
    var patcher = PluginUtil.getDatabaseBuilder();

    patcher.initialize(new File(DeployProperties.getDeployBin(), "metadata"));
    patcher.run();
  }
}