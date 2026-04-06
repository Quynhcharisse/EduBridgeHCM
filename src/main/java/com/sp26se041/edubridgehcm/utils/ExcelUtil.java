package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.models.School;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

public class ExcelUtil {

    public static <T> void exportToExcel(Path path,
                                        String sheetName,
                                        String[] headers,
                                        List<T> data,
                                        BiConsumer<T, Row> rowMapper) throws IOException {
        //path handling NIO.2
        Path parent = path.getParent();

        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent); // create directory if not already there
        }

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream os = Files.newOutputStream(path)) {

            Sheet sheet = workbook.createSheet(sheetName);

            // create header
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowIdx++);
                rowMapper.accept(item, row);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(os);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
