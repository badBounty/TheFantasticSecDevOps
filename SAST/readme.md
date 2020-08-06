# The Fantastic SecDevOps
## SAST

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker de la maquina de SAST que contiene SonaQube.

### Pre-requisitos
- Ubuntu 18 o 20
- Docker

### Instalacion
Los pasos a continuacion permiten crear la imagen de sonar correctamente creando tambien los perfiles.

1) Configurar en el script configure.sh las variables githubusr y githubpasswd con usuario y contraseña, respectivamente, de una cuenta de github con acceso al repositorio [SonarSecurityRules](https://github.com/badBounty/SonarSecurityRules)

2) Ejecutar el archivo start.sh de la siguiente manera para buildear y correr la imagen de sonar

```
start.sh {build | nobuild} {container name} {running sonar port} {ssh port}
```

