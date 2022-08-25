#!/bin/bash

function removeImagesContainers () {
    local containers=$(docker ps -a -q)
    local images=$(docker images -a)
    docker stop $containers
    docker rm -f -v $containers
    docker rmi $images
}

removeImagesContainers