/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.console.bs.areas;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.users.Account;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AreasService {

    private static final Log LOG = LogFactory.getLog(AreasService.class.getName());
    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private static final Geometry EMPTY = new GeometryFactory().createEmpty(2);

    /**
     * Used to obtain the {@link Org#getCities() list of city ids} from an
     * {@link Account account}'s organization
     */
    private final @NonNull OrgsDao orgsDao;

    /**
     * Required to resolve relative paths when {@link #areasURI} is set relative to
     * georchestra's data directory
     */
    private final @NonNull GeorchestraConfiguration georConfig;

    /**
     * Location of the GeoJSON FeatureCollection defining allowed areas. Maybe
     * relative to {@literal <datadir>/console/} (e.g. {@code cities.geojson}), or
     * an absolute URL
     */
    private @NonNull String areasURI;

    private AreasDataStore dataStore;

    @PostConstruct
    void initialize() throws IOException {
        LOG.info("Initializing %s from %s".formatted(getClass().getSimpleName(), areasURI));
        URL areasLocation = resolveAreasLocation();
        LOG.info("Areas URI resolved to %s".formatted(areasLocation));
        this.dataStore = new AreasDataStore(areasLocation);
    }

    @VisibleForTesting
    void setAreasURI(@NonNull String uri) throws IOException {
        this.areasURI = uri;
        initialize();
    }

    public URL resolveAreasLocation() throws MalformedURLException {
        URI uri = URI.create(areasURI);
        if (null == uri.getScheme()) {
            if (!georConfig.activated()) {
                throw new IllegalStateException("Georchestra datadirectory is not configured");
            }
            uri = URI.create("file://" + georConfig.getContextDataDir() + File.separator + areasURI);
        }
        return uri.toURL();
    }

    /**
     * Returns the area of competence for the calling user.
     * <p>
     * The area of competence is computed as the geometry union of the
     * {@link Org#getCities() cities} allowed to the organization the calling user
     * belongs to.
     *
     * @throws IOException
     */
    public Geometry getAreaOfCompetence(@NonNull Account account) throws IOException {
        final List<String> cityIds = getCityIds(account);
        if (null == cityIds || cityIds.isEmpty()) {
            LOG.debug("User %s has no area of competence set".formatted(account.getUid()));
            return null;
        }

        List<Geometry> geometries = getGeometries(cityIds);

        Geometry union = unionGeometries(geometries);

        return union;
    }

    private List<Geometry> getGeometries(List<String> cityIds) throws IOException {
        Stopwatch sw = Stopwatch.createStarted();
        List<Geometry> geometries = dataStore.findAreasById(cityIds);
        String msg = "Queried %,d geometries in %s".formatted(geometries.size(), sw.stop());
        LOG.debug(msg);
        return geometries;
    }

    private Geometry unionGeometries(List<Geometry> geometries) {
        Stopwatch sw = Stopwatch.createStarted();
        Geometry union = geometries.stream().parallel().filter(Objects::nonNull).map(g -> g.buffer(0d)).reduce(EMPTY,
                Geometry::union);

        String msg = "Unioned %,d geometries in %s".formatted(geometries.size(), sw.stop());
        LOG.debug(msg);
        return union;
    }

    private List<String> getCityIds(Account account) {
        Org org = orgsDao.findByUser(account);
        if (null == org) {
            return null;
        }

        LOG.debug("Computing area of competence for user %s, org %s".formatted(account.getUid(), org.getName()));
        return org.getCities();
    }

}
