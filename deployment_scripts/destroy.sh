#!/bin/bash

### chmod +x destroy.sh 

DEPENDENCIES=(aws)
declare  -i var OPTIONS_FOUND
OPTIONS_FOUND=0

while getopts ":s:p:r:" opt; do
#  OPTIONS_FOUND=1
  case $opt in
    s) STACK_NAME="$OPTARG" OPTIONS_FOUND+=1
    ;;
    p) AWS_PROFILE="$OPTARG" OPTIONS_FOUND+=1
    ;;
    r) AWS_REGION="$OPTARG" OPTIONS_FOUND+=1
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    exit 1
    ;;
    :) echo "Option -$OPTARG requires an argument." >&2
    exit 1
    ;;
  esac
done
if ((OPTIONS_FOUND!=3)); then
  echo "Please make sure to pass all the required options \"-s -p -r\""
  exit 1
fi

unset OPTIONS_FOUND

function check_dependencies_mac()
{
  dependencies=$1
  for name in ${dependencies[@]};
  do
    [[ $(which $name 2>/dev/null) ]] || { echo -en "\n$name needs to be installed. Use 'brew install $name'";deps=1; }
  done
  [[ $deps -ne 1 ]] || { echo -en "\nInstall the above and rerun this script\n";exit 1; }
}

function check_dependencies_linux()
{
  dependencies=$1
  for name in ${dependencies[@]};
  do
    [[ $(which $name 2>/dev/null) ]] || { echo -en "\n$name needs to be installed. Use 'sudo apt-get install $name'";deps=1; }
  done
  [[ $deps -ne 1 ]] || { echo -en "\nInstall the above and rerun this script\n";exit 1; }
}

## Check dependencies by OS
if [ "$(uname)" == "Darwin" ]; then
    check_dependencies_mac "${DEPENDENCIES[*]}"   
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    check_dependencies_linux "${DEPENDENCIES[*]}"
else
  echo "Only Mac and Linux OS supported, exiting ..."
  exit 1   
fi

aws cloudformation delete-stack --stack-name ${STACK_NAME} --profile ${AWS_PROFILE} --region ${AWS_REGION}