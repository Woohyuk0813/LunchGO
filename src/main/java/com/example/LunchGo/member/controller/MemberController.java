package com.example.LunchGo.member.controller;

import com.example.LunchGo.member.dto.*;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/info/user/{userId}")
    public ResponseEntity<?> userDetail(@PathVariable Long userId) {
        MemberInfo member = memberService.getMemberInfo(userId); //만약 예외 발생시 404 발생

        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @PutMapping(value = "/info/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestPart(value = "info") MemberUpdateInfo memberUpdateInfo,
                                        @RequestPart(value = "image", required = false) MultipartFile image) {
        memberService.updateMemberInfo(userId, memberUpdateInfo, image); //예외 발생시 404 발생

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/info/business/{ownerId}")
    public ResponseEntity<?> ownerDetail(@PathVariable Long ownerId) {
        OwnerInfo owner = memberService.getOwnerInfo(ownerId);

        return new ResponseEntity<>(owner, HttpStatus.OK);
    }

    @PutMapping(value = "/info/business/{ownerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateOwner(@PathVariable Long ownerId, @RequestPart(value = "info") OwnerUpdateInfo ownerUpdateInfo,
                                         @RequestPart(value = "image", required = false) MultipartFile image) {
        if(!StringUtils.hasLength(ownerUpdateInfo.getPhone()) && !StringUtils.hasLength(ownerUpdateInfo.getImage())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //아무 변경없이 수정하기 남발 금지
        }
        memberService.updateOwnerInfo(ownerId, ownerUpdateInfo, image);
        return new ResponseEntity<>(HttpStatus.OK);
    }

        @PostMapping("/business/staff")
        public ResponseEntity<?> addStaff(@RequestBody StaffInfo staffInfo) {
            if(!StringUtils.hasLength(staffInfo.getEmail())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            memberService.save(staffInfo); //해당 email을 가진 user 존재하지 않으면 404, 이미 등록한 email이면 409
            return new ResponseEntity<>(HttpStatus.OK);
        }

    @DeleteMapping("/business/staff")
    public ResponseEntity<?> deleteStaff(@RequestBody StaffInfo staffInfo) {
        if(staffInfo.getStaffId() == null || staffInfo.getOwnerId() == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        //해당 점주가 관리하는 직원에 대해서만 삭제 가능하도록
        memberService.delete(staffInfo); //delete 오류 발생시 404
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/business/staff/{ownerId}")
    public ResponseEntity<?> getStaffs (@PathVariable Long ownerId) {
        List<StaffInfo> staffs = memberService.getStaffs(ownerId);

        return new ResponseEntity<>(staffs, HttpStatus.OK);
    }
    
    @GetMapping("/admin/list/owner")
    public ResponseEntity<?> getOwners() {
        List<OwnerInfo> owners = memberService.getOwners(); //전체 사업자 조회
        
        return new ResponseEntity<>(owners, HttpStatus.OK);
    }
}
