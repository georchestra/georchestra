
SET search_path TO ogcstatistics,public,pg_catalog;
ALTER TABLE ogc_services_log ADD COLUMN roles text[];
CREATE INDEX roles_index ON ogc_services_log USING GIN (roles);

-- Then in a regular bash session
-- echo "SELECT distinct user_name FROM ogcstatistics.ogc_services_log" | psql -nt  georchestra
-- For each users, gets their groups:
--  ldapsearch -bou=groups,dc=georchestra,dc=org -x '(member=uid=user,ou=users,dc=georchestra,dc=org)'  cn | grep '^cn:' | cut -d' ' -f2
-- Then update the DB
-- echo "UPDATE ogcstatistics.ogc_services_log SET roles = string_to_array('list,of,groups', ',')::text[] WHERE user_name = 'user' ;" | psql georchestra


