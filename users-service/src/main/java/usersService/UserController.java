package usersService;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import usersService.dtos.BankAccountDTO;
import usersService.model.CustomUser;

@RestController
public class UserController {

	@Autowired
	private CustomUserRepository repo;

	@Autowired
	private BankAccountServiceProxy proxy;
	
	@GetMapping("/user")
	public List<CustomUser> getAllUsers(){
		return repo.findAll();
	}

	@GetMapping("/user/{email}")
	public ResponseEntity<CustomUser> getUserByEmail(@PathVariable String email){
		CustomUser user = repo.findByEmail(email);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@PostMapping("/user")
	public ResponseEntity<?> createUser(@RequestHeader("Authorization")String auth, @RequestBody CustomUser user) {
		if(repo.existsById(user.getId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("User %s already exists.", user.getEmail()));
		}

		CustomUser currentUser = getCurrentUser(auth);

		if(user.getRole().equals("ADMIN")) {
			if(!currentUser.getRole().equals("OWNER")) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission for this action.");
			}

			CustomUser createdUser = repo.save(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
		} else if (user.getRole().equals("OWNER")){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OWNER user can not be created.");
		} else {
			CustomUser createdUser = repo.save(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
		}
	}

	@PutMapping("/user")
	public ResponseEntity<?> updateUser(@RequestHeader("Authorization")String auth, @RequestBody CustomUser user){
		CustomUser currentUser = getCurrentUser(auth);

		if(!repo.existsById(user.getId())) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		CustomUser existingUser = repo.findById(user.getId());

		if(user.getRole().equals("ADMIN")) {

			if(!currentUser.getRole().equals("OWNER")) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission for this action.");
			}
			CustomUser savedUser = repo.save(user);
			return new ResponseEntity<>(savedUser, HttpStatus.OK);
		} else if (user.getRole().equals("OWNER")){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OWNER user can not be created.");
		} else {
			if(!existingUser.getRole().equals("USER")) {
				if(!currentUser.getRole().equals("OWNER")) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission for this action.");
				}
				if(existingUser.getRole().equals("OWNER")) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission for this action.");
				}
				CustomUser savedUser = repo.save(user);
				return new ResponseEntity<>(savedUser, HttpStatus.OK);
			}

			String existingUserEmail = existingUser.getEmail();

			BankAccountDTO account = proxy.getBankAccountByEmail(existingUserEmail).getBody();
			if(account != null){
				account.setEmail(user.getEmail());
				proxy.updateBankAccount(account, existingUserEmail);
			}

			CustomUser savedUser = repo.save(user);
			return new ResponseEntity<>(savedUser, HttpStatus.OK);
		}
	}

	@DeleteMapping("/user/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable long id, @RequestHeader("Authorization")String auth){
		if(!repo.existsById(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with provided id does not exist.");
		}

		var user = repo.findById(id);

		proxy.deleteBankAccountByEmail(user.getEmail());
		repo.deleteById(id);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User successfully deleted.");
	}

	@ExceptionHandler({Exception.class})
	public ResponseEntity<String> rateLimiterExceptionHandler(Exception ex){
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	private CustomUser getCurrentUser(String auth) {
		String credentials = new String(Base64.getDecoder().decode(auth.substring(6)));
		String[] emailPassword = credentials.split(":");
		String email = emailPassword[0];
		return repo.findByEmail(email);
	}
 }
