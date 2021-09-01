#!/bin/bash

ROOT=$(pwd)
cd "$ROOT/service-sdk" || exit
mvn clean install

cd "$ROOT/ui" || exit
mvn clean install