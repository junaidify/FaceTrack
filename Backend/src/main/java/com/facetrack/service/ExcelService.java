package com.facetrack.service;

import com.facetrack.dto.attendance.SessionReportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public byte[] buildSessionWorkbook(String className, String sessionStartedAt,
                                       List<SessionReportRow> rows) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet ws = wb.createSheet("Attendance");

            // Title row
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = ws.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Class: " + className + "   |   Session: " + sessionStartedAt);
            titleCell.setCellStyle(titleStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Header row
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {"Roll No", "Name", "Status", "Similarity", "Matched At", "Student ID"};
            Row headerRow = ws.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 3;
            for (SessionReportRow row : rows) {
                Row dataRow = ws.createRow(rowIdx++);
                dataRow.createCell(0).setCellValue(row.getRollNo());
                dataRow.createCell(1).setCellValue(row.getName());
                dataRow.createCell(2).setCellValue(row.getStatus().name().toLowerCase());
                if (row.getSimilarity() != null) {
                    dataRow.createCell(3).setCellValue(Math.round(row.getSimilarity() * 10000.0) / 10000.0);
                }
                if (row.getMatchedAt() != null) {
                    dataRow.createCell(4).setCellValue(DT_FMT.format(row.getMatchedAt()));
                }
                dataRow.createCell(5).setCellValue(row.getStudentId().toString());
            }

            // Column widths (in units of 1/256th of a character)
            int[] widths = {12, 24, 12, 12, 22, 30};
            for (int i = 0; i < widths.length; i++) {
                ws.setColumnWidth(i, widths[i] * 256);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel workbook", e);
        }
    }
}
