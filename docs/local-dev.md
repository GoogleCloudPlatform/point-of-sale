# Setting up the application for local development

You may run the application on a `Kubernetes cluster` whilst doing local
developement or run the services of the application locally as well.

### Prerequisites

- [gcloud CLI](https://cloud.google.com/sdk/docs/install)
- [kubectl CLI](https://kubernetes.io/docs/tasks/tools/)
- [skaffold CLI](https://skaffold.dev/docs/install/)
- A Google Cloud Project
- Java **(Version 11)**
- Node **(Version >=16.13.2, <17)**

---
### Clone the repo locally

```sh
git clone https://github.com/GoogleCloudPlatform/point-of-sale.git
cd point-of-sale
```

### Options available for local development:
- [Local dev with changes continuously deployed to a Kubernetes cluster](#local-development-whilst-running-the-app-in-a-k8s-cluster)

- [Local dev with the entire application running in the local machine](#local-development-whilst-running-everything-locally)

- [Hybrid setup (specifically for UI development)](#hybrid-setup-specifically-for-ui-development)
---

### Local development whilst running the app in a K8s cluster

We use [**skaffold**](https://skaffold.dev) to automatically re-deploy the local
changes to the cluster as we continue to update the application. Skaffold
monitors for file changes locally and updates the cluster when it detects one.
This option for local development is the most easiest to setup and get started.
However, redeploying to the cluster with every change made can be a time
consuming process.

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

### Local development whilst running everything locally

When running the whole application stack locally, you have to setup the Java
based API services and the NodeJS UI application separately. The steps to configure
it is [explained here](local-dev-everything-local.md).

---

### Hybrid setup (specifically for UI development)

If you are working specifically _only on UI changes_ then it can be too time
consuming to let `skaffold` build each of your UI changes and deploy to a cluster.
One alternative would be to run the [entire application stack locally](#local-development-whilst-running-everything-locally). However, for this you
still have to setup the local environment for the API services to run eventhough
you will be making changes only to the UI code.

An alternative setup is to run your application in a Kubernetes cluster using
`skaffold` and ***run only the UI locally***. To make the local build of the UI
work with the remote cluster, we need to tweak the `local environment` config of
the UI project to point to the public IP address of the _API Server Service_.

#### Steps
- Follow the steps for the [**Local development whilst running the app in a K8s cluster**](#local-development-whilst-running-the-app-in-a-k8s-cluster) guide
- Next, follow the steps for the [**Run the **NodeJS VueJS** based UI service**](local-dev-everything-local.md#run-the-nodejs-vuejs-based-ui-service) guide

  - When following the steps here use the `Public IP Address` of the
    `API Server Service` instead of `localhost:8081`.

    ```sh
    API_SERVER_IP=$(kubectl get service/api-server-lb -o jsonpath={'.status.loadBalancer.ingress[0].ip'})

    sed "s/IP_ADDRESS/$API_SERVER_IP/g" src/ui/.env.development.sample > src/ui/.env.development.local
    ```

You may now access the application by visiting `http://localhost:8080/` in a
browser. The browser is serving the UI from the local build. But the UI code
will be using the `Public IP Address` of the `API Server Service` to hit the
service running in the cluster. This way, you only have to worry about the UI
project running locally and the rest will be running inside the cluster.

**If you type in the Public IP Address directly in the browser, then the UI that
is loaded is not from your local build; thus you will not see your changes
through that IP. You must use `http://localhost:8080/`**.