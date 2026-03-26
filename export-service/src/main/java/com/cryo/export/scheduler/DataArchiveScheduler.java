/*package com.cryo.export.scheduler;
import com.cryo.export.dto.DeviceInfoDto;
import com.cryo.export.service.DeviceService;
import com.cryo.export.service.ExportService;
import com.cryo.export.service.NextcloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataArchiveScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(DataArchiveScheduler.class);

    private final ExportService exportService;
    private final DeviceService deviceService;
    private final NextcloudStorageService storage;

    public DataArchiveScheduler(
            ExportService exportService,
            DeviceService deviceService,
            NextcloudStorageService storage) {

        this.exportService = exportService;
        this.deviceService = deviceService;
        this.storage = storage;
    }

    // Runs daily at 12:02 AM
    @Scheduled(cron = "* 2 0 * * *")

    public void archiveDailyData() {

        log.info("Archive job started");

        try {

            LocalDate yesterday = LocalDate.now().minusDays(1);

            LocalDateTime from = yesterday.atStartOfDay();
            LocalDateTime to = yesterday.atTime(LocalTime.MAX);

            List<DeviceInfoDto> devices = deviceService.getAllDevices();

            if (devices.isEmpty()) {
                log.info("Archive job completed - no devices found");
                return;
            }

            int processed = 0;

            for (DeviceInfoDto device : devices) {

                try {

                    byte[] pdf = exportService.exportToPdf(
                            device.getFreezerId(),
                            from,
                            to,
                            null,
                            "internal"
                    );

                    if (pdf == null || pdf.length == 0) {
                        log.warn("No data for device {}", device.getFreezerId());
                        continue;
                    }

                    uploadToNextcloud(
                            device.getOwnerUserId(),
                            device.getFreezerId(),
                            yesterday,
                            pdf
                    );

                    processed++;

                    log.info(
                            "Archived device {} for user {}",
                            device.getFreezerId(),
                            device.getOwnerUserId()
                    );

                } catch (Exception deviceError) {

                    log.error(
                            "Archive failed for device {}",
                            device.getFreezerId(),
                            deviceError
                    );
                }
            }

            log.info("Archive job completed. Devices processed: {}", processed);

        } catch (Exception e) {

            log.error("Archive job failed", e);
        }
    }


private void uploadToNextcloud(
        String userId,
        String deviceId,
        LocalDate date,
        byte[] data) {

    String base = storage.root();

    // Ensure folder structure
    storage.createFolder(base + "/" + userId);
    storage.createFolder(base + "/" + userId + "/" + deviceId);
    storage.createFolder(base + "/" + userId + "/" + deviceId + "/data");

    // Create formatted date (YYYY-MM-DD)
    String formattedDate =
            date.getYear() + "-" +
                    String.format("%02d", date.getMonthValue()) + "-" +
                    String.format("%02d", date.getDayOfMonth());

    // File name format: DEVICEID_YYYY-MM-DD.pdf
    String fileName =
            deviceId + "_" + formattedDate + ".pdf";

    String filePath =
            base + "/" +
                    userId + "/" +
                    deviceId + "/data/" +
                    fileName;

    storage.uploadFile(filePath, data);
}
}*/
package com.cryo.export.scheduler;

import com.cryo.export.dto.DeviceInfoDto;
import com.cryo.export.service.DeviceService;
import com.cryo.export.service.ExportService;
import com.cryo.export.service.NextcloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataArchiveScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(DataArchiveScheduler.class);

    private final ExportService exportService;
    private final DeviceService deviceService;
    private final NextcloudStorageService storage;

    public DataArchiveScheduler(
            ExportService exportService,
            DeviceService deviceService,
            NextcloudStorageService storage) {

        this.exportService = exportService;
        this.deviceService = deviceService;
        this.storage = storage;
    }

    // Runs daily at 12:02 AM
    @Scheduled(cron = "* 0 2 * * *")
    public void archiveDailyData() {

        log.info("Archive job started");

        try {

            LocalDate yesterday = LocalDate.now().minusDays(1);

            LocalDateTime from = yesterday.atStartOfDay();
            LocalDateTime to = yesterday.atTime(LocalTime.MAX);

            List<DeviceInfoDto> devices = deviceService.getAllDevices();

            if (devices.isEmpty()) {
                log.info("Archive job completed - no devices found");
                return;
            }

            int processed = 0;

            for (DeviceInfoDto device : devices) {

                try {

                    // ======================
                    // Generate PDF
                    // ======================

                    byte[] pdf = exportService.exportToPdf(
                            device.getFreezerId(),
                            from,
                            to,
                            null,
                            "internal"
                    );

                    // ======================
                    // Generate CSV
                    // ======================

                    byte[] csv = exportService.exportToCsv(
                            device.getFreezerId(),
                            from,
                            to,
                            null,
                            "internal"
                    );

                    if ((pdf == null || pdf.length == 0) &&
                            (csv == null || csv.length == 0)) {

                        log.warn("No data for device {}", device.getFreezerId());
                        continue;
                    }

                    // Upload PDF
                    if (pdf != null && pdf.length > 0) {
                        uploadPdfToNextcloud(
                                device.getOwnerUserId(),
                                device.getFreezerId(),
                                yesterday,
                                pdf
                        );
                    }

                    // Upload CSV
                    if (csv != null && csv.length > 0) {
                        uploadCsvToNextcloud(
                                device.getOwnerUserId(),
                                device.getFreezerId(),
                                yesterday,
                                csv
                        );
                    }

                    processed++;

                    log.info(
                            "Archived device {} for user {}",
                            device.getFreezerId(),
                            device.getOwnerUserId()
                    );

                } catch (Exception deviceError) {

                    log.error(
                            "Archive failed for device {}",
                            device.getFreezerId(),
                            deviceError
                    );
                }
            }

            log.info("Archive job completed. Devices processed: {}", processed);

        } catch (Exception e) {

            log.error("Archive job failed", e);
        }
    }

    // =========================
    // PDF UPLOAD
    // =========================

    private void uploadPdfToNextcloud(
            String userId,
            String deviceId,
            LocalDate date,
            byte[] data) {

        String base = storage.root();

        storage.createFolder(base + "/" + userId);
        storage.createFolder(base + "/" + userId + "/" + deviceId);
        storage.createFolder(base + "/" + userId + "/" + deviceId + "/data");
        storage.createFolder(base + "/" + userId + "/" + deviceId + "/data/pdf");

        String formattedDate =
                date.getYear() + "-" +
                        String.format("%02d", date.getMonthValue()) + "-" +
                        String.format("%02d", date.getDayOfMonth());

        String fileName =
                deviceId + "_" + formattedDate + ".pdf";

        String filePath =
                base + "/" +
                        userId + "/" +
                        deviceId + "/data/pdf/" +
                        fileName;

        storage.uploadFile(filePath, data);
    }

    // =========================
    // CSV UPLOAD
    // =========================

    private void uploadCsvToNextcloud(
            String userId,
            String deviceId,
            LocalDate date,
            byte[] data) {

        String base = storage.root();

        storage.createFolder(base + "/" + userId);
        storage.createFolder(base + "/" + userId + "/" + deviceId);
        storage.createFolder(base + "/" + userId + "/" + deviceId + "/data");
        storage.createFolder(base + "/" + userId + "/" + deviceId + "/data/csv");

        String formattedDate =
                date.getYear() + "-" +
                        String.format("%02d", date.getMonthValue()) + "-" +
                        String.format("%02d", date.getDayOfMonth());

        String fileName =
                deviceId + "_" + formattedDate + ".csv";

        String filePath =
                base + "/" +
                        userId + "/" +
                        deviceId + "/data/csv/" +
                        fileName;

        storage.uploadFile(filePath, data);
    }
}
