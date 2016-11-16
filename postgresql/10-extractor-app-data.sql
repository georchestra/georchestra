SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = extractorapp, pg_catalog;

INSERT INTO extractor_log (id, creation_date, duration, username, roles, org, request_id) VALUES (1, NOW() - (random() * '1 month'::interval), NULL, 'testadmin', '{ROLE_MOD_LDAPADMIN,ROLE_MOD_EXTRACTOR_APP}', 'psc', '6af2cec3-a828-4dee-8f4b-4e1dc61808f0');
INSERT INTO extractor_log (id, creation_date, duration, username, roles, org, request_id) VALUES (2, NOW() - (random() * '1 month'::interval), '00:00:11.398534', 'testuser', '{ROLE_MOD_LDAPADMIN,ROLE_MOD_EXTRACTOR_APP}', 'psc', 'ff702dac-5159-4f73-b2eb-0681385bf49b');
SELECT pg_catalog.setval('extractor_log_id_seq', 2, true);

INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (1, 1, 'EPSG:2154', 10, 'shp', '0103000020E610000001000000050000002A76A3D255B500405ECCD7BDC37D49402A76A3D255B500406D650596BB7F4940B93F9115FFF500406D650596BB7F4940B93F9115FFF500405ECCD7BDC37D49402A76A3D255B500405ECCD7BDC37D4940', 'WFS', 'https://sdi.georchestra.org/geoserver/wfs?', 'test:adresse_dep59', NULL);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (3, 1, 'EPSG:2154', 10, 'shp', '0103000020E610000001000000050000002A76A3D255B500405ECCD7BDC37D49402A76A3D255B500406D650596BB7F4940B93F9115FFF500406D650596BB7F4940B93F9115FFF500405ECCD7BDC37D49402A76A3D255B500405ECCD7BDC37D4940', 'WFS', 'https://sdi.georchestra.org/geoserver/wfs?', 'pmauduit_test:armoires-fo', NULL);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (4, 1, 'EPSG:2154', 10, 'geotiff', '0103000020E61000000100000005000000CFE5306CEC360840087476BDA14F4940CFE5306CEC3608405410737E0C5849407504D710536F09405410737E0C5849407504D710536F0940087476BDA14F4940CFE5306CEC360840087476BDA14F4940', 'WCS', 'https://sdi.georchestra.org/geoserver/wcs?', 'test:scan_50_historique', NULL);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (2, 1, 'EPSG:2154', 5, 'shp', '0103000020E610000001000000050000000857AB9030B50040736E80B9C37D49400857AB9030B500406D650596BB7F4940B93F9115FFF500406D650596BB7F4940B93F9115FFF50040736E80B9C37D49400857AB9030B50040736E80B9C37D4940', 'WFS', 'https://sdi.georchestra.org/geoserver/wfs?', 'test:adresse_dep62', false);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (6, 2, 'EPSG:2154', 5, 'shp', '0103000020E610000001000000050000000857AB9030B50040736E80B9C37D49400857AB9030B500406D650596BB7F4940B93F9115FFF500406D650596BB7F4940B93F9115FFF50040736E80B9C37D49400857AB9030B50040736E80B9C37D4940', 'WFS', 'https://sdi.georchestra.org/geoserver/wfs?', 'test:adresse_dep62', false);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (5, 2, 'EPSG:2154', 10, 'shp', '0103000020E610000001000000050000002A76A3D255B500405ECCD7BDC37D49402A76A3D255B500406D650596BB7F4940B93F9115FFF500406D650596BB7F4940B93F9115FFF500405ECCD7BDC37D49402A76A3D255B500405ECCD7BDC37D4940', 'WFS', 'https://sdi.georchestra.org/geoserver/wfs?', 'test:adresse_dep59', true);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (7, 2, 'EPSG:2154', 10, 'shp', '0103000020E610000001000000050000002A76A3D255B500405ECCD7BDC37D49402A76A3D255B500406D650596BB7F4940B93F9115FFF500406D650596BB7F4940B93F9115FFF500405ECCD7BDC37D49402A76A3D255B500405ECCD7BDC37D4940', 'WFS', 'https://sdi.georchestra.org/geoserver/wfs?', 'pmauduit_test:armoires-fo', true);
INSERT INTO extractor_layer_log (id, extractor_log_id, projection, resolution, format, bbox, owstype, owsurl, layer_name, is_successful) VALUES (8, 2, 'EPSG:2154', 10, 'geotiff', '0103000020E61000000100000005000000CFE5306CEC360840087476BDA14F4940CFE5306CEC3608405410737E0C5849407504D710536F09405410737E0C5849407504D710536F0940087476BDA14F4940CFE5306CEC360840087476BDA14F4940', 'WCS', 'https://sdi.georchestra.org/geoserver/wcs?', 'test:scan_50_historique', true);

SELECT pg_catalog.setval('extractor_layer_log_id_seq', 8, true);
