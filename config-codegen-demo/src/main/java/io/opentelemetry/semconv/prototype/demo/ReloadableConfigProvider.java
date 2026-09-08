package io.opentelemetry.semconv.prototype.demo;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.semconv.prototype.config.DynamicConfigProvider;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

final class ReloadableConfigProvider implements DynamicConfigProvider {

  private volatile DeclarativeConfigProperties config;
  private final List<Consumer<DeclarativeConfigProperties>> listeners =
      new CopyOnWriteArrayList<>();

  ReloadableConfigProvider(DeclarativeConfigProperties config) {
    this.config = config;
  }

  @Override
  public DeclarativeConfigProperties getInstrumentationConfig() {
    return config;
  }

  @Override
  public void addInstrumentationConfigListener(
      Consumer<DeclarativeConfigProperties> listener) {
    listeners.add(listener);
  }

  void update(DeclarativeConfigProperties config) {
    this.config = config;
    listeners.forEach(listener -> listener.accept(config));
  }
}
