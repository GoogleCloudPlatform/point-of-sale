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
def getVersions(sementicVersion, releaseType: str) -> Union[str, str, str]:
    currentReleaseVersion = None
    currentReleaseVersion = sementicVersion.finalize_version()
    if releaseType == 'minor':
        currentReleaseVersion = currentReleaseVersion.bump_minor()
    elif releaseType == 'major':
        currentReleaseVersion = currentReleaseVersion.bump_major()

    nextVersion = currentReleaseVersion.bump_patch()
    nextVersion = semver.VersionInfo(*(nextVersion.major, nextVersion.minor, nextVersion.patch, "SNAPSHOT"))
    return currentReleaseVersion, nextVersion

def updateVersions(version: str):
    updatePackageJson(UI_PACKAGE_JSON, str(version))
    updatePackageJson(RELEASE_PACKAGE_JSON, str(version))
    updatePomWithNewVersion(parser, PARENT_POM, str(version), False)
    for pom in POM_SOURCES_PATH:
        updatePomWithNewVersion(parser, pom, str(version), True)

def main(releaseType: str, justPrint: bool, setToSnapshot: bool):
    parser = ET.XMLParser(remove_comments=False)
    currentVersion = getCurrentVersion(parser, PARENT_POM)
    sementicVersion = semver.VersionInfo.parse(currentVersion)

    currentReleaseVersion, nextVersion = getVersions(sementicVersion, releaseType)
    if justPrint:
        # we print the current version and exit
        print(currentReleaseVersion)

    elif setToSnapshot:
        # this is to set the versions to the next SNAPSHOT version after the
        # release artifacts are piublished
        nextPatchV = currentReleaseVersion.bump_patch()
        snapshotVer = semver.VersionInfo(*(nextPatchV.major, nextPatchV.minor, nextPatchV.patch, "SNAPSHOT"))
        updateVersions(str(snapshotVer), releaseType)

    else:
        # set the versions to the next release version and prepare to publish
        # artifacts
        print("""
            Version on main: {}
            Version released now: {}
            Updated version on main: {}""".format(sementicVersion, currentReleaseVersion, nextVersion))

        if sementicVersion.prerelease != "SNAPSHOT":
            print("Root pom version is {}; Can only release from a SNAPSHOT version".format(sementicVersion))
            exit(0)

        updateVersions(str(snapshotVer), currentReleaseVersion)
        # update release manifests
        for file in listdir(RELEASE_YAML_DIR):
            filePath = "{}{}".format(RELEASE_YAML_DIR, file)
            filaName = file.split(".")[0]
            updateReleaseYaml(filePath, filaName, str(currentReleaseVersion))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("Release manager")
    parser.add_argument(
        "-t",
        dest='type',
        type=str,
        default="minor",
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