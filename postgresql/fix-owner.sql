CREATE OR REPLACE FUNCTION change_owner(my_schema text, new_owner text)
  RETURNS void AS
$BODY$
DECLARE
  my_query text;
BEGIN

  -- fix schemas owner
  RAISE NOTICE 'Fixing schemas owner';
  FOR my_query IN SELECT 'ALTER SCHEMA "'|| schemaname ||'" OWNER TO "' || new_owner || '"' FROM pg_tables WHERE schemaname = my_schema GROUP BY schemaname LOOP
    RAISE NOTICE 'query : %', my_query;
    EXECUTE my_query;
  END LOOP;

  -- fix table owner
  RAISE NOTICE 'Fixing tables owner';
  FOR my_query IN SELECT 'ALTER TABLE "'|| schemaname || '"."' || tablename ||'" OWNER TO "' || new_owner || '"' FROM pg_tables WHERE schemaname = my_schema ORDER BY schemaname, tablename LOOP
    RAISE NOTICE 'query : %', my_query;
    EXECUTE my_query;
  END LOOP;

  -- fix sequence owner
  RAISE NOTICE 'Fixing sequence owner';
  FOR my_query IN SELECT 'ALTER SEQUENCE "'|| sequence_schema || '"."' || sequence_name ||'" OWNER TO "' || new_owner || '"' FROM information_schema.sequences WHERE sequence_schema = my_schema ORDER BY sequence_schema, sequence_name LOOP
    RAISE NOTICE 'query : %', my_query;
    EXECUTE my_query;
  END LOOP;

-- fix views owner
  RAISE NOTICE 'Fixing views owner';
  FOR my_query IN SELECT 'ALTER VIEW "'|| table_schema || '"."' || table_name ||'" OWNER TO "' || new_owner || '"' FROM information_schema.views WHERE table_schema = my_schema ORDER BY table_schema, table_name LOOP
    RAISE NOTICE 'query : %', my_query;
    EXECUTE my_query;
  END LOOP;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE;