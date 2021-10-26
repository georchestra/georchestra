package org.georchestra.datafeeder.api;

import java.util.Optional;

import org.georchestra.config.security.GeorchestraUserDetails;
import org.georchestra.datafeeder.model.Organization;
import org.georchestra.datafeeder.model.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserInfoMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "postalAddress", source = "user.postalAddress")
    @Mapping(target = "telephoneNumber", source = "user.telephoneNumber")
    @Mapping(target = "title", source = "user.title")
    @Mapping(target = "notes", source = "user.notes")
    UserInfo map(GeorchestraUserDetails principal);

    default Organization map(Optional<org.georchestra.security.model.Organization> value) {
        return map(value.orElse(null));
    }

    Organization map(org.georchestra.security.model.Organization georOrg);
}
