package com.app.returns.global.exception;

import com.app.returns.domain.generalaccount.exception.GeneralAccountException;
import com.app.returns.domain.exception.RegistrableStockException;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.generalaccount.exception.GeneralAccountNotFoundException;
import com.app.returns.domain.member.exception.MemberException;
import com.app.returns.domain.member.exception.MemberNotFoundException;
import com.app.returns.global.response.ApiResponseDTO;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MemberException.class)
  public ResponseEntity<ApiResponseDTO<String>> handleException(MemberException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of(e.getMessage()));
  }
  @ExceptionHandler(MemberNotFoundException.class)
  public ResponseEntity<ApiResponseDTO<Void>>handleMemberNotFound(MemberNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.of(e.getMessage()));
  }

  @ExceptionHandler(RegistrableStockException.class)
  public ResponseEntity<ApiResponseDTO<Void>> handleRegistrableStockException(RegistrableStockException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of(e.getMessage()));
  }
  @ExceptionHandler(RegistrableStockNotFoundException.class)
  public ResponseEntity<ApiResponseDTO<Void>> handleRegistrableStockNotFoundException(RegistrableStockNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.of(e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponseDTO<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of(message));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponseDTO<Void>> handleBindException(BindException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of(message));
  }

  //GeneralAccount
  @ExceptionHandler(GeneralAccountException.class)
  public ResponseEntity<ApiResponseDTO<Void>> handleGeneralAccountException(GeneralAccountException e){
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDTO.of(e.getMessage()));
  }

  @ExceptionHandler(GeneralAccountNotFoundException.class)
  public ResponseEntity<ApiResponseDTO<Void>> handleGeneralAccountNotFoundException(GeneralAccountNotFoundException e){
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDTO.of(e.getMessage()));
  }
}
