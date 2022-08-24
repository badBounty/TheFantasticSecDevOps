#!/bin/bash

elasticVersion=$1
network=$2
kibanaVersion=$3

function startProcess () {
    sysctl -w vm.max_map_count=262144
    elasticPull
}

#Check if docker is installed

#Check if elastic or kibana image already exists. If exists, exit process.

#network

function createNetwork () {
    docker network rm $network
    if docker network create $network
        then
            elasticRun
    else
        echo "Network $network could not be created."
    fi
}

#elastic

function elasticPull () {
    if docker pull docker.elastic.co/elasticsearch/elasticsearch:$elasticVersion ;
        then
            createNetwork
            elasticRun
    else
        echo "Elasticsearch version: $elasticVersion could not be pulled."
    fi
}

function elasticRun () {
    if docker run --name es01 --net $network -p 9200:9200 -d -it docker.elastic.co/elasticsearch/elasticsearch:$elasticVersion ; 
        then
            #configure elastic, user and pass, index and mapping
            kibanaPull
    else
        echo "Elastic image could not be ran."
    fi        
}

#kibana

function kibanaPull () {
    if docker pull docker.elastic.co/kibana/kibana:$kibanaVersion ;
        then
            kibanaRun
    else
        echo "Kibana version: $kibanaVersion could not be pulled."
    fi
}

function kibanaRun () {
    if docker run --name kib-01 --net $network -p 5601:5601 -d docker.elastic.co/kibana/kibana:$kibanaVersion ; 
        then
            finishProcess
    else
        echo "Kibana image could not be ran."
    fi  
}

function finishProcess () {
    echo "Proceso finalizado"
}

startProcess




