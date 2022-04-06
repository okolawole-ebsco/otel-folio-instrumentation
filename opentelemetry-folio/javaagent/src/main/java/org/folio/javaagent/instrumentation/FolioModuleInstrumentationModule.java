/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.HelperResourceBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;

@AutoService(InstrumentationModule.class)
public class FolioModuleInstrumentationModule extends InstrumentationModule {

  public FolioModuleInstrumentationModule() {
    super("folio");
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
        new VertxQueryResultBuilderInstrumentation(),
        new ResourceInjectingTypeInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("org.folio");
  }

  @Override
  public void registerHelperResources(HelperResourceBuilder helperResourceBuilder) {
    helperResourceBuilder.register(
            "META-INF/services/org.apache.logging.log4j.core.util.ContextDataProvider",
            "META-INF/services/application.org.apache.logging.log4j.core.util.ContextDataProvider");
  }

  // A type instrumentation is needed to trigger resource injection.
  public static class ResourceInjectingTypeInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
      return named("application.org.apache.logging.log4j.core.util.ContextDataProvider");
    }

    @Override
    public void transform(TypeTransformer transformer) {
      // Nothing to transform, this type instrumentation is only used for injecting resources.
    }
  }
}
