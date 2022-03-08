package org.folio.javaagent.vertx;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.vertx.core.Context;
import io.vertx.core.spi.tracing.SpanKind;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;

import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static org.folio.javaagent.vertx.VertxContextStorageProvider.ACTIVE_CONTEXT;

class OpenTelemetryTracer implements VertxTracer<Scope, Scope> {

  private static final TextMapGetter<Iterable<Entry<String, String>>> getter =
      new HeadersPropagatorGetter();
  private static final TextMapSetter<BiConsumer<String, String>> setter =
      new HeadersPropagatorSetter();

  private final Tracer tracer;
  private final ContextPropagators propagators;

  OpenTelemetryTracer(final OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("io.vertx");
    this.propagators = openTelemetry.getPropagators();
  }

  @Override
  public <R> Scope receiveRequest(
      final Context context,
      final SpanKind kind,
      final TracingPolicy policy,
      final R request,
      final String operation,
      final Iterable<Entry<String, String>> headers,
      final TagExtractor<R> tagExtractor) {

    if (TracingPolicy.IGNORE.equals(policy)) {
      return null;
    }

    io.opentelemetry.context.Context tracingContext = context.getLocal(ACTIVE_CONTEXT);
    if (tracingContext == null) {
      tracingContext = io.opentelemetry.context.Context.root();
    }
    tracingContext = propagators.getTextMapPropagator().extract(tracingContext, headers, getter);

    // If no span, and policy is PROPAGATE, then don't create the span
    if (Span.fromContextOrNull(tracingContext) == null && TracingPolicy.PROPAGATE.equals(policy)) {
      return null;
    }

    tracingContext.makeCurrent();

    final Span span =
        tracer
            .spanBuilder(operation)
            .setParent(tracingContext)
            .setSpanKind(
                SpanKind.RPC.equals(kind)
                    ? io.opentelemetry.api.trace.SpanKind.SERVER
                    : io.opentelemetry.api.trace.SpanKind.CONSUMER)
            .startSpan();

    tagExtractor.extractTo(request, span::setAttribute);

    return VertxContextStorageProvider.VertxContextStorage.INSTANCE.attach(
        context, tracingContext.with(span));
  }

  @Override
  public <R> void sendResponse(
      final Context context,
      final R response,
      final Scope scope,
      final Throwable failure,
      final TagExtractor<R> tagExtractor) {

    if (scope == null) {
      return;
    }

    Span span = Span.fromContext(context.getLocal(ACTIVE_CONTEXT));

    if (failure != null) {
      span.recordException(failure);
    }

    if (response != null) {
      tagExtractor.extractTo(response, span::setAttribute);
    }

    span.end();
    scope.close();
  }

  @Override
  public <R> Scope sendRequest(
      final Context context,
      final SpanKind kind,
      final TracingPolicy policy,
      final R request,
      final String operation,
      final BiConsumer<String, String> headers,
      final TagExtractor<R> tagExtractor) {

    if (TracingPolicy.IGNORE.equals(policy) || request == null) {
      return null;
    }

    io.opentelemetry.context.Context tracingContext = context.getLocal(ACTIVE_CONTEXT);

    if (tracingContext == null && !TracingPolicy.ALWAYS.equals(policy)) {
      return null;
    }

    if (tracingContext == null) {
      tracingContext = io.opentelemetry.context.Context.root();
    }

    final Span span =
        tracer
            .spanBuilder(operation)
            .setParent(tracingContext)
            .setSpanKind(
                SpanKind.RPC.equals(kind)
                    ? io.opentelemetry.api.trace.SpanKind.CLIENT
                    : io.opentelemetry.api.trace.SpanKind.PRODUCER)
            .startSpan();
    tagExtractor.extractTo(request, span::setAttribute);

    tracingContext = tracingContext.with(span);
    propagators.getTextMapPropagator().inject(tracingContext, headers, setter);

    return VertxContextStorageProvider.VertxContextStorage.INSTANCE.attach(context, tracingContext);
  }

  @Override
  public <R> void receiveResponse(
      final Context context,
      final R response,
      final Scope scope,
      final Throwable failure,
      final TagExtractor<R> tagExtractor) {
    this.sendResponse(context, response, scope, failure, tagExtractor);
  }
}
