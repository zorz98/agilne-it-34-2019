package apiGateway.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

import authentication.dtos.CustomUserDto;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {
	
	@Bean
	public MapReactiveUserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		List<CustomUserDto> usersFromDatabase;
		
		ResponseEntity<CustomUserDto[]> response = 
		new RestTemplate().getForEntity("http://localhost:8770/user", CustomUserDto[].class);
		
		usersFromDatabase = Arrays.asList(response.getBody());
		
		for(CustomUserDto cud: usersFromDatabase) {
			users.add(User.withUsername(cud.getEmail())
					.password(encoder.encode(cud.getPassword()))
					.roles(cud.getRole())
					.build());
		}
		
		
		return new MapReactiveUserDetailsService(users);
	}
	
	@Bean
	public BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
		http.csrf().disable()
		.authorizeExchange()
		.pathMatchers(HttpMethod.PUT, "/user/**").hasAnyRole("ADMIN","OWNER")
		.pathMatchers(HttpMethod.DELETE, "/user/**").hasAnyRole("OWNER")
		.pathMatchers(HttpMethod.POST, "/user/**").hasAnyRole("OWNER", "ADMIN")
		.pathMatchers(HttpMethod.GET,"/user").hasAnyRole("ADMIN","OWNER")
		.pathMatchers(HttpMethod.GET,"/user/**").permitAll()
		.pathMatchers("/currency-exchange/**").permitAll()
		.pathMatchers("/bank-account/current-user").hasRole("USER")
		.pathMatchers(HttpMethod.POST,"/bank-account").hasRole("ADMIN")
		.pathMatchers(HttpMethod.PUT,"/bank-account/{email}").hasRole("ADMIN")
		.pathMatchers(HttpMethod.DELETE,"/bank-account/{email}").hasRole("ADMIN")
		.pathMatchers("/currency-conversion/**").hasAnyRole("ADMIN","USER")
		.and()
		.httpBasic();
		
		return http.build();
	}
}
