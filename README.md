# Game On! JSR107 Room

[Game On!](https://game-on.org/) is both a sample microservices application, and a throwback text adventure brought to you by the WASdev team at IBM. You can learn more about Game On! at [http://game-on.org/](http://game-on.org/).

This project is a Room for Game On that demonstrates use of the JSR107 Caching annotations api. 

For general setup & getting started instructions creating your own Room, check the original sample room at [https://github.com/gameontext/sample-room-java]

For more information on JSR107 via Redisson in Bluemix, check [my other project](https://github.com/BarDweller/JSR107-RI-CDI-Redisson-Bluemix)

## Heads up!!

> This room is under development, it's starting as a straight fork of the sample room, and I'll be gradually adding the stuff below =)

## Introduction

This room uses JSR107 Caching Annotations to provide the behavior for a number of virtual objects in the room. 

Because the caches are backed by Redis, as a remote service. The state of the caches will work across scaled instances of the Room, if this room is deployed as a container group with multiple instances, then each instance should have the same view of the state stored within the caches, leading to a consistent experience regardless of which instance a players request is routed to.

The room contains a number of Cache based objects that you can interact with, that use JSR107 in various ways...

### Secret Store
#### Overview
The room provides a Secret Store, that will store a secret per Player. This is an example of [@CacheResult](http://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/CacheResult.html) and [@CachePut] (http://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/CachePut.html) annotations, where getting the secret calls a method annotated with @CacheResult, and setting or updating the secret uses a method annotated with @CachePut. 
#### Further adventures.. 
Additionally the Secret Store is configured with a [cacheResolverFactory](http://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/CacheDefaults.html#cacheResolverFactory()) annotation that configures the cache used by the Secret Store to have a 5 minute expiry. This ensures we won't end storing secrets for every player forever. 
#### Usage
Set a secret in the room with `/secret mysecret` and query your secret with `/secret`. You can confirm 5 minutes after setting the secret, that it is forgotten.

### Toggle Switch
#### Overview
The room has a Toggle Switch, that will turn the light on/off in the room. The Toggle does not use JSR107 annotations, but instead constructs the Cache using the non annotated API. 
(Sadly in this case, that's a little more complex than it should be, because I really need to go edit the JSR107 annotation project to allow a better way to configure the default caching provider, compare the methods `getCacheUsingDefaults`, and `getCacheAbusingDefaultProvider` on the [Toggle](https://github.com/BarDweller/gameon-jsr107-room/blob/master/src/main/java/org/gameontext/sample/jsr107toggle/Toggle.java) class for how it should be done, vs how I had to do it at present)
#### Further adventures.. 
Additionally, the Toggle class registers a Cache listener, that will be invoked when the Cache changes. The callbacks from these events are used to trigger messages back to the room. This means that if the room were scaled to mulitple concurrent instances, that flipping the Toggle in one room, will still cause every instance of the room to be aware the state has changed.
#### Usage
Flip the toggle using `/toggle`, and notice the response. Now have someone else flip the toggle, and notice everyone is informed of the state change. 

### Boxes and Items
#### Overview
There are a set of items in the room, and a set of boxes. The Boxes are using named caches, with an additional 'invisible' box acting as the Room itself. The Player is able to move items from the room to a box, or from a box to another box. Each box has it's own named cache storing the items in the box. When a player moves an item from a box to another box, the item is removed from one cache, then added to the other. To keep this operation atomic, an additional cache is used keyed by the item with a value of the player interacting with the item. This acts as a lock, preventing 2 players from moving the same item to different locations.
#### Further adventures..
The box, and the items are built using a simple set of java interfaces that integrate with the room description. This enables items in the room to show up in the `/look` response (via an appropriate [Location](https://book.gameontext.org/microservices/WebSocketProtocol.html#_room_mediator_client_location_message) message. Also, the items are integrated to the main `processCommand` method in [RoomImplementation](https://github.com/BarDweller/gameon-jsr107-room/blob/master/src/main/java/org/gameontext/sample/RoomImplementation.java) where the processCommand invocation is passed onto the [Items](https://github.com/BarDweller/gameon-jsr107-room/blob/master/src/main/java/org/gameontext/sample/items/Items.java) object that acts as a kind of registry of known items. The Items object loops through the items, and asks each in turn if they want to process the command, and the first one that agrees to do so, gets to be the one that handles the invocation. 
The items are also queried for the commands they support, to supplement the `/help` response (again handled via the _Location_ response message). 
The approach is less sophisticated than the one used by the example rooms `RecRoom` and `MugRoom`, which are part of the [gameon-room](https://github.com/gameontext/gameon-room), which also has an extensible item & command approach, but with a more complex parser.
#### Usage
`/examine item` will examine `item`
`/take item from red box` will remove `item` from the `red box` and place it in the room.
`/put item in blue box` will remove `item` from the room and place it in the `red box`
