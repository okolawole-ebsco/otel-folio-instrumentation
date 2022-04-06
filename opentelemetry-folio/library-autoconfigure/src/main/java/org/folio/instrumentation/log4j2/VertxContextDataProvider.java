/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.folio.instrumentation.log4j2;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.core.util.ContextDataProvider;

import java.util.HashMap;
import java.util.Map;

public class VertxContextDataProvider implements ContextDataProvider {

  @Override
  public Map<String, String> supplyContextData() {
    Map<String, String> contextData = new HashMap<>();

    Context context = Vertx.currentContext();
    if (context != null) {
      String folio_requestid = context.getLocal("folio_requestid");
      if (folio_requestid != null) {
        contextData.put("reqId", folio_requestid);
      }
    }
    return contextData;
  }
}
