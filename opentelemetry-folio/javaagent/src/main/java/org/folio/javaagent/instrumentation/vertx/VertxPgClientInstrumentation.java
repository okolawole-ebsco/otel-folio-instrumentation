package org.folio.javaagent.instrumentation.vertx;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.vertx.pgclient.PgConnectOptions;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Optional;

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
    String serviceName =
            Optional.ofNullable(ResourceConfiguration.createEnvironmentResource()
                    .getAttribute(ResourceAttributes.SERVICE_NAME))
                    .orElse("unknown-service");
    System.setProperty("folio_service_name", serviceName);
    transformer.applyAdviceToMethod(
        named("init"), VertxPgClientInstrumentation.class.getName() + "$setApplicationNameAdvice");
  }

  @SuppressWarnings("unused")
  public static class setApplicationNameAdvice {

    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void setApplicationName(@Advice.This PgConnectOptions connectOptions) {
      String otelServiceName = System.getProperty("folio_service_name");
      if (otelServiceName == null) {
        return;
      }
      connectOptions.addProperty("application_name", otelServiceName);
    }
  }
}
