## Instalación

1. Ejecutar el archivo "DockerCleaner.sh". Este archivo borrará los contenedores e imagenes previamente creadas del servidor. (Únicamente ejecutar en caso de querer borrar las imágenes y containers).
2. Ejecutar el archivo "Vulnery.sh". Este archivo configurará el servidor, creará una docker network e instalará elasticsearch y kibana en dos containers separados, conectados a la misma network. Se debe pasar como parámetro la versión de elasticsearch, el nombre de la red de docker y la versión de kibana a instalar.
3. Al finalizar la ejecución del anterior archivo, en pantalla se verá el username y password de elastic, así como el token y código de verificación para configurar kibana. Luego de ingresar, establecer los índices de la aplicación y su mapping. (Se hará automatizado).
4. Instalar Django, requisitos y correr la aplicación VulneryAPI (Se hará automatizado en el paso anterior).
