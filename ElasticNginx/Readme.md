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
