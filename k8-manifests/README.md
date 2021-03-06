# Kubernetes manifests

#### 🚀 &nbsp; (TLDR) Where is the manifest I can apply to my cluster!
```sh
# generate the release kubernetes manifests
skaffold render -p release > pos-quickstart.yaml

# apply the yaml files to the cluster
kubectl apply -f pos-quickstart.yaml
```

#### 🤷 &nbsp; I don't have [skaffold](https://skaffold.dev/docs/install/) installed !
```sh
# apply all the manifests from the common/ folder except the 'springprofile-h2.yaml' file
find k8-manifests/common -type f -not '(' -name '*h2.yaml' ')' | xargs -n 1 -I '{}' kubectl apply -f {}

# apply the manifest for the PoS applications
kubectl apply -f release/
```
---

This directory holds the Kubernetes manifest files that can be used to deploy
the **Point-of-Sale** application. The manifests are seperated into categories
based on the deployment environment. You will notice multiple directories _(
e.g. `dev`, `release`)_ that contains the manifest files for the same Kubernetes
resources with minor differences according to the environment. The manifest
files for resources that does not change based on the environment are found at
the root of this _(`k8-manifests`)_ directory.

- [**dev** directory:](dev/)
    - contains the manifest definition for the `Deployments` that can be used
      during local development. These manifests are specific to the [**skaffold**](https://skaffold.dev/) tool. The container image
      definitions ([1](dev/api-server.yaml#L33), [2](dev/inventory.yaml#L33)
      , [3](dev/payments.yaml#L33)) in these files match the images defined in
      the [**skaffold build context**](/skaffold.yaml#L55-L63)
      of the `skaffold.yaml` file. This ensures, during the _dev flow_, skaffold
      can push the images built from the locally available source to some
      container-image repository _(
      e.g. [Google Container Registry](https://cloud.google.com/container-registry)
      ,
      [Google Artifact Registry](https://cloud.google.com/artifact-registry))_
      and update the manifests to point to those freshly built images.  
      <br />
      > **Note:** These manifests cannot be used with `kubectl` to directly apply to
      > a cluster since they don't have a fully qualified URI for the
      > container-images. You must use `skaffold` with the **dev** profile.

      ```shell
      # Example with Google Container Registry
      skaffold dev -p dev --default-repo gcr.io/<GOOGLE_CLOUD_PROJECT>

      # Example with Google Artifact Registry
      skaffold dev -p dev --default-repo us-docker.pkg.dev/<GOOGLE_CLOUD_PROJECT>/<IMAGE_REPO> 
      ```

- [**release** directory:](release/)
    - contains the manifests that use the
      **latest released** images of the `Deployments`. The container image
      definitions ([1](release/api-server.yaml#L34)
      , [2](release/inventory.yaml#L34)
      , [3](release/payments.yaml#L34)) in these files are pinned to the most
      recent release tag. The release container-images are publicly accessible.
      Thus, these manifests can be directly applied to any cluster
      using `kubectl`.

      ```shell
      # Example with kubctl
      kubectl apply -f common/    # apply the common resources first
      kubectl apply -f release/   # apply the Deployments next

      # Example with skaffold
      skaffold dev -p release     # just point to the release profile
      ```

### Running with an embedded h2 database

The default deployment scenario for the application is to use a MySQL backend
that is running as a
separate [StatefulSet.](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/)
However, there can be scenarios where we want to deploy the application without
an external MySQL instance. To support such scenarios we have included a
separate mode where the applications can be started with an **embedded H2
database** that they connect to. This is enabled by an additional _skaffold
profile_ called **inmemory**. Switching this profile ON ensures that the MySQL
StatefulSets are not deployed and the application uses the embedded h2 database.

```shell
# Example with Google Container Registry
skaffold dev -p dev,inmemory --default-repo gcr.io/<GOOGLE_CLOUD_PROJECT>

# Example with Google Artifact Registry
skaffold dev -p dev,inmemory --default-repo us-docker.pkg.dev/<GOOGLE_CLOUD_PROJECT>/<IMAGE_REPO>
```