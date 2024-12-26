package app.wio.mapper;

import app.wio.dto.response.CompanyDto;
import app.wio.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {java.util.stream.Collectors.class})
public interface CompanyMapper {

    @Mapping(
            target = "floorIds",
            expression = "java(company.getFloors().stream()"
                    + ".map(f -> f.getId()).collect(Collectors.toList()))"
    )
    @Mapping(
            target = "userIds",
            expression = "java(company.getUsers().stream()"
                    + ".map(u -> u.getId()).collect(Collectors.toList()))"
    )
    CompanyDto toDto(Company company);
}
