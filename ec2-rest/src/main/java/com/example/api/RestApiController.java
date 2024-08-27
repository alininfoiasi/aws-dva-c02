package com.example.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.AwsEc2Service;

@RestController
@RequestMapping("/api/ec2")
public class RestApiController {

	private AwsEc2Service awsEc2Service;

	public RestApiController(AwsEc2Service awsEc2Service) {
		this.awsEc2Service = awsEc2Service;
	}

	@GetMapping
	public String getIndex() {
		return "Spring Boot controller default index response.";
	}

	@GetMapping("/region")
	public String getRegion() {
		return awsEc2Service.getRegion();
	}

	@GetMapping("/az")
	public String getAZ() {
		return awsEc2Service.getAZ();
	}

}