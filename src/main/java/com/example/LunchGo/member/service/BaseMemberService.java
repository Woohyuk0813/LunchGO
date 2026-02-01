package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.FindPwdRequest;
import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.image.dto.ImageUploadResponse;
import com.example.LunchGo.image.service.ObjectStorageService;
import com.example.LunchGo.member.domain.CustomRole;
import com.example.LunchGo.member.domain.OwnerStatus;
import com.example.LunchGo.member.domain.UserStatus;
import com.example.LunchGo.member.dto.*;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.Staff;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.mapper.MemberMapper;
import com.example.LunchGo.member.repository.ManagerRepository;
import com.example.LunchGo.member.repository.OwnerRepository;
import com.example.LunchGo.member.repository.StaffRepository;
import com.example.LunchGo.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.LunchGo.account.dto.CustomUserDetails;

@Service
@RequiredArgsConstructor
@Log4j2
public class BaseMemberService implements MemberService {
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ManagerRepository managerRepository;
    private final MemberMapper memberMapper;
    private final StaffRepository staffRepository;
    private final ObjectStorageService objectStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void save(UserJoinRequest userReq) {
        userRepository.save(User.builder()
                .email(userReq.getEmail())
                .password(passwordEncoder.encode(userReq.getPassword())) //암호화
                .name(userReq.getName())
                .companyName(userReq.getCompanyName())
                .companyAddress(userReq.getCompanyAddress())
                .phone(userReq.getPhone())
                .marketingAgree(userReq.getMarketingAgree())
                .role(CustomRole.USER)
                .emailAuthentication(false) //기본값
                .status(UserStatus.ACTIVE) //기본값
                .build());
    }

    @Override
    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.info("User already exists with email: " + email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }
    }

    @Override
    public void save(OwnerJoinRequest ownerReq) {
        ownerRepository.save(Owner.builder()
                .loginId(ownerReq.getLoginId())
                .password(passwordEncoder.encode(ownerReq.getPassword()))
                .name(ownerReq.getName())
                .startAt(ownerReq.getStartAt())
                .businessNum(ownerReq.getBusinessNum())
                .phone(ownerReq.getPhone())
                .role(CustomRole.OWNER)
                .status(OwnerStatus.ACTIVE)
                .build());
    }

    @Override
    public void existsByLoginId(String loginId) {
        if (ownerRepository.existsByLoginId(loginId)) {
            log.info("User already exists with loginId: " + loginId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }
    }

    @Override
    public User find(String name, String phone) {
        return userRepository.findByNameAndPhone(name, phone).orElseThrow( () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 이메일을 가진 사용자가 없습니다."
                ));

    }

    @Override
    public Owner find(String name, String businessNum, String phone) {
        return ownerRepository.findByNameAndBusinessNumAndPhone(name, businessNum, phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 아이디를 가진 사업자가 없습니다."));
    }

    @Override
    public void check(FindPwdRequest findPwdReq) {
        if(StringUtils.hasLength(findPwdReq.getEmail())) { //사용자인 경우
            if(!userRepository.existsByEmailAndNameAndPhone(findPwdReq.getEmail(), findPwdReq.getName(), findPwdReq.getPhone())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }
            return;
        }
        if(StringUtils.hasLength(findPwdReq.getLoginId())) { //사업자인 경우
            if(!ownerRepository.existsByLoginIdAndNameAndPhone(findPwdReq.getLoginId(), findPwdReq.getName(), findPwdReq.getPhone())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다.");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST); //아이디나 이메일을 입력하지 않은 경우
    }

    @Override
    @Transactional
    public void updatePwd(FindPwdRequest findPwdReq) { //비밀번호 암호화 필수
        if(StringUtils.hasLength(findPwdReq.getEmail())) { //사용자인 경우
            if(userRepository.updatePassword(findPwdReq.getEmail(), passwordEncoder.encode(findPwdReq.getPassword())) == 0){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }
            return;
        }
        if(StringUtils.hasLength(findPwdReq.getLoginId())) { //사업자인 경우
            if(ownerRepository.updatePassword(findPwdReq.getLoginId(), passwordEncoder.encode(findPwdReq.getPassword())) == 0){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다.");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST); //아이디나 이메일이 없는 경우
    }

    @Override
    public MemberInfo getMemberInfo(Long userId) {
        MemberInfo member = memberMapper.selectUser(userId);

        if(member == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        return member;
    }

    @Override
    @Transactional
    public void updateMemberInfo(Long userId, MemberUpdateInfo info, MultipartFile image) {
        String imgUrl = info.getImage();
        if(image != null && !image.isEmpty()){
            ImageUploadResponse response = objectStorageService.upload("profile", image);
            imgUrl = response.getFileUrl();
        }

        Integer result = memberMapper.updateUser(userId, info.getNickname(), info.getBirth(),
                info.getGender(), info.getPhone(), info.getCompanyName(),
                info.getCompanyAddress(), info.getEmailAuthentication(), imgUrl);

        if(result <= 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");

        memberMapper.deleteUserSpecialities(userId); //먼저 기존 것 삭제 후

        if(info.getSpecialities() != null && !info.getSpecialities().isEmpty()) {
            memberMapper.insertUserSpecialities(userId, info.getSpecialities()); //변경, 신규, 삭제로 인한 변경 추가
        }
    }

    @Override
    public OwnerInfo getOwnerInfo(Long ownerId) {
        Owner owner = ownerRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다"));

        return OwnerInfo.builder()
                .loginId(owner.getLoginId())
                .name(owner.getName())
                .phone(owner.getPhone())
                .businessNum(owner.getBusinessNum())
                .startAt(owner.getStartAt())
                .image(owner.getImage())
                .build();
    }

    @Override
    @Transactional
    public void updateOwnerInfo(Long ownerId , OwnerUpdateInfo ownerUpdateInfo, MultipartFile image) {
        String imgUrl = ownerUpdateInfo.getImage();

        if(image != null && !image.isEmpty()){
            ImageUploadResponse response = objectStorageService.upload("profile", image);
            imgUrl = response.getFileUrl();
        }
        int result = ownerRepository.updateOwner(ownerId, ownerUpdateInfo.getPhone(), imgUrl);

        if(result <= 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다.");
    }

    @Override
    public List<String> getEmails(Long ownerId) {
        List<String> emails = memberMapper.getPromotionEmails(ownerId);

        if(emails == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자를 찾을 수 없습니다.");

        return emails;
    }

    @Override
    public void save(StaffInfo staffInfo) {
        User user = userRepository.findByEmail(staffInfo.getEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if(staffRepository.existsByEmail(staffInfo.getEmail().trim())){ //만약 이미 등록한 임직원이면
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 임직원입니다.");
        }

        staffRepository.save(Staff.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .role(CustomRole.STAFF) //임직원 권한 주기
                .ownerId(staffInfo.getOwnerId())
                .build());
    }

    @Override
    @Transactional
    public void delete(StaffInfo staffInfo) {
        int result = staffRepository.deleteByStaffId(staffInfo.getStaffId(), staffInfo.getOwnerId());

        if(result <= 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "임직원을 찾을 수 없습니다.");
    }

    @Override
    public List<StaffInfo> getStaffs(Long ownerId) { //임직원 id일 수도 있음
        Long resolvedOwnerId = ownerId;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            if ("ROLE_STAFF".equals(userDetails.getRole())) {
                Staff staff = staffRepository.findByStaffId(userDetails.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found."));
                resolvedOwnerId = staff.getOwnerId();
            }
        }

        if (resolvedOwnerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "임직원에 사업자 id가 존재하지 않습니다.");
        }

        List<Staff> staffList = staffRepository.searchByOwnerId(resolvedOwnerId);

        return staffList.stream().map(
                staff -> StaffInfo.builder()
                        .email(staff.getEmail())
                        .name(staff.getName())
                        .staffId(staff.getStaffId())
                        .build()).collect(Collectors.toList());
    }

    @Override
    @Async("taskExecutor")
    @Transactional
    public void updateLastLoginAt(String userType, Long memberId) {
        if (!StringUtils.hasText(userType) || memberId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "최근접속일 update 중 에러 발생");
        }

        switch (userType) {
            case "USER" -> userRepository.updateLastLoginAt(memberId);
            case "OWNER" -> ownerRepository.updateLastLoginAt(memberId);
            case "STAFF" -> staffRepository.updateLastLoginAt(memberId);
            case "MANAGER" -> managerRepository.updateLastLoginAt(memberId);
            default -> {
            }
        }
    }

    @Override
    public List<OwnerInfo> getOwners() {
        List<Owner> owners = ownerRepository.findAll();

        return owners.stream().map(o -> OwnerInfo.builder()
                .name(o.getName()).businessNum(o.getBusinessNum())
                .startAt(o.getStartAt())
                .loginId(o.getLoginId())
                .status(o.getStatus())
                .build()
        ).collect(Collectors.toList());
    }
}
