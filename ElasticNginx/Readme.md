# The Fantastic SecDevOps
## Dashboard Nginx

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker para mostrar los dashboards de kibana en un IFrame.

### Instalacion
1. Modificar la URL en el iframe para apuntar al kibana (Tener en cuenta que la modificación debe hacerse antes de buildear la imagen, ya que el Dockerfile copia el html del repositorio al path donde Nginx levanta html).

2. Utilizar docker para crear la imagen
```
docker build --no-cache -t dashboards .
```
3. Utilizar docker para correr la imagen
```
docker run -p 80:80 --name dashboards dashboards
```

### Problemas
En caso que lo de redmine no se vea bien reflejado en los dashboard es seguro un tema del sync. Hay que chequear como se ve redmine http://{IP_PUBLICA}:3000 el issue como se ve en mongo http://{IP_PUBLICA}:4000/code_vulnerabilities/ y como se ve en elastic http://{IP_PUBLICA}:5601/. Y si lo que pasa es que redmine no refleja los cambios a mongo y elastic hay que actualizar a mano.
1. Persistir cambios de redmine a mongo haciendo un POST sin contenido a: http://{IP_PUBLICA}:4000/sync_redmine
2. Persistir cambios de mongo a elastic haciendo un POST sin contenido a: http://{IP_PUBLICA}:4000/update_elasticsearch

**Nota**: El puerto 4000 no es obligatorio. Es el puerto el cual se bindea el contenedor Orchestrator. En este caso fue seleccionado el puerto 4000.
