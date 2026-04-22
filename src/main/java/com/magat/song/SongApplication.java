package com.magat.song;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SongApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SongApplication.class);
        application.setDefaultProperties(buildDatabaseDefaults());
        application.run(args);
    }

    private static Map<String, Object> buildDatabaseDefaults() {
        Map<String, Object> defaults = new HashMap<>();

        String databaseUrl = firstPresent("SPRING_DATASOURCE_URL", "JDBC_DATABASE_URL", "DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            if (applyDatabaseUrl(defaults, databaseUrl)) {
                return defaults;
            }
        }

        String host = firstPresent("DB_HOST", "PGHOST");
        if (host == null || host.isBlank()) {
            defaults.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/db_song?sslmode=disable");
            defaults.put("spring.datasource.username", "postgres");
            defaults.put("spring.datasource.password", "admin");
            return defaults;
        }

        String port = defaultIfBlank(firstPresent("DB_PORT", "PGPORT"), "5432");
        String name = defaultIfBlank(firstPresent("DB_NAME", "PGDATABASE"), "postgres");
        String params = firstPresent("DB_PARAMS");

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + name;
        if (params == null || params.isBlank()) {
            jdbcUrl = jdbcUrl + "?sslmode=require";
        } else {
            jdbcUrl = jdbcUrl + ensureLeadingQuestion(params);
        }

        defaults.put("spring.datasource.url", jdbcUrl);
        applyCredentialFallbacks(defaults, false);
        return defaults;
    }

    private static boolean applyDatabaseUrl(Map<String, Object> defaults, String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            defaults.put("spring.datasource.url", ensureSslMode(databaseUrl));
            extractUserAndPasswordFromJdbcQuery(defaults, databaseUrl);
            return true;
        }

        if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            URI uri = URI.create(databaseUrl);
            StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());

            if (uri.getPort() != -1) {
                jdbc.append(":").append(uri.getPort());
            }

            jdbc.append(uri.getPath() == null ? "" : uri.getPath());

            String query = uri.getQuery();
            if (query == null || query.isBlank()) {
                jdbc.append("?sslmode=require");
            } else {
                jdbc.append("?").append(query);
                if (!query.toLowerCase().contains("sslmode=")) {
                    jdbc.append("&sslmode=require");
                }
            }

            defaults.put("spring.datasource.url", jdbc.toString());

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                if (parts.length > 0 && !parts[0].isBlank()) {
                    defaults.put("spring.datasource.username", decode(parts[0]));
                }
                if (parts.length > 1 && !parts[1].isBlank()) {
                    defaults.put("spring.datasource.password", decode(parts[1]));
                }
            }

            return true;
        }

        return false;
    }

    private static void applyCredentialFallbacks(Map<String, Object> defaults, boolean fromDatabaseUrl) {
        if (!defaults.containsKey("spring.datasource.username")) {
            String username = fromDatabaseUrl
                ? firstPresent("SPRING_DATASOURCE_USERNAME", "DATABASE_USERNAME", "PGUSER")
                : firstPresent("SPRING_DATASOURCE_USERNAME", "DB_USERNAME", "DB_USER", "PGUSER");
            if (username != null && !username.isBlank()) {
                defaults.put("spring.datasource.username", username);
            }
        }

        if (!defaults.containsKey("spring.datasource.password")) {
            String password = fromDatabaseUrl
                ? firstPresent("SPRING_DATASOURCE_PASSWORD", "DATABASE_PASSWORD", "PGPASSWORD")
                : firstPresent("SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD", "PGPASSWORD");
            if (password != null && !password.isBlank()) {
                defaults.put("spring.datasource.password", password);
            }
        }
    }

    private static void extractUserAndPasswordFromJdbcQuery(Map<String, Object> defaults, String jdbcUrl) {
        String username = queryValue(jdbcUrl, "user");
        String password = queryValue(jdbcUrl, "password");

        if (username != null && !username.isBlank()) {
            defaults.put("spring.datasource.username", decode(username));
        }

        if (password != null && !password.isBlank()) {
            defaults.put("spring.datasource.password", decode(password));
        }
    }

    private static String queryValue(String jdbcUrl, String key) {
        int queryStart = jdbcUrl.indexOf('?');
        if (queryStart < 0 || queryStart + 1 >= jdbcUrl.length()) {
            return null;
        }

        String query = jdbcUrl.substring(queryStart + 1);
        String[] entries = query.split("&");
        for (String entry : entries) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && parts[0].equalsIgnoreCase(key)) {
                return parts[1];
            }
        }

        return null;
    }

    private static String firstPresent(String... keys) {
        for (String key : keys) {
            String value = System.getenv(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String ensureLeadingQuestion(String queryParams) {
        if (queryParams.startsWith("?")) {
            return queryParams;
        }
        if (queryParams.startsWith("&")) {
            return "?" + queryParams.substring(1);
        }
        return "?" + queryParams;
    }

    private static String ensureSslMode(String jdbcUrl) {
        if (jdbcUrl.toLowerCase().contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}