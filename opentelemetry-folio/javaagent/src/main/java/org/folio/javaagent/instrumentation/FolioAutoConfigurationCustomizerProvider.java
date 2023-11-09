package org.folio.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

import static io.opentelemetry.semconv.SemanticAttributes.URL_FULL;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class FolioAutoConfigurationCustomizerProvider
    implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
            .addTracerProviderCustomizer(this::configureSdkTracerProvider)
            .addSamplerCustomizer(
        ((sampler, configProperties) ->
            RuleBasedRoutingSampler.builder(SpanKind.SERVER, sampler)
                .drop(URL_FULL, ".*/admin/health")
                .build()));
  }

  private SdkTracerProviderBuilder configureSdkTracerProvider(
          SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
    return tracerProvider
            .addSpanProcessor(new FolioRequestAttributesSpanProcessor());
  }
}
