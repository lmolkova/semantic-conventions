package io.opentelemetry.semconv.prototype.config;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Config {

  private Config() {}

  public static DeclarativeConfigProperties instrumentation(ConfigProvider configProvider) {
    DeclarativeConfigProperties config =
        configProvider == null ? null : configProvider.getInstrumentationConfig();
    return config == null ? DeclarativeConfigProperties.empty() : config;
  }

  public static void onInstrumentationChange(
      ConfigProvider configProvider, Consumer<DeclarativeConfigProperties> listener) {
    if (configProvider instanceof DynamicConfigProvider) {
      ((DynamicConfigProvider) configProvider)
          .addInstrumentationConfigListener(
              config ->
                  listener.accept(
                      config == null ? DeclarativeConfigProperties.empty() : config));
    }
  }

  public static DeclarativeConfigProperties at(DeclarativeConfigProperties root, String path) {
    DeclarativeConfigProperties current = root == null ? DeclarativeConfigProperties.empty() : root;
    for (String segment : path.split("\\.")) {
      current = current.getStructured(segment, DeclarativeConfigProperties.empty());
    }
    return current;
  }

  /**
   * Resolves {@code key} against {@code scopes} in order, so a signal-scoped value takes precedence
   * over the wider scope a property declares.
   */
  public static DeclarativeConfigProperties resolve(
      DeclarativeConfigProperties root, String key, String... scopes) {
    for (String scope : scopes) {
      DeclarativeConfigProperties properties = at(root, scope);
      if (properties.getPropertyKeys().contains(key)) {
        return properties;
      }
    }
    return DeclarativeConfigProperties.empty();
  }

  public static boolean experimental(DeclarativeConfigProperties root, String domain) {
    return at(root, "general." + domain + ".semconv").getBoolean("experimental", false);
  }

  public static List<String> stringList(
      DeclarativeConfigProperties properties, String key, List<String> defaultValue) {
    return List.copyOf(properties.getScalarList(key, String.class, defaultValue));
  }

  public static Map<String, String> stringMap(
      List<DeclarativeConfigProperties> entries, String keyProperty, String valueProperty) {
    Map<String, String> result = new LinkedHashMap<>();
    for (DeclarativeConfigProperties entry : entries) {
      String key = entry.getString(keyProperty);
      String value = entry.getString(valueProperty);
      if (key != null && value != null) {
        result.put(key, value);
      }
    }
    return Map.copyOf(result);
  }
}
