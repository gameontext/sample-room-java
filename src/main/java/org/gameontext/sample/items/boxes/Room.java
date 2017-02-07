package org.gameontext.sample.items.boxes;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gameontext.sample.items.Items;

@ApplicationScoped
public class Room extends AbstractBox{
    
    @Inject
    Items items;
    
    Set<String> initialContent = Stream.of("kitten","badger","red box","blue box").collect(Collectors.toSet());
    
    @PostConstruct
    public void init() {
        this.setInitialContents(initialContent);
    }
    
    @Override
    public String getName() {
        return "room";
    }

    @Override
    public String getDescription() {
        return "The Invisible Box that holds the Room Inventory.:)";
    }
}
