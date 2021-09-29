# The Fantastic SecDevOps
## SAST

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker de la maquina de SAST que contiene SonarQube. Actualmente Jenkins se encarga de acceder a la Máquina SAST a través de SSH, y generar un contenedor SAST bajo demanda y eliminarlo (al finalizar los análisis). Por lo que es necesario instalar contar con los pre-requisitos en la máquina SAST, y buildear la imagen, para que Jenkins solo tenga que crear el contenedor.

##

La carpeta SAST contiene los siguientes **elementos**:

|Elemento                     | Descripción                                                              |
|-----------------------------|--------------------------------------------------------------------------|
|Nuclei Custos Templates      | Carpeta donde adentro se añaden Templates custom para Nuclei             |
|DependencyCheck.sh           | Bash script que contiene la ejecución de DependencyCheck y su parser .py |
|blacklistVulns.json          | Json para blacklistear resultados de DotNet, Njsscan y SonarQube         |
|normalization.json           | Json para whitelistear resultados de DotNet, Njsscan y SonarQube         |
|configure.sh                 | Bash script para añadir el plugin de Sonar a DependencyCheck             |
|start.sh                     | Bash script para correr la imagen de SAST y levantar SonarQube           |
|titleNormalization.py        | Script hecho en python que permite normalizar info y whitelistear vulns  |

##

La carpeta SAST contiene los siguientes **Parsers**:

|Parser                       | Descripción                                                                                    |
|-----------------------------|------------------------------------------------------------------------------------------------|
|Flawfinder Parser            | Parser hecho en python para la tool Flawfinder                                                 |
|Nuclei Parser                | Parser hecho en python para la tool Nuclei                                                     |
|Log Parser                   | Parser hecho en python para logs de DotNet build luego de agregarse PumaScan y SecurityCodeScan|
|Nodejsscan Parser            | Parser hecho en python para la tool Njsscan                                                    |
|NPM Audit  Parser            | Parser hecho en python para la tool NPM Audit                                                  |
|DependencyCheck Parser       | Parser hecho en python para la tool DependencyCheck                                            |

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

### Parsers:

- **¿Por que se utilizan Parsers?**  
	- Se utilizan Parsers ya que cada tool da un output diferente. Al momento de hacer POST al orquestador, se requiere un formato único para cada vuln. Por ende, se debe parsear el formato de output de la tool en un formato único.

### Vulns:

- **¿Por que se debe whitelistear/blacklistear?**  
	- Se debe realizar una blacklist o una whitelist a fin de evitar realizar lo mayor posible POST de falsos positivos. Muchas tools dan como resultado varios falsos positivos.

##
