package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@WebFilter(urlPatterns = "*")
public class AuthenticationFilter extends BasicAuthenticationFilter {

    private AntPathRequestMatcher requestMatcher;

    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        requestMatcher = new AntPathRequestMatcher("/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

//        if(!this.requestMatcher.matches(request)){
//            response.setStatus(HttpStatus.OK.value());
//            chain.doFilter(request, response);
//            return;
//        }

        String token = request.getHeader("Authorization");
        String justToken = token.substring(6);
        byte[] tokenBytes = Base64.getDecoder().decode(justToken);
        String decodedString = new String(tokenBytes);

        String[] split = decodedString.split(":");
        String username = split[0];
        String password = split[1];

        System.out.println(username);
        System.out.println("\n" + password);
        Connection con;
        Statement stmt = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://192.168.1.73:3306/whatsupp_db", "tester", "password-123");;

            String query = "SELECT COUNT(*) AS nusers, user_id, username, password, name " +
                    "FROM users where username = '" + username + "'";

            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            int nUsers = rs.getInt("nusers");
            if (nUsers > 1) {
                rs.close();
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            } else if (nUsers == 1) {
                if (rs.getString("username").equals(username) && rs.getString("password").equals(password)) {
                    if(requestMatcher.matches(request)) {
                        response.setHeader("Authorization", token);
                        stmt = con.createStatement();
                        String queryInsert = ("UPDATE users SET token = '" + token + "', auth_time = current_timestamp() WHERE (username = '" + username + "')");
                        stmt.executeUpdate(queryInsert);

                        UsernamePasswordAuthenticationToken uPAT =
                                new UsernamePasswordAuthenticationToken(token, null, null);
                        SecurityContextHolder.getContext().setAuthentication(uPAT);
                    }
                }
                rs.close();
            } else {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("[login] Error querying the database:\r\n\t" + e);
        } finally {
            try {
                assert stmt != null;
                stmt.close();
            } catch (SQLException e) {
                System.out.println("SQLException problem:\r\n\t" + e);
            }
        }

        chain.doFilter(request, response);
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> corsFilterRegistration() {
        FilterRegistrationBean<AuthenticationFilter> filterRegistrationBean =
                new FilterRegistrationBean<>(new AuthenticationFilter(getAuthenticationManager()));
        filterRegistrationBean.setUrlPatterns(Collections.singleton("/login"));
        return filterRegistrationBean;
    }
}
