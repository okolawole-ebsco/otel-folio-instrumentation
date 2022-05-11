package org.folio.javaagent.instrumentation.vertx;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.vertx.core.VertxOptions;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class VertxOptionsInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("io.vertx.core.VertxOptions");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("io.vertx.core.VertxOptions");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isConstructor(), VertxOptionsInstrumentation.class.getName() + "$enableTracingAdvice");
  }

  @SuppressWarnings("unused")
  public static class enableTracingAdvice {
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void enableTracing(@Advice.This VertxOptions options) {
      if (options.getTracingOptions() == null) {
        options.setTracingOptions(new OpenTelemetryOptions());
      }
    }
  }
}
