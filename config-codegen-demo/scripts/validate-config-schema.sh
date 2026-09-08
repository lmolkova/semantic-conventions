#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DEMO_DIR=$(dirname "$SCRIPT_DIR")
CHECKOUT="$DEMO_DIR/build/opentelemetry-configuration"
GENERATED="$DEMO_DIR/generated"
MERGED="$GENERATED/merged"
CONFIG_REF=faf104bc0262fe7e84226f1a7f5db997c41ed851

if [ ! -d "$CHECKOUT/.git" ] || [ "$(git -C "$CHECKOUT" rev-parse HEAD 2>/dev/null || true)" != "$CONFIG_REF" ]; then
  rm -rf "$CHECKOUT"
  mkdir -p "$CHECKOUT"
  git -C "$CHECKOUT" init --quiet
  git -C "$CHECKOUT" remote add origin https://github.com/open-telemetry/opentelemetry-configuration.git
  git -C "$CHECKOUT" fetch --quiet --depth=1 origin "$CONFIG_REF"
  git -C "$CHECKOUT" checkout --quiet --detach FETCH_HEAD
fi

git -C "$CHECKOUT" restore --source=HEAD -- schema/instrumentation.yaml opentelemetry_configuration.json

(
  cd "$CHECKOUT"
  npm install --no-audit --no-fund
  node "$SCRIPT_DIR/merge-generated-config.mjs" "$CHECKOUT" "$GENERATED"
  npm run compile-schema
  npx --no ajv-cli compile --spec=draft2020 --allow-matching-properties \
    -s ./opentelemetry_configuration.json
)

mkdir -p "$MERGED/schema"
cp "$CHECKOUT/schema/instrumentation.yaml" "$MERGED/schema/instrumentation.yaml"
cp "$CHECKOUT/opentelemetry_configuration.json" "$MERGED/opentelemetry_configuration.json"
