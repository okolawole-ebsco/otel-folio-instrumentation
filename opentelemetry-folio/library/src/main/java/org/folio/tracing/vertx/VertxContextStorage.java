package org.folio.tracing.vertx;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;
import io.vertx.core.Vertx;

public class VertxContextStorage implements ContextStorage {
  public static String ACTIVE_CONTEXT = "folio.tracing.context";

  private static VertxContextStorage INSTANCE;

  private VertxContextStorage() {}

  public static VertxContextStorage getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new VertxContextStorage();
    }
    return INSTANCE;
  }

  @Override
  public Scope attach(Context toAttach) {
    return attach(Vertx.currentContext(), toAttach);
  }

  public Scope attach(io.vertx.core.Context vertxCtx, Context toAttach) {
    Context current = vertxCtx.getLocal(ACTIVE_CONTEXT);

    if (current == toAttach) {
      return Scope.noop();
    }

    vertxCtx.putLocal(ACTIVE_CONTEXT, toAttach);

    if (current == null) {
      return () -> vertxCtx.removeLocal(ACTIVE_CONTEXT);
    }
    return () -> vertxCtx.putLocal(ACTIVE_CONTEXT, current);
  }

  @Override
  public Context current() {
    io.vertx.core.Context vertxCtx = Vertx.currentContext();
    if (vertxCtx == null) {
      return null;
    }
    return vertxCtx.getLocal(ACTIVE_CONTEXT);
  }
}
