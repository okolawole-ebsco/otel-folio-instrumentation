/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.javaagent.instrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.not;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(InstrumentationModule.class)
public class VertxCoreInstrumentationModule extends InstrumentationModule {

  public VertxCoreInstrumentationModule() {
    super("vertx-core", "vertx");
  }

  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    // class removed in 4.0
    return not(hasClassesNamed("io.vertx.core.Starter"));
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return asList(new HttpClientConnectionInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    System.out.println(className.startsWith("org.folio.javaagent"));
    return className.startsWith("org.folio.javaagent");
  }

  @Override
  public List<String> getAdditionalHelperClassNames() {
    return Arrays.asList(
            "org.folio.javaagent.instrumentation.HandlerWrapper");
  }
}
