<img align="right" width=350 src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/ef/Emojione_1F458.svg/256px-Emojione_1F458.svg.png">

# SAP SCIMono Library
Open source SCIM 2.0 client and server library.

[![REUSE status](https://api.reuse.software/badge/github.com/SAP/scimono)](https://api.reuse.software/info/github.com/SAP/scimono)

# Description
## Features

SCIMono provides drop-in support for serving a SCIM v2 API. Supported features:

  * Fully SCIM v2 compliant
  * Support for the following resources: Users, Groups, Schemas
  * Resource paging (index-based as required by SCIM spec. & id-based for custom scenarios)
  * Filtering (full support for SCIM filtering syntax spec.)
  * Any auth method (OAuth, by default)
  * Patch support (complex single-/multi-resource updated)
  * ETags
 
# Requirements

 * [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher
 * [Apache Maven 3.0](https://maven.apache.org) or higher
 * JAX-RS Reference Implementation ([Jersey](https://jersey.github.io), [Apache CXF](http://cxf.apache.org))
 * [ANTLR 4.x](https://www.antlr.org) (filter syntax grammar)
 * [SLF4J 1.7.x](https://www.slf4j.org)
 
# Download and Installation
## Download
  Clone the repository to your local machine.
  ```shell
  git clone https://github.com/SAP/scimono.git
  cd scimono
```

## Installation
```shell
mvn clean install
```
## Usage

  To use it, you need the following Maven dependency:
  ```xml
    <dependency>
        <groupId>com.sap.scimono</groupId>
        <artifactId>scimono-server</artifactId>
        <version>${project.version}</version>
    </dependency>
  ```
    
  Exposing an API endpoint is then as easy as:
    
  ```java
    import com.sap.scimono.SCIMApplication;
    
    import jakarta.ws.rs.ApplicationPath;
    
    @ApplicationPath("scim")
    public class MySCIMApi extends SCIMApplication {}
  ```  

# Configuration

Out of the box, you get a default configuration (exposed via the standard /ServiceProvider):

  * 100 resources per page
  * OAuth auth

The default implementation will return *501 Not Implemented* for resource operations.

The library provides 5 standard callbacks that plug into the default resources:

  * UsersCallback
  * GroupsCallback
  * SchemasCallback (for exposing custom schemas)
  * ConfigurationCallback
  * ResourceTypesCallback (for exposing custom resource types)

They are instantiated on a per-request basis (multi-tenancy support is straightforward to achieve) and are cached for the lifetime of the request. To use them, override the corresponding methods exposed by SCIMApplication:

```java
import com.sap.scimono.SCIMApplication;
import com.sap.scimono.callback.config.SCIMConfigurationCallback;
import com.sap.scimono.callback.groups.GroupsCallback;
import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.callback.users.ResourceTypesCallback;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("scim")
public class MySCIMApi extends SCIMApplication {
  @Override
  public UsersCallback getUsersCallback() {
    return new MongoUserStorage(currentTenant);
  }

  @Override
  public GroupsCallback getGroupsCallback() {
    return new MongoGroupStorage(currentTenant);
  }

  @Override
  public SchemasCallback getSchemasCallback() {
    return new MongoSchemaStorage(currentTenant);
  }

  @Override
  public SCIMConfigurationCallback getConfigurationCallback() {
    return new MySCIMConfiguration();
  }

  @Override
  public ResourceTypesCallback getResourceTypesCallback() {
      return new new MongoResourceTypeStorage(currentTenant);
  }
}
```

The library also provides an extension point for custom resources. Example snippet:
```java
import com.sap.scimono.SCIMApplication;

import jakarta.ws.rs.ApplicationPath;
import java.util.Set;

@ApplicationPath("scim")
public class TestApplication extends SCIMApplication {

  @Override
  public Set<Class<?>> getAdditionalResourceProviders() {
    return super.getAdditionalResourceProviders();
  }
}
```


# Limitations
The current features are **not** currently supported but might be in the future:
  * Passwords
  * Sorting
  * Bulk operations
  
# Known Issues

A list of known issues is available on the [GitHub issues page](https://github.com/SAP/scimono/issues) of this project.

# How to obtain support

For any question please [open an issue](https://github.com/SAP/scimono/issues/new) in GitHub and make 
use of the labels in order to refer to the sample and to categorize the kind of the issue.

# Contributing

Our aim is to build a lively community, hence, we welcome any exchange and collaboration with individuals and organizations interested in the use, support and extension of the open-source SAP SCIMono Library.

Please follow [this document](/CONTRIBUTING.md) for more information on the process.

# To-Do (upcoming changes)

*  We are currently working on client-side support for the SAP SCIMono Library.  This will allow easy SCIM 2.0 client implementations.

# License

Copyright 2019-2021 SAP SE or an SAP affiliate company and scimono contributors. Please see our [LICENSE](LICENSE.txt) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available [via the REUSE tool](https://api.reuse.software/info/github.com/SAP/scimono).
