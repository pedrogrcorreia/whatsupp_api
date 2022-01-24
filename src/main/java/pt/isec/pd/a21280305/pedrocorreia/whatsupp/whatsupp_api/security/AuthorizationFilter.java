package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@WebFilter(urlPatterns = {"/messages/*", "/friends/*", "/changeName", "/groups"})
public class AuthorizationFilter extends BasicAuthenticationFilter {

    public AuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader("Authorization");

        /* Not needed because it should use the token */
//        String justToken = token.substring(6);
//        byte[] tokenBytes = Base64.getDecoder().decode(justToken);
//        String decodedString = new String(tokenBytes);
//
//        String[] split = decodedString.split(":");
//        String username = split[0];
//        String password = split[1];

        Connection con;
        Statement stmt = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://192.168.1.73:3306/whatsupp_db", "tester", "password-123");;

            String query = "SELECT auth_time FROM users WHERE token = '" + token + "'";

            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            Timestamp time = rs.getTimestamp("auth_time");
            Timestamp curTime = Timestamp.from(Instant.now());
            long diff = curTime.getTime() - time.getTime();
            int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(diff);
            logger.debug("Token time: " + seconds + " seconds.");
            if(seconds > 120){
                logger.error("Expired token");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                throw new Exception("");
                return;
            } else{
                response.setStatus(HttpStatus.OK.value());
            }
        } catch (SQLException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            ((HttpServletResponse) response).sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
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
}
