# Postgresql docker image

⚠️ If you use postgresql in docker, there's a change since https://github.com/georchestra/georchestra/pull/4574

The volume used by postgres to store data is now `/var/lib/postgresql/` instead of `/var/lib/postgresql/data`.

