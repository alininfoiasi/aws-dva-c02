package com.example.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.model.S3Metadata;
import com.example.service.AwsS3Service;

@RestController
@RequestMapping("/api/s3")
public class S3RestApiController {

	private AwsS3Service awsS3Service;

	public S3RestApiController(AwsS3Service awsS3Service) {
		this.awsS3Service = awsS3Service;
	}

	@GetMapping
	public String getIndex() {
		return "Spring Boot S3 controller default index response.";
	}

	@GetMapping("/metadata")
	public S3Metadata getMetadata(@RequestParam String file) {
		return awsS3Service.getMetadata(file);
	}

	@PostMapping("/uploads")
    public ResponseEntity<?> uploadFile(@RequestParam String file, @RequestParam(name = "fileContents") MultipartFile fileContents){
        if (awsS3Service.upload(file, fileContents)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		};
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}