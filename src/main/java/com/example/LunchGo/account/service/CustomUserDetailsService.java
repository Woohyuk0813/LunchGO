package com.example.LunchGo.account.service;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.member.entity.Manager;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.Staff;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.ManagerRepository;
import com.example.LunchGo.member.repository.OwnerRepository;
import com.example.LunchGo.member.repository.StaffRepository;
import com.example.LunchGo.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final StaffRepository staffRepository;
    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String combine) throws UsernameNotFoundException {
        //combine의 형태는 "ADMIN:email"형태이어야함
        String[] parts = combine.split(":");

        if(parts.length != 2) {
            throw new UsernameNotFoundException("올바르지 않은 로그인 요청 형식");
        }

        String userType = parts[0];
        String email = parts[1]; //사용자, 임직원은 email, 사업자, 관리자는 loginId

        return switch(userType){
            case "USER" -> {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사용자 계정을 찾을 수 없습니다."));
                yield CustomUserDetails.from(user);
            }
            case "OWNER" -> {
                Owner owner = ownerRepository.findByLoginId(email)
                        .orElseThrow(() -> new UsernameNotFoundException("사업자 계정을 찾을 수 없습니다."));
                yield CustomUserDetails.from(owner);
            }
            case "STAFF" -> {
                Staff staff = staffRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("임직원 계정을 찾을 수 없습니다."));
                yield CustomUserDetails.from(staff);
            }
            case "MANAGER" -> {
                Manager manager = managerRepository.findByLoginId(email)
                        .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));
                yield CustomUserDetails.from(manager);
            }
            default -> throw new UsernameNotFoundException("올바르지 않은 로그인 권한 형식");
        };
    }
}
