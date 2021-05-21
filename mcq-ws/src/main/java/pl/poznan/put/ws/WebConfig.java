package pl.poznan.put.ws;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.schema.StructureOutputDTO;
import pl.poznan.put.ws.entities.StructureInput;
import pl.poznan.put.ws.entities.StructureOutput;

import java.time.Instant;
import java.util.UUID;

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
        .typeMap(StructureInputDTO.class, StructureInput.class)
        .addMappings(
            mapper -> {
              mapper.using(toUUID).map(src -> src.getId(), StructureInput::setId);
              mapper.map(src -> Instant.now(), StructureInput::setCreatedAt);
            });

    modelMapper
        .typeMap(StructureInput.class, StructureInputDTO.class)
        .addMappings(
            mapper -> {
              mapper.using(toString).map(src -> src.getId(), StructureInputDTO::setId);
            });

    modelMapper
        .typeMap(StructureOutputDTO.class, StructureOutput.class)
        .addMappings(
            mapper -> {
              mapper.using(toUUID).map(src -> src.getId(), StructureOutput::setId);
            });

    modelMapper
        .typeMap(StructureOutput.class, StructureOutputDTO.class)
        .addMappings(
            mapper -> {
              mapper.using(toString).map(src -> src.getId(), StructureOutputDTO::setId);
            });
    return modelMapper;
  }
}
