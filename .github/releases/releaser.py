# import xml.etree.ElementTree as ET
import argparse
import semver
import lxml.etree as ET

MAVEN_POM_SCHEMA_URL = "http://maven.apache.org/POM/4.0.0"
PARENT_POM = "pom.xml"
SDK_POM = "src/service-sdk/pom.xml"
UI_POM = "src/ui/pom.xml"
API_SERVER_POM = "src/api-server/pom.xml"
INVENTORY_POM = "src/inventory/pom.xml"
PAYMENTS_POM = "src/payments/pom.xml"

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
        exit(1)
    return versionElement.text

def updatePomWithNewVersion(xmlParser, pom: str, version: str, isSubModule: bool) -> None:
    xml = ET.parse(pom, parser=xmlParser)
    if isSubModule:
        versionElement = xml.find("./{*}parent/{*}version")
    else:
        versionElement = xml.find("./{*}version")
    versionElement.text = version
    with open(pom, 'wb') as f:
        f.write(ET.tostring(xml, encoding="utf-8", pretty_print=True, xml_declaration=True))

def main(releaseType: str):
    parser = ET.XMLParser(remove_comments=False)
    currentVersion = getCurrentVersion(parser, PARENT_POM)
    sementicVersion = semver.VersionInfo.parse(currentVersion)
    if sementicVersion.prerelease != "SNAPSHOT":
        print("Root pom version is {}; Can only release from a SNAPSHOT version".format(sementicVersion))
        exit(1)

    releaseVersion = sementicVersion.finalize_version()
    nextVersion = releaseVersion.next_version(releaseType)
    nextVersionSnapshot = semver.VersionInfo(*(nextVersion.major, nextVersion.minor, nextVersion.patch, "SNAPSHOT"))

    updatePomWithNewVersion(parser, PARENT_POM, str(releaseVersion), False)
    for pom in POM_SOURCES_PATH:
        print("Updating pom file at path: {}".format(pom))
        updatePomWithNewVersion(parser, pom, str(releaseVersion), True)


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