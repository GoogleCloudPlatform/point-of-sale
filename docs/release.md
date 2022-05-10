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
- This will automatically trigger the release steps defined in the [`pos-pr-open-for-release`](.github/cloudbuild/pos-pr-open-for-release.yaml) file. This trigger:
  - Updates the files _(pom.xml & package.json)_ with the next release version using
    the [release script](/.github/releases/releaser.py). The type of release is deduced from the
    **branch name**.
  - Pushes the changes to the branch and adds a comment to the pull request with the next steps.
    <p align="center">
    <img src="/docs/images/release1.png">
  </p>
  