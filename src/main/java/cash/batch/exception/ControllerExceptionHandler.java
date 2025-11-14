package cash.batch.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerExceptionHandler {

//  use this logger to log all the technical exception you write a custom handler for them.
//  logger.log("error",exceptionObject); -> will log all of the stackTrace
    Logger logger = LoggerFactory.getLogger("ControllerExceptionHandlerLogger");


    /*
    BindException is thrown when a constraint of the DTO gets violated in the incoming HTTP request while using @Valid
   */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseBody> handleBindException(BindException bindException, WebRequest webRequest) {

        List<String> errorMessages=bindException.getBindingResult()
                .getFieldErrors().stream()
                .map(error->error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toCollection(ArrayList::new));

        ErrorResponseBody responseBody=new ErrorResponseBody(
                new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())
                ,MDC.get("transactionID")
                ,ErrorCodes.INPUT_VALIDATION_FAILED.getCode()
                ,ErrorCodes.INPUT_VALIDATION_FAILED.getDescription()

                ,errorMessages
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }


    @ExceptionHandler(NoSuchBeanDefinitionException.class)
    ResponseEntity<ErrorResponseBody> noSuchBeanDefinitionException(Exception e) {
        ErrorResponseBody responseBody = ErrorResponseBody
                .builder()
                .timeStamp(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()))
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponseBody> exception(Exception e) {
        ErrorResponseBody responseBody = ErrorResponseBody
                .builder()
                .timeStamp(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()))
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

//    @ExceptionHandler(JobExecutionAlreadyRunningException.class)
//    ResponseEntity<ErrorResponseBody> handleJobExecutionAlreadyRunningException(JobExecutionAlreadyRunningException e) {
//        ErrorResponseBody errorResponseBody = ErrorResponseBody
//                .builder()
//                .timeStamp(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()))
//                .message(e.getMessage())
//                .transactionID(MDC.get("transactionID"))
//                .status(ErrorCodes)
//    }

}

