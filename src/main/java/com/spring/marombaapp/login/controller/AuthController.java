package com.spring.marombaapp.login.controller;

import com.spring.marombaapp.login.model.Perfil;
import com.spring.marombaapp.login.model.PerfilEnum;
import com.spring.marombaapp.login.model.Usuario;
import com.spring.marombaapp.login.payload.request.LoginRequest;
import com.spring.marombaapp.login.payload.request.SignUpRequest;
import com.spring.marombaapp.login.payload.response.MessageResponse;
import com.spring.marombaapp.login.payload.response.UserInfoResponse;
import com.spring.marombaapp.login.repository.PerfilRepository;
import com.spring.marombaapp.login.repository.UsuarioRepository;
import com.spring.marombaapp.login.security.jwt.JwtUtils;
import com.spring.marombaapp.login.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PerfilRepository perfilRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie responseCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item->item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                                            userDetails.getUsername(),
                                            userDetails.getEmail(),
                                            roles));

    }
    @PostMapping("/signUp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        if(usuarioRepository.existePorNomeUsuario(signUpRequest.getUsername())){
            return ResponseEntity.badRequest().body(new MessageResponse("Erro: Nome já utilizado!"));
        }
        if(usuarioRepository.existePorEmail(signUpRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Erro: Email em uso!"));
        }

        //Criar novo usuario
        Usuario usuario = new Usuario(signUpRequest.getUsername(),
                                        signUpRequest.getEmail(),
                                        passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Perfil> roles = new HashSet<>();

        if (strRoles == null) {
            Perfil userRole = perfilRepository.buscaPorNome(PerfilEnum.PERFIL_USUARIO)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Perfil adminRole = perfilRepository.buscaPorNome(PerfilEnum.PERFIL_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Perfil modRole = perfilRepository.buscaPorNome(PerfilEnum.PERFIL_MODERADOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Perfil userRole = perfilRepository.buscaPorNome(PerfilEnum.PERFIL_USUARIO)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        usuario.setPerfis(roles);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
