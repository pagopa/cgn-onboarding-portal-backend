# cgn-onboarding-portal-backend

Execute load test data generator:
- ```mvn -Pload-test -Dexec.mainClass=it.gov.pagopa.cgn.portal.load_performance.SampleDataGenerate -Dexec.classpathScope=test test-compile exec:java -Dexec.args="{row_to_generate}" ```
where ```row_to_generate``` parameter is the number of rows to generate. If the argument is not passed, 10000 will be used.