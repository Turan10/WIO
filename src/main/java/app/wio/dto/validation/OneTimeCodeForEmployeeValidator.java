package app.wio.dto.validation;

import app.wio.dto.UserRegistrationDto;
import app.wio.dto.UserRoleDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class OneTimeCodeForEmployeeValidator implements
        ConstraintValidator<ValidOneTimeCodeForEmployee, UserRegistrationDto> {

    @Override
    public boolean isValid(UserRegistrationDto dto, ConstraintValidatorContext context) {
        if (dto.getRole() == UserRoleDto.EMPLOYEE) {
            String oneTimeCode = dto.getOneTimeCode();
            if (oneTimeCode == null || oneTimeCode.trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "One-time code is required for EMPLOYEE registrations."
                ).addPropertyNode("oneTimeCode").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
