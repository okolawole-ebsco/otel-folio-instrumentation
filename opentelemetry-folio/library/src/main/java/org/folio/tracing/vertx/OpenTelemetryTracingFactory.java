package org.folio.tracing.vertx;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VertxTracerFactory;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingOptions;

public class OpenTelemetryTracingFactory implements VertxTracerFactory {

  @Override
  public VertxTracer<Span, Span> tracer(final TracingOptions options) {
    OpenTelemetryOptions openTelemetryOptions;
    if (options instanceof OpenTelemetryOptions) {
      openTelemetryOptions = (OpenTelemetryOptions) options;
    } else {
      throw new RuntimeException("Not the right type");
    }
    return openTelemetryOptions.buildTracer();
  }

  @Override
  public TracingOptions newOptions() {
    return new OpenTelemetryOptions();
  }

  @Override
  public TracingOptions newOptions(JsonObject jsonObject) {
    throw new RuntimeException("Creating new tracing options via JSON is not implemented");
  }
}
