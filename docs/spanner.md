## Deploy with Cloud Spanner as backend

You can deploy this application with [Google Cloud Spanner](https://cloud.google.com/spanner)
as the database. However, this setup requires code changes which is different
from using a MySQL or H2 database backend. Thus, we maintain the Spanner based
solution in a seperate branch called `spanner`. These changes may in future get
merged into the `main` branch where we have all three deployment options
_(MySQL, H2 In-memory and Spanner)_ within a same bundle.

The Spanner based deployment of this application is used in the
_"Dynamic webapp application using Java"_,
[Jump Start Solution](https://pantheon.corp.google.com/products/solutions/catalog)
. This solution is an example of how to configure the Google Cloud
infrastructure to deploy a Java webapp into a
[Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine) cluster.

You can try this solution either in the Google Cloud console through the Jump
Start Solutions [catalog](https://pantheon.corp.google.com/products/solutions/catalog)
or using Terraform through the [solution's Github repository](https://github.com/GoogleCloudPlatform/terraform-example-java-dynamic-point-of-sale). That
repository contains the infrastructure setuo for the spanner based deployment as
code (Terraform). You can try it and tweak it to suit your own needs.

---

### Try with Spanner without Terraform

If you'd like to experiment with the Spanner based solution independant of the
Terraform scripts in the
[GoogleCloudPlatform/terraform-example-java-dynamic-point-of-sale](https://github.com/GoogleCloudPlatform/terraform-example-java-dynamic-point-of-sale)
repository, then you may follow these steps:

#### Spanner setup
- Create a
  [Cloud Spanner](https://cloud.google.com/spanner/docs/quickstart-console)
  instance
- Create a database for the application inside the Spanner instance
- Apply the
  [schema for this application](https://github.com/GoogleCloudPlatform/point-of-sale/blob/spanner/extras/spanner/pos_db.sql) into the created database
  _(you can apply via cloud console)_

#### Workload identity setup
- Create a [new GKE cluster with Workload Identity](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity)
- Create a Kubernetes Service Account called `pos-wi`
  ```bash
  kubectl create serviceaccount pos-wi \
        --namespace default
  ```
- Create a new Google Cloud Service Account called `pos-wi`
  ```bash
  gcloud iam service-accounts create pos-wi \
        --project=<REPLACE_PROJECT_ID>
  ```
- Apply the `spanner.databaseUser` role to the Google Cloud Service Account
  ```bash
  export PROJECT_ID=<REPLACE_PROJECT_ID>

  gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member "serviceAccount:pos-wi@$PROJECT_ID.iam.gserviceaccount.com" \
    --role "roles/spanner.databaseUser"
  ```
- Associate the Kubernetes Service Account principal to the Google Service Account
  ```bash
  export PROJECT_ID=<REPLACE_PROJECT_ID>

  gcloud iam service-accounts add-iam-policy-binding                  \
    pos-wi@$PROJECT_ID.iam.gserviceaccount.com                        \
    --role "roles/iam.workloadIdentityUser"                           \
    --member "serviceAccount:$PROJECT_ID.svc.id.goog[default/pos-wi]" \
    --project $PROJECT_ID
  ```
- Annotate the Kubernetes Service Account to use the Google Service Account
  ```bash
  kubectl annotate serviceaccount pos-wi --namespace default
    iam.gke.io/gcp-service-account=pos-wi@shabir-jss-22-test.iam.gserviceaccount.com
  ```

#### Deploying the application
- Use branch `spanner` of this repository
- Generate the Kubernetes manifests for the application
  ```bash
  skaffold render -p release > pos-quickstart.yaml
  ```
- Update the values for the following variables in the generated
  `pos-quickstart.yaml` file
  ```bash
  # file: k8-manifests/common/service-configs.yaml
  PROJECT_ID: "UPDATE_ME"
  SPANNER_ID: "UPDATE_ME"
  SPANNER_DATABASE: "UPDATE_ME"
  ```
- Set the `springprofile` to `jss` inside the `pos-quickstart.yaml` file
  ```bash
  SPRING_PROFILES_ACTIVE: jss
  ```
- Apply the Yaml manifest to the GKE cluster.

---
### Try with Spanner emulator

- Follow the steps [here](https://cloud.google.com/spanner/docs/emulator) to
  setup the emulator
- Follow the steps for the
  [local environment development](docs/local-dev-everything-local.md)
  with the following changes:
  - Update the following spring settings inside the `application-jss.properties`
    file of the `inventory` and `payments` microservices:
    ```bash
    spring.cloud.gcp.spanner.emulator.enabled=false
    spring.cloud.gcp.spanner.emulator-host=${SPANNER_EMULATOR_URL}
    ```