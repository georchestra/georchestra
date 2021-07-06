package org.georchestra.datafeeder.api;

import org.georchestra.config.security.GeorchestraUserDetails;
import org.georchestra.datafeeder.model.UserInfo;
import org.mapstruct.Mapper;

@Mapper
public interface UserInfoMapper {

    UserInfo map(GeorchestraUserDetails principal);
}
