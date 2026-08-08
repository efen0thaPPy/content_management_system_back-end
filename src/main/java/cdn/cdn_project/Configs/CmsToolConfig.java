package cdn.cdn_project.Configs;

import cdn.cdn_project.Services.CmsToolsService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CmsToolConfig {

    @Bean
    public ToolCallbackProvider cmsToolCallbacks(CmsToolsService cmsToolsService){
        return MethodToolCallbackProvider.builder().toolObjects(cmsToolsService).build();
    }
}
