package com.swaply.userservice.mapper;

import com.swaply.userservice.dto.user.UserDto;
import com.swaply.userservice.entity.Admin;
import com.swaply.userservice.entity.Seller;
import com.swaply.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "userRole", constant = "CUSTOMER")
    UserDto entityToDto(User user);

    @Mapping(target = "userRole", constant = "SELLER")
    UserDto sellerToDto(Seller seller);

    @Mapping(target = "userRole", constant = "ADMIN")
    UserDto adminToDto(Admin admin);

    User dtoToEntity(UserDto userDto);

}
