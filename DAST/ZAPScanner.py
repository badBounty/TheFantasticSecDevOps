import time
from zapv2 import ZAPv2
import requests
import os
import subprocess
import sys
import json
import base64

targetDomain =  sys.argv[1]
authUser = sys.argv[2]
authPass = sys.argv[3]

apikey = 'fvm39bpj135u20812je6ibgupv'
zap = ZAPv2(apikey=apikey, proxies={'http': 'http://127.0.0.1:8080', 'https': 'http://127.0.0.1:8080'})

# ZAP starts accessing the target.
print ('Accessing target ' + targetDomain)
zap.urlopen(targetDomain)
time.sleep(2)

context_name = "new_context"
if context_name in zap.context.context_list:
	zap.context.remove_context(context_name, apikey)
context_id = zap.context.new_context(context_name)

zap.context.include_in_context(context_name, targetDomain, apikey)
zap.context.include_in_context(context_name, targetDomain + ".*", apikey)
zap.context.set_context_in_scope(context_name, True, apikey)

print(zap.sessionManagement.set_session_management_method(context_id, 'httpAuthSessionManagement', None, apikey))


print(zap.authentication.set_authentication_method(context_id, 'httpAuthentication', "hostname=rt.com.directvgo.com&realm=global&port=443", apikey))

print(zap.authentication.set_logged_in_indicator(context_id, 'HTTP[^a-z]1.1\s200\sOK', apikey))
print(zap.authentication.set_logged_out_indicator(context_id, 'HTTP[^a-z]1.1\s401\sUnauthorized', apikey))
   
user_id = zap.users.new_user(context_id, "user", apikey)
zap.users.set_authentication_credentials(context_id,user_id, 'username=' + authUser + '&password=' + authPass,apikey)

zap.users.set_user_enabled(context_id, user_id, True, apikey)

zap.forcedUser.set_forced_user(context_id, user_id, apikey)
zap.forcedUser.set_forced_user_mode_enabled(True, apikey)

print("Spidering target")
scanid = zap.spider.scan_as_user(context_id, user_id, targetDomain, None, True, None, apikey)

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
print ('Scanning target: ' + targetDomain)
scanid = zap.ascan.scan(targetDomain)
while (int(zap.ascan.status(scanid)) < 100):
   print ('Scan progress: ' + zap.ascan.status(scanid))
   time.sleep(20)

print('Scan completed')

# Report the results
print ('Hosts: ' + ', '.join(zap.core.hosts))
print ('Alerts: ')

alerts = zap.core.alerts()

json.dump(alerts, open('output.json', 'w'), indent=4)