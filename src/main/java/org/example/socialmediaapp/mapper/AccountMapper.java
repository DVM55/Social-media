package org.example.socialmediaapp.mapper;

import org.example.socialmediaapp.dto.req.UpdateAccountRequest;

import org.example.socialmediaapp.dto.res.RegisterResponse;
import org.example.socialmediaapp.dto.res.UpdateAccountResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.UserDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { MinioUrlMapper.class})
public interface AccountMapper {
    RegisterResponse toDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateAccountFromDTO(UpdateAccountRequest accountDTO, @MappingTarget Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    void updateUserDetailFromDTO(UpdateAccountRequest userDetailDto, @MappingTarget UserDetail userDetail);

}
