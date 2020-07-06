package com.file.poller;

import java.io.File;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();

			System.out.println(fileSource);

			Path path = Paths.get(fileSource);

			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			WatchKey key;
			while ((key = watchService.take()) != null) {
				for (WatchEvent<?> event : key.pollEvents()) {
					try {
						System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
						String fileName = "" + event.context();
						if (fileName.contains("xlsx")) {
							System.out.println("inside " + fileName);

							Path sourcePath = Paths.get(fileSource + fileName);

							XSSFWorkbook workbook = new ReadWriteExcelFile().processExcelFile(fileSource + fileName, fileSheet, threadSleepTime);

							OutputStream out = new FileOutputStream(new File(fileIntermediate + fileName));
							workbook.write(out);

							workbook.close();
							out.close();

							boolean exists = Files.exists(sourcePath);
							if (exists) {
								Files.delete(sourcePath);
								System.out.println(" SourcePath file deleted " + sourcePath);
							}

							fileUpload(fileName);

						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				key.reset();
				System.out.println("File posted to destination");
				System.out.println("key is reseted ready for next file upload");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fileUpload(String fileName) {

		try {

			CloseableHttpClient httpclient = HttpClients.createDefault();
			MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();

			entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			File file = new File(fileIntermediate + fileName);
			entitybuilder.addBinaryBody("applicationScoresSheet", file);
			HttpEntity mutiPartHttpEntity = entitybuilder.build();

			RequestBuilder reqbuilder = RequestBuilder
					.post(uploadUrl);

			reqbuilder.setEntity(mutiPartHttpEntity);
			HttpUriRequest multipartRequest = reqbuilder.build();
			HttpResponse httpresponse = httpclient.execute(multipartRequest);

			HttpEntity entity = httpresponse.getEntity();

			try (InputStream inputStream = entity.getContent()) {
				Files.copy(inputStream, Paths.get(fileTarget + "result_summary" + fileName),
						StandardCopyOption.REPLACE_EXISTING);
			}

			
			Path interPath = Paths.get(fileIntermediate + fileName);
			boolean interExists = Files.exists(interPath);

			if (interExists) {
				Files.delete(interPath);
				System.out.println("Intermediate file deleted " + interPath);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
