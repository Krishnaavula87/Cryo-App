package com.cryo.export.service;
import com.cryo.export.dto.DataLoggerExportDto;
import com.cryo.export.dto.ExportResponseWrapper;
import com.cryo.export.dto.NormalFreezerExportDto;
import com.cryo.export.util.PageNumberFooter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class ExportService {

    private final FreezerDataService freezerDataService;
    private final ObjectMapper mapper;
    // Reusable fonts (improves performance and reduces PDF size)
    private static final Font TITLE_FONT =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

    private static final Font NORMAL_FONT =
            FontFactory.getFont(FontFactory.HELVETICA, 9);

    private static final Font HEADER_FONT =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    private static final Font CELL_FONT =
            FontFactory.getFont(FontFactory.HELVETICA, 8);

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExportService(FreezerDataService freezerDataService,
                         ObjectMapper mapper) {
        this.freezerDataService = freezerDataService;
        this.mapper = mapper;
    }

    // =========================================================
    // ======================== CSV =============================
    // =========================================================

    public byte[] exportToCsv(String freezerId,
                              LocalDateTime from,
                              LocalDateTime to,
                              String channel,
                              String auth) throws Exception {

        String tableJson =
                freezerDataService.getExportData(freezerId, from, to, auth);

        ExportResponseWrapper wrapper =
                //mapper.readValue(tableJson, ExportResponseWrapper.class);
                mergeResponses(tableJson);

        if ("DATA_LOGGER".equals(wrapper.getDeviceType())) {

            DataLoggerExportDto dto =
                    mapper.convertValue(wrapper, DataLoggerExportDto.class);

            return generateDataLoggerCsv(dto, channel);

        } else {

            NormalFreezerExportDto dto =
                    mapper.convertValue(wrapper, NormalFreezerExportDto.class);

            return generateNormalFreezerCsv(dto);
        }
    }


    private byte[] generateNormalFreezerCsv(NormalFreezerExportDto res) throws Exception {

        try (StringWriter writer = new StringWriter();
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            var c = res.getCommon();

            csv.printRecord("CRYO SCIENTIFIC SYSTEMS - NORMAL FREEZER REPORT");
            csv.printRecord("Generated On", LocalDateTime.now().format(FORMAT));
            csv.println();

            // ✅ Column map at TOP
            csv.printRecord("COLUMN MAP");
            csv.printRecord("Temp = Temperature",
                    "High Temp = High Temperature",
                    "Low Temp = Low Temperature",
                    "Batt % = Battery Percentage",
                    "setTemp=setTemp",
                    "Door Alm = Door Alarm",
                    "High Alm = High Alarm",
                    "Low Alm = Low Alarm",
                    "Batt Alm = Battery Alarm");
            csv.println();

            // Common Fields
            csv.printRecord("Device ID", c.topic);
            csv.printRecord("PO Number", c.po);
            csv.printRecord("Ambient Temp", c.ambientTemperature);
            csv.printRecord("ambientHumidity", c.ambientHumidity);
            csv.println();

            csv.printRecord("Time",
                    "Temp",
                    "High Temp",
                    "Low Temp",
                    "Batt %",
                    "setTemp",
                    "Door Alm",
                    "High Alm",
                    "Low Alm",
                    "Batt Alm");

            for (var r : res.getReadings()) {
                csv.printRecord(
                        r.timestamp.format(FORMAT),
                        r.temperature,
                        c.highTemp,
                        c.lowTemp,
                        c.batteryPercentage,
                        c.setTemp,
                        bool(r.doorAlarm),
                        bool(r.highTempAlarm),
                        bool(r.lowTempAlarm),
                        bool(r.batteryAlarm)
                );
            }

            csv.flush();
            return writer.toString().getBytes();
        }
    }


    private byte[] generateDataLoggerCsv(
            DataLoggerExportDto res,
            String channelFilter) throws Exception {

        try (StringWriter writer = new StringWriter();
             CSVPrinter csv =
                     new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            var c = res.getCommon();

            // =====================================================
            // ================= HEADER SECTION =====================
            // =====================================================

            csv.printRecord("CRYO SCIENTIFIC SYSTEMS");
            csv.printRecord("DATA LOGGER REPORT");
            csv.printRecord("Generated On",
                    LocalDateTime.now().format(FORMAT));
            csv.println();

            // Device Details
            csv.printRecord("DEVICE DETAILS");
            csv.printRecord("Device ID", c.topic);
            csv.printRecord("PO Number", c.po);
            csv.printRecord("Power", c.power);
            //csv.printRecord("Battery %", c.batteryPercentage);
            csv.printRecord("Ambient Temp", c.ambientTemperature);
            csv.printRecord("ambientHumidity", c.ambientHumidity);
            //csv.printRecord("Set Temp", c.setTemp);
            csv.println();

            // Column Map
            csv.printRecord("COLUMN MAP");
            csv.printRecord("Ch = Channel");
            csv.printRecord("Time = Timestamp");
            csv.printRecord("Temp = Temperature");
            csv.printRecord("High Temp = High Temperature");
            csv.printRecord("Low Temp = Low Temperature");
            csv.printRecord("Status = Channel Status");
            csv.printRecord("High Alm = High Alarm");
            csv.printRecord("Low Alm = Low Alarm");
            csv.printRecord("Set Temp = Set Temperature");
            csv.printRecord("Batt % = Battery Percentage");
            csv.println();

            csv.printRecord("CHANNEL DATA");
            csv.println();

            // =====================================================
            // ================= TABLE HEADER ======================
            // =====================================================

            csv.printRecord(
                    "Ch",
                    "Time",
                    "Temp",
                    "High Temp",
                    "Low Temp",
                    "Status",
                    "High Alm",
                    "Low Alm",
                    "Set Temp",
                    "Batt %"
            );

            // =====================================================
            // ================= TABLE DATA ========================
            // =====================================================

            for (var r : res.getReadings()) {

                // 🔥 Channel filter support
                if (channelFilter != null &&
                        !channelFilter.isBlank() &&
                        !channelFilter.equalsIgnoreCase(r.channelNumber)) {
                    continue;
                }

                csv.printRecord(
                        r.channelNumber,
                        r.timestamp.format(FORMAT),
                        value(r.temperature),
                        value(r.highTemp),
                        value(r.lowTemp),
                        r.status,
                        r.highTempAlarm,
                        r.lowTempAlarm,
                        //value(r.setTemp),
                        r.setTemp,
                        value(c.batteryPercentage)
                );
            }

            csv.flush();
            return writer.toString().getBytes();
        }
    }
    // =========================================================
    // ======================== PDF =============================
    // =========================================================

    public byte[] exportToPdf(String freezerId,
                              LocalDateTime from,
                              LocalDateTime to,
                              String channel,
                              String auth) throws Exception {

        String tableJson =
                freezerDataService.getExportData(freezerId, from, to, auth);

        String chartJson =
                freezerDataService.getChartData(
                        freezerId, from, to, channel, auth);

        ExportResponseWrapper wrapper =
                //mapper.readValue(tableJson, ExportResponseWrapper.class);
                mergeResponses(tableJson);

        List<ChartPoint> chartPoints =
                parseChartPoints(chartJson);

        if ("DATA_LOGGER".equals(wrapper.getDeviceType())) {

            DataLoggerExportDto dto =
                    mapper.convertValue(wrapper, DataLoggerExportDto.class);



            // 🔥 FILTER TABLE DATA IF CHANNEL PROVIDED
            if (channel != null && !channel.isBlank()) {
                dto.setReadings(
                        dto.getReadings()
                                .stream()
                                .filter(r -> channel.equalsIgnoreCase(r.channelNumber))
                                .toList()
                );
            }

            return generateDataLoggerPdf(dto, chartPoints);
        } else {

            NormalFreezerExportDto dto =
                    mapper.convertValue(wrapper, NormalFreezerExportDto.class);

            return generateNormalFreezerPdf(dto, chartPoints);
        }
    }




    private byte[] generateNormalFreezerPdf(
            NormalFreezerExportDto res,
            List<ChartPoint> chartPoints) throws Exception {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
//            PdfWriter writer = PdfWriter.getInstance(document, baos);
//            writer.setPageEvent(new PageNumberFooter());
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setFullCompression(); // reduces PDF size
            writer.setPageEvent(new PageNumberFooter());
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph(
                    "CRYO SCIENTIFIC SYSTEMS - NORMAL FREEZER REPORT",
                    titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph gen = new Paragraph(
                    "Generated On: " + LocalDateTime.now().format(FORMAT),
                    normalFont);
            gen.setAlignment(Element.ALIGN_CENTER);
            document.add(gen);

            document.add(new Paragraph(" "));

            var c = res.getCommon();

            // =================================================
            // HEADER SECTION (DEVICE LEFT + COLUMN MAP RIGHT)
            // =================================================

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20);
            headerTable.setWidths(new float[]{1, 1});

            // DEVICE TABLE
            PdfPTable deviceTable = new PdfPTable(2);
            deviceTable.setWidthPercentage(100);

            addHeader(deviceTable, "Field");
            addHeader(deviceTable, "Value");

            addCell(deviceTable, "Device ID", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.topic), Element.ALIGN_LEFT);

            addCell(deviceTable, "PO Number", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.po), Element.ALIGN_LEFT);

            addCell(deviceTable, "Ambient Temp", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.ambientTemperature), Element.ALIGN_LEFT);

            addCell(deviceTable, "ambientHumidity", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.ambientHumidity), Element.ALIGN_LEFT);

            PdfPCell deviceCell = new PdfPCell(deviceTable);
            deviceCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(deviceCell);

            // COLUMN MAP TABLE
            PdfPTable columnTable = new PdfPTable(2);
            columnTable.setWidthPercentage(100);

            addHeader(columnTable, "Column");
            addHeader(columnTable, "Meaning");

            addCell(columnTable, "Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "High Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "High Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "Low Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "Low Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "Batt %", Element.ALIGN_LEFT);
            addCell(columnTable, "Battery Percentage", Element.ALIGN_LEFT);

            addCell(columnTable, "Set Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "Set Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "Door Alm", Element.ALIGN_LEFT);
            addCell(columnTable, "Door Alarm", Element.ALIGN_LEFT);

            addCell(columnTable, "High Alm", Element.ALIGN_LEFT);
            addCell(columnTable, "High Alarm", Element.ALIGN_LEFT);

            addCell(columnTable, "Low Alm", Element.ALIGN_LEFT);
            addCell(columnTable, "Low Alarm", Element.ALIGN_LEFT);

            addCell(columnTable, "Batt Alm", Element.ALIGN_LEFT);
            addCell(columnTable, "Battery Alarm", Element.ALIGN_LEFT);

            PdfPCell columnCell = new PdfPCell(columnTable);
            columnCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(columnCell);

            document.add(headerTable);

            // ================= GRAPH =================

            if (chartPoints != null && !chartPoints.isEmpty()) {

                Image chartImage = createMultiLineChart(chartPoints);
                chartImage.scaleToFit(850, 300);
                chartImage.setAlignment(Image.ALIGN_CENTER);

                document.add(chartImage);
                document.newPage();
            }

            // ================= DATA TABLE =================

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);

            table.setWidths(new float[]{
                    2.2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f
            });

            addHeader(table, "Time");
            addHeader(table, "Temp");
            addHeader(table, "High Temp");
            addHeader(table, "Low Temp");
            addHeader(table, "Batt %");
            addHeader(table, "Set Temp");
            addHeader(table, "Door Alm");
            addHeader(table, "High Alm");
            addHeader(table, "Low Alm");
            addHeader(table, "Batt Alm");

            for (var r : res.getReadings()) {

                addCell(table, r.timestamp.format(FORMAT), Element.ALIGN_LEFT);
                addCell(table, value(r.temperature), Element.ALIGN_RIGHT);
                addCell(table, value(c.highTemp), Element.ALIGN_RIGHT);
                addCell(table, value(c.lowTemp), Element.ALIGN_RIGHT);
                addCell(table, value(c.batteryPercentage), Element.ALIGN_RIGHT);
                addCell(table, value(c.setTemp), Element.ALIGN_RIGHT);
                addCell(table, bool(r.doorAlarm), Element.ALIGN_CENTER);
                addCell(table, bool(r.highTempAlarm), Element.ALIGN_CENTER);
                addCell(table, bool(r.lowTempAlarm), Element.ALIGN_CENTER);
                addCell(table, bool(r.batteryAlarm), Element.ALIGN_CENTER);
            }

            document.add(table);

            document.close();
            return baos.toByteArray();
        }
    }
    private byte[] generateDataLoggerPdf(
            DataLoggerExportDto res,
            List<ChartPoint> chartPoints) throws Exception {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document =
                    new Document(PageSize.A4.rotate(), 30, 30, 40, 40);

            PdfWriter writer =
                    PdfWriter.getInstance(document, baos);
            writer.setFullCompression(); // reduces PDF size
            writer.setPageEvent(new PageNumberFooter());

            document.open();

            Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title =
                    new Paragraph("CRYO SCIENTIFIC SYSTEMS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle =
                    new Paragraph("DATA LOGGER REPORT", titleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            document.add(new Paragraph(
                    "Generated On: " + LocalDateTime.now().format(FORMAT),
                    normalFont));

            document.add(new Paragraph(" "));

            var c = res.getCommon();

            // ===== HEADER TABLE (UNCHANGED) =====

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20);

            PdfPTable deviceTable = new PdfPTable(2);

            addHeader(deviceTable, "Field");
            addHeader(deviceTable, "Value");

            addCell(deviceTable, "Device ID", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.topic), Element.ALIGN_LEFT);

            addCell(deviceTable, "PO Number", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.po), Element.ALIGN_LEFT);

            addCell(deviceTable, "Power", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.power), Element.ALIGN_LEFT);

            addCell(deviceTable, "Ambient Temp", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.ambientTemperature), Element.ALIGN_LEFT);

            addCell(deviceTable, "ambientHumidity", Element.ALIGN_LEFT);
            addCell(deviceTable, value(c.ambientHumidity), Element.ALIGN_LEFT);

            PdfPCell deviceCell = new PdfPCell(deviceTable);
            deviceCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(deviceCell);

            PdfPTable columnTable = new PdfPTable(2);

            addHeader(columnTable, "Column");
            addHeader(columnTable, "Meaning");

            addCell(columnTable, "Ch", Element.ALIGN_LEFT);
            addCell(columnTable, "Channel", Element.ALIGN_LEFT);

            addCell(columnTable, "Time", Element.ALIGN_LEFT);
            addCell(columnTable, "Timestamp", Element.ALIGN_LEFT);

            addCell(columnTable, "Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "High Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "High Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "Low Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "Low Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "Status", Element.ALIGN_LEFT);
            addCell(columnTable, "Channel Status", Element.ALIGN_LEFT);

            addCell(columnTable, "High Alm", Element.ALIGN_LEFT);
            addCell(columnTable, "High Alarm", Element.ALIGN_LEFT);

            addCell(columnTable, "Low Alm", Element.ALIGN_LEFT);
            addCell(columnTable, "Low Alarm", Element.ALIGN_LEFT);

            addCell(columnTable, "Set Temp", Element.ALIGN_LEFT);
            addCell(columnTable, "Set Temperature", Element.ALIGN_LEFT);

            addCell(columnTable, "Batt %", Element.ALIGN_LEFT);
            addCell(columnTable, "Battery Percentage", Element.ALIGN_LEFT);

            PdfPCell columnCell = new PdfPCell(columnTable);
            columnCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(columnCell);

            document.add(headerTable);

            // ===== GRAPH (UNCHANGED) =====

            if (chartPoints != null && !chartPoints.isEmpty()) {

                Image chartImage = createMultiLineChart(chartPoints);
                chartImage.scaleToFit(850, 300);
                chartImage.setAlignment(Image.ALIGN_CENTER);

                document.add(chartImage);
            }

            document.newPage();

            // ===== DETECT SINGLE CHANNEL =====

            Set<String> channels = new LinkedHashSet<>();

            for (var r : res.getReadings())
                channels.add(r.channelNumber);

            boolean singleChannelRequest = channels.size() == 1;

            // =====================================================
            // CASE 1 : FILTERED CHANNEL → USE OLD TABLE
            // =====================================================

            if (singleChannelRequest) {

                PdfPTable table = new PdfPTable(10);
                table.setWidthPercentage(100);

                table.setWidths(new float[]{
                        0.8f, 1.8f, 0.8f, 0.9f, 0.9f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f
                });

                addHeader(table, "Ch");
                addHeader(table, "Time");
                addHeader(table, "Temp");
                addHeader(table, "High Temp");
                addHeader(table, "Low Temp");
                addHeader(table, "Status");
                addHeader(table, "High Alm");
                addHeader(table, "Low Alm");
                addHeader(table, "Set Temp");
                addHeader(table, "Batt %");

                for (var r : res.getReadings()) {

                    addCell(table, r.channelNumber, Element.ALIGN_CENTER);
                    addCell(table, r.timestamp.format(FORMAT), Element.ALIGN_LEFT);
                    addCell(table, value(r.temperature), Element.ALIGN_RIGHT);
                    addCell(table, value(r.highTemp), Element.ALIGN_RIGHT);
                    addCell(table, value(r.lowTemp), Element.ALIGN_RIGHT);
                    addCell(table, r.status, Element.ALIGN_CENTER);
                    addCell(table, r.highTempAlarm, Element.ALIGN_CENTER);
                    addCell(table, r.lowTempAlarm, Element.ALIGN_CENTER);
                    //addCell(table, value(c.setTemp), Element.ALIGN_RIGHT);
                    addCell(table, value(r.setTemp), Element.ALIGN_RIGHT); // ✅ FIXED

                    addCell(table, value(c.batteryPercentage), Element.ALIGN_RIGHT);
                }

                document.add(table);

            }

            // =====================================================
            // CASE 2 : ALL CHANNELS → USE NEW MATRIX LAYOUT
            // =====================================================

            else {

                // ===============================
                // TEMPERATURE MATRIX TABLE
                // ===============================

                Map<LocalDateTime, Map<String, String>> matrix = new LinkedHashMap<>();

                for (var r : res.getReadings()) {

                    matrix.computeIfAbsent(r.timestamp, k -> new HashMap<>())
                            .put(r.channelNumber, value(r.temperature));
                }

                PdfPTable tempTable = new PdfPTable(channels.size() + 1);
                tempTable.setWidthPercentage(100);

                addHeader(tempTable, "Time");

                for (String ch : channels)
                    addHeader(tempTable, ch);

                for (var entry : matrix.entrySet()) {

                    addCell(tempTable,
                            entry.getKey().format(FORMAT),
                            Element.ALIGN_LEFT);

                    for (String ch : channels) {

                        String temp = entry.getValue()
                                .getOrDefault(ch, "-");

                        addCell(tempTable, temp, Element.ALIGN_RIGHT);
                    }
                }

                document.add(new Paragraph("Temperature Table (All Channels)", titleFont));
                document.add(new Paragraph(" "));
                document.add(tempTable);

                document.newPage();

                // ===============================
                // CHANNEL WISE TABLES
                // ===============================

                Map<String, List<DataLoggerExportDto.ChannelReading>> channelMap =
                        new LinkedHashMap<>();

                for (var r : res.getReadings()) {

                    channelMap
                            .computeIfAbsent(r.channelNumber,
                                    k -> new ArrayList<>())
                            .add(r);
                }

                for (String ch : channelMap.keySet()) {

                    document.add(new Paragraph("Channel " + ch, titleFont));
                    document.add(new Paragraph(" "));

                    PdfPTable table = new PdfPTable(9);
                    table.setWidthPercentage(100);

                    addHeader(table, "Time");
                    addHeader(table, "Temp");       // ✅ NEW
                    addHeader(table, "High Temp");
                    addHeader(table, "Low Temp");
                    addHeader(table, "Status");
                    addHeader(table, "High Alm");
                    addHeader(table, "Low Alm");
                    addHeader(table, "Set Temp");
                    addHeader(table, "Batt %");

                    for (var r : channelMap.get(ch)) {

                        addCell(table,
                                r.timestamp.format(FORMAT),
                                Element.ALIGN_LEFT);

                        addCell(table,
                                value(r.temperature),  // ✅ NEW
                                Element.ALIGN_RIGHT);

                        addCell(table,
                                value(r.highTemp),
                                Element.ALIGN_RIGHT);

                        addCell(table,
                                value(r.lowTemp),
                                Element.ALIGN_RIGHT);

                        addCell(table,
                                value(r.status),
                                Element.ALIGN_CENTER);

                        addCell(table,
                                value(r.highTempAlarm),
                                Element.ALIGN_CENTER);

                        addCell(table,
                                value(r.lowTempAlarm),
                                Element.ALIGN_CENTER);
//
//                        addCell(table,
//                                value(c.setTemp),
//                                Element.ALIGN_RIGHT);

                        addCell(table,
                                value(r.setTemp),     // ✅ FIXED
                                Element.ALIGN_RIGHT);

                        addCell(table,
                                value(c.batteryPercentage),
                                Element.ALIGN_RIGHT);
                    }

                    document.add(table);
                    document.newPage();
                }
            }

            document.close();

            return baos.toByteArray();
        }
    }


    // =========================================================
    // ====================== CHART LOGIC ======================
    // =========================================================

    private List<ChartPoint> parseChartPoints(String json) throws Exception {
        List<ChartPoint> list = new ArrayList<>();
        JsonNode root = mapper.readTree(json);
        JsonNode data = root.get("data");

        for (JsonNode node : data) {
            ChartPoint p = new ChartPoint();
            p.timestamp = LocalDateTime.parse(node.get("timestamp").asText());
            p.temperature = node.get("temperature").asDouble();
            p.channel = node.has("channelNumber")
                    ? node.get("channelNumber").asText()
                    : "TEMP";
            list.add(p);
        }
        return list;
    }

    private Image createMultiLineChart(List<ChartPoint> points) throws Exception {

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        Map<String, TimeSeries> map = new HashMap<>();

        for (ChartPoint p : points) {

            TimeSeries series =
                    map.computeIfAbsent(p.channel, TimeSeries::new);

            series.addOrUpdate(
                    new Millisecond(
                            Date.from(p.timestamp
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant())),
                    p.temperature);
        }

        map.values().forEach(dataset::addSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Temperature vs Time",
                "Time",
                "Temperature (°C)",
                dataset
        );

        // 🔥 FIX SCIENTIFIC NOTATION
        NumberAxis yAxis =
                (NumberAxis) chart.getXYPlot().getRangeAxis();
        yAxis.setNumberFormatOverride(
                new DecimalFormat("0.0"));

        // Optional: force normal range padding
        yAxis.setAutoRangeIncludesZero(false);

        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
        chart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);

        BufferedImage image =
                chart.createBufferedImage(850, 320); // 🔥 Smaller height

        return Image.getInstance(image, null);
    }

    private ExportResponseWrapper mergeResponses(String json) throws Exception {

        if (json == null || json.isBlank()) {
            return null;
        }

        // ✅ If chunked response (array)
        if (json.trim().startsWith("[")) {

            java.util.List<ExportResponseWrapper> list =
                    mapper.readValue(json,
                            new com.fasterxml.jackson.core.type.TypeReference<
                                    java.util.List<ExportResponseWrapper>>() {});

            if (list == null || list.isEmpty()) {
                return null;
            }

            ExportResponseWrapper merged = list.get(0);

            for (int i = 1; i < list.size(); i++) {

                ExportResponseWrapper current = list.get(i);

                Object mergedReadingsObj = merged.getReadings();
                Object currentReadingsObj = current.getReadings();

                // ✅ SAFE CASTING
                if (mergedReadingsObj instanceof java.util.List &&
                        currentReadingsObj instanceof java.util.List) {

                    java.util.List mergedList =
                            (java.util.List) mergedReadingsObj;

                    java.util.List currentList =
                            (java.util.List) currentReadingsObj;

                    mergedList.addAll(currentList);
                }
            }

            return merged;
        }

        // ✅ Normal (non-chunked)
        return mapper.readValue(json, ExportResponseWrapper.class);
    }

    private String bool(Boolean value) {
        if (value == null) return "OFF";
        return value ? "ON" : "OFF";
    }

    private void addHeader(PdfPTable table, String text) {
//        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
//        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, int align) {
//        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
//        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "-" : text, font));
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "-" : text, CELL_FONT));
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String value(Object obj) {
        return obj == null ? "-" : obj.toString();
    }

    private static class ChartPoint {
        LocalDateTime timestamp;
        double temperature;
        String channel;
    }
}
