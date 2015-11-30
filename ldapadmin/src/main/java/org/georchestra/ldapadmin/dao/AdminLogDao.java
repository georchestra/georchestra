package org.georchestra.ldapadmin.dao;

import org.georchestra.ldapadmin.model.AdminLogEntry;
import org.georchestra.ldapadmin.model.EmailEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminLogDao extends CrudRepository<AdminLogEntry, Long> {

    @Transactional
    List<AdminLogEntry> findByAdmin(UUID admin);

    @Transactional
    List<AdminLogEntry> findByTarget(UUID target);


    List<AdminLogEntry> findByType(UUID target);

    List<AdminLogEntry> findByTargetPageable(UUID target, Pageable pageable);

}
