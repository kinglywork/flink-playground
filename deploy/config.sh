#!/bin/bash
set -eufo pipefail

MAJOR_VERSION="1"
MINOR_VERSION="0"

# VERSIONED VARIABLES

JAR_ID="${BUILDKITE_BUILD_NUMBER:-"local-$(date +%Y%m%d%H%M%S)"}"
VERSION="${MAJOR_VERSION}-${MINOR_VERSION}"
ENVIRONMENT="${ENVIRONMENT:-prod}"
ENVIRONMENT_SUFFIX=""
SCHEMA_REGISTRY_URL="https://schema-registry"
KAFKA_BOOTSTRAP_SERVERS="b-1.kafka-server.aaaaaa.c4.kafka.ap-southeast-2.amazonaws.com:9094,b-2.kafka-server.bbbbbb.c4.kafka.ap-southeast-2.amazonaws.com:9094,b-3.kafka-server.cccccc.c4.kafka.ap-southeast-2.amazonaws.com:9094"
VPC_JSON_SETTINGS='{ "SubnetIds": ["subnet-111", "subnet-222", "subnet-333"], "SecurityGroupIds": ["sg-888"] }'
ES_SERVER="https://es-server"

if [ "$ENVIRONMENT" != "prod" ]; then
  ENVIRONMENT_SUFFIX="-$ENVIRONMENT"
fi

if [ "$ENVIRONMENT" == "dev" ]; then
  SCHEMA_REGISTRY_URL="https://schema-registry-dev"
  KAFKA_BOOTSTRAP_SERVERS="b-1.kafka-server-dev.aaaaaa.c4.kafka.ap-southeast-2.amazonaws.com:9094,b-2.kafka-server-dev.bbbbbb.c4.kafka.ap-southeast-2.amazonaws.com:9094,b-3.kafka-server-dev.cccccc.c4.kafka.ap-southeast-2.amazonaws.com:9094"
  VPC_JSON_SETTINGS='{ "SubnetIds": ["subnet-444", "subnet-555", "subnet-666"], "SecurityGroupIds": ["sg-999"] }'
  ES_SERVER="https://es-server-dev"
fi

# The app name contains the major and minor versions so a new pipeline is deployed for each minor
# version bump
export APP_NAME="flink-playground$ENVIRONMENT_SUFFIX-v$VERSION"
# The infrastructure name only contains the environment as we don't want to deploy new
# infrastructure for each schema version
export INFRASTRUCTURE_NAME="flink-playground-infrastructure$ENVIRONMENT_SUFFIX"
# The jar bucket only contains the environment name because we don't want a new bucket deployed for
# each minor version
export JAR_BUCKET_NAME="flink-playground-jars$ENVIRONMENT_SUFFIX"
# The JAR_ID is unique on each build so adding the version to the name is really just for
# documentation
export JAR_FILE_KEY="app-v$VERSION-$JAR_ID.jar"
export IMAGE_NAME="11111111111.dkr.ecr.ap-southeast-2.amazonaws.com/roy/flink-playground"
export DEPENDENCIES_NAME="$IMAGE_NAME-dependencies"


# UNVERSIONED VARIABLES

export SCHEMA_REGISTRY_URL
export KAFKA_BOOTSTRAP_SERVERS
export VPC_JSON_SETTINGS
export ES_SERVER
export ES_INDEX_NAME="stock-share-volume-top-n-by-industry"

SBT_VERSION=$(grep sbt.version ./project/build.properties | cut -d = -f 2)
export SBT_VERSION

DOCKER_IMAGE_TAG="${BUILDKITE_BUILD_NUMBER:-"latest"}"
if [[ "${BUILDKITE_BRANCH:-}" == 'master' ]]; then
  DOCKER_IMAGE_TAG=latest
fi
export DOCKER_IMAGE_TAG
