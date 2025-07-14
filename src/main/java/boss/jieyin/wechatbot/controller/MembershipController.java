package boss.jieyin.wechatbot.controller;

import boss.jieyin.wechatbot.pojo.ResponseEntity;
import boss.jieyin.wechatbot.pojo.member.MemberReq;
import boss.jieyin.wechatbot.service.MembershipService;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping("/modify/member")
    public ResponseEntity<String> modifyMember(@RequestBody MemberReq memberReq) {
        boolean success = membershipService.modifyMember(memberReq);
        return success ? ResponseEntity.ok("已成功升级为 VIP")
                : ResponseEntity.error("升级失败");
    }
}
