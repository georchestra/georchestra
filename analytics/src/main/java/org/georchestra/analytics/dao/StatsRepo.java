package org.georchestra.analytics.dao;

import org.georchestra.analytics.model.Stats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsRepo extends CrudRepository<Stats, Long> {

    public List<Stats> findBySurname(String surname);

    public List<Stats> findByName(String name);

}