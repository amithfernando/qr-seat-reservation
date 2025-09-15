package com.amithfernando.qrseatreservation.core.service;



import com.amithfernando.qrseatreservation.core.model.Setting;
import com.amithfernando.qrseatreservation.core.repsitory.SettingRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {

    @Mock
    private SettingRepository settingRepository;

    private SettingService service;

    @BeforeEach
    void setUp() {
        service = new SettingService(settingRepository);
    }

    @Test
    void initializeSettings_createsAndSaves_whenEmpty(@TempDir Path tempDir) throws Exception {
        // Arrange: no settings exist
        when(settingRepository.findAll()).thenReturn(List.of());

        // Create a temp image file as base image source
        Path img = tempDir.resolve("base.jpg");
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        Files.write(img, imageBytes);

        // Inject @Value fields via reflection
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

        // Act
        service.initializeSettings();

        // Assert
        verify(settingRepository, times(1)).save(captor.capture());
        Setting saved = captor.getValue();
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
    void initializeSettings_doesNothing_whenAlreadyExists() {
        // Arrange: repository already has a setting
        when(settingRepository.findAll()).thenReturn(List.of(Setting.builder().build()));

        // Act
        service.initializeSettings();

        // Assert
        verify(settingRepository, never()).save(any());
    }

    @Test
    void initializeSettings_skipsSave_whenBaseImageMissing() throws Exception {
        // Arrange: empty repo, but base image path invalid -> internal error path
        when(settingRepository.findAll()).thenReturn(List.of());

        setStr("baseImagePath", "/does/not/exist/image.jpg");
        setInt("tableSize", 20);
        setInt("seatSize", 6);
        setInt("noOfColumns", 4);
        setInt("fontSize", 24);
        setInt("qrX", 100);
        setInt("qrY", 200);
        setInt("textX", 300);
        setInt("textY", 400);
        setStr("ticketPrefix", "X-");
        setIntObj("noOfDigits", 4);
        setIntObj("maxNoOfTickets", 50);

        // Act
        service.initializeSettings();

        // Assert: error is caught and logged, and save is not called
        verify(settingRepository, never()).save(any());
    }

    @Test
    void getSetting_returnsFirst() {
        Setting s = Setting.builder().eventName("Event").build();
        when(settingRepository.findAll()).thenReturn(List.of(s));

        Setting result = service.getSetting();

        assertThat(result).isSameAs(s);
        verify(settingRepository, times(1)).findAll();
    }

    @Test
    void save_delegatesToRepository_andReturnsSaved() {
        Setting s = Setting.builder().eventName("E").build();
        when(settingRepository.save(s)).thenReturn(s);

        Setting out = service.save(s);

        assertThat(out).isSameAs(s);
        verify(settingRepository, times(1)).save(s);
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