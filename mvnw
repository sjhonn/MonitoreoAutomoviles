#!/bin/sh
set -eu

MAVEN_VERSION="3.9.16"
MAVEN_ROOT="${HOME}/.m2/wrapper/dists"
MAVEN_HOME="${MAVEN_ROOT}/apache-maven-${MAVEN_VERSION}"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"
MAVEN_SHA512="ed41650d42485cfc243fad22158caf9cbb5dc408ce7a09ddb94dd42a019de929ca43065bfa450612cf12bf78b5cafa3884b96c090de326ff590448c933454af3"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  mkdir -p "${MAVEN_ROOT}"
  TMP_FILE="${TMPDIR:-/tmp}/apache-maven-${MAVEN_VERSION}-bin.zip"
  echo "Downloading Apache Maven ${MAVEN_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "${MAVEN_URL}" -o "${TMP_FILE}"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "${MAVEN_URL}" -O "${TMP_FILE}"
  else
    echo "curl or wget is required for the first Maven Wrapper run." >&2
    exit 1
  fi

  if command -v sha512sum >/dev/null 2>&1; then
    ACTUAL_SHA512=$(sha512sum "${TMP_FILE}" | awk '{print $1}')
    if [ "${ACTUAL_SHA512}" != "${MAVEN_SHA512}" ]; then
      rm -f "${TMP_FILE}"
      echo "Apache Maven checksum validation failed." >&2
      exit 1
    fi
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "${TMP_FILE}" -d "${MAVEN_ROOT}"
  else
    echo "unzip is required for the first Maven Wrapper run." >&2
    exit 1
  fi
  rm -f "${TMP_FILE}"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
