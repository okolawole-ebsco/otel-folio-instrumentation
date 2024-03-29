package org.folio.javaagent.instrumentation.logging;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.HelperResourceBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.named;

@AutoService(InstrumentationModule.class)
public class FolioLoggingInstrumentationModule extends InstrumentationModule {

  public FolioLoggingInstrumentationModule() {
    super("folio-logging", "folio");
  }

  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    return hasClassesNamed("org.apache.logging.log4j.Logger");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return List.of(
            new ResourceInjectingTypeInstrumentation());
  }

  @Override
  public void registerHelperResources(HelperResourceBuilder helperResourceBuilder) {
    helperResourceBuilder.register(
            "META-INF/services/org.apache.logging.log4j.core.util.ContextDataProvider",
            "META-INF/services/org.apache.logging.log4j.core.util.ContextDataProvider");
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("org.folio.instrumentation.log4j2.FolioContextDataProvider");
  }

  // A type instrumentation is needed to trigger resource injection.
  public static class ResourceInjectingTypeInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
      return named("org.folio.instrumentation.log4j2.FolioContextDataProvider");
    }

    @Override
    public void transform(TypeTransformer transformer) {
      // Nothing to transform, this type instrumentation is only used for injecting resources.
    }
  }
}
