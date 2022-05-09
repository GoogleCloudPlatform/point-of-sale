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

def readAndUpdateYaml(yamlPath: str, containerName: str, version: str) -> Union[str, dict]:
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
        currentReleaseVersion = currentImage.split(":v")[1]
        container['image'] =  "{}:v{}".format(currentImage.split(":v")[0], version)
        return currentReleaseVersion, yDefinition

    # was unable to find the container in the yaml file
    return None, None

def updateReleaseYaml(yamlPath: str, containerName: str, version: str) -> None:
    _, yDefinition = readAndUpdateYaml(yamlPath, containerName, version)
    if yDefinition == None:
        print("Loaded yaml definiton from path [{}] is empty".format(yamlPath))
        exit(1)

    with open(yamlPath, 'w') as file:
        yaml = YAML()
        print("Updating image of container [{}] in yaml file [{}]".format(containerName, yamlPath))
        yDefinition = yaml.dump(yDefinition, file)
        file.close()

"""
We always set the version to the SNAPSHOT of next minor release. so we have
construct the next release version based on the requested release type:
    patch-release:
        for patch releases we have to keep the previous minor version but
        increment the patch identifier.
        E.g: current version on main branch: 0.5.0-SNAPSHOT
                previous release version: 0.4.7
                released version: 0.4.8
                main branch after release: 0.5.0-SNAPSHOT (unchanged)

                for patch release we have to get the previous release version
                and incrment the patch number.

    minor-release:
        since the version on the main branch will be the next minor release
        version with SNAPSHOT, we just remove the SNAPSHOT part and release
        E.g: current version on main branch: 0.5.0-SNAPSHOT
                released version: 0.5.0
                main branch after release: 0.6.0-SNAPSHOT (minor bumped)

    major-release:
        for major releases we bump the major version
            E.g: current version on main branch: 0.5.0-SNAPSHOT
                released version: 1.0.0
                main branch after release: 1.1.0-SNAPSHOT (minor bumped from release)
"""
def getVersions(sementicVersion, releaseType: str) -> Union[str, str, str]:
    currentReleaseVersion = None
    if releaseType == 'patch':
        mostRecentReleaseVersion, _ = readAndUpdateYaml(RELEASE_YAML_DIR + "api-server.yaml", "api-server", "")
        currentReleaseVersion = semver.VersionInfo.parse(mostRecentReleaseVersion).bump_patch()
        nextVersion = sementicVersion
    else:
        if releaseType == 'minor':
            currentReleaseVersion = sementicVersion.finalize_version()
        else:
            currentReleaseVersion = sementicVersion.next_version(releaseType)
        nextVersion = currentReleaseVersion.bump_minor()
        nextVersion = semver.VersionInfo(*(nextVersion.major, nextVersion.minor, nextVersion.patch, "SNAPSHOT"))

    return currentReleaseVersion, nextVersion


def main(releaseType: str, justPrint: bool, setToSnapshot: bool):
    parser = ET.XMLParser(remove_comments=False)
    currentVersion = getCurrentVersion(parser, PARENT_POM)
    sementicVersion = semver.VersionInfo.parse(currentVersion)

    currentReleaseVersion, nextVersion = getVersions(sementicVersion, releaseType)
    if justPrint:
        print(currentReleaseVersion)
        exit(0)

    print("""
        Version on main: {}
        Version released now: {}
        Updated version on main: {}""".format(sementicVersion, currentReleaseVersion, nextVersion))

    # if sementicVersion.prerelease != "SNAPSHOT":
    #     print("Root pom version is {}; Can only release from a SNAPSHOT version".format(sementicVersion))
    #     exit(0)

    # updatePackageJson(UI_PACKAGE_JSON, str(currentReleaseVersion))
    # updatePackageJson(RELEASE_PACKAGE_JSON, str(currentReleaseVersion))
    # updatePomWithNewVersion(parser, PARENT_POM, str(currentReleaseVersion), False)
    # for pom in POM_SOURCES_PATH:
    #     updatePomWithNewVersion(parser, pom, str(currentReleaseVersion), True)

    # for file in listdir(RELEASE_YAML_DIR):
    #     filePath = "{}{}".format(RELEASE_YAML_DIR, file)
    #     filaName = file.split(".")[0]
    #     updateReleaseYaml(filePath, filaName, str(currentReleaseVersion))

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