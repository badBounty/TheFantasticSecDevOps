from datetime import datetime
from elasticsearch import Elasticsearch

def getInstance(url, port, username, password):
    return Elasticsearch([f'https://{url}:{port}'], http_auth=(username, password))

def uploadSASTVuln(elastic, vulnSAST):
    #recieve instance and upload vuln to index. observe results
    pass

def uploadDASTVuln():
    pass