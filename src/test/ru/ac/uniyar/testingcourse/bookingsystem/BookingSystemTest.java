package ru.ac.uniyar.testingcourse.bookingsystem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BookingSystemTest {

    private BookingSystem bookingSystem = new BookingSystem();

    @Test
    void bookedHoursListShouldBeEmptyAfterCreation() {
        List<Integer> bookedHours = bookingSystem.getBookedHoursList();
        assertThat(bookedHours).isEmpty();
    }
    
    @Test
    void possibleToBookOneInterval() {
        assertThat(bookingSystem.book("user", 12, 14)).isTrue();
        List<Integer> bookedHours = bookingSystem.getBookedHoursList();
        assertThat(bookedHours).containsExactly(12, 13);
    }

    // absorbed by impossibleToBookIntervalBeyondBoundaries
    @Test
    void impossibleToBookIntervalEarlierThan8am() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> bookingSystem.book("user", 4, 7));
        assertThat(bookingSystem.getBookedHoursList()).isEmpty();
    }

    // absorbed by impossibleToBookIntervalBeyondBoundaries
    @Test
    void impossibleToBookIntervalLaterThan8pm() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> bookingSystem.book("user", 20, 22));
        assertThat(bookingSystem.getBookedHoursList()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "4, 7",
        "20, 22"
    })
    void impossibleToBookIntervalBeyondBoundaries(int from, int till) {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> bookingSystem.book("user", from, till));
        assertThat(bookingSystem.getBookedHoursList()).isEmpty();
    }

    static Stream<Arguments> dataForTwoNonCrossingInvervalsTest() {
        return Stream.of(
            arguments(10, 12, 14, 17, Arrays.asList(10, 11, 14, 15, 16)),
            arguments(10, 12, 12, 14, Arrays.asList(10, 11, 12, 13))
        );
    }

    @ParameterizedTest
    @MethodSource("dataForTwoNonCrossingInvervalsTest")
    void possibleToBookTwoNonCrossingInvervals(int from1, int till1,
            int from2, int till2, List<Integer> result) {
        bookingSystem.book("user", from1, till1);
        assertThat(bookingSystem.book("user", from2, till2)).isTrue();
        assertThat(bookingSystem.getBookedHoursList()).isEqualTo(result);
    }
    
    @Test
    void impossibleToBookCrossingIntervals() {
        bookingSystem.book("user", 10, 18);
        assertThat(bookingSystem.book("user", 9, 11)).isFalse();
        assertThat(bookingSystem.getBookedHoursList()).doesNotContain(9);
    }
    
    @Test
    void possibleToCancelPreviouslyBookedInterval() {
        bookingSystem.book("user", 9, 16);
        bookingSystem.cancelBooking("user", 9, 16);
        assertThat(bookingSystem.getBookedHoursList()).isEmpty();
    }
    
    @Test
    void possibleToCancelPreviouslyBookedIntervalPartially() {
        bookingSystem.book("user", 9, 16);
        bookingSystem.cancelBooking("user", 10, 14);
        assertThat(bookingSystem.getBookedHoursList()).containsExactly(9, 14, 15);
    }
    
    @Test
    void impossibleToCancelIntervalBeyondBoundaries() {
        bookingSystem.book("user", 19, 20);
        List<Integer> bookedHours = bookingSystem.getBookedHoursList();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> bookingSystem.cancelBooking("user", 19, 21));
        assertThat(bookingSystem.getBookedHoursList()).isEqualTo(bookedHours);
    }
    
    @Test
    void impossibleToCancelNotPreviouslyBookedInterval() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> bookingSystem.cancelBooking("user", 10, 14));
    }
    
    @Test
    void impossibleToCancelIntervalContainingBookedAndCancelledSubintervals() {
        bookingSystem.book("user", 9, 16);
        List<Integer> bookedHours = bookingSystem.getBookedHoursList();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> bookingSystem.cancelBooking("user", 12, 18));
        assertThat(bookingSystem.getBookedHoursList()).isEqualTo(bookedHours);
    }
    
    @Test
    void impossibleToCancelIntervalBookedByAnotherUser() {
        bookingSystem.book("user", 9, 14);
        List<Integer> bookedHours = bookingSystem.getBookedHoursList();
        assertThatExceptionOfType(BookingSystem.BookedByAnotherUserException.class)
                .isThrownBy(() -> bookingSystem.cancelBooking("other user", 9, 14));
        assertThat(bookingSystem.getBookedHoursList()).isEqualTo(bookedHours);
    }
}
