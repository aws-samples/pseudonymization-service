# Building a pseudonymization service on AWS to protect sensitive data
Data is changing the world and this fact makes this resource even more
valuable than oil. Given the importance of this new asset law makers are keen to protect the privacy of individuals and prevent any misuse. Organisations often face challenges as they aim to comply
with data privacy regulations like Europe's [General Data Protection Regulation](https://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=OJ%3AL%3A2016%3A119%3AFULL) (GDPR) and the [California Consumer Privacy Act](https://oag.ca.gov/privacy/ccpa) (CCPA). These regulations demand strict access controls to protect sensitive personal data. This project shows a solution which uses a microservice based approach to enable fast and cost-effective pseudonymization of data sets. The solution relies on [AES-GCM-SIV](https://en.wikipedia.org/wiki/AES-GCM-SIV) algorithm to Psedonymize sensitive data.


## Blogpost URL
[Part 1: Build a pseudonymization service on AWS to protect sensitive data](https://aws.amazon.com/blogs/big-data/part-1-build-a-pseudonymization-service-on-aws-to-protect-sensitive-data/)


## Solution Overview

The solution follows a [serverless architecture](https://aws.amazon.com/lambda/serverless-architectures-learn-more/) approach. Pseudonymization logic is written in [java](https://www.java.com/en/) and leverages the java implementation of AES-GCM-SIV developed by [codahale](https://github.com/codahale/aes-gcm-siv). The source code is hosted in an [AWS Lambda lambda](https://aws.amazon.com/lambda/) function. Secret keys are stored securely on [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/). [AWS Key Management System](https://aws.amazon.com/kms/) ensures that secrets and sensitive components are protected at rest. The service is exposed to consumers via [Amazon API Gateway](https://aws.amazon.com/api-gateway/) as a REST Interface. Consumers are authenticated and authorized to consume the endpoints via [API Keys](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-setup-api-key-with-console.html). The solution per se is technology agnostic, it can be addopted by any form of consumer as long as they are able to consume REST Interfaces.


## Architecture 

![Alt text](./Architecture.png?raw=true "Architecture Diagram")

## Resource Deployment

The cloudformation stack will create the following resources:
- API Gateway REST Interface with 2 resources
- Lambda Function acting as the API integration
- Secrets Manager Secret
- KMS Key
- IAM Roles & Policies
- CloudWatch Logs Group

### Parameters:
- **STACK_NAME** - CloudFormation stack name
- **AWS_REGION** - AWS region where the solution will be deployed
- **AWS_PROFILE** - Named profile that will apply to the AWS CLI command
- **ARTEFACT_S3_BUCKET** - S3 bucket where the infrastructure code will be stored. (*The bucket must be created in the same region where the solution lives*)

### Outputs:

- PseudonymizationUrl
- ReidentificationUrl
- KmsKeyArn
- SecretName

## Deployment Commands
All deployments are done using bash scripts, in this case we use the following commands:
 - ```./deployment_scripts/deploy.sh```    -  Packages, builds and deploys the local artifacts that your AWS CloudFormation template (e.g: cfn_template.yaml) is referencing

   ```bash
   ./deployment_scripts/deploy.sh -s STACK_NAME \
   -b ARTEFACT_S3_BUCKET -r AWS_REGION \
   -p AWS_PROFILE
   ```

 - ```./deployment_scripts/destroy.sh```   -  Destroys the CloudFormation Stack you created in the deployment above (e.g: cfnstackdeployment)
   ```bash
   ./deployment_scripts/destroy.sh -s STACK_NAME \
   -p AWS_PROFILE -r AWS_REGION
## Generate and store secrets in Secrets Manager

Run the python script ```helper_scripts\key_generator.py``` to generate the encryption keys via KMS and persist them in Secrets Manager.

```bash
python ./helper_scripts/key_generator.py \
-k KmsKeyArn -s SecretName -r AWS_REGION \
-p AWS_PROFILE 
```

## Test
You may test the solution via postman. In the test folder you can find the postman collection json file with all the necessary configurations to call the REST endpoints. Once imported make sure to fill the variables in the collection. All values will be outputted from `./deployment_scripts/deploy.sh`, except for the API_KEY which you have to fetch from the API Gateway console and the deterministic one which it's up to you to set it to `True` or `False`.

## Prerequisites
- [AWS SAM](https://aws.amazon.com/serverless/sam/)
- [Java JDK](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven](https://maven.apache.org/)
- [AWS CLI](https://aws.amazon.com/cli/)
- [Python](https://www.python.org/)
- [AWS SDK for Python](https://boto3.amazonaws.com/v1/documentation/api/latest/index.html)


## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the [LICENSE](LICENSE) file.
