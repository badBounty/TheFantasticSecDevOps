# Guia de instalacion:

## Requisitos:

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

### Amass:
```
brew tap caffix/amass
brew install amass
```
### Sublister:
```
git clone https://github.com/aboul3la/Sublist3r.git
sudo pip install -r requirements.txt
```
Nota: Es necesario reemeplazar el diccionario usado por el diccionario sugerido en la metodología.

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
sudo apt install httprobe
```
### Aquatone: 
```
sudo apt install aquatone
```

### Slackcat:
```
curl -Lo slackcat https://github.com/bcicen/slackcat/releases/download/1.7.2/slackcat-1.7.2-$(uname -s)-amd64
sudo mv slackcat /usr/local/bin/
sudo chmod +x /usr/local/bin/slackcat
```
Hace falta configurar slackcat para que se conecte con nuestro espacio y canal. El mismo se utilizara para enviar resultados.

#### Configuracion:
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

## Enumeración de subdominios: 
TODO
```
TODO
```
## Escaneo con nuclei:
TODO
```
TODO
```

## Fuzzing de directorios y archivos:
TODO
```
TODO
```

## Ejecución con Bug Hunter, proceso continuo:
TODO
```
TODO
```
