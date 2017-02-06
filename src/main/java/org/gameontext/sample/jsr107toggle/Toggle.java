package org.gameontext.sample.jsr107toggle;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import javax.cache.spi.CachingProvider;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gameontext.sample.Log;
import org.gameontext.sample.protocol.Message;
import org.gameontext.sample.protocol.SessionSender;
import org.jsr107.ri.annotations.DefaultCacheResolverFactory;
import org.jsr107.ri.annotations.DefaultCacheResolverFactory.CacheConfigCustomizer;

@ApplicationScoped
public class Toggle {
    
    @Inject
    SessionSender sender;
    
    private Cache<String,String> toggleCache;
    
    public class MyCacheEntryListener implements CacheEntryCreatedListener<String, String>,
            CacheEntryUpdatedListener<String, String>, Serializable {
        private static final long serialVersionUID = -1306798197522730101L;

        public MyCacheEntryListener() {
        }

        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            Log.log(Level.INFO, this, "Cache.onCreated invoked");
            for (CacheEntryEvent<? extends String, ? extends String> entryEvent : cacheEntryEvents) {
                Message m = Message.createBroadcastEvent("The toggle has been initialized, it is currently "+entryEvent.getValue());
                sender.sendMessage(m);
            }
        }

        @Override
        public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            Log.log(Level.INFO, this, "Cache.onUpdated invoked");
            for (CacheEntryEvent<? extends String, ? extends String> entryEvent : cacheEntryEvents) {
                Message m = Message.createBroadcastEvent("The toggle has changed state, it is now "+entryEvent.getValue());
                sender.sendMessage(m);
            }
        }
    }
    
    private Cache<String,String> getCacheUsingDefaults(){
        CachingProvider prov = Caching.getCachingProvider();
        
        CacheManager mgr = prov.getCacheManager();
        
        MutableConfiguration<String, String> config =
                new MutableConfiguration<String,String>().setStoreByValue(true);
        
        return mgr.createCache("toggle", config);
    }
    
    private Cache<String,String> getCacheAbusingDefaultProvider(){
        DefaultCacheResolverFactory dcrf = new DefaultCacheResolverFactory(new CacheConfigCustomizer() {
            @Override
            public void customizeConfiguration(MutableConfiguration<Object, Object> config) {
                config.setStoreByValue(true);
            }
        });
        
        CacheResolver cr = dcrf.getCacheResolver(new CacheMethodDetails<Annotation>() {
            @Override
            public Method getMethod() {
                return null;
            }
            @Override
            public Set<Annotation> getAnnotations() {
                return null;
            }
            @Override
            public Annotation getCacheAnnotation() {
                return null;
            }
            @Override
            public String getCacheName() {
                return "toggle";
            }
        });
        
        return cr.resolveCache(null);
    }
    
    
    
    @PostConstruct
    public void init(){
        //without using annotations, we have to setup our cache bit by bit.. 
        //toggleCache = getCacheUsingDefaults();
        toggleCache = getCacheAbusingDefaultProvider();

        MyCacheEntryListener mcel = new MyCacheEntryListener();
        
        CacheEntryListenerConfiguration<String,String> listenConfig = new MutableCacheEntryListenerConfiguration<String,String>(
                FactoryBuilder.factoryOf(mcel),null,false,true);
        
        toggleCache.registerCacheEntryListener(listenConfig); 
        
        toggleCache.putIfAbsent("toggle", "on");
    }
    
    public static class BooleanToggle implements EntryProcessor<String,String,Object>{

        @Override
        public Object process(MutableEntry<String,String> entry, Object... arguments) throws EntryProcessorException {
            Log.log(Level.INFO, this, "toggling toggle");
            if(entry.getValue().equals("off"))
                entry.setValue("on");
            else {
                entry.setValue("off");
            }
            return null;
        }
        
    }
    
    public void toggle(){
        toggleCache.invoke("toggle", new BooleanToggle());
    }
    
    public String getToggleState(){
        return toggleCache.get("toggle");
    }

}
