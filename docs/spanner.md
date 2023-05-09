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

#### Try with Spanner without Terraform

If you'd like to experiment with the Spanner based solution independant of the
Terraform scripts in the
[GoogleCloudPlatform/terraform-example-java-dynamic-point-of-sale](https://github.com/GoogleCloudPlatform/terraform-example-java-dynamic-point-of-sale)
repository, then you may follow these steps:

- Create a [Cloud Spanner](https://cloud.google.com/spanner/docs/quickstart-console) instance
- Create a database for the application inside the Spanner instance
- Follow the quickstart guide to get the application deployed with the following
  changes:
  - Use branch `spanner`
  - Generate Yaml files with the spring profile `jss`
    ```
    skaffold render -p jss > pos-quickstart.yaml
    ```
  - Update the values for the following variables in the
