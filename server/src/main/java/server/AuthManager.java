package server;

import common.auth.Credentials;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class AuthManager {
    private final DatabaseManager dbManager;

    public AuthManager(DatabaseManager dbManager){
        this.dbManager = dbManager;
    }
    public static String hashMD2(String inp){
        try{
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] digiest = md.digest(inp.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte bytedig: digiest){
                sb.append(String.format("%02x", bytedig));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("MD2 алгоритма не нашел", e);
        }
    }
    public boolean authenticate(Credentials credentials){
        if (credentials == null) return false;
        String hash = hashMD2(credentials.getPassword());
        Optional<Integer> userId = dbManager.authenticate(credentials.getLogin(), hash);
        return userId.isPresent();
    }

    public boolean userExists(String login) {
        return dbManager.userExists(login);
    }

    public int getUserId(Credentials credentials){
        if (credentials == null) return -1;
        String hash = hashMD2(credentials.getPassword());
        Optional<Integer> userId = dbManager.authenticate(credentials.getLogin(), hash);
        return userId.orElse(-1);
    }
    public boolean register(Credentials credentials){
        if (credentials == null) return false;
        String hash = hashMD2(credentials.getPassword());
        return dbManager.registers(credentials.getLogin(), hash);
    }
}
