from xml.etree import ElementTree as et
import argparse
import semver

MAVEN_POM_SCHEMA_URL = "http://maven.apache.org/POM/4.0.0"
PARENT_POM = "pom.xml"
SDK_POM = "src/service-sdk/pom.xml"
UI_POM = "src/ui/pom.xml"
API_SERVER_POM = "src/api-server/pom.xml"
INVENTORY_POM = "src/inventory/pom.xml"
PAYMENTS_POM = "src/payments/pom.xml"

POM_SOURCES_PATH = [
    PARENT_POM,
    SDK_POM,
    UI_POM,
    API_SERVER_POM,
    INVENTORY_POM,
    PAYMENTS_POM
]

def getCurrentVersion(xmlParser: et.ElementTree, pom: str) -> str:
    xmlParser.parse(pom)
    versionPointer = xmlParser.getroot().find("{%s}version" % MAVEN_POM_SCHEMA_URL)
    return versionPointer.text

def updatePomWithNewVersion(xmlParser: et.ElementTree, pom: str, version: str) -> None:
    xmlParser.parse(pom)
    versionPointer = xmlParser.getroot().find("{%s}version" % MAVEN_POM_SCHEMA_URL)
    versionPointer.text = version
    xmlParser.write(pom)

def main(releaseType: str):
    et.register_namespace('', MAVEN_POM_SCHEMA_URL)
    xmlParser = et.ElementTree()
    currentVersion = getCurrentVersion(xmlParser, PARENT_POM)
    sementicVersion = semver.VersionInfo.parse(currentVersion)
    if sementicVersion.prerelease != "SNAPSHOT":
        print("Root pom version is {}; Can only release from a SNAPSHOT version".format(sementicVersion))
        exit(1)

    releaseVersion = sementicVersion.finalize_version()
    nextVersion = releaseVersion.next_version(releaseType)
    nextVersionSnapshot = semver.VersionInfo(*(nextVersion.major, nextVersion.minor, nextVersion.patch, "SNAPSHOT"))

    for pom in POM_SOURCES_PATH:
        print("Updating pom file at path: {}\n".format(pom))
        updatePomWithNewVersion(xmlParser, pom, str(releaseVersion))


if __name__ == "__main__":
    parser = argparse.ArgumentParser("Release manager")
    parser.add_argument(
        "-t",
        dest='type',
        type=str,
        default="minor",
        choices=['major', 'minor', 'patch'],
        help="The sementic version type to bump")
    args = parser.parse_args()
    main(args.type)