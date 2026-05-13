package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.imageservice.ImageService;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class SecurityService {

    private final ImageService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    public void setArmingStatus(ArmingStatus armingStatus) {

        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        if (armingStatus == ArmingStatus.ARMED_HOME ||
                armingStatus == ArmingStatus.ARMED_AWAY) {

            for (Sensor sensor : securityRepository.getSensors()) {
                sensor.setActive(false);
                securityRepository.updateSensor(sensor);
            }
        }

        securityRepository.setArmingStatus(armingStatus);
    }

    void catDetected(Boolean cat) {

        if (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!cat) {

            boolean activeSensors = securityRepository.getSensors()
                    .stream()
                    .anyMatch(Sensor::getActive);

            if (!activeSensors) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    private void handleSensorActivated() {

        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }

        switch (securityRepository.getAlarmStatus()) {

            case NO_ALARM ->
                    setAlarmStatus(AlarmStatus.PENDING_ALARM);

            case PENDING_ALARM ->
                    setAlarmStatus(AlarmStatus.ALARM);

            case ALARM -> {
            }
        }
    }

    private void handleSensorDeactivated() {

        if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {

            boolean anyActiveSensors = securityRepository.getSensors()
                    .stream()
                    .anyMatch(Sensor::getActive);

            if (!anyActiveSensors) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {

        if (securityRepository.getAlarmStatus() == AlarmStatus.ALARM) {
            sensor.setActive(active);
            securityRepository.updateSensor(sensor);
            return;
        }

        if (sensor.getActive() && active &&
                securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {

            setAlarmStatus(AlarmStatus.ALARM);
        }

        else if (!sensor.getActive() && active) {
            handleSensorActivated();
        }

        else if (sensor.getActive() && !active) {
            sensor.setActive(false);
            securityRepository.updateSensor(sensor);
            handleSensorDeactivated();
            return;
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}