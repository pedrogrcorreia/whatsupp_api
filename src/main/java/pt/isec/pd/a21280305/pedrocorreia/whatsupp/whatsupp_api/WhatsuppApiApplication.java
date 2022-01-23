package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.security.AuthenticationFilter;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.security.AuthorizationFilter;

import java.util.Collections;

@ServletComponentScan
@SpringBootApplication
public class WhatsuppApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsuppApiApplication.class, args);
    }

    @EnableWebSecurity
    @Configuration


//    @Order(1)
    class WebSecurityConfig extends WebSecurityConfigurerAdapter
    {
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception
        {


                    //HttpSecurity allows configuring web based security for specific http requests
            http.csrf().disable()
//                    .addFilterAfter(new AuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
//                    .addFilterBefore(new AuthorizationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/login").permitAll()
                    .antMatchers(HttpMethod.GET, "/messages").permitAll()
                    .anyRequest().authenticated().and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//            http.csrf().disable()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                    .and()
//                    .authorizeRequests().anyRequest().authenticated()
////                    .and()
////                    .antMatcher("/messages")
////                    .addFilterBefore(new AuthorizationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
////                    .authorizeRequests().anyRequest().authenticated()
////                    .and()
////                    .antMatcher("/login")
////                    .addFilterBefore(new AuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);
        }
    }

//    @Configuration
////    @Order(2)
//    class WebSecurityConfig2 extends WebSecurityConfigurerAdapter
//    {
//        @Override
//        protected void configure(HttpSecurity http) throws Exception
//        {
//            //HttpSecurity allows configuring web based security for specific http requests
//            http.csrf().disable()
//                    .addFilterAfter(new AuthorizationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
//                    .authorizeRequests()
//                    .antMatchers(HttpMethod.GET, "/messages").authenticated().and()
////                    .anyRequest().authenticated().and()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        }
//    }
}
