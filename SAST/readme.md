# The Fantastic SecDevOps
## SAST

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker de la maquina de SAST que contiene SonarQube. Actualmente Jenkins se encarga de acceder a la Máquina SAST a través de SSH, y generar un contenedor SAST bajo demanda y eliminarlo (al finalizar los análisis). Por lo que es necesario instalar contar con los pre-requisitos en la máquina SAST, y buildear la imagen, para que Jenkins solo tenga que crear el contenedor.

### Pre-requisitos
- Ubuntu 18 o 20
- SSH con public key activo
- Docker (debe ejecutarse sin necesidad de root)
- Buildear la imagen

### Instalación

Solo es necesario builder la imagen. La misma retorna la private key en pantalla (durante el building) que debe ser configurada como credencial *ssh-key-SAST-image* en Jenkins, ya que esa private key corresponde a la imagen de SAST generada y no al server.

```
docker image build --no-cache -t secpipeline-sast .
```

**Nota**: **NO** hace falta correr la imagen de SAST, ya que Jenkins se encarga de generar el contenedor y eliminarlo en cada Pipeline.

## FAQ (Completar)
