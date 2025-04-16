package ru.fishing.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ExceptionHandlerController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        logger.error("Ошибка валидации данных: {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxSizeException(MaxUploadSizeExceededException e, Model model) {
        logger.error("Превышен размер загружаемого файла: {}", e.getMessage());
        model.addAttribute("errorMessage", "Превышен максимальный размер загружаемого файла");
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        logger.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Произошла непредвиденная ошибка");
        return "error/500";
    }
}

