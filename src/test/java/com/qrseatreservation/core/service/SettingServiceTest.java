package com.qrseatreservation.core.service;



import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.repository.EventRepository;
import com.qrseatreservation.core.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private EventContext eventContext;

    @Mock
    private EventRepository eventRepository;

    private SettingService service;

    private final Event event = Event.builder().id(1L).name("E").venue("Hall").status(EventStatus.ACTIVE).build();

    @BeforeEach
    void setUp() {
        service = new SettingService(settingRepository, eventContext, eventRepository);
    }

    @Test
    void createDefaultSetting_createsAndSaves(@TempDir Path tempDir) throws Exception {
        Path img = tempDir.resolve("base.jpg");
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        Files.write(img, imageBytes);

        setInt("tableSize", 30);
        setInt("seatSize", 8);
        setInt("noOfColumns", 5);
        setStr("baseImagePath", img.toAbsolutePath().toString());
        setInt("fontSize", 28);
        setInt("qrX", 900);
        setInt("qrY", 800);
        setInt("textX", 900);
        setInt("textY", 1100);
        setStr("ticketPrefix", "T-");
        setIntObj("noOfDigits", 5);
        setIntObj("maxNoOfTickets", 100);

        ArgumentCaptor<Setting> captor = ArgumentCaptor.forClass(Setting.class);

        service.createDefaultSetting(event);

        verify(settingRepository, times(1)).save(captor.capture());
        Setting saved = captor.getValue();
        assertThat(saved.getEvent()).isSameAs(event);
        assertThat(saved.getEventName()).isEqualTo("E");
        assertThat(saved.getVenue()).isEqualTo("Hall");
        assertThat(saved.getTableSize()).isEqualTo(30);
        assertThat(saved.getSeatSize()).isEqualTo(8);
        assertThat(saved.getNoOfColumns()).isEqualTo(5);
        assertThat(saved.getFontSize()).isEqualTo(28);
        assertThat(saved.getQrX()).isEqualTo(900);
        assertThat(saved.getQrY()).isEqualTo(800);
        assertThat(saved.getTextX()).isEqualTo(900);
        assertThat(saved.getTextY()).isEqualTo(1100);
        assertThat(saved.getTicketPrefix()).isEqualTo("T-");
        assertThat(saved.getNoOfDigits()).isEqualTo(5);
        assertThat(saved.getMaxNoOfTickets()).isEqualTo(100);
        assertThat(saved.getBaseImage()).isNotNull().containsExactly(imageBytes);
    }

    @Test
    void createDefaultSetting_savesWithoutImage_whenBaseImageMissing() throws Exception {
        setStr("baseImagePath", "/does/not/exist/image.jpg");
        setStr("ticketPrefix", "X-");
        setIntObj("noOfDigits", 4);
        setIntObj("maxNoOfTickets", 50);

        ArgumentCaptor<Setting> captor = ArgumentCaptor.forClass(Setting.class);

        service.createDefaultSetting(event);

        verify(settingRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getBaseImage()).isNull();
        assertThat(captor.getValue().getEvent()).isSameAs(event);
    }

    @Test
    void copySetting_copiesTemplateToTargetEvent() {
        Event source = Event.builder().id(1L).name("Old").status(EventStatus.ARCHIVED).build();
        Event target = Event.builder().id(2L).name("New").venue("V2").status(EventStatus.DRAFT).build();
        Setting src = Setting.builder().event(source).eventName("Old").ticketPrefix("P-").noOfDigits(6)
                .maxNoOfTickets(10).baseImage(new byte[]{9}).fontSize(12).build();

        Setting copy = service.copySetting(src, target);

        verify(settingRepository).save(copy);
        assertThat(copy).isNotSameAs(src);
        assertThat(copy.getEvent()).isSameAs(target);
        assertThat(copy.getEventName()).isEqualTo("New");
        assertThat(copy.getVenue()).isEqualTo("V2");
        assertThat(copy.getTicketPrefix()).isEqualTo("P-");
        assertThat(copy.getNoOfDigits()).isEqualTo(6);
        assertThat(copy.getMaxNoOfTickets()).isEqualTo(10);
        assertThat(copy.getBaseImage()).containsExactly(9);
    }

    @Test
    void getSetting_returnsSettingOfCurrentEvent() {
        Setting s = Setting.builder().eventName("Event").event(event).build();
        when(eventContext.currentId()).thenReturn(1L);
        when(settingRepository.findByEventId(1L)).thenReturn(Optional.of(s));

        Setting result = service.getSetting();

        assertThat(result).isSameAs(s);
        verify(settingRepository, times(1)).findByEventId(1L);
    }

    @Test
    void getSetting_throws_whenNoSettingsForEvent() {
        when(settingRepository.findByEventId(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSetting(5L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void save_delegatesToRepository_andReturnsSaved() {
        Setting s = Setting.builder().eventName("E2").event(event).build();
        when(eventContext.requireEditable()).thenReturn(event);

        Setting out = service.save(s);

        assertThat(out).isSameAs(s);
        verify(settingRepository, times(1)).save(s);
        verify(eventRepository).save(event);
        assertThat(event.getName()).isEqualTo("E2");
    }

    @Test
    void save_rejectsSettingOfOtherEvent() {
        Event other = Event.builder().id(2L).name("Other").status(EventStatus.ACTIVE).build();
        Setting s = Setting.builder().eventName("E").event(other).build();
        when(eventContext.requireEditable()).thenReturn(event);

        assertThatThrownBy(() -> service.save(s)).isInstanceOf(EventAccessException.class);
        verify(settingRepository, never()).save(any());
    }

    // Reflection helpers to inject @Value fields for unit testing
    private void setInt(String fieldName, int value) throws Exception {
        Field f = SettingService.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(service, value);
    }

    private void setIntObj(String fieldName, Integer value) throws Exception {
        Field f = SettingService.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(service, value);
    }

    private void setStr(String fieldName, String value) throws Exception {
        Field f = SettingService.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(service, value);
    }
}