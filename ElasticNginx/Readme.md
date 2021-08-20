# The Fantastic SecDevOps
## Dashboard Nginx

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker para mostrar los dashboards de kibana en un IFrame.

### Instalacion
1. Modificar la URL en el iframe para apuntar al kibana
2. Utilizar docker para crear la imagen
```
docker build --no-cache -t dashboards .
```
3. Utilizar docker para correr la imagen
```
docker run -p 80:80 --name dashboards dashboards
```

### Problemas
En caso que lo de redmine no se vea bien reflejado en los dashboard es seguro un tema del sync. Hay que chequear como se ve redmine http://{IP}:3000 el issue como se ve en mongo http://{IP}:4000/code_vulnerabilities/ y como se ve en elastic http://{IP}:5601/. Y si lo que pasa es que redmine no refleja los cambios a mongo y elastic hay que actualizar a mano.
1. Persistir cambios de redmine a mongo haciendo un POST sin contenido a: http://{IP}:4000/sync_redmine
2. Persistir cambios de mongo a elastic haciendo un POST sin contenido a: http://{IP}:4000/update_elasticsearch

**Nota**: El puerto 4000 no es obligatorio. Es el puerto el cual se bindea el contenedor Orchestrator. En este caso fue seleccionado el puerto 4000.
