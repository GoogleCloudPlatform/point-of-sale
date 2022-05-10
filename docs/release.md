# Releasing a new version of the Point of Sale Application

All releases of the sample application are handled using Cloud Build triggers
set up in the [`point-of-sale-ci`](https://console.cloud.google.com/cloud-build/triggers;region=global?project=point-of-sale-ci) GCP project. We use the custom written [releaser.py](/.github/releases/releaser.py) script for updating the configuration files with the correct
versions during the release process. The release process includes **three stages**:
1. Bumping the version in files _(pom.xml, package.json, Kubernetes manifests)_
   to the required release version
2. Publishing the release artifacts _(jar files, container images)_ to a central
   repository
3. Bumping the version in files _(pom.xml, package.json, Kubernetes manifests)_
   to the next release version `SNAPSHOT`
---
### How to carry-out a release

- Checkout the repo locally
  
- Create a new branch which has the following name format: `release-<type>`. The
  `type` can be one of: ***patch***, ***minor***, ***major*** _(e.g. release-patch, release-minor)_
- Update the `CHANGELOG.md` for the new release
    ```sh
    # run from the root of the repo
    npm install && npm run release
    ```
    The above will update the [`CHANGELOG.md`](/CHANGELOG.md) file with _release notes_
    for the current release, based on commit messages. Inspect the [`CHANGELOG.md`](/CHANGELOG.md)
    file and adjust the auto-generated notes as suitable.
- Create a **pull request** from the release branch _(e.g. `release-patch`)_ to
  the **main** branch.
- This will automatically trigger the release steps defined in the [`pos-pr-open-for-release`](.github/cloudbuild/pos-pr-open-for-release.yaml) file:
  - Trigger updates the files _(pom.xml & package.json)_ with the next release version using
    the [release script](/.github/releases/releaser.py). The type of release is deduced from the
    **branch name**.
  - Trigger then, pushes the changes to the branch and adds a comment to the _pull request_ with the next steps.
  <p>
    <img src="/docs/images/release1.png">
  </p>
- Follow the instructions on the _pull-request_ comment and trigger the `Cloud Build`
  trigger backed by the [`pos-publish-release-artifacts`](/.github/cloudbuild/pos-publish-release-artifacts.yaml) file. You have to manually trigger this in
  GCP console. Make sure, you run the trigger against the branch you create.
  <p>
    <img src="/docs/images/trigger.png">
  </p>

    - Trigger builds and deploys the ***Maven Jars*** for the sample application
      to the [`pos-jars`](https://console.cloud.google.com/artifacts/maven/point-of-sale-ci/us/pos-jars?project=point-of-sale-ci) repository.
    - Trigger builds and deploys the ***container images*** for the sample application
      to the [`pos-images`](https://pantheon.corp.google.com/artifacts/docker/point-of-sale-ci/us/pos-images?project=point-of-sale-ci) repository.
    - Trigger creates a [***Git Tag***](https://github.com/GoogleCloudPlatform/point-of-sale/tags) and a [**draft** Github release](https://github.com/GoogleCloudPlatform/point-of-sale/releases) which you can inspect and publish later.
    - Trigger updates the versions in the config files to the SNAPSHOT version of
      the next patch release.
    - Trigger pushes the changes to the branch and adds a comment to the _pull request_ with a
      status update.
    <p>
      <img src="/docs/images/release1.png">
    </p>
- Inspect the changes in the PR.
- Merge the PR into **main**.
- Publish the [**draft** Github release](https://github.com/GoogleCloudPlatform/point-of-sale/releases).
- Finally, run the [`pos-deploy-release`](/.github/cloudbuild/pos-deploy-release.yaml) trigger
  _(on the main branch)_ to deploy the latest released version of the application
  to the main Kubernetes cluster.

---
### Versioning
The versioning in this repo follows the [semantic versioning](https://semver.org/)
guide. Thus, the release process supports triggering `major`, `minor` or `patch`
releases. The configuration files _(pom.xml & package.json)_ in the **main**
branch will always have the **SNAPSHOT** version of the upcoming **patch**
release version. The [release script](/.github/releases/releaser.py) relies on
this to decide the release version during the time of the release.

_The following examples shows how the versions will change for each type of releases._
| **Example** _(state of the repo before release)_    |   |
|---                                                    |---|
| _Most recent release version_                         | 4.12.3           |
| _Version in the main branch_                          | 4.12.4-SNAPSHOT  |


| Triggered release type    | Released version | **"main"** updated to  |
|---                        |---               |---                     |
| _patch_                   | 4.12.4           | 4.12.5-SNAPSHOT  |
| _minor_                   | 4.13.0           | 4.13.1-SNAPSHOT  |
| _major_                   | 5.0.0            | 5.0.1-SNAPSHOT  |