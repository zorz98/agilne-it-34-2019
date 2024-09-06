package currencyExchange.circuitBreakerTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("currency-exchange")
public class CircuitBreakerController {

	private final Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);
	private int counter = 0;
	
	@GetMapping("test")
	@CircuitBreaker(name = "circuitBreakerTest", fallbackMethod = "circuitBreakerFallBack")
	public String testSample() {
		counter++;
		logger.info("No. of method call: " + counter);
		return new RestTemplate().getForEntity("http://localhost:8000/fakeUrl", String.class).getBody();
	}
	
	public String hardCodedResponse(Exception ex) {
		return "Service is currently unavailable";
	}
	
	public String circuitBreakerFallBack(Exception ex) {
		return "Circuit breaker activated";
	}
}
