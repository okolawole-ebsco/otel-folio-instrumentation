package org.folio.javaagent.instrumentation.kafka;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.KafkaHeader;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.folio.kafka.KafkaHeaderUtils;
import org.folio.okapi.common.logging.FolioLoggingContext;
import org.folio.rest.RestVerticle;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class FolioAsyncRecordHandlerInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return implementsInterface(named("org.folio.kafka.AsyncRecordHandler"));
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("org.folio.kafka.AsyncRecordHandler");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("handle")
            .and(takesArgument(0, named("io.vertx.kafka.client.consumer.KafkaConsumerRecord"))),
        FolioAsyncRecordHandlerInstrumentation.class.getName() + "$handleAdvice");
  }

  @SuppressWarnings("unused")
  public static class handleAdvice {
    @SuppressWarnings("unchecked")
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void setContext(@Advice.Argument(value = 0) KafkaConsumerRecord record) {
      List<KafkaHeader> kafkaHeaders = record.headers();
      FolioLoggingContext.put(
          FolioLoggingContext.REQUEST_ID_LOGGING_VAR_NAME,
          KafkaHeaderUtils.kafkaHeadersToMap(kafkaHeaders)
              .get(RestVerticle.OKAPI_REQUESTID_HEADER.toLowerCase()));
    }
  }
}
