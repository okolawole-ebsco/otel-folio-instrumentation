package org.folio.instrumentation.vertx;

import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import org.folio.tracing.vertx.VertxContextStorage;

public class VertxContextStorageProvider implements ContextStorageProvider {

  @Override
  public ContextStorage get() {
    return VertxContextStorage.getInstance();
  }
}
