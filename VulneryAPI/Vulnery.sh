#!/bin/bash

elasticVersion=$1
network=$2
kibanaVersion=$3

function startProcess () {
    #check params
    sysctl -w vm.max_map_count=262144
    createNetwork
}

#Check if docker is installed

#Check if elastic or kibana image already exists. If exists, exit process.

#network

function createNetwork () {
    echo "Removing docker network: $network"
    docker network rm $network
    if docker network create $network
        then
            elasticPull
    else
        echo "Network $network could not be created."
    fi
}

#elastic

function elasticPull () {
    if docker pull docker.elastic.co/elasticsearch/elasticsearch:$elasticVersion ;
        then
            elasticRun
    else
        echo "Elasticsearch version: $elasticVersion could not be pulled."
    fi
}

function elasticRun () {
    if docker run --name elasticsearch --net $network -p 9200:9200 -d -it docker.elastic.co/elasticsearch/elasticsearch:$elasticVersion ; 
        then
            #configure elastic, user and pass, index and mapping (using json file)
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
    if docker run --name kibana --net $network -p 5601:5601 -d docker.elastic.co/kibana/kibana:$kibanaVersion ; 
        then
            finishProcess
    else
        echo "Kibana image could not be ran."
    fi  
}

function finishProcess () {
    echo "Resetting elastic password and kibana token..."
    sleep 2
    echo "Elastic user: elastic"
    echo "Elastic password:"
    docker exec -it elasticsearch /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic --batch
    echo "Kibana token: "
    docker exec -it elasticsearch /usr/share/elasticsearch/bin/elasticsearch-create-enrollment-token -s kibana
    echo "Kibana verification code (put after token): "
    docker exec -it kibana /usr/share/kibana/bin/kibana-verification-code
    echo "Proceso finalizado"
}

startProcess
