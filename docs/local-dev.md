# Setting up the application for local development

The steps below assumes that the IDE used is [IntelliJ IDEA](https://www.jetbrains.com/idea/).
However, it is possible to replicate the same setup on another IDE _(e.g. VSCode)_.

### Prerequisites

- [gcloud CLI](https://cloud.google.com/sdk/docs/install)
- [kubectl CLI](https://kubernetes.io/docs/tasks/tools/)
- [skaffold CLI](https://skaffold.dev/docs/install/)
- A Google Cloud Project

---

### Steps

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
  