package org.georchestra.ldapadmin.dao;

import org.georchestra.ldapadmin.model.EmailEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Repository
public interface EmailDao extends CrudRepository<EmailEntry, Long> {

    @Transactional
    List<EmailEntry> findBySender(UUID sender);

    @Transactional
    List<EmailEntry> findByRecipient(UUID recipient);

}
