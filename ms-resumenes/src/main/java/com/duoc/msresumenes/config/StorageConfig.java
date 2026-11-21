package com.duoc.msresumenes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

/**
 * Credenciales con las que el SDK de AWS se conecta a S3 (igual que en la semana 2).
 *
 *  - AWS Academy entrega credenciales TEMPORALES de 3 partes (key, secret y session token).
 *  - Si no hay llaves configuradas, se usa la cadena por defecto del SDK
 *    (por ejemplo, el rol LabInstanceProfile asignado a la EC2).
 */
@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(
            @Value("${app.aws.access-key:}") String accessKey,
            @Value("${app.aws.secret-key:}") String secretKey,
            @Value("${app.aws.session-token:}") String sessionToken) {

        if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
            log.warn("No hay credenciales AWS en la configuración; se usa la cadena por defecto del SDK (rol IAM de la EC2)");
            return DefaultCredentialsProvider.builder().build();
        }
        if (StringUtils.hasText(sessionToken)) {
            return StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(accessKey.trim(), secretKey.trim(), sessionToken.trim()));
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
    }
}
