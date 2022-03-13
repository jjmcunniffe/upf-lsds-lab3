# upf-lsds-lab3
## Installation
1. Create a [Twitter Developer](https://developer.twitter.com/) account. Create a new application and add its API key, secret and bearer token to a config file located under `/src/main/resources/application.properties`. Apply for Elevated account access.
2. Install [Docker Desktop](https://www.docker.com/get-started) on your computer.
  a. Ensure that the Docker Daemon is running by typing `docker ps` in your command line. You should an empty table.
  b. Run `docker compose up -d`. This will install everything and start the container.
  c. Run `docker ps` again. Copy the "CONTAINER_ID" value.
3. Build the application using `mvn package`. Your `.jar` file will be located in `/target`.

### IMPORTANT: PROJECT CONFIGURATION
This development toolchain is configured with IntelliJ IDEA in mind. You will need to ensure that your compiled .jar files are placed in a `target/` directory in order for them to be seen by docker-compose and the container.

## Usage
1. To execute "Twitter Streaming Example" task, run `job_twitterstreamingexample.sh`.
2. To execute "Real-time Twitter Stateless Exercise" task, run `job_twitterstateless.sh`.
3. ...

## Contributers
* Jonathan Cunniffe (249512)
* Belal "Bill" Khalil (______)
