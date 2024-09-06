package usersService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usersService.dtos.BankAccountDTO;

@FeignClient(name = "bank-account")
public interface  BankAccountServiceProxy {
    @GetMapping("/bank-account/account/{email}")
    public ResponseEntity<BankAccountDTO> getBankAccountByEmail(@PathVariable String email);

    @DeleteMapping("/bank-account/account/{email}")
    public void deleteBankAccountByEmail(@PathVariable String email);

    @PutMapping("/bank-account/account/{email}")
    public ResponseEntity<?> updateBankAccount(@RequestBody BankAccountDTO updatedAccount, @PathVariable String email);
}
