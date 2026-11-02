/**
 *
 */
package net.geoprism.registry;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * Calls {@link InstanceTestClassListener#beforeClassSetup()} once per test
 * class, on the first test instance after Spring has injected it, and
 * {@link InstanceTestClassListener#afterClassSetup()} on that same instance
 * after all of the class's tests have run. Unlike a static @BeforeAll method,
 * the setup can use the instance's autowired services.
 */
public class InstanceTestClassExtension implements BeforeEachCallback, AfterAllCallback
{
  private static final Namespace NAMESPACE = Namespace.create(InstanceTestClassExtension.class);

  private static final String    LISTENER  = "listener";

  @Override
  public void beforeEach(ExtensionContext context) throws Exception
  {
    // Runs after the test instance is created and injected, and before its
    // @BeforeEach methods
    Object test = context.getRequiredTestInstance();
    Store store = getClassStore(context);

    if (test instanceof InstanceTestClassListener && store.get(LISTENER) == null)
    {
      InstanceTestClassListener listener = (InstanceTestClassListener) test;

      // Recorded before running the setup so a failed setup isn't retried for
      // every test, and the teardown still runs
      store.put(LISTENER, listener);

      listener.beforeClassSetup();
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception
  {
    InstanceTestClassListener listener = getClassStore(context).get(LISTENER, InstanceTestClassListener.class);

    if (listener != null)
    {
      listener.afterClassSetup();
    }
  }

  private Store getClassStore(ExtensionContext context)
  {
    ExtensionContext classContext = context.getTestMethod().isPresent() ? context.getParent().orElseThrow() : context;

    return classContext.getStore(NAMESPACE);
  }
}
