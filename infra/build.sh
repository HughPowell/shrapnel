#!/usr/bin/env bash
set -e
set -o errexit

branch=$(git rev-parse --abbrev-ref HEAD)

if [ "$branch" != "main" ]; then
  echo "Running tests on $branch ..."
  clj -X:test
  echo "Success!"
  echo "Generating documentation ..."
  clj -X:release documentation/generate
  echo "Success!"
  echo "Merging into main ..."
  git checkout main
  git merge --no-ff $branch
  echo "Success!"
fi

echo "Cleaning up from last time ..."
clj -T:infra clean
echo "Success!"
echo "Running tests on main..."
clj -X:test
echo "Success!"
echo "Pushing commits to remote..."
git push origin main
echo "Success!"
echo "Building the jar ..."
clj -T:infra jar
echo "Success!"
set -o allexport
source secrets.env
set +o allexport
clj -X:deploy
echo "Deleting feature branch ..."
git branch -D "$branch"
git branch -D --remote "origin/$branch"
echo "Success"
