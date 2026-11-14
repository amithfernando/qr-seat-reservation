package com.qrseatreservation.core.event;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.model.Event;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventContextTest {

    private static Event event(long id, EventStatus status) {
        return Event.builder().id(id).name("E" + id).status(status).build();
    }

    @Test
    void assertBelongsTo_passesForSameEvent() {
        assertThatCode(() -> EventContext.assertBelongsTo(event(1, EventStatus.ACTIVE), 1L)).doesNotThrowAnyException();
    }

    @Test
    void assertBelongsTo_throwsForOtherEvent() {
        assertThatThrownBy(() -> EventContext.assertBelongsTo(event(2, EventStatus.ACTIVE), 1L))
                .isInstanceOf(EventAccessException.class);
    }

    @Test
    void assertBelongsTo_throwsForNullOwnerOrEventId() {
        assertThatThrownBy(() -> EventContext.assertBelongsTo(null, 1L)).isInstanceOf(EventAccessException.class);
        assertThatThrownBy(() -> EventContext.assertBelongsTo(event(1, EventStatus.ACTIVE), null))
                .isInstanceOf(EventAccessException.class);
    }

    @Test
    void requireEditable_throwsForArchivedEvent() {
        assertThatThrownBy(() -> EventContext.requireEditable(event(1, EventStatus.ARCHIVED)))
                .isInstanceOf(EventAccessException.class);
    }

    @Test
    void requireEditable_passesForDraftAndActive() {
        Event draft = event(1, EventStatus.DRAFT);
        Event active = event(2, EventStatus.ACTIVE);
        assertThat(EventContext.requireEditable(draft)).isSameAs(draft);
        assertThat(EventContext.requireEditable(active)).isSameAs(active);
    }
}
