/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.javaagent.common;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.vertx.core.http.HttpClientRequest;

public class HttpRequestHeaderSetter implements TextMapSetter<HttpClientRequest> {

  @Override
  public void set(HttpClientRequest carrier, String key, String value) {
    if (carrier != null) {
      carrier.putHeader(key, value);
    }
  }
}
