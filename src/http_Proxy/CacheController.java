package http_Proxy;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CacheController {
private static CacheController singleCache=null;
Map<String, String> cache=new HashMap<String,String>();//It has to be global.
private CacheController(){
	
}

public static synchronized CacheController getInstance(){
	if(singleCache==null){
		singleCache=new CacheController();
		return singleCache;
	}else{
		return singleCache;
	}
	
	
	}
public String getValue(String url){
	return cache.get(url);
}

public boolean hasValue(String url){
	 return cache.containsKey(url);
}

public void add(String key, String value){
	cache.put(key, value);
}

}
