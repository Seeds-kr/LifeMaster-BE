package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;
/*
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/googleUser")
public class GoogleUserController {

    private final GoogleLoginService googleLoginService;

    public GoogleUserController(GoogleLoginService googleLoginService) {
        this.googleLoginService = googleLoginService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, RedirectAttributes redirectAttributes) {
        Authentication authentication = (Authentication) principal;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");

        // 구글 인증된 사용자 정보 가져오기
        googleLoginService.loadUser((OAuth2UserRequest) oAuth2User);

        // 사용자 정보로 대시보드로 리디렉션
        redirectAttributes.addFlashAttribute("user", oAuth2User);
        return "redirect:/dashboard";  // 대시보드 페이지로 리디렉션
    }
}

 */


