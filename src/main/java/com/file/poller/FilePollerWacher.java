package com.file.poller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

public class FilePollerWacher {

	private String fileSource;

	private String fileIntermediate;

	private String fileTarget;

	private String fileSheet;

	private String uploadUrl;

	private long threadSleepTime;

	public FilePollerWacher() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FilePollerWacher(String fileSource, String fileIntermediate, String fileTarget, String fileSheet,
			String uploadUrl, long threadSleepTime) {
		super();
		this.fileSource = fileSource;
		this.fileIntermediate = fileIntermediate;
		this.fileTarget = fileTarget;
		this.fileSheet = fileSheet;
		this.uploadUrl = uploadUrl;
		this.threadSleepTime = threadSleepTime;
	}

	public void fileWatcher() {

		/*
		 * try { WatchService watchService = FileSystems.getDefault().newWatchService();
		 * 
		 * System.out.println(fileSource);
		 * 
		 * Path path = Paths.get(fileSource);
		 * 
		 * path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
		 * 
		 * WatchKey key; while ((key = watchService.take()) != null) { for
		 * (WatchEvent<?> event : key.pollEvents()) { try {
		 * System.out.println("Event kind:" + event.kind() + ". File affected: " +
		 * event.context() + "."); String fileName = "" + event.context(); if
		 * (fileName.contains("xlsx")) { System.out.println("inside " + fileName);
		 * 
		 * Path sourcePath = Paths.get(fileSource + fileName);
		 * 
		 * XSSFWorkbook workbook = new ReadWriteExcelFile().processExcelFile(fileSource
		 * + fileName, fileSheet, threadSleepTime);
		 * 
		 * OutputStream out = new FileOutputStream(new File(fileIntermediate +
		 * fileName)); workbook.write(out);
		 * 
		 * workbook.close(); out.close();
		 * 
		 * boolean exists = Files.exists(sourcePath); if (exists) {
		 * Files.delete(sourcePath); System.out.println(" SourcePath file deleted " +
		 * sourcePath); }
		 * 
		 * fileUpload(fileName);
		 * 
		 * } } catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * } key.reset(); System.out.println("File posted to destination");
		 * System.out.println("key is reseted ready for next file upload"); } } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 * catch (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		// while(true) {
		String[] assessmentTypeSuitability = { "suitability","TechnicalFitness" };
		String[] assessmentType = { "TechnicalFitness" };

		fileUpload("API_Input_tech.xlsx", assessmentType);
		fileUpload("API_Input_sus.xlsx", assessmentTypeSuitability);
		// }

	}

	public void fileUpload(String fileName, String[] assessmentTypeArr) {

		for (String assessmentType : assessmentTypeArr) {

			try {

				CloseableHttpClient httpclient = HttpClients.createDefault();
				MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();

				entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

				File file = new File(fileIntermediate + fileName);
				entitybuilder.addBinaryBody("applicationScoresSheet", file);
				entitybuilder.addTextBody("assessmentType", assessmentType);
				HttpEntity mutiPartHttpEntity = entitybuilder.build();

				RequestBuilder reqbuilder = RequestBuilder.post(uploadUrl);

				reqbuilder.setEntity(mutiPartHttpEntity);
				HttpUriRequest multipartRequest = reqbuilder.build();
				HttpResponse httpresponse = httpclient.execute(multipartRequest);

				HttpEntity entity = httpresponse.getEntity();

				try (InputStream inputStream = entity.getContent()) {
					XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
					for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
						System.out.println(workbook.getSheetName(i));
					}
					XSSFSheet sheet = workbook.getSheetAt(0);
					try {
						File f = new File("target.xlsx");
						if (f.exists()) {
							InputStream existingFile = new FileInputStream(f);
							XSSFWorkbook existingWorkbook = new XSSFWorkbook(existingFile);
							System.out.println("No of sheets: " + existingWorkbook.getNumberOfSheets());
							XSSFSheet newSheet = existingWorkbook.createSheet(sheet.getSheetName());
							copy(sheet, newSheet);
							FileOutputStream out = new FileOutputStream(f);
							existingWorkbook.write(out);
							out.close();
						/*	for (int i = 0; i <= sheet.getLastRowNum(); i++) {
								Row row = sheet.getRow(i);
								XSSFRow destRow = newSheet.createRow(i);
								short cellCount = row.getLastCellNum();
								for (int cellIterator = 0; cellIterator <= cellCount; cellIterator++) {
									destRow.createCell(cellIterator)
											.setCellValue(String.valueOf(row.getCell(cellIterator)));
								}
							} */
							// PoiCopySheet.copySheet(sheet, destinationSheet);

						} else {
							FileOutputStream out = new FileOutputStream(f);
							workbook.write(out);
							out.close();
						}
						// Write the workbook in file system

						System.out.println("howtodoinjava_demo.xlsx written successfully on disk.");
					} catch (Exception e) {
						e.printStackTrace();
					}

					/*
					 * Files.copy(inputStream, Paths.get(fileTarget + "result" + fileName),
					 * StandardCopyOption.REPLACE_EXISTING);
					 */
					// new ReadWriteExcelFile().readInputStream(inputStream, assessmentType);
				}

				// Path interPath = Paths.get(fileIntermediate + fileName);
				// boolean interExists = Files.exists(interPath);

				/*
				 * if (interExists) { Files.delete(interPath);
				 * System.out.println("Intermediate file deleted " + interPath); }
				 */
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void copy(Sheet sheetFromOldWorkbook, Sheet sheetForNewWorkbook) throws IOException {
		// Need this to copy over styles from old sheet to new sheet. Next step will be
		// processed below
		Row row;
		Cell cell;

		for (int rowIndex = 0; rowIndex < sheetFromOldWorkbook.getPhysicalNumberOfRows(); rowIndex++) {
			row = sheetForNewWorkbook.createRow(rowIndex); // create row in this new sheet
			for (int colIndex = 0; colIndex < sheetFromOldWorkbook.getRow(rowIndex)
					.getPhysicalNumberOfCells(); colIndex++) {
				cell = row.createCell(colIndex); // create cell in this row of this new sheet
				// get cell from old/original Workbook's sheet and when cell is null, return it
				// as blank cells.
				// And Blank cell will be returned as Blank cells. That will not change.
				Cell c = sheetFromOldWorkbook.getRow(rowIndex).getCell(colIndex,
						Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				if (c.getCellTypeEnum() == CellType.BLANK) {
					// System.out.println("This is BLANK " + ((XSSFCell) c).getReference());
				} else {
					// Below is where all the copying is happening.
					// First it copies the styles of each cell and then it copies the content.
					CellStyle origStyle = c.getCellStyle();

					switch (c.getCellTypeEnum()) {
					case STRING:
						cell.setCellValue(c.getRichStringCellValue().getString());
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							cell.setCellValue(c.getDateCellValue());
						} else {
							cell.setCellValue(c.getNumericCellValue());
						}
						break;
					case BOOLEAN:
						cell.setCellValue(c.getBooleanCellValue());
						break;
					case FORMULA:
						cell.setCellValue(c.getCellFormula());
						break;
					case BLANK:
						cell.setCellValue("");
						break;
					default:
						System.out.println();
					}
				}
			}
		}

		
	}

}
