package ee.aleksale.remindly.modules.adhoc.dto.mapper;

import ee.aleksale.remindly.core.model.domain.EventEntity;
import ee.aleksale.remindly.modules.adhoc.dto.Adhoc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdhocMapper {

  AdhocMapper INSTANCE = Mappers.getMapper(AdhocMapper.class);

  @Mapping(target = "type", constant = "ADHOC")
  EventEntity map(Adhoc adhoc);

}
