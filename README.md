# Microservices with a Game On! Room

[Game On!](https://game-on.org/) is both a sample microservices application, and a throwback text adventure brought to you by the WASdev team at IBM. This application demonstrates how microservice architectures work from two points of view:

1. As a Player: navigate through a network/maze of rooms, where each room is a unique implementation of a common API. Each room supports chat, and interaction with items (some of which may be in the room, some of which might be separately defined services as well).
2. As a Developer: learn about microservice architectures and their supporting infrastructure by extending the game with your own services. Write additional rooms or items and see how they interact with the rest of the system.

You can learn more about Game On! at [http://game-on.org/](http://game-on.org/).

## Introduction

This walkthrough will guide you through adding a room to a running Game On! microservices application.  You will be shown how to setup a room that is implemented in the Java programming language using Websphere Liberty and deployed as a Cloud Foundry application in Bluemix.  

### Installation prerequisites

When deployed using an instant runtime, Gameon-room-java requires the following:

- [Bluemix account](https://console.ng.bluemix.net)
- [IBM DevOps Services Account](https://hub.jazz.net/register)
- [GitHub account](https://github.com/)
- [Maven](https://maven.apache.org/install.html)
- Java 8: Any compliant JVM should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),  
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)

## Create Bluemix accounts and log in

Sign up for Bluemix at https://console.ng.bluemix.net and DevOps Services at https://hub.jazz.net. When you sign up, you'll create an IBM ID, create an alias, and register with Bluemix.
* Make a note of your username and org, as you will need both later.
  * By default, the space is dev and the org is the project creator's user name. For example, if sara@example.com signs in to Bluemix for the first time, the active space is dev and the org is sara@example.com.

## Get Game On! ID and Shared Secret

For a new room to register with the Game-On server, you must first log in to game-on.org and sign in using one of several methods to get your **Game On! ID** and **Shared Secret**.

1.  Go to [https://game-on.org/](https://game-on.org/) and click **Enter**.
2.  Select an authentication method to log in with your username and password for that type.
3.  View your user profile using the link in the top right. It is either your username or a person icon.
4.  You should now see your **Game On! ID** and **Shared Secret** near the bottom of the page.

## Getting the source code

The source code is located in GitHub, navigate to our [repository](https://github.com/cfsworkload/gameon-room-java.git) and download the ZIP file and unzip the code on to your local machine. Alternatively you can use the GitHub CLI to clone the repository with `git clone https://github.com/cfsworkload/gameon-room-java.git`.

## Build and deploy

1. `cd gameon-room-java`
2. `mvn install`
  After running this, you will have the server running locally at [http://localhost:9080/](http://localhost:9080/).
  You can use a browser extension to play with the WebSocket according to the
  [Game On! WebSocket protocol](https://gameontext.gitbooks.io/gameon-gitbook/content/microservices/WebsocketProtocol.html").

  Use `mvn clean -P stopServer` to stop the server.

3. Use a maven target profile to push the app to Bluemix:
```
mvn install -P bluemix \
    -Dcf.org= <your organization> \
    -Dcf.username=<your username> \
    -Dcf.password=<your password> \
    -Dcf.target=https://api.ng.bluemix.net \
    -Dapp.name=<cf-app-name> \
    -Dgameon.id=<Your Game On! ID> \
    -Dgameon.secret=<Your Game On! Shared Secret> \
```


### Additional notes:

* `app.name` is a unique, URL-friendly name for your deployed Bluemix app.
* `gameon.id` and `gameon.secret` are those retrieved [earlier](https://github.com/cfsworkload/gameon-room-java#get-game-on-id-and-shared-secret).
* Advanced: if you have an existing Bluemix account or require different Bluemix settings, you can set them on the command line. The default values that are applied:
  * `cf.context=eu-gb.mybluemix.net`
  * `cf.target=https://api.eu-gb.bluemix.net`
  * `cf.space=dev`

## Access room on Game On!

Once the room is set up and it has registered with the server, it will be accessible on [Game On!](https://game-on.org/). It may take a moment for the room to appear.

1. Log in to [Game On!](https://game-on.org/) using the authentication method you used to create your user ID and shared secret for the registered room.
2. Use the Game On! command `/listmyrooms` from The First Room, to see your list of rooms. Once your room is registered, it will appear in that list.
3. To get to your room, navigate through the network or go directly there by using the `/teleport` command from The First Room.
4. Look at the Bluemix log console to see "A new connection has been made to the room"


### List of host provided commands

The Game On! host provides a set a universal commands:
- **/exits** - List of all exits from the current room.
- **/help** - List of all available commands for the current room.
- **/sos** - Go back to The First Room.

### The First Room commands

The First Room is usually where new users will start in Game On!. From there, additional commands are available and maintained by Game On!. For the list of current commands use the `/help` command.
