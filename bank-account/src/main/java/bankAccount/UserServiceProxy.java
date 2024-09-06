package bankAccount;

import dtos.CustomUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service")
public interface UserServiceProxy {

    @GetMapping("/user/{email}")
    public ResponseEntity<CustomUserDTO> getUserByEmail(@PathVariable String email);
}
