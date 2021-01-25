#!/bin/bash
export ZAP_AUTH_HEADER_VALUE=$1
echo $ZAP_AUTH_HEADER_VALUE
./zap.sh -daemon -host 0.0.0.0 -port 8080 -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -config api.key=fvm39bpj135u20812je6ibgupv
