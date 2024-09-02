package com.example.api;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.persistence.AwsRdsService;
import com.example.service.AwsSnsService;

@RestController
@RequestMapping("/api/sns")
public class SnsRestApiController {

	private AwsSnsService awsSnsService;
	private AwsRdsService awsRdsService;

	public SnsRestApiController(AwsSnsService awsSnsService, AwsRdsService awsRdsService) {
		this.awsSnsService = awsSnsService;
		this.awsRdsService = awsRdsService;
	}

	@GetMapping
	public String getIndex() {
		return "Spring Boot Sns controller default index response.";
	}

	@PostMapping("/subscriptions")
	public ResponseEntity<?> uploadFile(@RequestParam String email, @RequestParam Optional<Boolean> unsubscribed) {
		if (!unsubscribed.isPresent()) {
			String subscriptionArn = awsSnsService.subscribe(email);
			if (subscriptionArn != null) {
				awsRdsService.saveSubscription(subscriptionArn, email);
				return new ResponseEntity<>(HttpStatus.OK);
			}
		} else if (unsubscribed.isPresent() && unsubscribed.get() == Boolean.TRUE) {
			String arnSubscription = awsRdsService.getArnSubscription(email).arn();
			if (arnSubscription != null && awsSnsService.unsubscribe(arnSubscription) == true) {
				awsRdsService.updateSubscription(email, unsubscribed.get());
				return new ResponseEntity<>(HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
