# Setting up the application for local development

You may run the application on a `Kubernetes cluster` whilst doing local
developement or run the services of the application locally as well. The steps
below assumes that the IDE used is [IntelliJ IDEA](https://www.jetbrains.com/idea/).
However, it must be possible to replicate the same setup on another IDE
_(e.g. VSCode)_ with some changes.

### Prerequisites

- [gcloud CLI](https://cloud.google.com/sdk/docs/install)
- [kubectl CLI](https://kubernetes.io/docs/tasks/tools/)
- [skaffold CLI](https://skaffold.dev/docs/install/)
- A Google Cloud Project

---
### Clone the repo locally

```sh
git clone https://github.com/GoogleCloudPlatform/point-of-sale.git
cd point-of-sale
```

### Running in a K8s cluster whilst local development

We use [**skaffold**](https://skaffold.dev) to automatically re-deploy the local
changes to the cluster as we continue to update the application.

#### Steps
- If you don't have a cluster, then create one by following the [quickstart steps](/docs/quickstart.md)
- Make sure you have the local `kubernetes context` pointing to the cluster
  where you want to deploy the application




### Stes
- **Clone this repository**
    ```sh
    git clone https://github.com/GoogleCloudPlatform/point-of-sale.git
    cd point-of-sale
    ```

- **Open the project in IntelliJ IDEA**

  This should automatically trigger the IDE to identify the [`root pom.xml`](/pom.xml)
  file and load all the maven projects under [`src`](/src/). If those projects
  were identified and properly loaded, then you should see them bolded as shown
  below:
    <p align="center">
        <img src="images/ide-loaded.png">
        <div align="center">
            <strong>(click to enlarge)</strong>
        </div>
    </p>

    IntelliJ will also identify them as `Springboot Projects` automatically and
    setup `Run configurations` for the three services: _api-server, inventory and payments_.

    <p align="center">
        <img src="images/run-config.png">
        <div align="center">
            <strong>(click to enlarge)</strong>
        </div>
    </p>

- **Set environment variables for the services**

  ssIf running the application

