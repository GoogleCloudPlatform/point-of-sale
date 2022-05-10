# Setting up the application for local development

You may run the application on a `Kubernetes cluster` whilst doing local
developement or run the services of the application locally as well.

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

### There are two ways to do local development
- [Local dev with changes continuously deployed to a Kubernetes cluster](#running-in-a-k8s-cluster-whilst-local-development)

- [Local dev with the entire application running in the local machine](#running-everything-locally-whilst-local-development)
  - [Setting up the API services](#setting-up-the-api-services)

  - [Setting up the UI project](#setting-up-the-ui-project)

---

### Running in a K8s cluster whilst local development

We use [**skaffold**](https://skaffold.dev) to automatically re-deploy the local
changes to the cluster as we continue to update the application.

#### Steps
- If you don't have a cluster, then create one by following the [quickstart steps](/docs/quickstart.md)
- Make sure you have the local `kubernetes context` pointing to the cluster
  where you want to deploy the application
- Run `skaffold dev` from the root directory of the repo
  ```sh
  # Example with Google Container Registry
  skaffold dev -p dev --default-repo gcr.io/<GOOGLE_CLOUD_PROJECT>

  # if you want to use the in memory db instead of deploying a MySQL container
  skaffold dev -p dev,inmemory --default-repo gcr.io/<GOOGLE_CLOUD_PROJECT>
  ```
- In a separate terminal window, get the public IP address of the _api-server_
  service and try accesing the application.
  ```sh
  kubectl get service/api-server-lb -o jsonpath={'.status.loadBalancer.ingress[0].ip'}
  ```
  ```sh
  # expected output (you will see a different IP address)
  35.238.98.46
  ```
- Whilst skaffold is running, you can open the project in any IDE of your choice and
  make changes. The changes will be automatically built and deployed to the
  cluster. You can also view logs from your applications in the terminal window
  where skaffold was run.
---

### Running everything locally whilst local development

#### Setting up the API services

The steps below assumes that the IDE used is [IntelliJ IDEA](https://www.jetbrains.com/idea/).
However, it must be possible to replicate the same setup on another IDE
_(e.g. VSCode)_ with some changes.

- **Open the cloned project in IntelliJ IDEA**

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

  To be able to run the services locally we must update the `Run configurations`
  with some additional parameters. These values are passed in to the services as
  `Config Map Values` in the K8s cluster based setup.

  - Click on `Edit Configurations` _(see image above)_ from the top right menu
  - In the window that pops up, set the following `Environment Variables` for
    the three different services. If you don't see an option to fill in
    environment variables, you can enable it from the `Modify options` dropdown.

    <p align="center">
        <img src="images/ide2.png">
        <div align="center">
            <strong>(click to enlarge)</strong>
        </div>
    </p>

    | Service               | Variable Key      | Variable Value            |
    |---------              |--------------     |----------------           |
    | ApiServerApplication  | server.port       | 8081                      |
    | ApiServerApplication  | INVENTORY_EP      | http://localhost:8082     |
    | ApiServerApplication  | PAYMENTS_EP       | http://localhost:8083     |
    | PaymentsApplication   | server.port       | 8083                      |
    | InventoryApplication  | server.port       | 8082                      |
    | InventoryApplication  | ACTIVE_ITEM_TYPE  | burgers                   |
    | InventoryApplication  | ITEMS             | items:\n  - name: "BigBurger"\n    type: "burgers"\n    price: 5.50\n    imageUrl: "usr/lib/images/bigburger.png"\n    quantity: 200\n    labels: [ "retail", "restaurant", "food" ]\n  - name: "DoubleBurger"\n    type: "burgers"\n    price: 7.20\n    imageUrl: "usr/lib/images/burgers.png"\n    quantity: 200\n    labels: [ "retail", "restaurant", "food" ]\n  - name: "Shirt"\n    type: "textile"\n    price: 15.50\n    imageUrl: "usr/lib/images/shirt.png"\n    quantity: 50\n    labels: [ "retail", "textile", "clothing" ]\n  - name: "Short"\n    type: "textile"\n    price: 17.20\n    imageUrl: "usr/lib/images/short.png"\n    quantity: 20\n    labels: [ "retail", "textile", "clothing" ]                  |

- **Run all three applications from the IDE**
    <p align="center">
        <img src="images/run-app.png">
        <div align="center">
            <strong>(click to enlarge)</strong>
        </div>
    </p>

#### Setting up the UI project