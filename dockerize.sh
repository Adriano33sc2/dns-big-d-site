#!/bin/bash
NAME="sn0wf1eld/dns-big-d-website"
VERSION=$1

echo "Building frontend..."
./node_modules/.bin/shadow-cljs release app

echo "Building uberjar..."
lein uberjar

mkdir docker_temp

cp target/dns-big-d-site-$VERSION-standalone.jar docker_temp/dns-big-d-site-standalone.jar

echo "Logging into Docker Hub..."
docker login

docker buildx create --use

echo "Building and pushing Docker image..."
docker buildx build \
  --provenance=true --sbom=true \
  --platform linux/arm64/v8,linux/amd64 \
  --push \
  --tag $NAME":"$VERSION \
  --tag $NAME":latest" \
  .

docker buildx rm

echo "Done! Pushed $NAME:$VERSION and $NAME:latest"
