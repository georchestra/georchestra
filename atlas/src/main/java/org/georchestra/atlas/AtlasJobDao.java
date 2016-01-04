package org.georchestra.atlas;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtlasJobDao extends CrudRepository<AtlasMFPJob, Long> {

//    public List<AtlasMFPJob> findByUUID(UUID uuid);

}
