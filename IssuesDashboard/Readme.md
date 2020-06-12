# Issues Dashboards

Proyecto ASP .Net core para subir guardar issues en elastic search a traves de una API y mostrar dashboards de las mismas.

## Step by step

1. Buildear el dockerfile dentro de esta carpeta.
```
docker image build -t issues . 
```
2. Correr la imagen publicando los puertos correspondientes

```
docker run --name issues -p 5000:5000 -p 5601:5601 -p 9200:9200  issues
```