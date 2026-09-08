import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";

const [configRoot, generatedRoot] = process.argv.slice(2);
if (!configRoot || !generatedRoot) {
  throw new Error("usage: merge-generated-config.mjs <config-root> <generated-root>");
}

const require = createRequire(path.join(configRoot, "package.json"));
const yaml = require("yaml");
const targetPath = path.join(configRoot, "schema", "instrumentation.yaml");
const target = yaml.parse(fs.readFileSync(targetPath, "utf8"));

for (const name of fs.readdirSync(generatedRoot).sort()) {
  if (!name.startsWith("instrumentation-") || !name.endsWith(".yaml")) {
    continue;
  }
  const generated = yaml.parse(fs.readFileSync(path.join(generatedRoot, name), "utf8"));
  for (const [typeName, definition] of Object.entries(generated.$defs ?? {})) {
    const existing = target.$defs[typeName];
    if (!existing) {
      target.$defs[typeName] = definition;
      continue;
    }
    if (existing.type !== definition.type) {
      throw new Error(`${typeName} has incompatible generated and configuration types`);
    }
    if (existing.additionalProperties !== definition.additionalProperties) {
      throw new Error(`${typeName} has incompatible additionalProperties rules`);
    }
    existing.properties = { ...existing.properties, ...definition.properties };
  }
}

fs.writeFileSync(targetPath, yaml.stringify(target, { lineWidth: 0 }));
