package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

//@WebFilter(urlPatterns = {"/login", "/messages/*", "/friends"})
public class AuthenticationFilter extends BasicAuthenticationFilter {

    private AntPathRequestMatcher requestMatcher;

    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        requestMatcher = new AntPathRequestMatcher("/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {




        Connection con;
        Statement stmt = null;
        try {

            String token = request.getHeader("Authorization");
            String justToken = token.substring(6);
            byte[] tokenBytes = Base64.getDecoder().decode(justToken);
            String decodedString = new String(tokenBytes);

            String[] split = decodedString.split(":");
            String username = split[0];
            String password = split[1];

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
                        response.getWriter().println("Login Success");
                        logger.info("User " + username + " logged successfully.");
                    }
                    UsernamePasswordAuthenticationToken uPAT =
                            new UsernamePasswordAuthenticationToken(token, null, null);
                    SecurityContextHolder.getContext().setAuthentication(uPAT);
                }else{
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    if(requestMatcher.matches(request)) {
                        response.getWriter().println("Incorrect password");
                    }
                }
                rs.close();
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                if(requestMatcher.matches(request)) {
                    response.getWriter().println("Username doesn't exist");
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("[login] Error querying the database:\r\n\t" + e);
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            if(requestMatcher.matches(request)) {
                response.getWriter().println("No credentials provided!");
            }
        }
        finally {
            try {
                if(stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                System.out.println("SQLException problem:\r\n\t" + e);
            }
        }

        chain.doFilter(request, response);
    }
}
