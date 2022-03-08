/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.javaagent.vertx;

public final class VertxSingletons {

  public static final OpenTelemetryTracingFactory OPEN_TELEMETRY_TRACING_FACTORY =
      new OpenTelemetryTracingFactory();
}
