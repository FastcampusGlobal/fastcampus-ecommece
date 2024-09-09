package com.fc.ecommerce.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudWatchService {

    private final AmazonCloudWatch cloudWatchClient;
    private static final String NAMESPACE = "Ecommerce App";

    @Async
    public CompletableFuture<Void> sendMetric(String namespace, String metricName, double value, StandardUnit unit, String... dimensions) {
        return CompletableFuture.runAsync(() -> {
            try {
                MetricDatum datum = new MetricDatum()
                    .withMetricName(metricName)
                    .withValue(value)
                    .withUnit(unit);

                for (int i = 0; i < dimensions.length; i += 2) {
                    datum.withDimensions(new Dimension()
                        .withName(dimensions[i])
                        .withValue(dimensions[i + 1]));
                }

                PutMetricDataRequest request = new PutMetricDataRequest()
                    .withNamespace(namespace)
                    .withMetricData(datum);

                cloudWatchClient.putMetricData(request);
                log.info("Successfully sent metric: {} to CloudWatch", metricName);
            } catch (Exception e) {
                log.error("Error sending metric to CloudWatch: {}", e.getMessage());
            }
        });
    }

    @Async
    public CompletableFuture<Void> recordControllerPerformance(String controllerName, String methodName, long executionTime) {
        return sendMetric(NAMESPACE, "ControllerPerformance", executionTime, StandardUnit.Milliseconds,
            "ControllerName", controllerName,
            "MethodName", methodName);
    }

    @Async
    public CompletableFuture<Void> incrementEndpointHits(String controllerName, String methodName) {
        return sendMetric(NAMESPACE, "EndpointHits", 1.0, StandardUnit.Count,
            "ControllerName", controllerName,
            "MethodName", methodName);
    }

    @Async
    public CompletableFuture<Void> recordCodeBlockPerformance(String controllerName, String methodName, String blockName, long executionTime) {
        return sendMetric(NAMESPACE, "CodeBlockPerformance", executionTime, StandardUnit.Milliseconds,
            "ControllerName", controllerName,
            "MethodName", methodName,
            "BlockName", blockName);
    }
}