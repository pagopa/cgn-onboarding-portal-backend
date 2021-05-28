# cgn-onboarding-portal-backend


## Load Test


### Data Gen

Execute load test data generator:

```
mvn -Pload-test -Dexec.mainClass=it.gov.pagopa.cgn.portal.load_performance.SampleDataGenerate -Dexec.classpathScope=test test-compile exec:java -Dexec.args="{row_to_generate}"
```

where `row_to_generate` parameter is the number of rows to generate. If the argument is not passed, 10000 will be used.
  
### Dump

```
pg_dump -U compose-postgres  -h localhost -F c -b postgres -f postgres-database-$(date '+%Y%m%d%H%M').backup
```

### Restore

#### Cleanup existing data

* Stop backend and function app

* Drop DB and create empty

```
psql -U  pgadmin@cgnportaltest-d-db-postgresql -h cgnportaltest-d-db-postgresql.postgres.database.azure.com -d postgres  << EOF\n    SELECT pg_terminate_backend(pg_stat_activity.pid)\n    FROM pg_stat_activity\n    WHERE pg_stat_activity.datname = 'cgnonboardingportal'\n    AND pid <> pg_backend_pid();\nEOF
psql -U  pgadmin@cgnportaltest-d-db-postgresql -h cgnportaltest-d-db-postgresql.postgres.database.azure.com -d postgres  -c 'DROP DATABASE cgnonboardingportal;'
psql -U  pgadmin@cgnportaltest-d-db-postgresql -h cgnportaltest-d-db-postgresql.postgres.database.azure.com -d postgres  -c 'CREATE DATABASE cgnonboardingportal;'
```

* Load dump data

```
pg_restore  -U pgadmin@cgnportaltest-d-db-postgresql -h cgnportaltest-d-db-postgresql.postgres.database.azure.com -d cgnonboardingportal postgres-database-202105271929.backup
```

* Start backend and function app
