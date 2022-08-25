## Instalación

1. Ejecutar el archivo "DockerCleaner.sh". Este archivo borrará los contenedores e imagenes previamente creadas del servidor. (Únicamente ejecutar en caso de querer borrar las imágenes y containers).
2. Ejecutar el archivo "Vulnery.sh". Este archivo configurará el servidor, creará una docker network e instalará elasticsearch y kibana en dos containers separados, conectados a la misma network. Se debe pasar como parámetro la versión de elasticsearch, el nombre de la red de docker y la versión de kibana a instalar.
```
sudo bash /path/Vulnery.sh 8.3.3 netelastic 8.3.3
```
3. Al finalizar la ejecución del anterior archivo, en pantalla se verá el username y password de elastic, así como el token y código de verificación para configurar kibana. Luego de ingresar, establecer los índices de la aplicación y su mapping. (Se hará automatizado).
4. Activar elastic-search. Es necesario ingresar a http://IP:5601/login y pegar el token, y el cod de activacion. Luego loguearse con el user y pass que da el script del paso 2 y crear el esquema de la db
```
PUT /sast_vulns
PUT /dast_vulns
PUT /infra_vulns

PUT /infra_vulns/_mapping
{
  "properties": {
    "vuln_title":{
      "type":"text"
    },
    "vuln_description":{
      "type":"text"
    },
    "vuln_observation":{
      "type":"text"
    },
    "vuln_domain":{
      "type":"text"
    },
    "vuln_subdomain":{
      "type":"text"
    },
    "vuln_extra_info":{
      "type":"text"
    },
    "vuln_cvss_score":{
      "type":"text"
    },
    "vuln_language":{
      "type":"text"
    },
    "vuln_severity":{
      "type":"keyword"
    },
    "vuln_recommendation":{
      "type":"text"
    },
    "vuln_date":{
      "type":"date",
      "format":"yyyy-MM-dd"
    },
    "vuln_status":{
      "type":"keyword"
    }
  }
}

PUT /dast_vulns/_mapping
{
  "properties": {
    "vuln_title":{
      "type":"text"
    },
    "vuln_description":{
      "type":"text"
    },
    "vuln_affected_resource":{
      "type":"text"
    },
    "vuln_affected_urls":{
      "type":"text"
    },
    "vuln_recommendation":{
      "type":"text"
    },
    "vuln_severity":{
      "type":"text"
    },
    "vuln_date":{
      "type":"date",
      "format":"yyyy-MM-dd"
    },
    "vuln_status":{
      "type":"text"
    }
  }
}

PUT /sast_vulns/_mapping
{
  "properties": {
    "vuln_title":{
      "type":"text"
    },
    "vuln_description":{
      "type":"text"
    },
    "vuln_component":{
      "type":"text"
    },
    "vuln_line":{
      "type":"integer"
    },
    "vuln_affected_code":{
      "type":"text"
    },
    "vuln_commit":{
      "type":"text"
    },
    "vuln_username":{
      "type":"text"
    },
    "vuln_pipeline":{
      "type":"text"
    },
    "vuln_branch":{
      "type":"text"
    },
    "vuln_language":{
      "type":"text"
    },
    "vuln_hash":{
      "type":"text"
    },
    "vuln_severity":{
      "type":"text"
    },
    "vuln_recommendation":{
      "type":"text"
    },
    "vuln_date":{
      "type":"date",
      "format":"yyyy-MM-dd"
    },
    "vuln_status":{
      "type":"text"
    }
  }
}
```
5. Instalar Django, requisitos y correr la aplicación VulneryAPI.
```
sudo apt install python3-pip
sudo pip3 install django
sudo pip3 install elasticsearch
sudo pip3 install --upgrade pip
sudo pip3 install --upgrade requests
sudo pip3 install --upgrade urllib3

python3 manage.py runserver 0.0.0.0:8000
```

## Utilización

Para utilizar la API, se deberán postear las vulns a los siguientes endpoints, dependiendo del tipo.

- /vulnsPoster/postVulnSAST/
- /vulnsPoster/postVulnDAST/
- /vulnsPoster/postVulnInfra/

## Formato de vulns

Cada tipo de vuln tiene su formato específico que debe ser respetado para ser posteado al servidor con éxito.

### SAST

{
    "Title": "Example",
    "Description": "Example",
    "Component": "Example",
    "Line": "34",
    "Affected_code": "Example",
    "Commit": "Example",
    "Username": "Example",
    "Pipeline_name": "Example",
    "Branch": "Example",
    "Language": "Example",
    "Hash": "Example",
    "Severity": "High",
    "Status": "Open",
    "Recommendation": "-"
}

### DAST

{
    "Title": "Example",
    "Description": "Example",
    "Affected_resource": "Example",
    "Affected_urls": "Example",
    "Recommendation": "Example",
    "Severity": "High",
    "Date": "2022-07-05",
    "Status": "Open"
}

### Infra

{
    "Title": "Example",
    "Description": "Example",
    "Observation": "Example",
    "Domain": "Example",
    "Subdomain": "Example",
    "Extra_info": "Example",
    "CVSS_Score": "9.5",
    "Language": "Example",
    "Severity": "High",
    "Recommendation": "-",
    "Date": "2022-07-25",
    "Status": "Open"
}

## Notas

- La recomendación es importante que exista como campo aunque está vacío para el correcto procesamiento del JSON.
- La severidad deberá ser High, Medium, Low, o Info.
- El estado deberá ser Open, Closed, o False Positive.
- En las vulns de SAST, no hace falta especificar la fecha, ya que se añade la fecha actual automáticamente al momento de ser posteada al servidor.
