package org.example.workloadms.component;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.util.Date;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RequiredArgsConstructor
public class WorkloadCucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final WorkloadTestContext context;
    private final MongoTemplate mongoTemplate;

    @Before
    public void setUp() {
        RestAssured.port = port;
        context.reset();
        context.setAuthToken(generateToken());
        mongoTemplate.getDb().drop();
    }

    private String generateToken() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .subject("test-user")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }
}