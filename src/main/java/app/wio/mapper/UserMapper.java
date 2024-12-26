package app.wio.mapper;

import app.wio.dto.UserRegistrationDto;
import app.wio.dto.response.UserResponseDto;
import app.wio.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "token", ignore = true)
    @Mapping(
            target = "companyId",
            expression = "java(user.getCompany() != null ? user.getCompany().getId() : null)"
    )

    @Mapping(
            target = "companyName",
            expression = "java(user.getCompany() != null ? user.getCompany().getName() : null)"
    )
    UserResponseDto toDto(User user);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "verificationTokens", ignore = true)
    @Mapping(target = "passwordResetTokens", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    User toEntity(UserRegistrationDto dto);
}
