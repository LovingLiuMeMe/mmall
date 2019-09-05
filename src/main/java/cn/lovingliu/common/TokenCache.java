package cn.lovingliu.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Author：LovingLiu
 * @Description: 存储token的缓存
 * @Date：Created in 2019-09-05
 */
public class TokenCache {

    public static final String TOKEN_PREFIX = "token_"; // token key的前缀

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    private static LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder()
            .initialCapacity(1000)  // 设置缓存的初始化容量 使用的是guava缓存
            .maximumSize(10000) // 设置最大的缓存容量 当大于该值时 才有LRU算法 删除最少使用值
            .expireAfterAccess(12, TimeUnit.HOURS) // 设置缓存有效时间
            .build(new CacheLoader<String, String>() {
                //默认的数据加载的实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法进行加载
                @Override
                public String load(String key) throws Exception {
                    return null;
                }
            });
    public static void setKey(String key,String value){
        loadingCache.put(key,value);
    }
    public static String getKey(String key){
        String value = null;
        try{
            value = loadingCache.get(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        return value;
    }

}
