package org.gameontext.sample.jsr107defaultprovider;

import java.io.StringReader;
import java.util.logging.Level;

import javax.cache.CacheManager;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.gameontext.sample.Log;
import org.jsr107.ri.annotations.DefaultCacheResolverFactory.DefaultCacheManagerProvider;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.JCacheManager;

public class RedissonCacheManagerProvider implements DefaultCacheManagerProvider {

    private static volatile RedissonClient redisson;
    
    private void parseVcapServices(){
        Log.log(Level.INFO,this,"Processing VCAP_SERVICES for configuration");
        //Attempt to configure redisson from vcap_Services
        String vcap_services = System.getenv("VCAP_SERVICES");
        if( vcap_services != null && vcap_services.length()>0 ){
              try{
                  //read the vcap_services
                  JsonReader reader = Json.createReader(new StringReader(vcap_services));
                  JsonObject root = reader.readObject();
                  //get back info for 'rediscloud' service, we only expect one, but 
                  //vcap allows for multiple, so it's an array..
                  JsonArray rediscloud = root.getJsonArray("rediscloud");
                  //possible the service isn't bound?
                  if(rediscloud!=null){
                      //if we had 2 different rediscloud services bound to this
                      //app, then we'd need to differentiate between them here.
                      //but thankfully, we can just use the one and only =)
                      JsonObject instance = rediscloud.getJsonObject(0);

                      //with the service being there, grab all the connection info.. 
                      JsonObject creds = instance.getJsonObject("credentials");
                      String port = creds.getString("port");
                      String host = creds.getString("hostname");
                      String pwd  = creds.getString("password");

                      Log.log(Level.INFO,this,"Using Redis server at "+host+":"+port);

                      //Build a direct redisson config for the bound redis service
                      Config redissonConfig = new Config();
                      redissonConfig.useSingleServer().setAddress(host+":"+port).setPassword(pwd);

                      //Configure our redisson client.
                      Log.log(Level.INFO,this,"Reddison Built ? "+(redisson!=null));
                      synchronized (this){
                        if(redisson == null){
                          Log.log(Level.INFO,this,"Storing redisson client for "+host+":"+port);
                          redisson = Redisson.create(redissonConfig);
                        }else{
                          Log.log(Level.INFO,this,"Not creating redisson, as already built");
                        }
                      }
                    
                  }else{
                      Log.log(Level.INFO,this,"vcap_services was missing the rediscloud entry, is the service bound?");
                  }

                  reader.close();
              }catch(Exception e){
                  //for now.. a generic catch all to prevent the errors being hidden by cdi during init.. 
                  Log.log(Level.SEVERE,this,"Caught Exception during vcap services processing ", e);
              }
        }
    }
    
    @Override
    public synchronized CacheManager getDefaultCacheManager() {
        if(redisson != null) {
            Log.log(Level.INFO,this,"Skipping VCAP_SERVICES parse and using existing redisson client");
        } else {
            parseVcapServices();
        }
        if(redisson !=null ){
            CacheManager manager = new JCacheManager((Redisson)redisson, JCacheManager.class.getClassLoader(), null, null, null);
            return manager;
        }
        throw new IllegalStateException("Unable to create JCacheManager, issue with vcap_services?");
    }


}
