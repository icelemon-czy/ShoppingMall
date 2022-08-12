package com.shoppingmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Store token in cache.
 */
public class TokenCache {
    public static final String token_prefix = "token_";
    private static Logger logger =  LoggerFactory.getLogger(TokenCache.class);
    // LRU Cache
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                // Override load method to load data
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    public static String getKey(String key){
        try{
            String value = localCache.get(key);
            if(value.equals("null")){
                return null;
            }
            return value;
        }catch (Exception e){
             logger.error("localCache get error",e);
             return null;
        }
    }

}
