#!/usr/bin/env sh
set -e
set -o errexit

echo "Cleaning up from last time ..."
clj -T:infra clean
echo "Success!"
echo "Running tests ..."
clj -X:test
echo "Success!"
echo "Generating documentation ..."
clj -X:release documentation/generate
echo "Success!"
echo "Building the jar ..."
clj -T:infra jar
echo "Success!"
