package pl.poznan.put.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class WebConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer configurePlaceholder(){
        PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        placeholderConfigurer.setLocations(new ClassPathResource("git.properties"));
    placeholderConfigurer.setIgnoreResourceNotFound(true);
    placeholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
    return placeholderConfigurer;
    }
}
