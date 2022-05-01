package org.folio.tracing.vertx;

public final class VertxSingletons {

  public static final OpenTelemetryTracingFactory OPEN_TELEMETRY_TRACING_FACTORY =
      new OpenTelemetryTracingFactory();

  public static final String INSTRUMENTATION_LIBRARY_NAME = "org.folio.telemetry";
}
