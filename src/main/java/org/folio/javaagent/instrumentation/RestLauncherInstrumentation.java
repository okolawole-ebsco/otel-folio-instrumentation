package org.folio.javaagent.instrumentation;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.vertx.core.VertxOptions;
import io.vertx.core.tracing.TracingOptions;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.folio.javaagent.vertx.OpenTelemetryOptions;
import org.folio.rest.RestLauncher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class RestLauncherInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.folio.rest.RestLauncher");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("org.folio.rest.RestLauncher", "org.folio.rest.RestVerticle");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("beforeStartingVertx").and(takesArgument(0, named("io.vertx.core.VertxOptions"))),
        RestLauncherInstrumentation.class.getName() + "$beforeStartingVertxAdvice");
  }

  @SuppressWarnings("unused")
  public static class beforeStartingVertxAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void enableTracing(
        @Advice.Argument(value = 0, readOnly = false) VertxOptions options) {
      options = options.setTracingOptions(new OpenTelemetryOptions());
    }
  }
}
