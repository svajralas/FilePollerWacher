package com.file.poller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/")
public class AssessmentController {

	public AssessmentController() {
		// TODO Auto-generated constructor stub
	}

	@Value("${upload.url}")
	private String uploadUrl;

	@GetMapping("/greet")
	public String getGreet() {
		return "welcome";
	}

	@PostMapping("/assessment")
	public void assessment(@RequestParam("applicationScoresSheet") MultipartFile multipartFile, @RequestParam("assessmentTypes") String assessmentTypes,
			HttpServletResponse response) {

		try {

			String resultFileName = "results.xlsx";
			String inputFile = "input.xlsx";
			
			Files.deleteIfExists(Paths.get(resultFileName));
			Files.deleteIfExists(Paths.get(inputFile));
			
			Files.copy(multipartFile.getInputStream(), Paths.get(inputFile), StandardCopyOption.REPLACE_EXISTING);
			File file = new File(inputFile);
			
			String[] assessmentTypeArr = assessmentTypes.split(",");
			File resultFile = new File(resultFileName);
			
			callToAssmentEngine(file, assessmentTypeArr, resultFile);
			InputStream inputStream = new BufferedInputStream(new FileInputStream(resultFile));

			response.setContentType("application/octet-stream");

			response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + resultFileName + "\""));

			response.setContentLength((int) resultFile.length());
			FileCopyUtils.copy(inputStream, response.getOutputStream());

			System.out.println("Finished the job");

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(" Exception occured while posting applicationScoresSheet ", e);
		}

	}

	public void callToAssmentEngine(File file, String[] assessmentTypeArr, File resultFile) {

		for (String assessmentType : assessmentTypeArr) {

			try {

				CloseableHttpClient httpclient = HttpClients.createDefault();
				MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();

				entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

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

					if (workbook.getNumberOfSheets() > 0) {
						try {
							XSSFSheet sheet = workbook.getSheetAt(0);

							if (resultFile.exists()) {
								InputStream existingFile = new FileInputStream(resultFile);
								XSSFWorkbook existingWorkbook = new XSSFWorkbook(existingFile);
								System.out.println("No of existing sheets " + existingWorkbook.getNumberOfSheets());
								System.out.println("New Sheet Name " + sheet.getSheetName());
								XSSFSheet newSheet = existingWorkbook.createSheet(sheet.getSheetName());
								copy(sheet, newSheet);
								FileOutputStream out = new FileOutputStream(resultFile);
								existingWorkbook.write(out);
								out.close();
								existingWorkbook.close();
							} else {
								FileOutputStream out = new FileOutputStream(resultFile);
								workbook.write(out);
								out.close();
							}

							System.out.println("Work book sheet added");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void copy(Sheet sheetFromOldWorkbook, Sheet sheetForNewWorkbook) throws IOException {

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
