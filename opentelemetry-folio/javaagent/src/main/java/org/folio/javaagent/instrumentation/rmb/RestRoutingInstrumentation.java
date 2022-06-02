package org.folio.javaagent.instrumentation.rmb;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.folio.rest.RestVerticle;

import java.util.Map;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static org.folio.javaagent.instrumentation.rmb.RmbSingletons.contextStorageProvider;

public class RestRoutingInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.folio.rest.RestRouting");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("org.folio.rest.RestRouting");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("invoke")
            .and(takesArgument(0, named("java.lang.reflect.Method")))
            .and(takesArgument(3, named("io.vertx.ext.web.RoutingContext")))
            .and(takesArgument(4, named("java.util.Map"))),
        RestRoutingInstrumentation.class.getName() + "$invokeAdvice");
  }

  @SuppressWarnings("unused")
  public static class invokeAdvice {
    @SuppressWarnings("unchecked")
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void setContext(@Advice.Argument(value = 4) Map<String, String> headers) {
      Context context = contextStorageProvider.get().current();
      Span.fromContext(context)
              .setAttribute("folio.userId", headers.get(RestVerticle.OKAPI_USERID_HEADER))
              .setAttribute("folio.requestId", headers.get(RestVerticle.OKAPI_REQUESTID_HEADER))
              .setAttribute("folio.tenantId", headers.get(RestVerticle.OKAPI_HEADER_TENANT));
    }
  }
}
