package cdn.cdn_project.Services.AiServices;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiConversationStore {
    private final Map<String, List<Map<String, Object>>> sessions=new ConcurrentHashMap<>();

    public List<Map<String,Object>>getHistory(String sessionId,String providerName){
        return sessions.computeIfAbsent(key(sessionId,providerName),id ->new ArrayList<>());

    }
   public String key(String sessionId, String providerName){
        return sessionId+"::"+providerName;

   }
   public void clear(String sessionId,String provider){
        sessions.remove(key(sessionId,provider));

   }
}
