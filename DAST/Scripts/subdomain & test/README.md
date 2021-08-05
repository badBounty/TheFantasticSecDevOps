# Guia de instalacion (Requisitos y uso):

# Requisitos:

### Go: 
```
Sudo apt install golang-go
```
### Nuclei:
```
GO111MODULE=on go get -v github.com/projectdiscovery/nuclei/v2/cmd/nuclei
```
![image](https://user-images.githubusercontent.com/50958708/128386086-67e9aec3-e2f9-4fd2-8e75-38c32d68a476.png)

Para la automatizacion de cada template creamos un archivo llamado "nuclei-scans" en el que se definan todos los templates que van a correrse:

![image](https://user-images.githubusercontent.com/50958708/128386124-2cb271e1-bd86-4a34-ad88-659008060041.png)


Si se desean agregar templates customizados se debe crear un archivo .yaml (el template) y ubicarlo en un directorio (con nombre "template-test" de ejemplo) dentro de "nuclei-templates". De esta manera solo hay que modificar nuestro "nuclei-scans" y agregar la entrada del nombre del directorio ("template-test" en nuestro caso)

![image](https://user-images.githubusercontent.com/50958708/128386160-d0570909-4c14-4b17-aee6-6cd30ef4997e.png)

![image](https://user-images.githubusercontent.com/50958708/128386190-f7fb3ede-d5db-4968-a24b-cf56d8719384.png)


## EXTRAS PARA DESCUBRIMIENTO DE SUBDOMINIOS:
### Amass:
```
brew tap caffix/amass
brew install amass
```

### MassDNS:
```
Sudo apt install massdns
```
Es necesario poseer un archivo que contenga las ip a las que se van a resolver los dominios (resolvers.txt)
https://github.com/blechschmidt/massdns/blob/master/lists/resolvers.txt

### AltDNS:
```
Sudo apt install altdns
```
Es necesario poseer un archivo que contenga posibles nombres de subdominios (words.txt)
https://github.com/infosec-au/altdns/blob/master/words.txt

### Httprobe: 
Utilizamos httprobe para testear aquellos dominios que poseen una aplicacion web mediante puertos.
Take a list of domains and probe for working http and https servers.
```
Sudo apt install httprobe
```
### Aquatone: 
Utilizamos aquatone para el testing de dominios que posean una aplicacion web asi como httprobe, tambien brinda funciones como sacar screenshots de la web renderizada.

### Slackcat:
```
curl -Lo slackcat https://github.com/bcicen/slackcat/releases/download/1.7.2/slackcat-1.7.2-$(uname -s)-amd64
sudo mv slackcat /usr/local/bin/
sudo chmod +x /usr/local/bin/slackcat
```
Hace falta configurar slackcat para que se conecte con nuestro espacio y canal. El mismo se utilizara para enviar los resultados que nuclei arroje.

Configuracion de usuario:
Lo primero es configurar el usuario y el espacio en el cual van a ir almacenandose los resultados, es importante primero tener una cuenta de slack abierta en el navegador antes de ir por este paso, luego se corre el comando slackcat --configure.

![image](https://user-images.githubusercontent.com/50958708/128386263-15602b3d-25d6-4ad7-99d6-4df22c9be206.png)


Se pide un nickname para el team, en este caso nuclei_testing.
Luego el programa nos abre una pestaña en el navegador para poder darle permisos a la aplicación y a la cuenta ya registrada, de esta manera se genera el token que nos pide por la terminal.

![image](https://user-images.githubusercontent.com/50958708/128386412-48289acc-4657-444e-aa76-0867550d34c3.png)
![image](https://user-images.githubusercontent.com/50958708/128386717-7d3016b0-89eb-4ab0-8800-347bee363e0f.png)

Copiamos el token en el input de la terminal:

![image](https://user-images.githubusercontent.com/50958708/128386849-5461619e-4531-4fa9-bdee-9ce5679d25ea.png)

Testeamos la funcionalidad:

![image](https://user-images.githubusercontent.com/50958708/128386932-5fe38377-8cc5-4411-a826-e5445f3ec7cc.png)

![image](https://user-images.githubusercontent.com/50958708/128386993-e141d0d6-c293-417d-a8bb-6a21d6e19438.png)


# Uso:

## Descripcion general: 
El script toma una lista de dominios y comienza realizar escaneos con nuclei en busca de vulnerabilidades que luego seran alertadas por un canal de slack, tambien puede configurarse un template customizado.

Previo a la ejecucion del programa es importante mencionar que los dominios que requieran descubrimiento de subdominios deben definirse con una wildcard seguido de un punto (ej. \*.google.com).

Lo primero despues de listar el archivo chequea si existe algun tipo de wildcard para buscar más subdominios, de no existir * se agrega el dominio tal como esta, una vez identificados comienza a chequear que los binarios necesarios esten instalados, luego realiza el descubrimiento de subdominios. Para este paso se ejecuta amass, massDNS (para resolver dominios), altDNS (para permutacion y resolucion de dominios). Finalmente, se hace un merge de todos los host obtenidos, se eliminan los duplicados y comienza la etapa de verificar cuales poseen una aplicacion web mediante chequeo de puertos, los programas que se utilizan son httprobe y aquatone. Nuevamente se hace un merge de los resultados obtenidos y se utiliza el output para comenzar la etapa de testing con Nuclei.

### Pasos:
Se crea un directorio en el cual ir almacenando los resultados obtenidos.
Se actualizan los templates.
Se itera sobre una lista de templates a testear, de esta forma se escanea cada template por cada subdominio en la lista.
Por cada resultado o finding obtenido se envia como mensaje hacia un canal preconfigurado de slack (slackcat).


