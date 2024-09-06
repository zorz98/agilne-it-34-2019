package currencyConversion;

import dtos.BankAccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bank-account")
public interface BankAccountServiceProxy {

    @GetMapping("/bank-account/{email}")
    public ResponseEntity<BankAccountDTO> getBankAccountByEmail(@PathVariable String email);

    @PutMapping("/bank-account/convert")
    public ResponseEntity<BankAccountDTO> convertCurrencies(@RequestBody BankAccountDTO updatedAccount);
}
