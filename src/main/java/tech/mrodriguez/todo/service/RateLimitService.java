package tech.mrodriguez.todo.service;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final long requestsPerMinute;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitService(@Value("${app.rate-limit.requests-per-minute}") long requestsPerMinute) {
        if (requestsPerMinute <= 0) requestsPerMinute = 60;
        this.requestsPerMinute = requestsPerMinute;
    }

    public Bucket resolveBucket(String userId) {
        return this.buckets.computeIfAbsent(userId, id -> Bucket.builder()
                .addLimit(limit -> limit.capacity(requestsPerMinute)
                        .refillIntervally(requestsPerMinute, Duration.ofMinutes(1)))
                .build());
    }
}
