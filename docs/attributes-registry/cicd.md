<!--- Hugo front matter used to generate the website version of this page:
--->

<!-- NOTE: THIS FILE IS AUTOGENERATED. DO NOT EDIT BY HAND. -->
<!-- see templates/registry/markdown/attribute_namespace.md.j2 -->

# CICD

## CI/CD Pipeline Attributes

This group describes attributes specific to pipelines within a Continuous Integration and Continuous Deployment (CI/CD) system. A [pipeline](https://wikipedia.org/wiki/Pipeline_(computing)) in this case is a series of steps that are performed in order to deliver a new version of software. This aligns with the [Britannica](https://www.britannica.com/dictionary/pipeline) definition of a pipeline where a **pipeline** is the system for developing and producing something. In the context of CI/CD, a pipeline produces or delivers software.

| Attribute | Type | Description | Examples | Stability |
|---|---|---|---|---|
| <a id="cicd-pipeline-name" href="#cicd-pipeline-name">`cicd.pipeline.name`</a> | string | The human readable name of the pipeline within a CI/CD system. | `Build and Test`; `Lint`; `Deploy Go Project`; `deploy_to_environment` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| <a id="cicd-pipeline-run-id" href="#cicd-pipeline-run-id">`cicd.pipeline.run.id`</a> | string | The unique identifier of a pipeline run within a CI/CD system. | `120912` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| <a id="cicd-pipeline-task-name" href="#cicd-pipeline-task-name">`cicd.pipeline.task.name`</a> | string | The human readable name of a task within a pipeline. Task here most closely aligns with a [computing process](https://wikipedia.org/wiki/Pipeline_(computing)) in a pipeline. Other terms for tasks include commands, steps, and procedures. | `Run GoLang Linter`; `Go Build`; `go-test`; `deploy_binary` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| <a id="cicd-pipeline-task-run-id" href="#cicd-pipeline-task-run-id">`cicd.pipeline.task.run.id`</a> | string | The unique identifier of a task run within a pipeline. | `12097` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| <a id="cicd-pipeline-task-run-url-full" href="#cicd-pipeline-task-run-url-full">`cicd.pipeline.task.run.url.full`</a> | string | The [URL](https://wikipedia.org/wiki/URL) of the pipeline run providing the complete address in order to locate and identify the pipeline run. | `https://github.com/open-telemetry/semantic-conventions/actions/runs/9753949763/job/26920038674?pr=1075` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| <a id="cicd-pipeline-task-type" href="#cicd-pipeline-task-type">`cicd.pipeline.task.type`</a> | string | The type of the task within a pipeline. | `build`; `test`; `deploy` | ![Experimental](https://img.shields.io/badge/-experimental-blue) |

---

`cicd.pipeline.task.type` has the following list of well-known values. If one of them applies, then the respective value MUST be used; otherwise, a custom value MAY be used.

| Value  | Description | Stability |
|---|---|---|
| `build` | build | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `deploy` | deploy | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
| `test` | test | ![Experimental](https://img.shields.io/badge/-experimental-blue) |
