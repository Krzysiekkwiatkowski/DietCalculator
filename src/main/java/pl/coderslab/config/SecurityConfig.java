package pl.coderslab.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.coderslab.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http
            .authorizeRequests()
                .antMatchers("/diet/home").permitAll()
                .antMatchers("/diet/user/login").permitAll()
                .antMatchers("/diet/user/register").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/diet/user/edit").hasRole("USER")
                .antMatchers("/diet/user/correct").hasRole("USER")
                .antMatchers("/diet/user/password").hasRole("USER")
                .antMatchers("/diet/user/option").hasRole("USER")
                .antMatchers("/diet/user/delete/**").hasRole("USER")
                .antMatchers("/diet/product/**").hasRole("USER")
                .antMatchers("/diet/category/**").hasRole("USER")
                .antMatchers("/diet/meal/**").hasRole("USER")
                .antMatchers("/diet/training/**").hasRole("USER")
                .antMatchers("/diet/daily/**").hasRole("USER")
            .and()
                .csrf().disable()
            .formLogin()
                .loginPage("/diet/user/login")
                .loginProcessingUrl("/diet/user/login")
                .defaultSuccessUrl("/diet/home")
                .successHandler(customAuthenticationSuccessHandler)
                .usernameParameter("email")
                .passwordParameter("password")
            .and()
                .logout()
                .logoutUrl("/diet/user/logout")
                .logoutSuccessUrl("/diet/user/login")
                .permitAll();
    }

    @Bean
    protected PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
}
