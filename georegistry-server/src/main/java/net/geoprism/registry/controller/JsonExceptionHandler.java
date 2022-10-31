package net.geoprism.registry.controller;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.runwaysdk.ProblemExceptionDTO;
import com.runwaysdk.transport.conversion.json.ProblemExceptionDTOToJSON;
import com.runwaysdk.transport.conversion.json.RunwayExceptionDTOToJSON;

@ControllerAdvice
class JsonExceptionHandler {
  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
    // If the exception is annotated with @ResponseStatus rethrow it and let the framework handle it
    if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
      throw e;

    Throwable t = (Throwable) e;
    if (t instanceof InvocationTargetException)
    {
      t = ( (InvocationTargetException) e ).getTargetException();
    }

    JSONObject json;
    if (e instanceof ProblemExceptionDTO)
    {
      ProblemExceptionDTOToJSON converter = new ProblemExceptionDTOToJSON((ProblemExceptionDTO) e);
      json = converter.populate();
    }
    else
    {
      RunwayExceptionDTOToJSON converter = new RunwayExceptionDTOToJSON(e.getClass().getName(), e.getMessage(), e.getLocalizedMessage());
      json = converter.populate();
    }
    
    return new ResponseEntity<String>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}