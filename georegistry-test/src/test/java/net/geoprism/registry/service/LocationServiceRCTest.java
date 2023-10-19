/**
 *
 */
package net.geoprism.registry.service;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
@Ignore
public class LocationServiceRCTest extends AbstractLocationServiceTest implements InstanceTestClassListener
{

}
