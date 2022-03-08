/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.HelperResourceBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.not;

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
    return asList(new RestLauncherInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("org.folio.javaagent.vertx");
  }

  @Override
  public List<String> getAdditionalHelperClassNames() {
    return singletonList(
            "io.opentelemetry.context.ContextStorageProvider");
  }
}
