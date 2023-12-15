#!/bin/bash
tag=$(curl https://hub.docker.com/v2/repositories/shiviraj/auth-service/tags | jq -r '.results | sort_by(.last_updated) | last(.[-2]).name')

if [ -z "$tag" ]; then
    echo "Failed to retrieve tags from Docker Hub. Setting default as 0.0.0"
    tag=0.0.0
fi
echo current tag: $tag
majorTag=$(echo $tag | cut -d '.' -f 1-2 )
minorTag=$(echo $tag | cut -d '.' -f 3)
((minorTag+=1))

newTag=$(echo $majorTag.$minorTag)
echo new tag: $newTag

./gradlew clean build

docker buildx build --no-cache --platform=linux/arm64,linux/amd64 -t shiviraj/auth-service:$newTag -t shiviraj/auth-service:latest --push .
