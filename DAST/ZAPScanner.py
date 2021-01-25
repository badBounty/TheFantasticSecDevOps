import time
from zapv2 import ZAPv2
import requests
import os
import subprocess
import sys
import json

targetDomain =  sys.argv[1]

# Here the target is defined and an instance of ZAP is created.
target = targetDomain
apikey = 'fvm39bpj135u20812je6ibgupv'
os.environ["ZAP_AUTH_HEADER_VALUE"] = "Basic ZGlyZWN0dmdvOkRUVkdvNC5MaWZlIQ=="
zap = ZAPv2(apikey=apikey, proxies={'http': 'http://127.0.0.1:8080', 'https': 'http://127.0.0.1:8080'})

# ZAP starts accessing the target.
print ('Accessing target ' + target)
zap.urlopen(target)
time.sleep(2)

# The spider starts crawling the website for URL’s
print ('Spidering target ' + target)
scanid = zap.spider.scan(target)

# Progress of spider
time.sleep(2)
print ('Status ' + zap.spider.status(scanid))
while (int(zap.spider.status(scanid)) < 100):
   print ('Spider progress: ' + zap.spider.status(scanid))
   time.sleep(10)

print ('Spider completed')

 

# Give the passive scanner a chance to finish
while (int(zap.pscan.records_to_scan) > 0):
      print ('Records to passive scan : {}'.format(zap.pscan.records_to_scan))
      time.sleep(2)

print ('Passive Scan completed')
time.sleep(5)

 

# The active scanning starts
print ('Scanning target: ' + target)
scanid = zap.ascan.scan(target)
while (int(zap.ascan.status(scanid)) < 100):
   print ('Scan progress: ' + zap.ascan.status(scanid))
   time.sleep(20)

print('Scan completed')

# Report the results
print ('Hosts: ' + ', '.join(zap.core.hosts))
print ('Alerts: ')

alerts = zap.core.alerts()

json.dump(alerts, open('/home/zap/output.json', 'w'), indent=4)
