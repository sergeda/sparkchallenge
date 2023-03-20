# Spark challenge
- [Commands](#commands)
- [Java, scala, sbt versions](#sdkman)
- [Implementation details](#details)

## Commands
* ```sbt test``` -> will run all tests
* ```sbt assembly``` --> will build fat jar

## SDKMAN
* one of the easiest ways to manage java, scala and sbt version in local environment is with [SDKMAN](https://sdkman.io/);
* we simply configure the versions in [.sdkmanrc](.sdkmanrc) file;
* run `sdk env` and it will setup all required versions for you;
* all you need to get up and running is to [install sdkman](https://sdkman.io/install);

## Details
* Uses ZIO Layers to handle dependency injection
* Main functionality implemented in the ChallengeRunner