# SCIM 2.0 Compliance Tests

A set of integration tests that are executed against SCIM 2.0 provider implementation to verify if it is compliant with the SCIM 2.0 standard.

## Prerequisite

### Required tools
- JDK Version 1.8 or higher

### Installation
If you don't want to use the released executable jar or add some more custom changes, you can build your own executable jar. For that reason `maven 3.x` is required.

Build project **scimono-compliance-tests** with maven:

```
mvn clean install
```

## Run integration tests

Just execute the shaded executable jar `scimono-compliance-tests-${version}-shaded.jar`

```
java -jar scimono-compliance-tests-${version}-shaded.jar scim.service.url=http://example.com/scim/v2
```

## Target System Authentication

Some of the SCIM 2.0 service providers require authentication. Our SCIM compliance tests, support two types of authentication:
### Basic Authentication

Values for the command line arguments  `basic.auth.user` and `basic.auth.password` must be provided alongside with `auth.type=Basic` parameter. For example:
```
java -jar scimono-compliance-tests-${version}-shaded.jar scim.service.url=http://localhost:8080/scim/v2 auth.type=Basic basic.auth.user=admin basic.auth.password=password
```

### Oauth 2.0 Authentication

Currently supported oauth grant is only **Client Credentials**
Values for the following command line arguments: `oauth.clientId`;  `oauth.secret`; `oauth.service.url` must be provided alongside with `oauth.grant=client_credentials` parameter. For example:

```
java -jar scimono-compliance-tests-${version}-shaded.jar scim.service.url=http://localhost:8080/scim/v2 auth.type=Oauth oauth.grant=client_credentials oauth.clientId=admin oauth.secret=secret oauth.service.url=http://localhost:8080/oauth/token
```

### Custom Headers
Headers should be in single quotes separated by comma
```
custom.headers='key:val','key2:val2'
```

```
java -jar scimono-compliance-tests-${version}-shaded.jar scim.service.url=http://localhost:8080/scim/v2 auth.type=Oauth oauth.grant=client_credentials oauth.clientId=admin oauth.secret=secret oauth.service.url=http://localhost:8080/oauth/token custom.headers='key:v1;v2','key2:value'
```

## Run custom tests

Many of the SCIM 2.0 service providers implement only some of the SCIM 2.0 features. It is possible to execute subset of all tests that are provided. You can specify which tests to be included in the test execution in `.csv` file with the following format:
```
${Full class name}=${test methods separated by ','}
```

Example:
```csv
com.sap.scimono.scim.system.tests.E2EUserComplianceTest=testCreateUserRequiredAttributes, testCreateUserRequiredAttributesAndGet
com.sap.scimono.scim.system.tests.UserOperationsHttpResponseCodeTest=testGetUser200, testGetUserNonExistingId404
...
```


## Command line parameters

| Arg name | Required | Description |
| --- | --- | --- |
| scim.service.url  |  Yes |  The root URL of SCIM 2.0 service provider, e.g. http://localhost:8080/scim/v2
| auth.type  |  No |  The authentication method that service provider requires. Supported values: `Basic` / `Oauth`
| oauth.grant  | No  | The grant type used used for Oauth 2.0. Supported values: `client_credentials`
| oauth.clientId  | No  | Oauth 2.0 clientId  |
| oauth.secret  | No  | Oauth 2.0 secret  |
| oauth.service.url  | No  | Oauth 2.0 service Url used to retrieve request token  |
| basic.auth.user  | No  | Username for Basic authentication  |
| basic.auth.password  | No  | Password for Basic authentication  |
| tests.file.path  | No  | Path to `.csv` file where custom subset of all tests is specified fo execution  |
| custom.headers  | No  | Custom headers. Should be in single quotes separated by comma


## Test Results

After tests execution finishes, a log file with name `testLogs.log` is generated in the current directory from where the executable jar was started. Its content sequentially describes each action executed against the target system.

JUnit 4 compatible XML reports are also generated in the same folder.