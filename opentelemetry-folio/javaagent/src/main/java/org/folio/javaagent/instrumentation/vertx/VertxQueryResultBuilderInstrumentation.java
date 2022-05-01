package org.folio.javaagent.instrumentation.vertx;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class VertxQueryResultBuilderInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("io.vertx.sqlclient.impl.QueryResultBuilder");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("io.vertx.sqlclient.impl.QueryResultBuilder");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod().and(named("tryComplete")).and(takesArguments(1)),
        VertxQueryResultBuilderInstrumentation.class.getName() + "$endTracingEarlyAdvice");
  }

  @SuppressWarnings("unused")
  public static class endTracingEarlyAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void endTracing(
        @Advice.FieldValue(value = "tracer") QueryTracer tracer,
        @Advice.FieldValue(value = "failure") Throwable failure,
        @Advice.FieldValue(value = "context") ContextInternal context,
        @Advice.FieldValue(value = "first") Object first,
        @Advice.FieldValue(value = "tracingPayload") Object tracingPayload) {
      if (failure == null && tracer != null) {
        // close the span early before executing the upcoming handler
        tracer.receiveResponse(context, tracingPayload, first, null);
      }
    }
  }
}
