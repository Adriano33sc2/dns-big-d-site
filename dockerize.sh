NAME="sn0wf1eld/dns-big-d-website"
VERSION=$1

docker login

docker buildx create --use

docker buildx build --build-arg --provenance=true --sbom=true --platform linux/arm64/v8,linux/amd64 --push --tag $NAME":"$VERSION --tag $NAME":latest" .

rm -rf docker_temp

docker buildx rm
