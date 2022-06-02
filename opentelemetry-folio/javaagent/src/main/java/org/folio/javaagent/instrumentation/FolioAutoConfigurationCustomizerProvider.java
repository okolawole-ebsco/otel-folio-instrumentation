package org.folio.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.samplers.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.HTTP_URL;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class FolioAutoConfigurationCustomizerProvider
    implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addSamplerCustomizer(
        ((sampler, configProperties) ->
            RuleBasedRoutingSampler.builder(SpanKind.SERVER, sampler)
                .drop(HTTP_URL, ".*/admin/health")
                .build()));
  }
}
