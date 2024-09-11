package com.example.api;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.model.S3Metadata;
import com.example.persistence.AwsRdsService;
import com.example.service.AwsLambdaService;
import com.example.service.AwsS3Service;
import com.example.service.AwsSqsService;

@RestController
@RequestMapping("/api/s3")
public class S3RestApiController {

	private AwsS3Service awsS3Service;
	private AwsRdsService awsRdsService;
	private AwsSqsService awsSqsService;
	private AwsLambdaService awsLambdaService;

	public S3RestApiController(AwsS3Service awsS3Service, AwsRdsService awsRdsService, AwsSqsService awsSqsService, AwsLambdaService awsLambdaService) {
		this.awsS3Service = awsS3Service;
		this.awsRdsService = awsRdsService;
		this.awsSqsService = awsSqsService;
		this.awsLambdaService = awsLambdaService;
	}

	@GetMapping
	public String getIndex() {
		return "Spring Boot S3 controller default index response.";
	}

	@GetMapping("/metadata")
	public S3Metadata getMetadata(@RequestParam String file) {
		return awsRdsService.getMetadata(file);
	}

	@PostMapping("/uploads")
	public ResponseEntity<?> uploadFile(@RequestParam String file,
			@RequestParam(name = "fileContents") MultipartFile fileContents) {
		if (awsS3Service.upload(file, fileContents)) {
			S3Metadata s3Metadata = awsS3Service.getMetadata(file);
			awsRdsService.saveMetadata(s3Metadata);
			awsSqsService.sendMessage(s3Metadata);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetMapping("/downloads")
	public ResponseEntity<?> downloadFile(@RequestParam String file) {
		byte[] fileContents = awsS3Service.download(file);
		if (fileContents != null) {
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.contentLength(fileContents.length)
					.header(HttpHeaders.CONTENT_DISPOSITION,
							ContentDisposition.attachment()
									.filename(file)
									.build().toString())
					.body(new ByteArrayResource(fileContents));
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@GetMapping("/inconsistency")
	public ResponseEntity<?> checkInconsistencies() {
		String result = awsLambdaService.invokeLambda();
		if (result != null) {
			return ResponseEntity.ok()
					.body(result);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}