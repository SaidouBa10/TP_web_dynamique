package com.example.tp1.security;

import com.example.tp1.security.User;
import com.example.tp1.security.UserRepository;
import com.example.tp1.security.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Register
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return "Utilisateur créé !";
    }

    // Login
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        User found = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

        if (!passwordEncoder.matches(user.getPassword(), found.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect !");
        }

        String token = jwtUtil.generateToken(found.getUsername());
        return Map.of("token", token);
    }
    // Afficher page login
    @GetMapping("/login-form")
    public String loginForm() {
        return "login";
    }

    // Traiter le login via formulaire
    @PostMapping("/login-form")
    public String loginFormPost(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        try {
            User found = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

            if (!passwordEncoder.matches(password, found.getPassword())) {
                model.addAttribute("error", "Mot de passe incorrect !");
                return "login";
            }

            String token = jwtUtil.generateToken(found.getUsername());
            session.setAttribute("token", token);
            session.setAttribute("username", username);
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Utilisateur non trouvé !");
            return "login";
        }
    }

    // Afficher page register
    @GetMapping("/register-form")
    public String registerForm() {
        return "register";
    }

    // Traiter le register via formulaire
    @PostMapping("/register-form")
    public String registerFormPost(@RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");
            userRepository.save(user);
            model.addAttribute("success", "Compte créé ! Connectez-vous.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création !");
            return "register";
        }
    }

    // Dashboard après login
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/auth/login-form";
        model.addAttribute("username", username);
        return "dashboard";
    }

    // Logout
    @GetMapping("/logout-form")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login-form";
    }
}