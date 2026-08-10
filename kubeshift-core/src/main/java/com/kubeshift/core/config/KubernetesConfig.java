package com.kubeshift.core.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KubernetesConfig {

    @Value("${kubeshift.cluster.mode:local}")
    private String clusterMode;

    @Bean
    public KubernetesClient kubernetesClient() {
        try {
            if ("in-cluster".equalsIgnoreCase(clusterMode)) {
                log.info("Creating Kubernetes client with in-cluster configuration");
                return new KubernetesClientBuilder().build();
            } else {
                log.info("Creating Kubernetes client with local configuration");
                Config config = new ConfigBuilder().build();
                return new KubernetesClientBuilder().withConfig(config).build();
            }
        } catch (Exception e) {
            log.warn("Failed to create Kubernetes client, falling back to default", e);
            return new KubernetesClientBuilder().build();
        }
    }
}
