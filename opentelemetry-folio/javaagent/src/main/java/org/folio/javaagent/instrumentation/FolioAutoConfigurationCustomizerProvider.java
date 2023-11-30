package org.folio.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.folio.javaagent.instrumentation.vertx.DbAttributesSpanProcessor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.HTTP_URL;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class FolioAutoConfigurationCustomizerProvider
    implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addResourceCustomizer(this::configureResourceCustomizer)
            .addTracerProviderCustomizer(this::configureSdkTracerProvider)
            .addMeterProviderCustomizer(this:: configureSdkMetricProvider)
            .addSamplerCustomizer(
        ((sampler, configProperties) ->
            RuleBasedRoutingSampler.builder(SpanKind.SERVER, sampler)
                .drop(HTTP_URL, ".*/admin/health")
                .build()));
  }

  private SdkTracerProviderBuilder configureSdkTracerProvider(
          SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
    return tracerProvider
            .addSpanProcessor(new FolioRequestAttributesSpanProcessor())
            .addSpanProcessor(new DbAttributesSpanProcessor());
  }

  private SdkMeterProviderBuilder configureSdkMetricProvider(SdkMeterProviderBuilder meterProvider, ConfigProperties config){
    return meterProvider;
  }

    private Resource configureResourceCustomizer(Resource resource, ConfigProperties configProperties) {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.out.println("Hostname could not be found");
            hostname = "UNKNOWN";
        }
        String finalHostname = hostname;

        Resource newResource = Resource.builder().put(ResourceAttributes.SERVICE_INSTANCE_ID, finalHostname)
                .build();
        return resource.merge(newResource);

    }
}
