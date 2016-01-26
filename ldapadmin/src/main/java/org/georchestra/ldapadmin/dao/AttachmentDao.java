package org.georchestra.ldapadmin.dao;

import org.georchestra.ldapadmin.model.Attachment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentDao extends CrudRepository<Attachment, Long> {


}
