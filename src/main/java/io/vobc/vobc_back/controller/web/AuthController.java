package io.vobc.vobc_back.controller.web;

import io.vobc.vobc_back.dto.JoinRequest;
import io.vobc.vobc_back.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("joinRequest", new JoinRequest());
        return "auth/join";
    }

    @PostMapping("/join")
    public String join(@Valid JoinRequest request,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/join";
        }

        try {
            memberService.join(request);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            return "auth/join";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "회원가입이 완료되었습니다. 로그인 해주세요."
        );
        return "redirect:/login";
    }
}
