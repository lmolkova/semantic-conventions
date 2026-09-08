package io.opentelemetry.semconv.prototype.config;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import java.util.function.Consumer;

/** Temporary adapter for the proposed {@link ConfigProvider} change-listener API. */
public interface DynamicConfigProvider extends ConfigProvider {

  void addInstrumentationConfigListener(Consumer<DeclarativeConfigProperties> listener);
}
