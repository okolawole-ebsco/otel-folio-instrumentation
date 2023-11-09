package org.folio.javaagent.instrumentation.vertx;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.HelperResourceBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;

@AutoService(InstrumentationModule.class)
public class FolioVertxInstrumentationModule extends InstrumentationModule {

    public FolioVertxInstrumentationModule() {
        super("folio-vertx", "folio");
    }

    @Override
    public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
        return hasClassesNamed("org.folio.rest.RestLauncher");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return asList(
                new VertxOptionsInstrumentation(),
                new VertxPgClientInstrumentation()
        );
    }

    @Override
    public void registerHelperResources(HelperResourceBuilder helperResourceBuilder) {
        // this service is coming from the vertx-opentelemetry library
        helperResourceBuilder.register(
                "META-INF/services/io.opentelemetry.javaagent.shaded.io.opentelemetry.context.ContextStorageProvider",
                "META-INF/services/io.vertx.context.VertxContextStorageProvider");
    }

    @Override
    public boolean isHelperClass(String className) {
        // used to pull in vertx-opentelemetry classes. This is configure the vertx storage provider as well
        // as the tracer
        return className.startsWith("io.vertx.tracing") ||
                className.startsWith("io.opentelemetry.extension.noopapi") ||
                className.startsWith("io.opentelemetry.sdk.autoconfigure");
    }

    // A type instrumentation is needed to trigger resource injection.
    public static class ResourceInjectingTypeInstrumentation implements TypeInstrumentation {
        @Override
        public ElementMatcher<TypeDescription> typeMatcher() {
            return named("org.folio.rest.RestLauncher");
        }

        @Override
        public void transform(TypeTransformer transformer) {
            // Nothing to transform, this type instrumentation is only used for injecting resources.
        }
    }


}
