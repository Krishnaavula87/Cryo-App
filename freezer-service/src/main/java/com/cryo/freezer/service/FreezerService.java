package com.cryo.freezer.service;

import com.cryo.common.exception.BadRequestException;
import com.cryo.common.exception.ResourceNotFoundException;
import com.cryo.freezer.dto.*;
import com.cryo.freezer.entity.*;
import com.cryo.freezer.repository.*;
import com.cryo.freezer.util.UserContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreezerService {
    private static final Logger logger = LoggerFactory.getLogger(FreezerService.class);

    private final FreezerRepository freezerRepository;
    private final FreezerReadingRepository freezerReadingRepository;
    private final DeviceInventoryRepository deviceInventoryRepository;
    private final FreezerReadingService readingService; // ✅ Inject ReadingService
    private final DataLoggerDeviceRepository dlDeviceRepository;
    private final DataLoggerChannelRepository dataLoggerChannelRepository;


    public FreezerService(FreezerRepository freezerRepository,
                          FreezerReadingRepository freezerReadingRepository,
                          DeviceInventoryRepository deviceInventoryRepository,
                          FreezerReadingService readingService, DataLoggerDeviceRepository dlDeviceRepository, DataLoggerChannelRepository dataLoggerChannelRepository) { // ✅ Add to Constructor
        this.freezerRepository = freezerRepository;
        this.freezerReadingRepository = freezerReadingRepository;
        this.deviceInventoryRepository = deviceInventoryRepository;
        this.readingService = readingService;
        this.dlDeviceRepository = dlDeviceRepository;
        this.dataLoggerChannelRepository = dataLoggerChannelRepository;
    }

    @Transactional
    public Freezer registerNewFreezer(String ownerUserId, FreezerRegisterRequest request) {
        DeviceInventory inventoryItem = deviceInventoryRepository.findByPoNumber(request.getPoNumber())
                .orElseThrow(() -> new BadRequestException("Invalid PO Number. Please contact support."));

        if (freezerRepository.existsByPoNumber(request.getPoNumber())) {
            throw new BadRequestException("PO Number is already active.");
        }

        Freezer newFreezer = new Freezer();
        newFreezer.setPoNumber(request.getPoNumber());
        newFreezer.setOwnerUserId(ownerUserId);
        newFreezer.setName(request.getName());
        newFreezer.setStatus(Freezer.FreezerStatus.ACTIVE);
        newFreezer.setS3Url(inventoryItem.getS3Url());
        // ✅ CORRECT WAY — get type from inventory
        newFreezer.setDeviceType(inventoryItem.getDeviceType());


        Freezer savedFreezer = freezerRepository.save(newFreezer);

        inventoryItem.setIsClaimed(true);
        deviceInventoryRepository.save(inventoryItem);

        logger.info("Freezer registered: PO={} Owner={}", request.getPoNumber(), ownerUserId);
        return savedFreezer;
    }


    public List<FreezerResponse> getAllFreezersForUser(String ownerUserId) {

        List<Freezer> devices = freezerRepository.findByOwnerUserId(ownerUserId);

        return devices.stream()
                .map(device -> new FreezerResponse(
                        device.getId(),
                        device.getFreezerId(),
                        device.getName(),
                        device.getStatus().name(),
                        device.getDeviceType().name()   // ✅ CRITICAL
                ))
                .collect(Collectors.toList());
    }


    public FreezerSummaryResponse getFreezerSummary(String ownerUserId) {

        List<Freezer> devices = freezerRepository.findByOwnerUserId(ownerUserId);
        if (devices.isEmpty()) {
            return new FreezerSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        long totalFreezers = 0;
        long activeFreezersCount = 0;
        long freezersOnCount = 0;
        long freezersOffCount = 0;
        long redAlertFreezersCount = 0;

        long totalDataLoggers = 0;
        long totalChannels = 0;
        long channelsSending = 0;
        long channelsNotSending = 0;
        long channelsInAlert = 0;

        for (Freezer device : devices) {

            if (device.getDeviceType() == Freezer.DeviceType.NORMAL_FREEZER) {

                totalFreezers++;

                if (device.getStatus() == Freezer.FreezerStatus.ACTIVE) {
                    activeFreezersCount++;

                    FreezerReading latest =
                            freezerReadingRepository
                                    .findFirstByFreezerIdOrderByTimestampDesc(device.getFreezerId())
                                    .orElse(null);

                    if (latest != null) {

                        if (Boolean.TRUE.equals(latest.getFreezerOn())) {
                            freezersOnCount++;
                        } else {
                            freezersOffCount++;
                        }

                        if (latest.isRedAlert()) {
                            redAlertFreezersCount++;
                        }
                    }
                }
            }

            // ===============================
            // DATA LOGGER
            // ===============================
            else {

                totalDataLoggers++;

                List<DataLoggerChannelReading> latestChannels =
                        dataLoggerChannelRepository.findLatestChannelsByTopic(device.getFreezerId());

                totalChannels += latestChannels.size();

                for (DataLoggerChannelReading ch : latestChannels) {

                    // SENDING LOGIC
                    if ("SENDING".equalsIgnoreCase(ch.getStatus())
                            || "ON".equalsIgnoreCase(ch.getStatus())
                            || Boolean.TRUE.equals(ch.getStatus())) {

                        channelsSending++;

                    } else {
                        channelsNotSending++;
                    }

                    // ALERT LOGIC
                    if (Boolean.TRUE.equals(ch.gethighTempAlarm())
                            || Boolean.TRUE.equals(ch.getlowTempAlarm())) {

                        channelsInAlert++;
                    }
                }
            }
        }

        return new FreezerSummaryResponse(
                totalFreezers,
                activeFreezersCount,
                freezersOnCount,
                freezersOffCount,
                redAlertFreezersCount,
                totalDataLoggers,
                totalChannels,
                channelsSending,
                channelsNotSending,
                channelsInAlert
        );
    }


    public FreezerStatusResponse getFreezerStatus(String freezerId) {

        Freezer freezer = freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));

        FreezerStatusResponse response = new FreezerStatusResponse();

        response.setFreezerId(freezer.getFreezerId());
        response.setName(freezer.getName());
        response.setDeviceType(freezer.getDeviceType().name());

        // ================= NORMAL FREEZER =================
        if (freezer.getDeviceType() == Freezer.DeviceType.NORMAL_FREEZER) {

            FreezerReading latest =
                    freezerReadingRepository
                            .findFirstByFreezerIdOrderByTimestampDesc(freezerId)
                            .orElse(null);

            if (latest != null) {
                response.setCurrentTemp(latest.getTemperature());
                response.setIsFreezerOn(latest.getFreezerOn());
                response.setIsDoorOpen(latest.getDoorOpen());
                response.setIsRedAlert(latest.isRedAlert());
                response.setTimestamp(latest.getTimestamp());
            }
        }

        // ================= DATA LOGGER =================

        else {

            List<DataLoggerChannelReading> channelReadings =
                    dataLoggerChannelRepository.findLatestChannelsByTopic(freezerId);

            List<DataLoggerChannelStatusDto> channelDtos =
                    channelReadings.stream()
                            .map(ch -> new DataLoggerChannelStatusDto(
                                    ch.getChannelNumber(),
                                    ch.getTemperature(),
                                    ch.getStatus(),
                                    ch.getsetTemp(),
                                    ch.gethighTemp(),
                                    Boolean.TRUE.equals(ch.gethighTempAlarm()),
                                    ch.getlowTemp(),
                                    Boolean.TRUE.equals(ch.getlowTempAlarm()),
                                    ch.getTimestamp()
                            ))
                            .collect(Collectors.toList());   // 🔥 DO NOT use toList()

            response.setTotalChannels(channelDtos.size());

            response.setActiveChannels(
                    (int) channelDtos.stream()
                            .filter(c -> "ON".equalsIgnoreCase(c.getStatus()))
                            .count()
            );

            response.setAlertChannels(
                    (int) channelDtos.stream()
                            .filter(c ->
                                    Boolean.TRUE.equals(c.gethighTempAlarm()) ||
                                            Boolean.TRUE.equals(c.getlowTempAlarm()))
                            .count()
            );

            response.setChannels(channelDtos);

            response.setTimestamp(
                    channelReadings.isEmpty()
                            ? null
                            : channelReadings.get(0).getTimestamp()
            );
        }
        return response;
    }


    public List<?> getFreezerChartData(
            String freezerId,
            LocalDateTime from,
            LocalDateTime to,
            String channel) {

        String ownerUserId = UserContext.getUserId();

        Freezer freezer = (ownerUserId != null)
                ? freezerRepository.findByFreezerIdAndOwnerUserId(freezerId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId))
                : freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));

        // ================= NORMAL FREEZER =================
        if (freezer.getDeviceType() == Freezer.DeviceType.NORMAL_FREEZER) {

            List<FreezerReading> readings =
                    freezerReadingRepository
                            .findByFreezerIdAndTimestampBetweenOrderByTimestampAsc(
                                    freezer.getFreezerId(), from, to);

            return readings.stream()
                    .map(r -> new FreezerChartDataPoint(
                            r.getTimestamp(),
                            r.getTemperature(),
                            r.getFreezerOn(),
                            r.getDoorOpen()
                    ))
                    .toList();
        }

        // ================= DATA LOGGER =================
        else {

            // ✅ CASE 1: All channels (no channel param)
            if (channel == null || channel.isBlank()) {

                List<DataLoggerChannelReading> readings =
                        dataLoggerChannelRepository
                                .findByTopicAndTimestampBetweenOrderByTimestampAsc(
                                        freezer.getFreezerId(),
                                        from,
                                        to);

                return readings.stream()
                        .map(r -> new DataLoggerChartDataPoint(
                                r.getTimestamp(),
                                r.getTemperature(),
                                r.getChannelNumber()
                        ))
                        .toList();
            }

            // ✅ CASE 2: Specific channel
            List<DataLoggerChannelReading> readings =
                    dataLoggerChannelRepository
                            .findByTopicAndChannelNumberAndTimestampBetweenOrderByTimestampAsc(
                                    freezer.getFreezerId(),
                                    channel,
                                    from,
                                    to);

            return readings.stream()
                    .map(r -> new DataLoggerChartDataPoint(
                            r.getTimestamp(),
                            r.getTemperature(),
                            r.getChannelNumber()
                    ))
                    .toList();
        }
    }


    public DataLoggerSnapshotResponse getLatestChannelSnapshot(String freezerId) {

        Freezer freezer = freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));

        if (freezer.getDeviceType() != Freezer.DeviceType.DATA_LOGGER) {
            throw new BadRequestException("Not a data logger device");
        }

        DataLoggerDevice device = dlDeviceRepository.findById(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("DataLogger", freezerId));

        List<DataLoggerChannelReading> latestChannels =
                dataLoggerChannelRepository.findLatestChannelsByTopic(freezerId);

        List<DataLoggerChannelStatusDto> channelDtos =
                latestChannels.stream()
                        .map(ch -> new DataLoggerChannelStatusDto(
                                ch.getChannelNumber(),
                                ch.getTemperature(),
                                ch.getStatus(),
                                Boolean.TRUE.equals(ch.gethighTempAlarm()),
                                Boolean.TRUE.equals(ch.getlowTempAlarm()),
                                ch.getTimestamp()
                        ))
                        .collect(Collectors.toList());
        long total = channelDtos.size();

        long channelsOn = channelDtos.stream()
                .filter(c -> "ON".equalsIgnoreCase(c.getStatus()))
                .count();

        long channelsOff = total - channelsOn;

        long channelsInAlert = channelDtos.stream()
                .filter(c -> Boolean.TRUE.equals(c.gethighTempAlarm())
                        || Boolean.TRUE.equals(c.getlowTempAlarm()))
                .count();

        return new DataLoggerSnapshotResponse(
                freezerId,
                device.getLastTimestamp(),
                device.getAmbientTemperature(),
                device.getBatteryPercentage(),
                device.getPower(),
                channelDtos,
                total,
                channelsOn,
                channelsOff,
                channelsInAlert
        );
    }


    // ✅ UPDATED: Get Detailed List with AVERAGE Calculation
    public List<FreezerDetailResponse> getFreezerDetailsForUser(String ownerUserId) {
        List<Freezer> freezers = freezerRepository.findByOwnerUserId(ownerUserId);
        if (freezers.isEmpty()) return List.of();

        List<String> activeIds = freezers.stream()
                .filter(f -> f.getStatus() == Freezer.FreezerStatus.ACTIVE && f.getFreezerId() != null)
                .map(Freezer::getFreezerId)
                .toList();

        List<FreezerReading> readings = List.of();
        if (!activeIds.isEmpty()) {
            readings = freezerReadingRepository.findLatestReadingsForFreezers(activeIds);
        }

        java.util.Map<String, FreezerReading> readingMap = readings.stream()
                .collect(java.util.stream.Collectors.toMap(FreezerReading::getFreezerId, r -> r, (e, r) -> e));

        return freezers.stream().map(f -> {
            FreezerReading r = readingMap.get(f.getFreezerId());

            // ✅ GET FAST AVERAGE FROM RAM (Does not hit DB)
            Double avgTemp = readingService.getFastOneMinuteAverage(f.getFreezerId());

            // Fallback: If average is not ready (app just started), use current temp
            if (avgTemp == null && r != null && r.getTemperature() != null) {
                avgTemp = r.getTemperature().doubleValue();
            }

            return new FreezerDetailResponse(
                    f.getFreezerId(),
                    f.getName(),
                    f.getPoNumber(),
                    f.getStatus().name(),
                    r != null ? r.getTemperature() : null,
                    r != null ? r.getFreezerOn() : null,
                    r != null ? r.getDoorOpen() : null,
                    r != null ? r.getTimestamp() : null,
                    r != null ? r.isRedAlert() : false,
                    avgTemp // ✅ PASS AVERAGE TO DTO
            );
        }).collect(Collectors.toList());
    }

    public FreezerConfigResponse getFreezerConfig(String freezerId) {

        Freezer freezer = freezerRepository.findByFreezerId(freezerId).orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));
        return new FreezerConfigResponse(freezer.getFreezerId(), freezer.getName());
    }

    public List<FreezerStatusResponse> getFullStatusForUser(String ownerUserId) {

        List<Freezer> freezers =
                freezerRepository.findByOwnerUserId(ownerUserId);

        return freezers.stream()
                .map(f -> getFreezerStatus(f.getFreezerId()))
                .toList();
    }


    public ExportResponseWrapper getExportData(String freezerId,
                                               LocalDateTime from,
                                               LocalDateTime to) {

        Freezer freezer = freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        // ================= NORMAL FREEZER =================
        if (freezer.getDeviceType() == Freezer.DeviceType.NORMAL_FREEZER) {

            var readings = freezerReadingRepository
                    .findByFreezerIdAndTimestampBetweenOrderByTimestampAsc(
                            freezerId, from, to);

            NormalFreezerExportDto.Common common =
                    new NormalFreezerExportDto.Common();

            common.topic = freezer.getFreezerId();
            common.po = freezer.getPoNumber();

            if (!readings.isEmpty()) {

                var latest = readings.get(readings.size() - 1);

                common.ambientTemperature = latest.getAmbientTemperature();
                common.ambientHumidity = latest.getambientHumidity();
                common.freezerOn = latest.getFreezerOn();
                common.compressorTemp = latest.getCompressorTemp();
                common.condenserTemp = latest.getCondenserTemp();
                common.setTemp = latest.getSetTemp();
                common.highTemp = latest.getHighTemp();
                common.lowTemp = latest.getLowTemp();
                common.batteryPercentage = latest.getBatteryPercentage();
            }

            var readingList = readings.stream().map(r -> {

                NormalFreezerExportDto.Reading dto =
                        new NormalFreezerExportDto.Reading();

                dto.timestamp = r.getTimestamp();
                dto.temperature = r.getTemperature();
                dto.doorAlarm = r.getDoorAlarm();
                dto.highTempAlarm = r.getHighTempAlarm();
                dto.lowTempAlarm = r.getLowTempAlarm();
                dto.batteryAlarm = r.getBatteryAlarm();

                return dto;

            }).toList();

            return new ExportResponseWrapper(
                    "NORMAL_FREEZER",
                    common,
                    readingList
            );
        }

        // ================= DATA LOGGER =================
        else {

            var channelReadings =
                    dataLoggerChannelRepository
                            .findByTopicAndTimestampBetweenOrderByTimestampAsc(
                                    freezerId, from, to);

            DataLoggerDevice device =
                    dlDeviceRepository.findById(freezerId)
                            .orElseThrow(() -> new RuntimeException("Device not found"));

            DataLoggerExportDto.Common common =
                    new DataLoggerExportDto.Common();

            common.topic = device.getTopic();
            common.po = device.getPoNumber();
            common.power = device.getPower();
            common.powerAlarm = device.getPowerAlarm();
            common.batteryPercentage = device.getBatteryPercentage();
            common.batteryAlarm = device.getBatteryAlarm();
            common.ambientTemperature = device.getAmbientTemperature();
            common.ambientHumidity = device.getambientHumidity();
            //common.setTemp = device.getsetTemp();

            var readingList = channelReadings.stream().map(r -> {

                DataLoggerExportDto.ChannelReading dto =
                        new DataLoggerExportDto.ChannelReading();

                dto.channelNumber = r.getChannelNumber();
                dto.timestamp = r.getTimestamp();
                dto.temperature = r.getTemperature();
                dto.status = r.getStatus();
                dto.highTemp = r.gethighTemp();
                dto.highTempAlarm = r.gethighTempAlarm();
                dto.lowTemp = r.getlowTemp();
                dto.lowTempAlarm = r.getlowTempAlarm();
                dto.setTemp = r.getsetTemp();   // ✅ CRITICAL FIX

                return dto;

            }).toList();

            return new ExportResponseWrapper(
                    "DATA_LOGGER",
                    common,
                    readingList
            );
        }
    }

    public List<DeviceInfoDto> getAllDevicesForExport() {

        List<Freezer> freezers = freezerRepository.findAll();

        return freezers.stream()
                .filter(f -> f.getFreezerId() != null)
                .map(f -> new DeviceInfoDto(
                        f.getFreezerId(),
                        f.getOwnerUserId(),
                        f.getDeviceType().name()
                ))
                .toList();
    }

}
