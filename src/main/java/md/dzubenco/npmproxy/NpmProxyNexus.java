package md.dzubenco.npmproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NpmProxyNexus {

    public static void main(String[] args) {
        SpringApplication.run(NpmProxyNexus.class, args);
    }

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

}

