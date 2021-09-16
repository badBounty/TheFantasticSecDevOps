#!/bin/bash

# ./ssl_vulns_test.sh [sslscan.out] [testssl.sh.out] [host]

parsesslscan (){

        SSLvTwo="not vulnerable"
        SSLPOODLE="not vulnerable"
        TLSBEAST="not vulnerable"

        IFS=$'\n'
        set -f

        for line in $(cat < $1); do

                if [[ "$line" == "SSLv2"* ]];then
                        SSLvTwo=$(echo $line)
                fi

                if [[ "$line" == "SSLv3"* ]];then
                        SSLPOODLE=$(echo $line)
                fi

                if [[ "$line" == "TLSv1.0   "* ]];then
                        TLSBEAST=$(echo $line)
                fi
        done

        echo -e "Weak TLS/SSL version enabled:" >> final.txt
        echo -e "-----------------------------\n" >> final.txt

        echo -e "SSLv2 (CVE-2015-3197): $SSLvTwo" >> final.txt
        echo -e "SSLv3 (POODLE SSL): $SSLPOODLE" >> final.txt
        echo -e "TLSv1.0 (BEAST): $TLSBEAST\n" >> final.txt

}

parsetestssl(){

        NULLCIPHER=""
        RCFour_Bar=""
        SHORT_KEY=""
        SWEET=""
        LOGJAM=""
        RSA_FREAK=""
        EDH_FREAK=""
        HEARTBLEED=""
        RENEGOTIATION_VULN=""
        DROWN=""
        CRIME=""
        BREACH=""
        LUCKY=""
        PF_SECRECY="not vulnerable"
        ROBOT=""
        TLS_SCSV=""
        POODLETLS=""

        IFS=$'\n'
        set -f
        for line in $(cat < $1); do

                if [[ "$line" == *"NULL ciphers (no encryption)"* ]]; then
                        NULLCIPHER=$(echo -e $line | sed -e "s/NULL ciphers (no encryption)                  //")
                fi

                if [[ "$line" == *"RC4 (CVE-2013-2566, CVE-2015-2808)"* ]]; then
                        RCFour_Bar=$(echo $line | sed -e "s/RC4 (CVE-2013-2566, CVE-2015-2808)        //")
                fi

                # FALTA SHORT KEY, se reporta RC4
                if [[ "$line" == *"SWEET32 (CVE-2016-2183, CVE-2016-6329)"* ]]; then
                        SWEET=$(echo $line | sed -e "s/SWEET32 (CVE-2016-2183, CVE-2016-6329)    //")
                fi

                if [[ "$line" == *"LOGJAM (CVE-2015-4000)"* ]]; then
                        LOGJAM=$(echo $line | sed -e "s/LOGJAM (CVE-2015-4000), experimental      //")
                fi

                # chequea tanto RSA como EDH
                if [[ "$line" == *"FREAK (CVE-2015-0204)"* ]];then
                        RSA_FREAK=$(echo $line | sed -e "s/FREAK (CVE-2015-0204)                     //")
                #       EDH_FREAK=$(echo $line | sed -e "s/FREAK (CVE-2015-0204)                     //")
                fi

                if [[ "$line" == *"Heartbleed (CVE-2014-0160)"* ]]; then
                        HEARTBLEED=$(echo $line | sed -e "s/Heartbleed (CVE-2014-0160)                //")
                fi

                if [[ "$line" == *"Secure Renegotiation (RFC 5746)"* ]];then
                        RENEGOTIATION_VULN=$(echo $line | sed -e "s/Secure Renegotiation (RFC 5746)           //")
                fi

                if [[ "$line" == *"DROWN (CVE-2016-0800, CVE-2016-0703)"* ]]; then
                        DROWN=$(echo $line | sed -e "s/DROWN (CVE-2016-0800, CVE-2016-0703)      //")
                fi

                if [[ "$line" == *"CRIME, TLS (CVE-2012-4929)"* ]];then
                        CRIME=$(echo $line | sed -e "s/CRIME, TLS (CVE-2012-4929)                //")
                fi

                if [[ "$line" == *"BREACH (CVE-2013-3587)"* ]];then
                        BREACH=$(echo $line | sed -e "s/BREACH (CVE-2013-3587)                    //")
                fi

                if [[ "$line" == *"LUCKY13 (CVE-2013-0169)"* ]];then
                        LUCKY=$(echo $line | sed -e "s/LUCKY13 (CVE-2013-0169), experimental     //")
                fi
                if [[ "$line" == *"No ciphers supporting Forward Secrecy (P)FS offered"* ]];then
                        PF_SECRECY=$(echo $line)
                fi
		if [[ "$line" == *"No ciphers supporting Forward Secrecy offered"* ]];then
			PF_SECRECY=$(echo $line)
		fi
                if [[ "$line" == *"ROBOT"* ]]; then
                        ROBOT=$(echo $line | sed -e "s/ROBOT                                     //")
                fi

                if [[ "$line" == *"TLS_FALLBACK_SCSV (RFC 7507)"* ]];then
                        TLS_SCSV=$(echo $line | sed -e "s/TLS_FALLBACK_SCSV (RFC 7507)              //")
                fi

                if [[ "$line" == *"POODLE, SSL (CVE-2014-3566)"* ]];then
                        POODLETLS=$(echo $line | sed -e "s/POODLE, SSL (CVE-2014-3566)               //")
                fi
        done

        echo -e "Weak TLS cipher-suites supported:" >> final.txt
        echo -e "---------------------------------\n" >> final.txt

        echo -e "NULL cipher suites: $NULLCIPHER" >> final.txt
        echo -e "RC4 encryption algorithm based cipher suites enabled (Bar Mitzvah & RC4 NOMORE): $RCFour_Bar" >> final.txt
        echo -e "Short key length of cipher suites enabled (Less than 128 bits): $SHORT_KEY" >> final.txt
        echo -e "64-bit block size cipher suites supported (SWEET-32): $SWEET" >> final.txt
        echo -e "Short key length of DHE cipher suites less than 2048 bits (Logjam): $LOGJAM" >> final.txt
        echo -e "Export grade RSA cipher suites enabled (FREAK): $RSA_FREAK" >> final.txt

        echo -e "Other TLS Vuls:" >> final.txt
        echo -e "---------------\n" >> final.txt

        echo -e "HeartBleed: $HEARTBLEED" >> final.txt
        echo -e "Insecure renegotiation vulnerability: $RENEGOTIATION_VULN" >> final.txt
        echo -e "DROWN SSL2: $DROWN" >> final.txt
        echo -e "Compression enabled (CRIME): $CRIME" >> final.txt
        echo -e "Compression enabled (BREACH): $BREACH" >> final.txt
        echo -e "LUCKY 13: $LUCKY" >> final.txt
        echo -e "Perfect Forward Secrecy not supported: $PF_SECRECY" >> final.txt
        echo -e "ROBOT: $ROBOT" >> final.txt
        echo -e "TLS Fallback SCSV: $TLS_SCSV" >> final.txt
        echo -e "POODLE TLS: $POODLETLS" >> final.txt
}

parsesslscan $1
parsetestssl $2

mv final.txt $3_ssl_final_output.txt

cat $3_ssl_final_output.txt
