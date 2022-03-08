/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.javaagent.instrumentation;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import org.folio.javaagent.common.VertxClientInstrumenterFactory;

public final class VertxSingletons {

  private static final Instrumenter<HttpClientRequest, HttpClientResponse> INSTRUMENTER =
      VertxClientInstrumenterFactory.create(
          "org.folio.javaagent.instrumentation.vertx-4.2",
          new Vertx4HttpAttributesGetter(),
          new Vertx4NetAttributesGetter());

  public static Instrumenter<HttpClientRequest, HttpClientResponse> instrumenter() {
    return INSTRUMENTER;
  }

  private VertxSingletons() {}
}
