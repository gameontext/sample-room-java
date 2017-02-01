# Game On! JSR107 Room

[Game On!](https://game-on.org/) is both a sample microservices application, and a throwback text adventure brought to you by the WASdev team at IBM. You can learn more about Game On! at [http://game-on.org/](http://game-on.org/).

This project is a Room for Game On that demonstrates use of the JSR107 Caching annotations api. 

For general setup & getting started instructions creating your own Room, check the original sample room at [https://github.com/gameontext/sample-room-java]

For more information on JSR107 via Redisson in Bluemix, check [my other project](https://github.com/BarDweller/JSR107-RI-CDI-Redisson-Bluemix)

## Heads up!!

| This room is under development, it's starting as a straight fork of the sample room, and I'll be gradually adding the stuff below =)

## Introduction

This room uses JSR107 Caching Annotations to provide the behavior for a number of virtual objects in the room. 
Each virtual object in the room has it's own implementation class, so you can look at how the annotations are used.

There's a Secret Store, that will store a secret per Player. This is an example of [@CacheResult](http://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/CacheResult.html) and [@CachePut] (http://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/CachePut.html) annotations, where getting the secret calls a method annotated with @CacheResult, and setting or updating the secret uses a method annotated with @CachePut

There's a Toggle Switch, that will turn the light on/off in the room. The Switch is backed by a method annotated with @CacheResult with the 'skipGet' parameter applied. 

There's also a set of items in the room, and a set of boxes. The Boxes are using named caches, with an additional 'invisible' box acting as the Room itself. The Player is able to move items from the room to a box, or from a box to another box. Each box has it's own named cache storing the items in the box. When a player moves an item from a box to another box, the item is removed from one cache, then added to the other. To keep this operation atomic, a final cache is used keyed by the item with a value of the player interacting with the item. This acts as a lock, preventing 2 players from moving the same item to different locations.

Finally, the room attempts to track the players in the room using a cache keyed by the player id. 

Because the caches are backed by Redis, as a remote service. The state of the caches will work across scaled instances of the Room, if this room is deployed as a container group with multiple instances, then each instance should have the same view of the state stored within the caches, leading to a consistent experience regardless of which instance a players request is routed to.

