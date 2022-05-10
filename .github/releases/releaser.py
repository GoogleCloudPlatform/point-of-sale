import json
import semver
import argparse
import lxml.etree as ET

from os import listdir
from typing import Union
from ruamel.yaml import YAML


MAVEN_POM_SCHEMA_URL = "http://maven.apache.org/POM/4.0.0"
PARENT_POM = "pom.xml"
SDK_POM = "src/service-sdk/pom.xml"
UI_POM = "src/ui/pom.xml"
API_SERVER_POM = "src/api-server/pom.xml"
INVENTORY_POM = "src/inventory/pom.xml"
PAYMENTS_POM = "src/payments/pom.xml"
UI_PACKAGE_JSON = "src/ui/package.json"
RELEASE_PACKAGE_JSON = "package.json"
RELEASE_YAML_DIR = "k8-manifests/release/"

POM_SOURCES_PATH = [
    SDK_POM,
    UI_POM,
    API_SERVER_POM,
    INVENTORY_POM,
    PAYMENTS_POM
]

def getCurrentVersion(xmlParser, pom: str) -> str:
    xml = ET.parse(pom, parser=xmlParser)
    versionElement = xml.find("./{*}version")
    if versionElement == None:
        print("pom.xml does not appear to have a <version> tag")
        exit(0)
    return versionElement.text

def updatePomWithNewVersion(xmlParser, pom: str, version: str, isSubModule: bool) -> None:
    print("Updating pom file at path [{}]".format(pom))
    xml = ET.parse(pom, parser=xmlParser)
    if isSubModule:
        versionElement = xml.find("./{*}parent/{*}version")
    else:
        versionElement = xml.find("./{*}version")
    versionElement.text = version
    with open(pom, 'wb') as f:
        f.write(ET.tostring(xml, encoding="utf-8", pretty_print=True, xml_declaration=True))

def updatePackageJson(path: str, version: str) -> None:
    print("Updating package.json file at path [{}]".format(path))
    with open(path, "r+") as packageJson:
        data = json.load(packageJson)
        data["version"] = version
        packageJson.seek(0)
        json.dump(data, packageJson, indent=2)
        packageJson.truncate()

"""
Utility method that takes in a path for a Deployment resource yaml file and
updates the container image version to the one provided.
"""
def updateReleaseYaml(yamlPath: str, containerName: str, version: str) -> None:
    yDefinition = {}
    with open(yamlPath, 'r') as file:
        yaml = YAML()
        yDefinition = yaml.load(file)
        file.close()

    if len(yDefinition) == 0:
        print("Loaded yaml definiton from path [{}] is empty".format(yamlPath))
        exit(1)

    containers = yDefinition['spec']['template']['spec']['containers']
    for container in containers:
        if container['name'] != containerName:
            continue;
        currentImage = container['image']
        container['image'] =  "{}:v{}".format(currentImage.split(":v")[0], version)
        break

    with open(yamlPath, 'w') as file:
        yaml = YAML()
        print("Updating image of container [{}] in yaml file [{}]".format(containerName, yamlPath))
        yDefinition = yaml.dump(yDefinition, file)
        file.close()

"""
We always set the version to the SNAPSHOT of next patch release. so we have
construct the next release version based on the requested release type:
    patch-release:
        for patch releases we have to keep the previous minor version but
        increment the patch identifier.
        E.g: current version on main branch: 0.5.3-SNAPSHOT
                released version: 0.5.3
                main branch after release: 0.5.4-SNAPSHOT (patch bumped)

    minor-release:
        for minor releases we bump minor and set patch to 0 for the release
        then we bump patch by one for the next release version
        E.g: current version on main branch: 0.5.3-SNAPSHOT
                released version: 0.6.0
                main branch after release: 0.6.1-SNAPSHOT (patch bumped from release)

    major-release:
        for major releases we bump the major version and set both minor/patch
        to 0 for the release; then we bump patch by one for the next release version
            E.g: current version on main branch: 0.5.3-SNAPSHOT
                released version: 1.0.0
                main branch after release: 1.0.1-SNAPSHOT (patch bumped from release)
"""
def getReleaseVersions(sementicVersion, releaseType: str) -> Union[str, str]:
    currentReleaseVersion = None
    currentReleaseVersion = sementicVersion.finalize_version()
    if releaseType == 'minor':
        currentReleaseVersion = currentReleaseVersion.bump_minor()
    elif releaseType == 'major':
        currentReleaseVersion = currentReleaseVersion.bump_major()

    nextVersion = currentReleaseVersion.bump_patch()
    nextVersion = semver.VersionInfo(*(nextVersion.major, nextVersion.minor, nextVersion.patch, "SNAPSHOT"))
    return currentReleaseVersion, nextVersion

"""
A utility method that takes in the parser and the new version string, and calls
the necessary methods to update each of the different types of versioned files
(pom.xml and package.json)
"""
def updateVersions(parser, version: str):
    updatePackageJson(UI_PACKAGE_JSON, version)
    updatePackageJson(RELEASE_PACKAGE_JSON, version)
    updatePomWithNewVersion(parser, PARENT_POM, version, False)
    for pom in POM_SOURCES_PATH:
        updatePomWithNewVersion(parser, pom, version, True)


"""
The main method reads the current version on the parent pom at the room of the
repository. Then based on the provided flags it does one of three things:

- p flag used: just print the current version and ecit
- s flag used: update the versions of all the files to the next SNAPSHOT version
- no flag used: update the versions of all the files to the next release version
                based on the release type flag. If no release type flag is
                provided then it is deemed as a patch release by default.
"""
def main(releaseType: str, justPrint: bool, setToSnapshot: bool):
    parser = ET.XMLParser(remove_comments=False)
    currentVersion = getCurrentVersion(parser, PARENT_POM)
    sementicVersion = semver.VersionInfo.parse(currentVersion)

    currentReleaseVersion, nextVersion = getReleaseVersions(sementicVersion, releaseType)
    if justPrint:
        # if the [-p True] flag is used we print the current version and exit
        print(sementicVersion)

    elif setToSnapshot:
        # if the [-s True] flag is used then the ask is to set the versions to
        # the next SNAPSHOT version after the release artifacts are published
        # we ensure that the current version is NOT a SNAPSHOT version and then
        # update it the next patch version

        if sementicVersion.prerelease == "SNAPSHOT":
            print("Root pom version is {}; It is already on SNAPSHOT version".format(sementicVersion))
            exit(1)
        nextPatchV = sementicVersion.bump_patch()
        snapshotVer = semver.VersionInfo(*(nextPatchV.major, nextPatchV.minor, nextPatchV.patch, "SNAPSHOT"))
        updateVersions(parser, str(snapshotVer))

    else:
        # if none of the two flags [-p True and -s True] are set then the ask is
        # to set the versions to the next release version and prepare to publish
        # artifacts
        print("""
            Version on main: {}
            Version released now: {}
            Updated version on main: {}""".format(sementicVersion, currentReleaseVersion, nextVersion))

        if sementicVersion.prerelease != "SNAPSHOT":
            print("Root pom version is {}; Can only release from a SNAPSHOT version".format(sementicVersion))
            exit(1)

        updateVersions(parser, str(currentReleaseVersion))
        # update release manifests
        for file in listdir(RELEASE_YAML_DIR):
            filePath = "{}{}".format(RELEASE_YAML_DIR, file)
            filaName = file.split(".")[0]
            updateReleaseYaml(filePath, filaName, str(currentReleaseVersion))

"""
Starting point of the releaser script
"""
if __name__ == "__main__":
    parser = argparse.ArgumentParser("Release manager")
    parser.add_argument(
        "-t",
        dest='type',
        type=str,
        default="patch",
        choices=['major', 'minor', 'patch'],
        help="The sementic version type to bump")
    parser.add_argument(
        "-s",
        dest='snapshot',
        type=bool,
        default=False,
        help="Set all versions to next SNAPSHOT; Don't update release manifests")
    parser.add_argument(
        "-p",
        dest='print',
        type=bool,
        default=False,
        help="Just print the new version")
    args = parser.parse_args()
    main(args.type, args.print, args.snapshot)