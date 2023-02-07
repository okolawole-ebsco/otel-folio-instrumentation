package org.folio.javaagent.instrumentation;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

import java.util.Objects;

import static org.folio.javaagent.instrumentation.Constants.*;

public class FolioRequestAttributesSpanProcessor implements SpanProcessor {
    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        // set folio headers from baggage into span
        Baggage current = Baggage.current();
        span.setAttribute(FOLIO_USER_ID,
                Objects.requireNonNullElse(current.getEntryValue(FOLIO_USER_ID), ""));
        span.setAttribute(FOLIO_REQUEST_ID,
                Objects.requireNonNullElse(current.getEntryValue(FOLIO_REQUEST_ID), ""));
        span.setAttribute(FOLIO_TENANT_ID,
                Objects.requireNonNullElse(current.getEntryValue(FOLIO_TENANT_ID), ""));
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {}

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
