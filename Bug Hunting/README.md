# Instalacion

## Instalación de herramientas necesarias:

Ejecutar el script de setup
```
./setup.sh
```
Como resultado de la ejecución, se instalan todas las herramientas necesarias para poder correr los script que contiene el repositorio.

## Slackcat - Configuracion:
Lo primero es configurar el usuario y el espacio en el cual van a ir almacenandose los resultados, es importante primero tener una cuenta de slack abierta en el navegador antes de ir por este paso, luego se corre el comando slackcat --configure.

![image](https://user-images.githubusercontent.com/50958708/128386263-15602b3d-25d6-4ad7-99d6-4df22c9be206.png)


Se pide un nickname para el team, en este caso "nuclei_testing". Luego el programa nos abre una pestaña en el navegador para poder darle permisos a la aplicación y a la cuenta ya registrada, de esta manera se genera el token que nos pide por la terminal.

![image](https://user-images.githubusercontent.com/50958708/128386412-48289acc-4657-444e-aa76-0867550d34c3.png)
![image](https://user-images.githubusercontent.com/50958708/128386717-7d3016b0-89eb-4ab0-8800-347bee363e0f.png)

Copiamos el token en el input de la terminal (incluyendo xoxp):

![image](https://user-images.githubusercontent.com/50958708/128386849-5461619e-4531-4fa9-bdee-9ce5679d25ea.png)

Testeamos la funcionalidad:

![image](https://user-images.githubusercontent.com/50958708/128386932-5fe38377-8cc5-4411-a826-e5445f3ec7cc.png)

![image](https://user-images.githubusercontent.com/50958708/128386993-e141d0d6-c293-417d-a8bb-6a21d6e19438.png)

# Uso:

## Ejecución con Bug Hunter, proceso continuo:
Comando para correr la herramienta
```
./bug-hunter.sh [domains list file] [slack channel]
```
Se encarga de correr los scripts del repositorio manejando los outputs internamente en el siguiente orden:
1. Enumeración de subdominios.
2. Enumeración de directorios, archivos y controladores.
3. Escaneo completo con Nuclei.
4. Escaneo completo con Nmap. 
5. Enumeración y escaneo completo de archivos .js.

Como resultado de la ejecución, se obtienen todos los resultados correspondientes a los scripts que se encuentran en el repositorio y se reportan por Slack.

VER REQUERIMIENTOS EN CADA SECCIÓN DE CADA SCRIPT INTERNO.

## Enumeración de subdominios: 

Comando para correr la herramienta:
```
./subdomain_enum.sh [domains list file] [slack channel]
```

Se encarga de realizar los siguientes pasos con el objetivo de encontrar la mayor cantidad de subdominios posibles:
1. Ejecuta Amass usando OSINT y el módulo de fuerza bruta para encontrar subdominios usando una lista de palabras de subdominios completa compuesta por otras listas de palabras.
2. Ejecuta AltDNS utilizando la salida anterior como entrada para descubrir subdominios a través de alteraciones y permutaciones.
3. Modifica las salidas para obtener salidas homogéneas.
4. Fusiona todas las salidas anteriores.
5. Ejecuta Aquatone sobre dicha fusión.
6. Informa a través de Slack, utilizando Slackcat, todos los resultados de la primera ejecución y solo los nuevos en las siguientes.

Como resultado de la ejecución, se obtiene un output único que contiene la lista de subdominios que fueron descubiertos y que además corren una aplicación web.

Requerimientos:
- Archivo subdomains-blacklist.txt que debe contener los subdominios que no son de interés analizar.

## Fuzzing de directorios y archivos:
Comando para correr la herramienta
```
./dirnfiles_enum.sh [subdomains list file] [slack channel]
```
Se encarga de realizar los siguientes pasos con el objetivo de encontrar la mayor cantidad de directorios, archivos y controladores:
1. Ejecuta dirsearch de manera que detecte las extensiones más importantes, trabaje a una velocidad razonable, ejecute un proceso lo más recursivo posible, tenga en cuenta únicamente los status code relevantes y utilice un diccionario que contenga diccionarios de: directorios, archivos y tecnologías.
2. Modifica la salida original para obtener una salida homogénea.
3. Informa a través de Slack, utilizando Slackcat, todos los resultados de la primera ejecución y solo los nuevos en las siguientes.

Como resultado de la ejecución, se obtienen un único output que contiene directorios generales encontrados, archivos generales encontrados, y directorios y archivos encontrados en base a las tecnologías Adobe Experience Manager, Nginx y Oracle. 

Requerimientos:
- Archivo basicauth.txt que debe contener los subdominios que no son de interés analizar.

## Escaneo con nuclei:
Comando para correr la herramienta:
```
./nuclei_scan.sh [subdomains list file] [slack channel]
```
Se encarga de ejecutar Nuclei haciendo uso de todas las templates que el mismo provee:
- cves
- default-logins
- dns
- exposed-panels
- cnvd
- workflows
- network
- takeovers
- technologies
- vulnerabilities
- exposures
- file
- fuzzing
- headless
- helpers
- iot
- miscellaneous
- misconfiguration

Como resultado de la ejecución, se obtiene un output único que contiene una lista de vulnerabilidades encontradas en todos los subdominios de interés. La misma se envía por Slack al canal establecido.

## Escaneo con nmap:
Comando para correr la herramienta:
```
./nmap_scan.sh [subdomains list file] [slack channel]
```
Se encarga de ejecutar Nmap sobre los puertos top 1000 TCP y top 10 UDP de manera que detecte servicios y corra con los siguientes scripts web:
- banner
- vuln
- vulscan/vulscan.nse
- http-enum
- http-webdav-scan
- http-backup-finder
- http-trace
- http-config-backup
- http-wordpress-enum
- http-rfi-spider
- http-cors
- http-cookie-flags
- http-waf-detect

Como resultado de la ejecución, se obtiene el mismo output en dos formatos distintos: .nmap (para leerlo en texto plano) y .xml (para leerlo mediante otro software). Ambos se envían por Slack al canal establecido.

Requerimientos:
- Archivo ports.txt que debe contener los puertos que son de interés analizar. Se sugiere utilizar el archivo que viene por defecto en el repositorio.

## Escaneo de archivos JS:
Comando para correr la herramienta:
```
./javascript_scan.sh [subdomains list file] [slack channel]
```
Se encarga de seguir una serie de pasos en el siguiente orden, con el objetivo de analizar la mayor cantidad de archivos .js que se encuentren:
1. Ejecuta hakrawler para encontrar archivos .js en todos los subdominios previamente encontrados.
2. Ejecuta Photon para encontrar archivos .js en todos los subdominios previamente encontrados.
3. Ejecuta gospider para encontrar archivos .js en todos los subdominios previamente encontrados.
4. Ejecuta getJS para encontrar archivos .js en todos los subdominios previamente encontrados.
5. Mergea todos los outputs de los anteriores pasos en un único output y lo reporta por Slack, además de borrar todos los archivos temporales creados.
6. Descarga todos los archivos .js encontrados.
7. Ejecuta LinkFinder para encontrar links en los archivos descargados y reporta el resultado por Slack.
8. Ejecuta DumpsterDiver para encontrar keys en los archivos descargados y reporta el resultado por Slack.
9. Ejecuta Retire.js para encontrar vulnerabilidades en los archivos descargados y reporta el resultado por Slack.
10. Ejecuta Nuclei para encontrar vulnerabilidades en los archivos encontrados (es decir, sobre las URLs que llevan a los archivos .js) y reporta el resultado por Slack.
