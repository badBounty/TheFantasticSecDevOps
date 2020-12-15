# The Fantastic SecDevOps
## SAST

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker de la maquina de SAST que contiene SonaQube. Actualmente Jenkins se encarga de acceder a la Máquina SAST a través de SSH, y generar un contenedor SAST bajo demanda y eliminarlo. Por lo que es necesario instalar contar con los pre-requisitos en la máquina SAST, y buildear la imagen, para que Jenkins solo tenga que crear el contenedor.

### Pre-requisitos
- Ubuntu 18 o 20
- SSH con public key activo
- Docker (debe ejecutarse sin necesidad de root)
- Buildear la imagen

### Instalacion

Solo es necesario builder la imagen. La misma retorna la public key que debe ser configurada en Jenkins.

```
docker image build --no-cache -t secpipeline-sast .
```
