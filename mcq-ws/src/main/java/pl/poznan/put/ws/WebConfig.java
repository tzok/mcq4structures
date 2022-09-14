package pl.poznan.put.ws;

import java.time.Instant;
import java.util.UUID;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import pl.poznan.put.schema.StructureContentDTO;
import pl.poznan.put.schema.UploadDTO;
import pl.poznan.put.ws.entities.StructureContent;

@Configuration
public class WebConfig {

  @Bean
  public PropertySourcesPlaceholderConfigurer configurePlaceholder() {
    PropertySourcesPlaceholderConfigurer placeholderConfigurer =
        new PropertySourcesPlaceholderConfigurer();
    placeholderConfigurer.setLocations(new ClassPathResource("git.properties"));
    placeholderConfigurer.setIgnoreResourceNotFound(true);
    placeholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
    return placeholderConfigurer;
  }

  @Bean
  public ModelMapper configureModelMapper() {
    ModelMapper modelMapper = new ModelMapper();

    Converter<String, UUID> toUUID = ctx -> UUID.fromString(ctx.getSource());
    Converter<UUID, String> toString = ctx -> ctx.getSource().toString();

    modelMapper
        .typeMap(StructureContent.class, UploadDTO.class)
        .addMappings(
            mapper -> {
              mapper.using(toString).map(src -> src.getId(), UploadDTO::setId);
            });

    modelMapper
        .typeMap(StructureContentDTO.class, StructureContent.class)
        .addMappings(
            mapper -> {
              mapper.map(src -> UUID.randomUUID(), StructureContent::setId);
              mapper.map(src -> Instant.now(), StructureContent::setCreatedAt);
            });

    return modelMapper;
  }
}
