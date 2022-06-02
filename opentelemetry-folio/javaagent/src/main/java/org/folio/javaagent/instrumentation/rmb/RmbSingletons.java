package org.folio.javaagent.instrumentation.rmb;

import io.vertx.tracing.opentelemetry.VertxContextStorageProvider;

public class RmbSingletons {
    public static VertxContextStorageProvider contextStorageProvider =  new VertxContextStorageProvider();
}
