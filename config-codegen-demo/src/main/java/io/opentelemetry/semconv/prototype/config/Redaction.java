package io.opentelemetry.semconv.prototype.config;

import java.util.List;

public final class Redaction {

  private Redaction() {}

  /** Replaces the values of {@code sensitiveKeys} in a `&`-delimited query string. */
  public static String redactQueryParameters(
      String query, List<String> sensitiveKeys, String replacement) {
    if (query == null || query.isEmpty() || sensitiveKeys.isEmpty()) {
      return query;
    }
    StringBuilder redacted = new StringBuilder(query.length());
    for (String parameter : query.split("&", -1)) {
      if (redacted.length() > 0) {
        redacted.append('&');
      }
      int separator = parameter.indexOf('=');
      String key = separator < 0 ? parameter : parameter.substring(0, separator);
      if (separator >= 0 && sensitiveKeys.contains(key)) {
        redacted.append(key).append('=').append(replacement);
      } else {
        redacted.append(parameter);
      }
    }
    return redacted.toString();
  }
}
