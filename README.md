# cgn-onboarding-portal-backend

This project is related to operator portal for CGN.

## Shims
This source code contains a couple shims for `springfox`:
- EnableSwagger2: modified to use the new `Swagger2DocumentationWebMvcConfiguration` instead of `Swagger2DocumentationConfiguration`
- RelativePathProvider: modified to extend the new `DefaultPathProvider`