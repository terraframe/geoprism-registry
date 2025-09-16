package net.geoprism.registry.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "net.geoprism.registry.config" })
public class TestApplication
{

  public static void main(String[] args)
  {
    SpringApplication.run(TestApplication.class, args);
  }

}
