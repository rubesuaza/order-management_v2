#!/bin/sh

#
# Gradle start-up script. Run 'gradle wrapper' to generate full wrapper if needed.
#

set -e

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Gradle not found. Install Gradle or run from an IDE with Gradle support."
    exit 1
fi
