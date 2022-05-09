import json
import semver
import argparse
from os import listdir
import lxml.etree as ET
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
    print("Updating pom file at path: {}".format(pom))
    xml = ET.parse(pom, parser=xmlParser)
    if isSubModule:
        versionElement = xml.find("./{*}parent/{*}version")
    else:
        versionElement = xml.find("./{*}version")
    versionElement.text = version
    with open(pom, 'wb') as f:
        f.write(ET.tostring(xml, encoding="utf-8", pretty_print=True, xml_declaration=True))

def updatePackageJson(path: str, version: str) -> None:
    print("Updating package.json file at path: {}".format(path))
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
        print("Loaded yaml definiton from path {} is empty".format(yamlPath))
        exit(1)

    updatedImage = None
    containers = yDefinition['spec']['template']['spec']['containers']
    for con in containers:
        if con['name'] != containerName:
            continue;
        currentImage = con['image']
        updatedImage = "{}:v{}".format(currentImage.split(":")[0], version)
        con['image'] = updatedImage
        break;

    if updatedImage == None:
        print("Could not find image definition for container [{}] at yaml path [{}]".format(containerName, yamlPath))
        exit(1)

    with open(yamlPath, 'w') as file:
        yaml = YAML()
        print("Updating image to [{}] for yaml file at [{}]".format(updatedImage, yamlPath))
        yDefinition = yaml.dump(yDefinition, file)
        file.close()

def main(releaseType: str, justPrint: bool):
    parser = ET.XMLParser(remove_comments=False)
    currentVersion = getCurrentVersion(parser, PARENT_POM)
    sementicVersion = semver.VersionInfo.parse(currentVersion)
    releaseVersion = sementicVersion.finalize_version()
    # nextVersion = releaseVersion.next_version(releaseType)
    # nextVersionSnapshot = semver.VersionInfo(*(nextVersion.major, nextVersion.minor, nextVersion.patch, "SNAPSHOT"))

    if justPrint:
        print(releaseVersion)
        exit(0)

    if sementicVersion.prerelease != "SNAPSHOT":
        print("Root pom version is {}; Can only release from a SNAPSHOT version".format(sementicVersion))
        exit(0)

    for file in listdir(RELEASE_YAML_DIR):
        filePath = "{}{}".format(RELEASE_YAML_DIR, file)
        filaName = file.split(".")[0]
        updateReleaseYaml(filePath, filaName, str(releaseVersion))
    # updatePackageJson(UI_PACKAGE_JSON, str(releaseVersion))
    # updatePackageJson(RELEASE_PACKAGE_JSON, str(releaseVersion))
    # updatePomWithNewVersion(parser, PARENT_POM, str(releaseVersion), False)
    # for pom in POM_SOURCES_PATH:
    #     updatePomWithNewVersion(parser, pom, str(releaseVersion), True)

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
        "-p",
        dest='print',
        type=bool,
        default=False,
        help="Just print the new version")
    args = parser.parse_args()
    main(args.type, args.print)