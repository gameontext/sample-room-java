package org.gameontext.sample.jsr107resolverfactories;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

public class FiveMinuteExpiringCacheResolverFactory extends AbstractCacheResolverFactory {
    
    //configure caches to have 5 minute expiry from creation.
    @Override
    protected MutableConfiguration<Object,Object> getConfig(){
        MutableConfiguration<Object,Object> config = new MutableConfiguration<Object,Object>();
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
        return config;
    }
    


}
