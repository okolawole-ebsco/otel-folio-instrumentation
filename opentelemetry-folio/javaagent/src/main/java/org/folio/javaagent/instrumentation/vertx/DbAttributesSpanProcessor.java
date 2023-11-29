package org.folio.javaagent.instrumentation.vertx;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.util.Objects;

import static org.folio.javaagent.instrumentation.Constants.FOLIO_REQUEST_ID;
import static org.folio.javaagent.instrumentation.Constants.FOLIO_TENANT_ID;
import static org.folio.javaagent.instrumentation.Constants.FOLIO_USER_ID;

public class DbAttributesSpanProcessor implements SpanProcessor {
    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        if (span.getAttribute(SemanticAttributes.DB_USER) != null) {
            span.setAttribute(SemanticAttributes.DB_SYSTEM, "postgresql");
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofSuccess();
    }
}
