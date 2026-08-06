package cdn.cdn_project;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiConversationStore {
    private final Map<String, List<Map<String, Object>>> sessions=new ConcurrentHashMap<>();

    public List<Map<String,Object>>getHistory(String sessionId){
        return sessions.computeIfAbsent(sessionId,id ->new ArrayList<>());

    }
    public void clear(String sessionId){
        sessions.remove(sessionId);
    }
}
