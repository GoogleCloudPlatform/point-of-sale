# Kubernetes manifests

This directory holds the Kubernetes manifest files that can be used to deploy
the **Point-of-Sale** application. The manifests are seperated into categories
based on the deployment environment. You will notice multiple directories _(
e.g. `dev`, `release`)_ that contains the manifest files for the same Kubernetes
resources with minor differences according to the environment. The manifest
files for resources that does not change based on the environment are found at
the root of this _(`k8-manifests`)_ directory.

- **dev:** Contains the manifest definition for the `Deployments` that can be
  used during local development. The container image definitions (1, 2, 3) in these files
  are specific to 
