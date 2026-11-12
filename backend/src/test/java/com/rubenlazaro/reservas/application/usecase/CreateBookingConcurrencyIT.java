package com.rubenlazaro.reservas.application.usecase;

import com.rubenlazaro.reservas.IntegrationTest;
import com.rubenlazaro.reservas.application.port.ResourceRepository;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.Resource;
import com.rubenlazaro.reservas.domain.TimeRange;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The conflict check in CreateBooking is a read followed by a write, so concurrent requests
 * can all pass it. This proves the database exclusion constraint, translated by the adapter,
 * is what keeps the slot from being double-booked.
 */
class CreateBookingConcurrencyIT extends IntegrationTest {

    private static final int ATTEMPTS = 8;

    @Autowired
    private CreateBooking createBooking;

    @Autowired
    private ResourceRepository resources;

    // Races are timing-dependent, so one passing run proves little: repeat it. The container and
    // the Spring context are reused across repetitions.
    @RepeatedTest(10)
    void onlyOneOfManyConcurrentRequestsForTheSameSlotSucceeds() throws Exception {
        Long court = resources.save(Resource.create("Court 1")).id();
        TimeRange slot = new TimeRange(
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T11:00:00Z"));

        ExecutorService pool = Executors.newFixedThreadPool(ATTEMPTS);
        CyclicBarrier allReady = new CyclicBarrier(ATTEMPTS);
        List<Future<Boolean>> outcomes = new ArrayList<>();
        try {
            for (int i = 0; i < ATTEMPTS; i++) {
                long userId = i + 1;
                outcomes.add(pool.submit(() -> {
                    allReady.await();
                    try {
                        createBooking.execute(court, userId, slot);
                        return true;
                    } catch (BookingOverlapException e) {
                        return false;
                    }
                }));
            }

            long successes = 0;
            for (Future<Boolean> outcome : outcomes) {
                if (outcome.get(30, TimeUnit.SECONDS)) {
                    successes++;
                }
            }

            assertThat(successes).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM booking", Integer.class)).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
