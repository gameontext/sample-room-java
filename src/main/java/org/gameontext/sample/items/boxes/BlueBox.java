package org.gameontext.sample.items.boxes;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlueBox extends AbstractBox{
    
    Set<String> initialContent = Stream.of("fish","dinosaur","ball").collect(Collectors.toSet());
    
    @PostConstruct
    public void init() {
        this.setInitialContents(initialContent);
    }
    
    @Override
    public String getName() {
        return "blue box";
    }

    @Override
    public String getDescription() {
        return "A Big Blue Box";
    }
}
