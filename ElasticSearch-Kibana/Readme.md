# The Fantastic SecDevOps
## ElasticSearch-Kibanna

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker conteniendo elasticsearch y kibanna para almacenar y mostrar informacion de dashboards.

### Pre-requisitos
- Ubuntu 18 o 20
- Docker

### Ejecucuion
Para levantar el docker de Elastic y Kibanna seguir los pasos pasos a continuacion.

1. Buildear la imagen utilizando el Dockerfile en este directorio.

    ```
    docker build -t ElasticKibana .
    ```

    Para utilizar una version especifica de elastic y Kibanna

    ```
    docker build -t ElasticKibana . --build-arg EK_VERSION=7.9.0
    ```

2. Correr la imagen en un contenedor.

    ```
    docker run -d --name elasticsearch-kibana -p 9200:9200 -p 5601:5601 ElasticKibana
    ```
