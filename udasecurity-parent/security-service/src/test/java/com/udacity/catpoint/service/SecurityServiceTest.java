package com.udacity.catpoint.service;

import com.udacity.catpoint.data.*;
import com.udacity.imageservice.FakeImageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        securityService =
                new SecurityService(
                        securityRepository,
                        new FakeImageService()
                );
    }

    @Test
    void setAlarmStatusToPendingWhenSensorActivated() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(
                sensor,
                true
        );

        verify(securityRepository)
                .setAlarmStatus(
                        AlarmStatus.PENDING_ALARM
                );
    }

    @Test
    void setAlarmStatusToAlarmWhenSensorActivatedWhilePending() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(
                sensor,
                true
        );

        verify(securityRepository)
                .setAlarmStatus(
                        AlarmStatus.ALARM
                );
    }

    @Test
    void setNoAlarmWhenAllSensorsInactive() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        sensor.setActive(true);

        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor);

        when(securityRepository.getSensors())
                .thenReturn(sensors);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(
                sensor,
                false
        );

        verify(securityRepository)
                .setAlarmStatus(
                        AlarmStatus.NO_ALARM
                );
    }

    @Test
    void sensorStateChangeShouldNotAffectAlarmWhenAlarmActive() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(
                sensor,
                true
        );

        verify(securityRepository, never())
                .setAlarmStatus(any());
    }

    @Test
    void sensorActivatedWhileAlreadyActiveAndPendingSetsAlarm() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(
                sensor,
                true
        );

        verify(securityRepository)
                .setAlarmStatus(
                        AlarmStatus.ALARM
                );
    }

    @Test
    void inactiveSensorDeactivatedShouldNotChangeState() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        sensor.setActive(false);

        securityService.changeSensorActivationStatus(
                sensor,
                false
        );

        verify(securityRepository, never())
                .setAlarmStatus(any());
    }

    @Test
    void setAlarmWhenCatDetectedAndArmedHome() {

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        securityService.catDetected(true);

        verify(securityRepository)
                .setAlarmStatus(
                        AlarmStatus.ALARM
                );
    }

    @Test
    void setNoAlarmWhenNoCatAndNoActiveSensors() {

        Set<Sensor> sensors = new HashSet<>();

        when(securityRepository.getSensors())
                .thenReturn(sensors);

        securityService.catDetected(false);

        verify(securityRepository)
                .setAlarmStatus(
                        AlarmStatus.NO_ALARM
                );
    }

    @Test
    void resetAllSensorsWhenArmed() {

        Sensor sensor =
                new Sensor(
                        "Front Door",
                        SensorType.DOOR
                );

        sensor.setActive(true);

        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor);

        when(securityRepository.getSensors())
                .thenReturn(sensors);

        securityService.setArmingStatus(
                ArmingStatus.ARMED_HOME
        );

        verify(securityRepository)
                .updateSensor(sensor);
    }
}