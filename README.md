# cgn-onboarding-portal-backend

This project is related to operator portal for CGN.

## Shims

This source code contains a couple shims for `springfox`:

- EnableSwagger2: modified to use the new `Swagger2DocumentationWebMvcConfiguration` instead of
  `Swagger2DocumentationConfiguration`
- RelativePathProvider: modified to extend the new `DefaultPathProvider`

## Changelog:

### 📢 vesion: 1.0.1

✨Added maven configuration for upgrade version:

1. These are the commands you can run:
      ```bash
      mvn validate versions:set -P [increment-patch | increment-minor | increment-major]
      ```

   For only evaluate:

      ```bash
      mvn validate help:evaluate -Dexpression=newVersion -q -DforceStdout -P [increment-patch | increment-minor | increment-major]
      ```

   After that, launch: mvn clean install.


2. Otherwise you can run this script which increases the version and then runs clean install
      ```bash
      mvn ./upgrade_version.sh  [increment-patch | increment-minor | increment-major]
      ```