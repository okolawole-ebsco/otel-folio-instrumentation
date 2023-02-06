package org.folio.tracing.vertx;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingOptions;

public class OpenTelemetryOptions extends TracingOptions {

  private OpenTelemetry openTelemetry;

  public OpenTelemetryOptions(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
    this.setFactory(VertxSingletons.OPEN_TELEMETRY_TRACING_FACTORY);
  }

  public OpenTelemetryOptions() {
    this.setFactory(VertxSingletons.OPEN_TELEMETRY_TRACING_FACTORY);
  }

  VertxTracer<Span, Span> buildTracer() {
    if (openTelemetry != null) {
      return new OpenTelemetryTracer(openTelemetry);
    } else {
      return new OpenTelemetryTracer(GlobalOpenTelemetry.get());
    }
  }
}
