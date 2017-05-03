# Game On! Microservices and Java

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f4e298d0b25849868cc9c0a16fd83485)](https://www.codacy.com/app/gameontext/sample-room-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gameontext/sample-room-java&amp;utm_campaign=Badge_Grade)
[![Known Vulnerabilities](https://snyk.io/test/github/gameontext/sample-room-java/badge.svg)](https://snyk.io/test/github/gameontext/sample-room-java)
[![Codecov Badge](https://codecov.io/gh/gameontext/sample-room-java/branch/master/graph/badge.svg)](https://codecov.io/gh/gameontext/sample-room-java)

[Game On!](https://gameontext.org/) is both a sample microservices application, and a throwback text adventure brought to you by the WASdev team at IBM. This application demonstrates how microservice architectures work from two points of view:

1. As a Player: navigate through a network/maze of rooms, and interact with other players and the items or actions available in each room.
2. As a Developer: extend the game by creating simple services that define rooms. Learn about microservice architectures and their supporting infrastructure as you build and scale your service.

You can learn more about Game On! at [http://gameontext.org/](http://gameontext.org/).

## Introduction

This walkthrough will guide you through creating and deploying a simple room (a microservice) to the running Game On! application. This microservice is written in Java as a web application deployed on Websphere Liberty.

The microservice can be (a) deployed as a Cloud Foundry application or (b) built into a docker container.

Game On! communicates with this service (a room) over WebSockets using the [Game On! WebSocket protocol](https://book.gameontext.org/microservices/WebSocketProtocol.html). Consider this a stand-in for asynchronous messaging like MQTT, which requires a lot more setup than a simple WebSocket does.

## Requirements

- [Maven](https://maven.apache.org/install.html)
- Java 8: Any compliant JVM should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)

## Let's get started!

1. Create your own fork of this repository ([what's a fork?](https://help.github.com/articles/fork-a-repo/))
2. Create a local clone of your fork ([Cloning a repository](https://help.github.com/articles/cloning-a-repository/))

## Build the service locally

1. `cd sample-room-java`
2. `mvn install`
3. `mvn liberty:run-server`

After running this, the server will be running locally at [http://localhost:9080/](http://localhost:9080/).
* Visiting this page provides a small form you can use to test the WebSocket endpoint in your service directly.
* A health URL is also defined by the service, at http://localhost:9080/rest/health

## Make your room public!

For Game On! to include your room, you need to tell it where the publicly reachable WebSocket endpoint is. This usually requires two steps:

* [hosting your service somewhere with a publicly reachable endpoint](https://book.gameontext.org/walkthroughs/deployRoom.html), and then
* [registering your room with the game](https://book.gameontext.org/walkthroughs/registerRoom.html).

## Build a docker container

Creating a Docker image is straight-up: `docker build .` right from the root menu.

A `docker-compose.yml` file is also there, which can be used to specify overlay volumes to allow local development without restarting the container. See the [Advanced Adventure for local development with Docker](https://book.gameontext.org/walkthroughs/local-docker.html) for a more detailed walkthrough.

## Ok. So this thing is running... Now what?

We know, this walkthrough was simple. You have a nice shiny service that has a REST API (/rest/health),
and emulates async messaging behavior via a WebSocket. So?

The purpose of this text-based adventure is to help you grapple with microservices concepts and technologies
while building something other than what you do for your day job (it can be easier to learn new things
when not bogged down with old habits). This means that the simple service that should be humming along
merrily with your name on it is the beginning of your adventures, rather than the end.

Here is a small roadmap to this basic service, so you can go about making it your own:

* `org.gameontext.sample.RoomImplementation`
   This is class contains the core elements that make your microservice unique from others.
   Custom commands and items can be added here (via the `org.gameontext.sample.RoomDescription`
   member variable). The imaginatively named `handleMessage` method, in particular, is called
   when new messages arrive.

* `org.gameontext.sample.protocol.*`
   This package contains a collection of classes that deal with the mechanics of the websocket
   connection that the game uses to allow players to interact with this service. `RoomEndpoint`
   is what it says: that is the WebSocket endpoint for the service.

* `org.gameontext.sample.rest.*`
   This package defines a REST endpoint, with a single defined path: /health

* `src/main/liberty` contains configuration for Liberty, a lightweight Java EE composable app server

* `src/test` -- Yes! There are tests!

Things you might try:

* Use RxJava to manage all of the connected WebSockets together as one event stream.
* Call out to another API (NodeRed integration, Watson API, Weather API) to perform actions in the room.
* Integrate this room with IFTTT, or Slack, or ...
* .. other [Advanced Adventures](https://book.gameontext.org/v/walkthrough/walkthroughs/createMore.html)!

Remember our https://gameontext.org/#/terms. Most importantly, there are kids around: make your parents proud.

## How the build works

This project is built using Maven and makes use of the [Liberty Maven plugin](https://github.com/WASdev/ci.maven) and the [Cloud Foundry Maven plugin](https://docs.run.pivotal.io/buildpacks/java/build-tool-int.html#maven) to integrate with Liberty and Bluemix.

### Server feature definitions

For those of you familiar with the Liberty server configuration you will know that features are enabled in the server by adding a <featureManager/> element to the server.xml. For this project the <featureManager/> is provided by snippets from the [Liberty app accelerator](http://liberty-app-accelerator.wasdev.developer.ibm.com/start/). This means that there is no <featureManager/> element in the [server.xml](src/main/liberty/config/server.xml) file. When the build is run these will appear in the server's configDropins/defaults directory.

### Testing

You can write two types of tests: unit and integration tests.  The unit tests will use the maven-surefire-plugin to run any tests found in packages that include "unit" in their name. The integration tests will:
1. Start a Liberty server
2. Use the maven-failsafe-plugin to run any tests that have packages that include "it" in their names
3. Stop the Liberty server
As integration tests are longer running they can be skipped by providing the skipTests flag: `mvn install -DskipTests`.

### Code Coverage

The [JaCoCo maven plugin](http://www.eclemma.org/jacoco/trunk/doc/maven.html) is included in the build to generate code coverage reports. It will generate reports in multiple formats (HTML, XML, and CSV) in `target/site/jacoco`.

You can also access code reports on the web at [codecov.io](https://codecov.io) if your project is a public Github project built with Travis. The included `travis.yml` file includes a command to upload the code coverage reports automatically.

### Build phases

The following shows what goals run at which phases in the [default Maven lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

| Phase                 | Plugin                  | Goal              | Profile          | Notes |
| --------------------- | ----------------------- | ----------------- | ---------------- | --- |
| initialize            | maven-dependency-plugin | properties        | All              | Enables the copy of server snippets later on |
| initialize            | maven-enforcer-plugin   | enforce           | bluemix          | Makes sure the properties are set if deploying to Bluemix |
| initialize            | maven-antrun-plugin     | run               | bluemix          | Prints out what is going to be pushed |
| initialize            | maven-enforcer-plugin   | enforce           | existing-install | Checks that if the liberty.install property is set that it points to an existing directory. |
| initialize            | jacoco-maven-plugin     | prepare-agent     | All              | Prepares a property pointing to the JaCoCo runtime agent for code coverage. |
| test                  | maven-surefire-plugin   | test              | All              |  |
| test                  | jacoco-maven-plugin     | report            | All              | Creates a code coverage report. |
| prepare-package       | liberty-maven-plugin    | install-server    | All              | Creates the server using the server.xml in the src directory |
| package               | maven-war-plugin        | war               | All              |  |
| package               | maven-dependency-plugin | copy-server-files | All              | Copies the server.xml snippets that contain the <featureManager/> elements |
| package               | maven-resources-plugin  | copy-resources    | All              | Copies the WAR into the server |
| package               | liberty-maven-plugin    | package-server    | All              | Creates a ZIP or JAR (depending of if the `runnable` profile is enabled) containing the server and WAR |
| package               | cf-maven-plugin         | push              | bluemix          | Pushes the server up to bluemix |
| pre-integration-test  | liberty-maven-plugin    | start-server      | liberty-test     | Doesn't run when -DskipTests is set |
| integration-test      | maven-failsafe-plugin   | integration-test  | All              |  |
| post-integration-test | liberty-maven-plugin    | stop-server       | liberty-test     | Doesn't run when -DskipTests is set |
| verify                | maven-failsafe-plugin   | verify            | All              |  |
| n/a                   | liberty-maven-plugin    | n/a               | runnable         | Just sets properties to indicate that a runnable JAR should be made rather than a ZIP when packaging the server |
| n/a                   | liberty-maven-plugin    | n/a               | downloadLiberty  | Just sets properties that are used in the install-server goal to installs the Liberty runtime. Doesn't run if liberty.install is set to an existing install of Liberty |
| n/a                   | liberty-maven-plugin    | n/a               | existing-install | Just sets properties that are used in the other Liberty goals to point to an existing Liberty install. Only runs if liberty.install is set to an existing install of Liberty |
