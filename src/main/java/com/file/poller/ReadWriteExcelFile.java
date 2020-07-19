package com.file.poller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadWriteExcelFile {

	public XSSFWorkbook processExcelFile(String readPath, String fileSheet, long threadSleepTime) {
		MultiValuedMap<String, List<Object>> transformedMap = readExcelFile(readPath, fileSheet, threadSleepTime);
		XSSFWorkbook workbook = writeToExcelFile(transformedMap);
		return workbook;
	}

	public void makeHeaderBold(XSSFWorkbook workbook, Row row) {
		XSSFFont font = workbook.createFont();
		font.setBold(true);

		CellStyle style = row.getRowStyle();
		style.setFont(font);
		for (int i = 0; i < row.getLastCellNum(); i++) {// For each cell in the row
			row.getCell(i).setCellStyle(style);// Set the style
		}
	}

	public XSSFWorkbook writeToExcelFile(MultiValuedMap<String, List<Object>> transformedMap) {
		// Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();
		// Create a blank sheet
		Collection<List<Object>> uniqueCellValue = transformedMap.get("cellvalues");
		List<Object> uniqueCellValList = uniqueCellValue.iterator().hasNext() ? uniqueCellValue.iterator().next()
				: null;
		for (Object uniqueCell : uniqueCellValList) {
			String val = String.valueOf(uniqueCell);
			XSSFSheet sheet = null;
			if (workbook.getSheet(val) == null) {
				sheet = workbook.createSheet(val);
				Row header = sheet.createRow(0);
				header.createCell(0).setCellValue("Tag");
				header.createCell(1).setCellValue("Questions");
				header.createCell(2).setCellValue("Answers");

			} else {
				sheet = workbook.getSheet(val);
			}
			// sheet.getRow(sheet.s)
			// Row row = sheet.createRow(rownum++);

			Collection<List<Object>> objArr = transformedMap.get(val);
			Iterator<List<Object>> iterator = objArr.iterator();
			int rowNum = 1;
			while (iterator.hasNext()) {
				Row row = sheet.createRow(rowNum++);
				List<Object> eachRow = iterator.next();
				int cellnum = 0;
				for (Object obj : eachRow) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}

		}
		return workbook;
	}

	public MultiValuedMap<String, List<Object>> readExcelFile(String readPath, String fileSheet, long threadSleepTime) {
		MultiValuedMap<String, List<Object>> map = new ArrayListValuedHashMap<>();
		try {
			DataFormatter dataFormatter = new DataFormatter();
			List<Object> uniqueCellValuesList = new ArrayList<>();
			Set<Object> uniqueCellValuesSet = new HashSet<Object>();

			Thread.sleep(threadSleepTime);

			InputStream file = new FileInputStream(new File(readPath));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheet(fileSheet);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				List<Object> eachRow = new ArrayList<>();
				Row row = rowIterator.next();
				if (row.getRowNum() == 0)
					continue;
				uniqueCellValuesSet.add(row.getCell(0).toString());
				// System.out.println("cell value: " + row.getCell(0));
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellTypeEnum()) {
					case NUMERIC:
						// System.out.print(cell.getNumericCellValue() + "\t\t");
						eachRow.add(cell.getNumericCellValue());
						break;
					case STRING:
						// System.out.print(cell.getStringCellValue() + "\t\t");
						eachRow.add(cell.getStringCellValue());
						break;
					case _NONE:
						break;
					default:
						break;
					}
				}
				// System.out.println("");
				map.put(row.getCell(0).toString(), eachRow);
			}
			uniqueCellValuesList.addAll(uniqueCellValuesSet);
			map.put("cellvalues", uniqueCellValuesList);

			workbook.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("-----------------Printing Map Values-----------------");
		// System.out.println(map);
		return map;
	}

	public void readInputStream(InputStream stream, String assesmentType) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook(stream);
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			System.out.println(workbook.getSheetName(i));
		}
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				switch (cell.getCellTypeEnum()) {
				case NUMERIC:
					System.out.print(cell.getNumericCellValue() + "\t\t");
					break;
				case STRING:
					System.out.print(cell.getStringCellValue() + "\t\t");
					break;
				case _NONE:
					break;
				default:
					break;
				}
			}
			System.out.println("");
		}
	}
}
