#!/bin/bash

### chmod +x deploy.sh 
SOURCE_TEMPLATE="cloudformation_template.yaml"

DEPENDENCIES=(mvn aws sam)
declare -i var OPTIONS_FOUND
OPTIONS_FOUND=0


while getopts ":s:b:p:r:" opt; do
  case $opt in
    s) STACK_NAME="$OPTARG" OPTIONS_FOUND+=1
    ;;
    b) ARTIFACT_S3_BUCKET="$OPTARG" OPTIONS_FOUND+=1
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

if ((OPTIONS_FOUND !=4)); then
  echo "Please make sure to pass all the required options \"-s -b -p -r\""
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

#1. Build JAR
mvn clean package

# #2. SAM deployment
sam deploy -t ${SOURCE_TEMPLATE} \
      --stack-name ${STACK_NAME} \
      --s3-bucket ${ARTIFACT_S3_BUCKET} \
      --capabilities CAPABILITY_IAM \
      --region ${AWS_REGION} \
      --profile ${AWS_PROFILE}
