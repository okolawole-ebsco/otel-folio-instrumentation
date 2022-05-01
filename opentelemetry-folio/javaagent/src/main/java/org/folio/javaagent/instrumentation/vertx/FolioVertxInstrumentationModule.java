package org.folio.javaagent.instrumentation.vertx;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static java.util.Arrays.asList;

@AutoService(InstrumentationModule.class)
public class FolioVertxInstrumentationModule extends InstrumentationModule {

  public FolioVertxInstrumentationModule() {
    super("folio-vertx", "folio");
  }

  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    return hasClassesNamed("org.folio.rest.RestLauncher");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return asList(
        new VertxOptionsInstrumentation(),
        new VertxPgClientInstrumentation(),
        new VertxQueryResultBuilderInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("org.folio");
  }


}
