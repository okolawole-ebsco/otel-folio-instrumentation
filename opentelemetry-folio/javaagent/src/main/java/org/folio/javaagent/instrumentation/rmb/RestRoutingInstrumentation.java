package org.folio.javaagent.instrumentation.rmb;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.folio.rest.RestVerticle;

import java.util.Map;
import java.util.Objects;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static org.folio.javaagent.instrumentation.Constants.*;

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
            Baggage current = Baggage.current();
            // if request id is already set, don't overwrite it.
            if(current.getEntryValue(FOLIO_REQUEST_ID) != null &&
                    !Objects.equals(current.getEntryValue(FOLIO_REQUEST_ID),
                    headers.get(RestVerticle.OKAPI_REQUESTID_HEADER))){
                return;
            }
            current
                .toBuilder()
                .put(FOLIO_USER_ID, headers.get(RestVerticle.OKAPI_USERID_HEADER))
                .put(FOLIO_REQUEST_ID, headers.get(RestVerticle.OKAPI_REQUESTID_HEADER))
                .put(FOLIO_TENANT_ID, headers.get(RestVerticle.OKAPI_HEADER_TENANT))
                .build()
                .makeCurrent();
        }
    }
}
