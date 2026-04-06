package ru.yandex.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ProductNotFoundException.class,
            CartNotFoundException.class,
            NoDeliveryFoundException.class
    })
    public ResponseEntity<ErrorMessage> notFoundException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(exception.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class,
            NoSpecifiedProductInWarehouseException.class,
            SpecifiedProductAlreadyInWarehouseException.class,
            ProductInShoppingCartLowQuantityInWarehouseException.class
    })
    public ResponseEntity<ErrorMessage> illegalArgumentException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleAllExceptions(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage("Произошла непредвиденная ошибка"));
    }
}
