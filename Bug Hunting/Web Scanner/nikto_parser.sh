#!/bin/bash

# test.sh [nikto output]

isbody=0

IFS=$'\n'
set -f

for line in $(cat < $1);do

        if [[ "$line" == *"Target IP:"* ]];then
                echo $line >> $1_nikto_output.txt
        fi

        if [[ "$line" == *"Message:"* ]]; then
                echo $line >> $1_nikto_output.txt
                echo "---------------------------------------------------------------------------" >> $1_nikto_output.txt
        fi

        if [[ $isbody == 1 ]];then
                echo $line >> $1_nikto_output.txt
        fi

        if [[ "$line" == *"Server:"* ]]; then

                echo $line >> $1_nikto_output.txt
                if [[ $isbody != 1 ]];then
                        isbody=$(expr $isbody + 1)
                fi
        fi
done

cat $1_nikto_output.txt
