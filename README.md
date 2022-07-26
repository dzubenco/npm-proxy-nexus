# npm-proxy-nexus

`npm-proxy-nexus` is a simple proxy which is designed to redirect npm audit calls to https://registry.npmjs.org/ , and all other npm calls to your custom Nexus Proxy registry. This is useful in case if you have a private proxy registry for npm that does not support [npm audit](https://docs.npmjs.com/cli/v7/commands/npm-audit), such as [Nexus Repository OSS](https://de.sonatype.com/products/repository-oss).

![schema](https://i.ibb.co/588Z3zv/Screenshot-20220721-105610.png)

## Technology

**npm-proxy-nexus** is build in Java and based on [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Cloud Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/).

Configuration class which routes the requests is [NpmProxyNexus.java](src/main/java/md/dzubenco/npmproxy/NpmProxyNexus.java) and it contains the following bean:

```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
    .route("npm_audit", r -> r.path("/-/npm/v1/security/audits/**")
    .uri("https://registry.npmjs.org"))
    .route("npm_advisory", r -> r.path("/-/npm/v1/security/advisories/**")
    .uri("https://registry.npmjs.org"))
    .route("npm_else", r -> r.path("/**").filters(f -> f.rewritePath("/repository/npm-group/(?<segment>.*)", "/${segment}").rewritePath("/(?<segment>.*)", "/repository/npm-group/${segment}"))
    .uri("http://192.168.107.65:8081"))
    .build();
    }
```

where you need to change `http://192.168.107.65:8081` to the URL of your Nexus service, and `/repository/npm-group/` to the path to your npm registry.


## Build

Build **npm-proxy-nexus** with Maven:

```bash
mvn clean package
```

## Installation

Please refer to https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html on how to install Spring Boot applications. The file [systemd/npm-proxy-nexus.service](systemd/npm-proxy-nexus.service) might be helpful if you choose to start **npm-proxy-nexus** by systemd.

The port on which **npm-proxy-nexus** is running can be configured in the [application.properties](/src/main/resources/application.properties) file or passed as command line argument when starting **npm-proxy-nexus**: `--server.port=<port>`. The default is 8084.

In order to configure your npm to use this proxy, create .npmrc file in your home directory with the following content:


```
registry=http://localhost:8084
_auth=Base64encoded_creds
```

Where `http://localhost:8084` - URL for the running `npm-proxy-nexus` and `Base64encoded_creds` - base64-encoding string of `username:password` used for authentication to your Nexus repo using [Basic Auth](https://help.sonatype.com/repomanager3/nexus-repository-administration/formats/npm-registry/npm-security#npmSecurity-AuthenticationUsingBasicAuth).
For more details about configuration please refer to the [Configuring npm](https://help.sonatype.com/repomanager3/nexus-repository-administration/formats/npm-registry/configuring-npm) and [npm Security](https://help.sonatype.com/repomanager3/nexus-repository-administration/formats/npm-registry/npm-security) resources


## License

MIT