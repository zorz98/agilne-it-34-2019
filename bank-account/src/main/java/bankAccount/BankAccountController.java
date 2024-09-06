package bankAccount;

import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dtos.CustomUserDTO;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankAccountController {
    @Autowired
    private BankAccountRepository repo;

    @Autowired
    private UserServiceProxy proxy;

    @GetMapping("/bank-account")
    public ResponseEntity<List<BankAccount>> getAllBankAccounts(){
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/bank-account/{email}")
    public ResponseEntity<BankAccount> getBankAccountByEmail(@PathVariable String email){
        if(!repo.existsByEmail(email)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var account = repo.findByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @GetMapping("/bank-account/current-user")
    public ResponseEntity<?> getBankAccountForUser(@RequestHeader("Authorization") String auth){
        String currentUserEmail = getCurrentUserEmailFromAuth(auth);
        BankAccount bankAccount = repo.findByEmail(currentUserEmail);

        if(bankAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You do not have created bank account.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(bankAccount);
    }

    @PostMapping("/bank-account")
    public ResponseEntity<?> addNewBankAccount(@RequestBody BankAccount newAccount){
        ResponseEntity<CustomUserDTO> response = proxy.getUserByEmail(newAccount.getEmail());
        CustomUserDTO responseBody = response.getBody();

        if(responseBody == null) {
            return ResponseEntity.badRequest().body("User with given email doesn't exist!");
        }

        if(repo.existsById(newAccount.getId())) {
            return ResponseEntity.badRequest().body("Account with given id already exists!");
        }

        if(repo.existsByEmail(newAccount.getEmail())) {
            return ResponseEntity.badRequest().body("Account with given email already exists!");
        }

        repo.save(newAccount);
        return ResponseEntity.ok(newAccount);
    }

    @PutMapping("/bank-account/{email}")
    public ResponseEntity<?> updateBankAccount(@RequestBody BankAccount updatedAccount, @PathVariable String email){
        BankAccount currentBankAccount = repo.findByEmail(email);
        CustomUserDTO existingUser = proxy.getUserByEmail(updatedAccount.getEmail()).getBody();

        if(existingUser == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There is no user with provided email!");
        }

        if(currentBankAccount.getId() != updatedAccount.getId()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid, different bank account ids!");
        }

        repo.save(updatedAccount);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAccount);
    }

    @PutMapping("/bank-account/convert")
    public ResponseEntity<BankAccount> convertCurrencies(@RequestBody BankAccount updatedAccount){
        repo.save(updatedAccount);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAccount);
    }

    @DeleteMapping("/bank-account/{email}")
    public ResponseEntity<String> deleteBankAccountByEmail(@PathVariable String email){
        if (!repo.existsByEmail(email)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var bankAccount = repo.findByEmail(email);
        repo.delete(bankAccount);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> rateLimiterExceptionHandler(Exception ex){
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getCurrentUserEmailFromAuth(String auth) {
        String credentials = new String(Base64.getDecoder().decode(auth.substring(6)));
        String[] emailPassword = credentials.split(":");
        return emailPassword[0];
    }
}
