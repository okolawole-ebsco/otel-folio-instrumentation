package org.folio.javaagent.instrumentation.vertx;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.kafka.client.common.KafkaClientOptions;
import io.vertx.kafka.client.consumer.impl.KafkaReadStreamImpl;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class VertxKafkaReadStreamImplInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("io.vertx.kafka.client.consumer.impl.KafkaReadStreamImpl");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("io.vertx.core.VertxOptions");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
            isConstructor().and(takesArguments(3))
                    .and(takesArgument(0, named("io.vertx.core.Vertx")))
                    .and(takesArgument(2, named("io.vertx.kafka.client.common.KafkaClientOptions"))),
            VertxKafkaReadStreamImplInstrumentation.class.getName() + "$enableTracingAdvice");
  }

  @SuppressWarnings("unused")
  public static class enableTracingAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(@Advice.Argument(0) Vertx vertx,
                               @Advice.Argument(2) KafkaClientOptions options) {
      if (options != null) {
        options.setTracingPolicy(TracingPolicy.ALWAYS);
      }
    }
  }
}
