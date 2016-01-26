package org.georchestra.ldapadmin.dao;

import org.georchestra.ldapadmin.model.Attachment;
import org.georchestra.ldapadmin.model.EmailTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateDao extends CrudRepository<EmailTemplate, Long> {


}
