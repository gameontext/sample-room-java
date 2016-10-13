# Microservices with a Game On! Room

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4d099084aab34a57893e8fd29df79ae3)](https://www.codacy.com/app/gameontext/gameon-room-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gameontext/gameon-room-java&amp;utm_campaign=Badge_Grade)

[Game On!](https://game-on.org/) is both a sample microservices application, and a throwback text adventure brought to you by the WASdev team at IBM. This application demonstrates how microservice architectures work from two points of view:

1. As a Player: navigate through a network/maze of rooms. Each room is an autonomous service, supports chat, and may provide interaction with items (some of which may be in the room, some of which might be separately defined services as well).
2. As a Developer: learn about microservice architectures and their supporting infrastructure by creating your own microservices to extend the game.

You can learn more about Game On! at [http://game-on.org/](http://game-on.org/).

## Introduction

This walkthrough will guide you through creating and deploying a microservice that adds a simple room to the running Game On! microservices application.  You will be shown how to setup a room that is implemented in the Java programming language using Websphere Liberty and (a) deployed as a Cloud Foundry application in Bluemix, or (b) as a docker container that can be run locally or published to the IBM Container Service in Bluemix. 

## Installation prerequisites

- [Maven](https://maven.apache.org/install.html)
- Java 8: Any compliant JVM should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),  
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)
    
## Getting the source code

The source code is located in GitHub, navigate to our [repository](https://github.com/gameontext/gameon-room-java.git) and create a fork of the repository into your own repo. Navigate to your new fork and clone the repository with `git clone https://github.com/<<yourGitHubId>>/gameon-room-java.git`. Alternatively you can download the ZIP file and unzip the code on to your local machine.

## Building the app locally

1. `cd gameon-room-java`
2. `mvn install`
3. `mvn package -PstartLocal`
  After running this, you will have the server running locally at [http://localhost:9080/](http://localhost:9080/).
  You can use a browser extension to play with the WebSocket according to the
  [Game On! WebSocket protocol](https://book.game-on.org/microservices/WebSocketProtocol.html).

## Deploying your room

To deploy your room you can either:
* [Deploy to Bluemix as a Cloud Foundry app](#deploying-to-bluemix-as-a-cloud-foundry-app)
or
* [Deploy using Docker](#deploy-using-docker)

## Registering your room

Microservices in production should support automatic scaling, with multiple instances of the room microservice running in parallel, with new instances starting or existing instances stopping at unpredictable times.

To register your room you need the websocket endpoint. This will vary depending on where you have deployed your app, but should look something like:
* Bluemix/Container group in IBM Container Service:
  * US South: `ws://<cf-app-name>.mybluemix.net/room`
  * United Kingdom: `ws://<cf-app-name>.eu-gb.mybluemix.net/room`
* Single container instance in IBM Container Service
  * `ws://<ip address>:9080/room`

Use the Edit Rooms dialog in Game On! to register your room:

1.  Go to [GameOn](https://game-on.org) and sign in.
2.  Once you are signed in, go to the top right of the browser window and click on your username (or person icon).
3.  From this window, again click the top right panel to select **Edit rooms**.
4.  Under **Select or create a room**, make sure **create a room** is selected from the dropdown.
5.  Fill in the room information as specified. If you don't know all the details yet, leave those blank and come back and fill them in later.
6.  Click **Create** and the room will be created for you.

## Visit your room in the game

Once the room is set up and it has registered with Game On!, it will be accessible as a room in the game.

1. If you aren't in The First Room, use `/sos` to return there.
2. Use the Game On! command `/listmyrooms` from The First Room, to see your list of rooms. Your newly registered room should appear in that list.
3. Use the `/teleport` command to go directly to your room from The First Room to see it in action.

Congratulations, you've deployed a microservice that extended an existing microservices-based application so that it can do something new.

Suggested activities:
* Make it more resilient -- add additional instances using the autoscaling add-on: https://console.ng.bluemix.net/catalog/services/auto-scaling
* Consider how to allow chat messages to propagate between independent instances using a shared datastore or cache, or an event bus, or...
* Want some more ideas, check out the [Advanced Adventures section](https://gameontext.gitbooks.io/gameon-gitbook/content/walkthroughs/createMore.html) of our GitBook.


### List of host provided commands

The Game On! host provides a set a universal commands:
- **/exits** - List of all exits from the current room.
- **/help** - List of all available commands for the current room.
- **/sos** - Go back to The First Room.

### The First Room commands

The First Room is usually where new users will start in Game On!. From there, additional commands are available and maintained by Game On!. For the list of current commands use the `/help` command.


## Deploying to Bluemix as a Cloud Foundry app

### Prerequisites for Bluemix deployment

- [Bluemix account](https://console.ng.bluemix.net)
- [IBM DevOps Services Account](https://hub.jazz.net/register)
- [GitHub account](https://github.com/)
- [Cloud Foundry command line](https://docs.cloudfoundry.org/cf-cli/)

### Create Bluemix accounts and log in

Sign up for Bluemix at https://console.ng.bluemix.net and DevOps Services at https://hub.jazz.net. When you sign up, you'll create an IBM ID, create an alias, and register with Bluemix.
* Make a note of your username and org, as you will need both later.
  * By default, the space is dev and the org is the project creator's user name. For example, if sara@example.com signs in to Bluemix for the first time, the active space is dev and the org is sara@example.com.
* Make a note of your region! (US South, United Kingdom, or Australia)
  * When you log into Bluemix, your logged in username, organization, and space are shown in the top right. If you click in the top right corner, you'll see the region displayed in the panel displayed on the right side of the screen.
  
### Deploying the app

1. Login to the Cloud Foundry command line: `cf login`
2. Enter Bluemix API endpoint
  * From the Bluemix console, click on your username in the top right corner. You'll see the region displayed in the panel on the right side of the screen.
  * US South: `https://api.ng.bluemix.net`
  * London: `https://api.eu-gb.bluemix.net`
3. Enter email and password for Bluemix login
4. Enter the Bluemix organization
5. Enter the Bluemix space
6. `cf push <cf-app-name> -p gojava-wlpcfg/target/wlp/usr/servers/gojava-room/gojava-room.zip`

**NOTE:** Choose a unique app name to be included as part of the URL (`cf-app-name`). It must not contain spaces or special characters.

After your room has been pushed, you should be able to view it at:
  * US South: `http://<cf-app-name>.mybluemix.net/`
  * United Kingdom: `http://<cf-app-name>.eu-gb.mybluemix.net/`

#### Additional notes:

* `app.name` is a unique, URL-friendly name for your deployed Bluemix app.
* `gameon.id` and `gameon.secret` are those retrieved [earlier](https://github.com/cfsworkload/gameon-room-java#get-game-on-id-and-shared-secret).  

### Next step

Now you need to [register your room](#registering-your-room) using the Websocket URL. Go to your app endpoint, either by going directly to the URL or by clicking on the route in your Bluemix Dashboard to see your Websocket URL. This will vary by region, but should look something like:
  * US South: `ws://<cf-app-name>.mybluemix.net/room`
  * United Kingdom: `ws://<cf-app-name>.eu-gb.mybluemix.net/room` 



## Deploy using Docker

### Deploying locally

It is possible to deploy your room locally into a Docker container. This can be useful if you want to test aspects such as the room registration and configuration retrieval. Remember, this room will be running locally on your hardware so Game On will not be able to access it, unless your machine is also publicly accessible.

#### Installation prerequisites

1. [Docker Engine](https://docs.docker.com/engine/installation/)
2. [Docker Compose](https://docs.docker.com/compose/install/)

#### Deploying

Once docker is installed, then you deploy your room with

* `mvn package` to build your room.
* Create a file called `docker-compose.override.yml` which contains the folllowing
```
gojava:
 volumes:
   - './gojava-wlpcfg/target/wlp/usr/servers/gojava-room:/opt/ibm/wlp/usr/servers/defaultServer'
```
* `docker-compose build`
* `docker-compose up`

Note: you can optionally use `docker-compose up -d` to run the container as a background process. Use `docker-compose stop` to stop the container.

After this you will have a docker container with your room, running Liberty, and listening on port 9080.
* If you’re running a \*nix variant, you can access it at http://127.0.0.1/
* If you’re running Mac or Windows, access it using the [IP of the host](https://gameontext.gitbooks.io/gameon-gitbook/content/walkthroughs/local-docker.html#dockerhost)

A note about `docker-compose.override.yml`, this is an override file that can be used to change, or add to, an existing docker build file. In this case, it maps the file system on the local machine into the dropins directory for the Liberty server running inside the container. The end result is that if you make some changes to your code and run `mvn package` again to rebuild your war file, then Liberty will see that the file has changed and automatically reload your room without having to build or restart the container.

#### Debugging your room

It is possible to attach a debugger to your room so that you can set breakpoints and step through code. Add the following lines to the `docker-compose.override.yml` file

```
ports:
 - "7777:7777"
environment:
 - LIBERTY_MODE: debug
```

The `ports` section instructs docker to expose the port 7777 from inside the container, so that the debugger can attach. The `environment` statement sets an environment variable called `LIBERTY_MODE` to debug. This variable is read by the Liberty startup script and controls how the server is started, in this case in debug mode.

### Deploying to Bluemix with IBM Container Services

#### Installation prerequisites

1. [Cloud foundry API](https://github.com/cloudfoundry/cli/releases)
2. [Install the IBM COntainers plugin](https://console.ng.bluemix.net/docs/containers/container_cli_cfic_install.html)

#### Deploying

1. Log in to the IBM container service. This needs to be done in two stages:
  1. Log into the Cloud Foundry CLI using `cf login`. Ypu will need to specify the API endpoint as `api.ng.bluemix.net` for the US South server, or `api.eu-gb.bluemix.net` for the UK server.
  2. After this run the command `cf ic login`. This will perform the authentication to the IBM Container Service.
2. Build the container in the Bluemix registry by running the command  `cf ic build -t gojava .` from inside the `gojava-wlpcfg` directory.
3. Run `cf ic images` and check your image is available.
4. Start the container by running the command `cf ic run -p 9080 --name gojava <registry>/<namespace>/gojava`. You can find the full path from the output of `cf ic images`. An example would be:

  `cf ic run -p 9080 --name gojava registry.ng.bluemix.net/pavittr/gojava`

5. While you are waiting for the container to start, request a public IP address using the command `cf ic ip request`. This will return you a public IP address you can bind to your container.
6. With the returned IP address, bind it using the command `cf ic ip bind <ip address> gojava`.
7. Issue `cf ic ps`, and wait for your container to go from "Networking" to "Running".
8. Now you can go to `http://<ip address>:9080` and access the Liberty welcome page.
  
### Deploy as a container group

Instead of deploying a container as a single instance, you can instead deploy a container group. A container group can be used to deploy multiple instances of the same container and load balance between them.

1. Log in to the IBM container service. This needs to be done in two stages:
  1. Log into the Cloud Foundry CLI using `cf login`. Ypu will need to specify the API endpoint as `api.ng.bluemix.net` for the US South server, or `api.eu-gb.bluemix.net` for the UK server.
  2. After this run the command `cf ic login`. This will perform the authentication to the IBM Container Service.
2. Run `cf ic images` and check the `gojava` image is available. If not, run the command `cf ic build -t gojava .` from inside the `gojava-wlpcfg` directory to create it.
3. Create the container group by running `cf ic group create -p 9080 -n <appName> --name gojavagroup <registry>/<namespace>/gojava`. You can find the full path from the output of `cf ic images`. An example would be:

  `cf ic group create -p 9080 --name gojavagroup registry.ng.bluemix.net/pavittr/gojava`

4. Run the command ` cf ic route map -n <appHost> -d mybluemix.net  gojavagroup`. This will make your containers available at <appHost>.mybluemix.net.
5. Run the command `cf ic group instances gojavagroup` to check the status of your instances. Once they are in "Running" state your group is ready to use.
6. Now you can go to `http://<appHost>.mybluemix.net` and access the Liberty welcome page.

