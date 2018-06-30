# Wecky - Website Checker
Checks a given website for modifications. 
Does so by generating a hash of the webpage and adding a new hash and content if the webpage changed.

## Dependencies
Uses `DynamoDB` for persistence. Creates the table `WebsiteHashes` if it doesn't exist yet.

## Running locally
Using [sam-cli](https://docs.aws.amazon.com/lambda/latest/dg/sam-cli-requirements.html) you can run the lambda function locally.
You need a [local DynamoDB](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html) running. 
Set the environment variable `DYNDB_LOCAL` to the URI of your local DynamoDB.

Then run `sam local invoke "CrawlWebsite" -e event.json`
