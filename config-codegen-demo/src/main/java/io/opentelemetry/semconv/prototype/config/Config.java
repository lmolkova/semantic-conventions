package io.opentelemetry.semconv.prototype.config;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;

public final class Config {

  private Config() {}

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
}
