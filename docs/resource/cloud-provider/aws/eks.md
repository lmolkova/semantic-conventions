<!--- Hugo front matter used to generate the website version of this page:
linkTitle: EKS
--->

# AWS EKS

**Status**: [Development][DocumentStatus]

An AWS Elastic Kubernetes Service (EKS) cluster is represented by the
[`cloud`](/docs/resource/cloud.md) and
[`k8s.cluster`](/docs/registry/entities/k8s.md#k8s-cluster) entities.
These describe the AWS and Kubernetes identities of the same cluster.

For telemetry describing the EKS cluster, set `cloud.resource_id` to the cluster
ARN, `cloud.provider` to `aws`, and `cloud.platform` to `aws_eks`. Use
`k8s.cluster.name` and `k8s.cluster.uid` for the Kubernetes cluster identity.
The cluster ARN is not a replacement for `k8s.cluster.uid`.

When `cloud.resource_id` identifies another object, such as a worker node, do not
overwrite it with the cluster ARN. Kubernetes cluster attributes can accompany
the workload or node attributes to identify the associated cluster.

The `aws.eks` entity has been removed. The `aws.eks.cluster.arn` attribute is
[deprecated](/docs/registry/attributes/aws.md#deprecated-amazon-eks-attributes).

[DocumentStatus]: https://opentelemetry.io/docs/specs/otel/document-status/
