#!/bin/bash

  ID=$(echo $1 | awk -F[/\"] '$0=$(NF-1)')
  TRESULT_DIR=$ID/tdirectories.txt
  RESULT_DIR=$ID/directories.txt
  TRESULT_F=$ID/tfiles.txt
  RESULT_F=$ID/files.txt
  TRESULT_T=$ID/ttechnologies.txt
  RESULT_T=$ID/technologies.txt
  BAUTH=$(cat basicauth.txt)

  echo "Starting directories and files enumeration..." | slackcat -c general -s

  buildOutputAndNotify() {
    OFILE=$ID/toutput.txt

    if [ ! -f $2 ]
    then
      cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2
      slackcat -c general $2
    else
      cp $2 $OFILE
      cat $1 | awk -F, '(NR==1){h1=$1;h2=$2;h3=$3;h4=$4;next} {print $1}' | sort > $2

      if cmp --silent -- "$2" "$OFILE"; then
        echo "Nothing new found." | slackcat -c general -s
      else
        slackcat -c general $2
      fi

      rm $OFILE
    fi

    rm $1
  }

  if [ ! -d $ID ];
  then
    mkdir $ID
  fi

  dirsearch -l $1 -w dictionaries/directory-list-2.3-big.txt,dictionaries/KitchensinkDirectories.fuzz.txt,dictionaries/raft-large-directories.txt --force-recursive -o $TRESULT_DIR --format=csv --auth-type=basic --auth=$BAUTH
  buildOutputAndNotify $TRESULT_DIR $RESULT_DIR

  dirsearch -l $RESULT_DIR -w dictionaries/raft-large-files.txt -o $TRESULT_F --format=csv --auth-type=basic --auth=$BAUTH
  buildOutputAndNotify $TRESULT_F $RESULT_F

  dirsearch -l $1 -w dictionaries/AdobeCQ-AEM.txt,dictionaries/nginx.txt,dictionaries/oracle.txt --force-recursive -o $TRESULT_T --format=csv --auth-type=basic --auth=$BAUTH
  buildOutputAndNotify $TRESULT_T $RESULT_T

  echo "Directories and files enumeration finished." | slackcat -c general -s
