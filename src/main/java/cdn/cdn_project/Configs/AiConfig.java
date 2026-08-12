package cdn.cdn_project.Configs;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;


    @Value("${groq.api.key}")
    private String groqApiKey;

    @Bean
    public RestClient geminiRestClient(RestClient.Builder builder) {


        return builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("x-goog-api-key", geminiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient groqRestClient(RestClient.Builder builder) {


        return builder
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer "+ groqApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

