package org.gameontext.sample.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gameontext.sample.Item;
import org.gameontext.sample.items.boxes.BlueBox;
import org.gameontext.sample.items.boxes.RedBox;
import org.gameontext.sample.items.boxes.Room;

@ApplicationScoped
public class Items {
    
    @Inject
    protected RedBox redBox;
    @Inject
    protected BlueBox blueBox;
    @Inject
    protected Room room ;
    
    @Inject
    protected Ball ball;
    @Inject
    protected Badger badger;
    @Inject 
    protected Dinosaur dinosaur;
    @Inject
    protected Fish fish;
    @Inject
    protected Giraffe giraffe;
    @Inject
    protected Kitten kitten;
    @Inject
    protected Stilettos stilettos;
    @Inject
    protected Wedges wedges;
    
    Map<String,Item> itemsByName = new HashMap<String,Item>();
    
    @PostConstruct
    public void init(){
        itemsByName.put(redBox.getName(),redBox);
        itemsByName.put(blueBox.getName(),blueBox);
        itemsByName.put(badger.getName(), badger);
        itemsByName.put(ball.getName(),ball);        
        itemsByName.put(dinosaur.getName(), dinosaur);
        itemsByName.put(fish.getName(), fish);
        itemsByName.put(giraffe.getName(), giraffe);
        itemsByName.put(kitten.getName(), kitten);
        itemsByName.put(stilettos.getName(), stilettos);
        itemsByName.put(wedges.getName(), wedges);
        //don't include the room box here.. 
    }
    
    //Something somewhere needs to map from strings to actual items.. 
    public Item getItemByName(String name){
        if(itemsByName.containsKey(name))
            return itemsByName.get(name);
        else
            throw new IllegalArgumentException("Unknown item "+name);
    }
    
    public Set<Item> getItemsInRoom(){
        return room.getContents().stream().map(this::getItemByName).collect(Collectors.toSet());
    }
    
    public boolean processCommand(String command, String userId, String username){
        //loop through the room objects and see if any of them want to handle this request.. 
        boolean handled = getItemsInRoom().stream().anyMatch(item -> item.processCommand(command, userId, username));
        
        if(!handled){
            //loop through container items and see if any of them want to handle the request.
            handled = redBox.getContents().stream().map(this::getItemByName).anyMatch(item -> item.processCommand(command, userId, username));
        }
        if(!handled){
            //loop through container items and see if any of them want to handle the request.
            handled = blueBox.getContents().stream().map(this::getItemByName).anyMatch(item -> item.processCommand(command, userId, username));
        }
        return handled;
    }
}
