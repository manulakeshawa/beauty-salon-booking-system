package com.example.beautysalonreview;

import jakarta.servlet.http.HttpSession;

public class SecurityUtils {
    public static boolean isManager(HttpSession session) {
        return "MANAGER".equals(session.getAttribute("staffRole"));
    }
}
