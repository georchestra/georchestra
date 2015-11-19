package org.georchestra.ldapadmin.dao;

import org.georchestra.ldapadmin.model.EmailEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public interface EmailDao extends CrudRepository<EmailEntry, Long> {

    List<EmailEntry> findBySender(UUID sender);
    List<EmailEntry> findByRecipient(UUID recipient);

}
