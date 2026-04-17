package com.app.auth.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "railwayDatabaseUrl";
    private static final String[] DATABASE_URL_KEYS = {
            "MYSQL_JDBC_URL",
            "DATABASE_URL",
            "DB_URL",
            "MYSQL_URL",
            "MYSQL_PUBLIC_URL"
    };

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new LinkedHashMap<>();

        String jdbcUrl = firstText(environment, "MYSQL_JDBC_URL");
        if (StringUtils.hasText(jdbcUrl)) {
            properties.put("spring.datasource.url", jdbcUrl);
        } else {
            ParsedMysqlUrl parsed = findParsedMysqlUrl(environment);
            if (parsed != null) {
                properties.put("spring.datasource.url", parsed.toJdbcUrl(environment));
                putIfMissing(environment, properties, "spring.datasource.username", "MYSQL_USERNAME", parsed.username());
                putIfMissing(environment, properties, "spring.datasource.password", "MYSQL_PASSWORD", parsed.password());
            }
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private ParsedMysqlUrl findParsedMysqlUrl(ConfigurableEnvironment environment) {
        for (String key : DATABASE_URL_KEYS) {
            String value = firstText(environment, key);
            if (StringUtils.hasText(value) && value.startsWith("mysql://")) {
                return ParsedMysqlUrl.parse(value);
            }
        }
        return null;
    }

    private void putIfMissing(
            ConfigurableEnvironment environment,
            Map<String, Object> properties,
            String propertyKey,
            String envKey,
            String parsedValue) {

        if (!StringUtils.hasText(environment.getProperty(propertyKey))
                && !StringUtils.hasText(environment.getProperty(envKey))
                && StringUtils.hasText(parsedValue)) {
            properties.put(propertyKey, parsedValue);
        }
    }

    private String firstText(ConfigurableEnvironment environment, String key) {
        String value = environment.getProperty(key);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 11;
    }

    private record ParsedMysqlUrl(String username, String password, String host, String port, String database) {

        static ParsedMysqlUrl parse(String url) {
            String value = url.substring("mysql://".length());
            int atIndex = value.lastIndexOf('@');
            String credentials = atIndex >= 0 ? value.substring(0, atIndex) : "";
            String location = atIndex >= 0 ? value.substring(atIndex + 1) : value;

            String username = "";
            String password = "";
            int passwordIndex = credentials.indexOf(':');
            if (passwordIndex >= 0) {
                username = decode(credentials.substring(0, passwordIndex));
                password = decode(credentials.substring(passwordIndex + 1));
            } else if (StringUtils.hasText(credentials)) {
                username = decode(credentials);
            }

            int queryIndex = location.indexOf('?');
            if (queryIndex >= 0) {
                location = location.substring(0, queryIndex);
            }

            int slashIndex = location.indexOf('/');
            String hostPort = slashIndex >= 0 ? location.substring(0, slashIndex) : location;
            String database = slashIndex >= 0 ? location.substring(slashIndex + 1) : "";

            int portIndex = hostPort.lastIndexOf(':');
            String host = portIndex >= 0 ? hostPort.substring(0, portIndex) : hostPort;
            String port = portIndex >= 0 ? hostPort.substring(portIndex + 1) : "3306";

            return new ParsedMysqlUrl(username, password, host, port, decode(database));
        }

        String toJdbcUrl(ConfigurableEnvironment environment) {
            String useSsl = environment.getProperty("MYSQL_USE_SSL", "false");
            return "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=" + useSsl
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true";
        }

        private static String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
    }
}
