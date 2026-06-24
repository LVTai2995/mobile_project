package com.example.weather_trip.data.repository;

import android.content.Context;

import com.example.weather_trip.data.local.dao.UserDao;
import com.example.weather_trip.data.local.dao.SessionDao;
import com.example.weather_trip.domain.model.User;

import org.mindrot.jbcrypt.BCrypt;

public class UserRepository {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    private final UserDao userDao;
    private final SessionDao sessionDao;

    public UserRepository(Context context) {
        this.userDao = new UserDao(context);
        this.sessionDao = new SessionDao(context);
    }

    public User register(String email, String password, String displayName) {
        if (userDao.findByEmail(email) != null) {
            return null;
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashPassword(password));
        user.setDisplayName(displayName != null && !displayName.isEmpty() ? displayName : email.split("@")[0]);
        user.setOauthProvider("email");

        long id = userDao.insert(user);
        if (id > 0) {
            sessionDao.createSession(user);
            return user;
        }
        return null;
    }

    public User login(String email, String password) {
        User user = userDao.authenticate(email, password);
        if (user != null) {
            sessionDao.createSession(user);
        }
        return user;
    }

    public User loginWithGoogle(String oauthId, String email, String name) {
        User user = userDao.findByOauthId(oauthId, "google");

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setOauthProvider("google");
            user.setOauthId(oauthId);

            String baseName = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
            user.setDisplayName(baseName);

            long id = userDao.insert(user);
            if (id <= 0) return null;
        }

        sessionDao.createSession(user);
        return user;
    }

    public User getCurrentUser() {
        long userId = sessionDao.getUserId();
        if (userId <= 0) return null;
        return userDao.findById(userId);
    }

    public long getCurrentUserId() {
        return sessionDao.getUserId();
    }

    public boolean isLoggedIn() {
        return sessionDao.isLoggedIn();
    }

    public void logout() {
        sessionDao.clearSession();
    }

    public String getCurrentUserName() {
        return sessionDao.getUserName();
    }

    public String getCurrentUserEmail() {
        return sessionDao.getUserEmail();
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    public boolean verifyPassword(String password, String storedHash) {
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
