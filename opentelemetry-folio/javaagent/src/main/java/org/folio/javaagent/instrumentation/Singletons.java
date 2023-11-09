package org.folio.javaagent.instrumentation;

import io.vertx.tracing.opentelemetry.VertxContextStorageProvider;

public class Singletons {
    // this is used in advices. The context storage provider isn't visible in application space
    public static VertxContextStorageProvider CONTEXT_STORAGE_PROVIDER =  new VertxContextStorageProvider();
}
