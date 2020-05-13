# TheFantasticSecDevOps
## SAST

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker de la maquina de SAST que contiene SonaQube.

### Pre-requisitos
- Apache maven (https://maven.apache.org)
- Java 11 

### Instalacion
Los pasos a continuacion permiten crear la imagen de sonar correctamente creando tambien los perfiles.

1) Clonar el repositorio con las reglas personalizadas de sonar (https://github.com/badBounty/SonarSecurityRules)

2) Compilar este proyecto utiliando maven como se muestra a continuacion
    ```
    $ mvn clean package 
    ```
3) Luego de compilar, copiar el archivo jar resultante, que se encuentra dentro de la carpeta target, hacía la carpeta donde se encuentra el dockerfile.
 
5) Clonar el repositorio del plugin spotbugs-findbugs (https://github.com/spotbugs/sonar-findbugs)

6) Una vez clonado compilarlo
    ```
    $ mvn clean package 
    ```

8) Copiar el archivo sonar-findbugs-plugin.jar que se encuentra en la carpeta target hacia la carpeta donde se encuentra el dockerfile.

9) Descargar el archivo .jar del plugin dependency check. (https://github.com/dependency-check/dependency-check-sonar-plugin/releases). Guardarlo en la carpeta donde se encuentra el dockerfile.

10) Una vez realizados los pasos anteriores ejecutar el archivo start.sh