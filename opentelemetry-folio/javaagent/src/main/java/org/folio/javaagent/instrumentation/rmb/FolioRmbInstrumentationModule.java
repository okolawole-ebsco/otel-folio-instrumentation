package org.folio.javaagent.instrumentation.rmb;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;

@AutoService(InstrumentationModule.class)
public class FolioRmbInstrumentationModule extends InstrumentationModule {

  public FolioRmbInstrumentationModule() {
    super("folio-rmb", "folio");
  }

  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    return hasClassesNamed("org.folio.rest.RestRouting");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return List.of(new RestRoutingInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("io.vertx.tracing") ||
            className.equals("org.folio.javaagent.instrumentation.rmb.RmbSingletons");
  }
}
