package com.sena.springpoo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = (statusObj != null) ? Integer.parseInt(statusObj.toString()) : 500;
        
        logger.warn("Petición fallida capturada por CustomErrorController. Status: {}", status);

        if (status >= 500) {
            response.sendRedirect("/500.html");
        } else {
            response.sendRedirect("/error.html?status=" + status);
        }
    }
}
