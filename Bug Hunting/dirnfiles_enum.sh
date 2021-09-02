#!/bin/bash

  TRESULT_DIR=tdirectories.txt
  RESULT_DIR=directories.txt
  TRESULT_F=tfiles.txt
  RESULT_F=files.txt
  TRESULT_T=ttechnologies.txt
  RESULT_T=technologies.txt
  BAUTH=$(cat basicauth.txt)

  echo "dirnfile - starting..." | slackcat -c bug-hunter -s

  buildOutputAndNotify() 
  {
    OFILE=toutput.txt

    if [ ! -f $2 ]
    then
      cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2
      slackcat -c bug-hunter $2
    else
      cp $2 $OFILE
      cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2

      if cmp --silent -- "$2" "$OFILE"; then
        echo "no new results were found" | slackcat -c bug-hunter -s
      else
	      NEWFOUND=dirnfiles-newfound.txt
        comm -23 <(sort $2) <(sort $OFILE) > $NEWFOUND
        slackcat -c bug-hunter $NEWFOUND
        rm $NEWFOUND
      fi

      rm $OFILE
    fi

    rm $1
  }

  echo "dirnfiles - directories enumeration starting..." | slackcat -c bug-hunter -s
  python3 /home/admin/dirsearch/dirsearch.py -l $1 -w dictionaries/directories_dicc.txt --force-recursive -o $TRESULT_DIR --format=csv --auth-type=basic --auth=$BAUTH
  buildOutputAndNotify $TRESULT_DIR $RESULT_DIR
  echo "dirnfiles - directories enumeration done" | slackcat -c bug-hunter -s

  echo "dirnfiles - files enumeration starting..." | slackcat -c bug-hunter -s
  python3 /home/admin/dirsearch/dirsearch.py -l $RESULT_DIR -w dictionaries/files_dicc.txt -o $TRESULT_F --format=csv --auth-type=basic --auth=$BAUTH
  buildOutputAndNotify $TRESULT_F $RESULT_F
  echo "dirnfiles - files enumeration done " | slackcat -c bug-hunter -s

  echo "dirnfiles - technologies enumeration starting..." | slackcat -c bug-hunter -s
  python3 /home/admin/dirsearch/dirsearch.py -l $1 -w dictionaries/technologies_dicc.txt --force-recursive -o $TRESULT_T --format=csv --auth-type=basic --auth=$BAUTH
  buildOutputAndNotify $TRESULT_T $RESULT_T
  echo "dirnfiles - technologies enumeration done" | slackcat -c bug-hunter -s

  echo "dirnfile - done" | slackcat -c bug-hunter -s