package org.georchestra.atlas.repository;

import org.georchestra.atlas.AtlasJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtlasJobRepository extends JpaRepository<AtlasJob, Long> {

    public AtlasJob findOneByIdAndToken(Long id, String token);
}
