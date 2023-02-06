package org.folio.tracing.vertx;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
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

import static org.folio.tracing.vertx.VertxSingletons.INSTRUMENTATION_LIBRARY_NAME;

class OpenTelemetryTracer implements VertxTracer<Span, Span> {

    private static final TextMapGetter<Iterable<Entry<String, String>>> getter =
            new HeadersPropagatorGetter();
    private static final TextMapSetter<BiConsumer<String, String>> setter =
            new HeadersPropagatorSetter();

    private final Tracer tracer;
    private final ContextPropagators propagators;

    OpenTelemetryTracer(final OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_LIBRARY_NAME);
        this.propagators = openTelemetry.getPropagators();
    }

    @Override
    public <R> Span receiveRequest(
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

        io.opentelemetry.context.Context tracingContext = propagators.getTextMapPropagator().extract(io.opentelemetry.context.Context.root(), headers, getter);


        // If no span, and policy is PROPAGATE, then don't create the span
        if (Span.fromContextOrNull(tracingContext) == null && TracingPolicy.PROPAGATE.equals(policy)) {
            return null;
        }

        final Span span = reportTagsAndStart(
                tracer
                        .spanBuilder(operation)
                        .setParent(tracingContext)
                        .setSpanKind(SpanKind.RPC.equals(kind) ? io.opentelemetry.api.trace.SpanKind.SERVER : io.opentelemetry.api.trace.SpanKind.CONSUMER), request, tagExtractor);

        VertxContextStorage.getInstance().attach(context, tracingContext.with(span));
        return span;
    }

    @Override
    public <R> void sendResponse(
            final Context context,
            final R response,
            final Span span,
            final Throwable failure,
            final TagExtractor<R> tagExtractor) {

        if (span != null) {
            context.remove(VertxContextStorage.ACTIVE_CONTEXT);
            end(span, response, tagExtractor, failure);
        }
    }

    private static <R> void end(Span span, R response, TagExtractor<R> tagExtractor, Throwable failure) {
        if (failure != null) {
            span.recordException(failure);
        }

        if (response != null) {
            tagExtractor.extractTo(response, span::setAttribute);
        }

        span.end();
    }

    @Override
    public <R> Span sendRequest(
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

        io.opentelemetry.context.Context tracingContext = context.getLocal(VertxContextStorage.ACTIVE_CONTEXT);

        if (tracingContext == null && !TracingPolicy.ALWAYS.equals(policy)) {
            return null;
        }

        if (tracingContext == null) {
            tracingContext = io.opentelemetry.context.Context.root();
        }

        final Span span =
                reportTagsAndStart(tracer
                                .spanBuilder(operation)
                                .setParent(tracingContext)
                                .setSpanKind(SpanKind.RPC.equals(kind) ? io.opentelemetry.api.trace.SpanKind.CLIENT : io.opentelemetry.api.trace.SpanKind.PRODUCER)
                        , request, tagExtractor);

        tracingContext = tracingContext.with(span);
        propagators.getTextMapPropagator().inject(tracingContext, headers, setter);

        return span;
    }

    @Override
    public <R> void receiveResponse(
            final Context context,
            final R response,
            final Span span,
            final Throwable failure,
            final TagExtractor<R> tagExtractor) {
        this.sendResponse(context, response, span, failure, tagExtractor);
    }

    // tags need to be set before start, otherwise any sampler registered won't have access to it
    private <T> Span reportTagsAndStart(SpanBuilder span, T obj, TagExtractor<T> tagExtractor) {
        int len = tagExtractor.len(obj);
        for (int idx = 0; idx < len; idx++) {
            span.setAttribute(tagExtractor.name(obj, idx), tagExtractor.value(obj, idx));
        }
        return span.startSpan();
    }
}
