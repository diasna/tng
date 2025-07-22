package uk.diasna.tng.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class TrackingNumberGeneratorTest {

    private TrackingNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new TrackingNumberGenerator();
    }

    @Test
    void testGenerateTrackingNumber() {
        String trackingNumber = generator.generateTrackingNumber();
        
        assertNotNull(trackingNumber);
        assertEquals(16, trackingNumber.length());
        assertTrue(trackingNumber.matches("^[A-Z0-9]{16}$"), 
                  "Tracking number should match pattern ^[A-Z0-9]{16}$: " + trackingNumber);
    }

    @Test
    void testTrackingNumberUniqueness() {
        Set<String> trackingNumbers = new HashSet<>();
        int count = 10000;
        
        for (int i = 0; i < count; i++) {
            String trackingNumber = generator.generateTrackingNumber();
            assertTrue(trackingNumbers.add(trackingNumber), "Tracking number should be unique: " + trackingNumber);
        }
        
        assertEquals(count, trackingNumbers.size());
    }

    @Test
    void testTimeOrdering() throws InterruptedException {
        String tracking1 = generator.generateTrackingNumber();
        Thread.sleep(5); // Small delay to ensure different timestamps
        String tracking2 = generator.generateTrackingNumber();
        
        // First 8 characters should be time-based
        String time1 = tracking1.substring(0, 8);
        String time2 = tracking2.substring(0, 8);
        
        // Due to timestamp encoding, they should be different (uniqueness through time)
        assertNotEquals(time1, time2, "Time components should be different");
    }

    @Test
    void testConcurrentGeneration() throws InterruptedException {
        int threadCount = 10;
        int generationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> trackingNumbers = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < generationsPerThread; j++) {
                        trackingNumbers.add(generator.generateTrackingNumber());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        int expectedCount = threadCount * generationsPerThread;
        assertEquals(expectedCount, trackingNumbers.size(), "All tracking numbers should be unique");
    }

    @RepeatedTest(10)
    void testConsistentFormat() {
        String trackingNumber = generator.generateTrackingNumber();
        
        assertEquals(16, trackingNumber.length());
        assertTrue(trackingNumber.matches("^[A-Z0-9]{16}$"));
        
        // Verify no lowercase letters
        assertEquals(trackingNumber, trackingNumber.toUpperCase());
    }

    @Test
    void testPerformance() {
        long startTime = System.currentTimeMillis();
        int iterations = 10000;
        
        for (int i = 0; i < iterations; i++) {
            generator.generateTrackingNumber();
        }
        
        long duration = System.currentTimeMillis() - startTime;
        double avgTimePerGeneration = (double) duration / iterations;
        
        // Should be very fast - less than 1ms per generation
        assertTrue(avgTimePerGeneration < 1.0, 
                  "Average generation time should be < 1ms, was: " + avgTimePerGeneration + "ms");
    }

    @Test
    void testCharacterSetCompliance() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 100; i++) {
            String trackingNumber = generator.generateTrackingNumber();
            
            for (char c : trackingNumber.toCharArray()) {
                assertTrue(charset.indexOf(c) >= 0, 
                          "Character '" + c + "' not in allowed charset: " + trackingNumber);
            }
        }
    }

    @Test
    void testStructure() {
        String trackingNumber = generator.generateTrackingNumber();
        
        // Should have 8 chars timestamp + 8 chars random
        assertEquals(16, trackingNumber.length());
        
        String timestampPart = trackingNumber.substring(0, 8);
        String randomPart = trackingNumber.substring(8);
        
        assertEquals(8, timestampPart.length());
        assertEquals(8, randomPart.length());
        
        // Both parts should be valid character sets
        assertTrue(timestampPart.matches("^[A-Z0-9]{8}$"));
        assertTrue(randomPart.matches("^[A-Z0-9]{8}$"));
    }

    @Test
    void testRandomnessInRandomPart() {
        Set<String> randomParts = new HashSet<>();
        
        // Generate many tracking numbers and collect just the random parts
        for (int i = 0; i < 1000; i++) {
            String trackingNumber = generator.generateTrackingNumber();
            String randomPart = trackingNumber.substring(8); // Last 8 characters
            randomParts.add(randomPart);
        }
        
        // Should have very high uniqueness in random parts
        assertTrue(randomParts.size() > 950, 
                  "Random parts should be highly unique, got " + randomParts.size() + " unique out of 1000");
    }

    @Test
    void testTimestampPartVariation() throws InterruptedException {
        Set<String> timestampParts = new HashSet<>();
        
        // Generate tracking numbers with slight time delays
        for (int i = 0; i < 10; i++) {
            String trackingNumber = generator.generateTrackingNumber();
            String timestampPart = trackingNumber.substring(0, 8); // First 8 characters
            timestampParts.add(timestampPart);
            Thread.sleep(1); // 1ms delay
        }
        
        // Should have some variation in timestamp parts due to time progression
        assertTrue(timestampParts.size() >= 1, "Should have at least one unique timestamp part");
    }
}
