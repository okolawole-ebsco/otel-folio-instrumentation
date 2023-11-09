package org.folio.instrumentation.log4j2;

import io.opentelemetry.api.baggage.Baggage;
import org.apache.logging.log4j.core.util.ContextDataProvider;

import java.util.HashMap;
import java.util.Map;

public class VertxContextDataProvider implements ContextDataProvider {

  @Override
  public Map<String, String> supplyContextData() {
    Map<String, String> contextData = new HashMap<>();

    Baggage baggage = Baggage.current();
    String requestId = baggage.getEntryValue("folio.requestId");
    String tenantId = baggage.getEntryValue("folio.tenantId");
    String userId = baggage.getEntryValue("folio.userId");


    if (requestId != null) {
      contextData.put("reqId", requestId);
      contextData.put("tenantId", tenantId);
      contextData.put("userId", userId);
    }

    return contextData;
  }
}
