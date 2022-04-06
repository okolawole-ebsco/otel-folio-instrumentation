package org.folio.javaagent.instrumentation;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.vertx.pgclient.PgConnectOptions;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class VertxPgClientInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("io.vertx.pgclient.PgConnectOptions");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("io.vertx.pgclient.PgConnectOptions");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("init"),
        VertxPgClientInstrumentation.class.getName() + "$setApplicationNameAdvice");
  }

  @SuppressWarnings("unused")
  public static class setApplicationNameAdvice {
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void setApplicationName(@Advice.This PgConnectOptions connectOptions) {
      String otelServiceName = System.getProperty("otel.service.name");
      if (otelServiceName == null) {
        return;
      }
      connectOptions.addProperty("application_name", otelServiceName);
    }
  }
}
