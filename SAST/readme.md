# TheFantasticSecDevOps
## SAST

### Contenido
Esta carpeta contiene los archivos para crear una imagen docker de la maquina de SAST que contiene sonar.

### Prerequisitos
- Apache maven (https://maven.apache.org)
- Java 11 

### Paso a paso
Los pasos a continuacion permiten crear la imagen de sonar correctamente creando tambien los perfiles dentro de 

1) Clonar el repositorio con las reglas personalizadas de sonar (https://github.com/badBounty/SonarSecurityRules)
2) Compilar este proyecto con utiliando maven como se muestra a continuacion
    ```
    $ mvn clean install 
    ```
3) Luego de compilar, copiar el archivo java-custom-rules-1.0-SNAPSHOT.jar, que se encuentra dentro de la carpeta target, hacía la carpeta donde se encuentra el dockerfile
4) Clonar el repositorio del plugin spotbugs-findbugs (https://github.com/spotbugs/sonar-findbugs)
5) Una vez clonado compilarlo nuevamente como en el paso 2.
6) Copiar el archivo sonar-findbugs-plugin.jar que se encuentra en la carpeta target hacia la carpeta donde se encuentra el dockerfile.
7) Descargar el archivo .jar del plugin dependency check. (https://github.com/dependency-check/dependency-check-sonar-plugin/releases). Guardarlo en la carpeta donde se encuentra el dockerfile.
8) Una vez realizados los pasos anteriores ejecutar el archivo start.sh.