package org.folio.javaagent.instrumentation.kafka;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;

@AutoService(InstrumentationModule.class)
public class FolioKafkaInstrumentationModule extends InstrumentationModule {

  public FolioKafkaInstrumentationModule() {
    super("folio-kafka", "folio");
  }

  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    return hasClassesNamed("org.folio.kafka.AsyncRecordHandler");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return List.of(
            new FolioAsyncRecordHandlerInstrumentation());
  }
}
