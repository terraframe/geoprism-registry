package net.geoprism.registry.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.runwaysdk.ProblemExceptionDTO;
import com.runwaysdk.business.ProblemDTOIF;
import com.runwaysdk.transport.conversion.json.ProblemExceptionDTOToJSON;
import com.runwaysdk.transport.conversion.json.RunwayExceptionDTOToJSON;

@ControllerAdvice
class JsonExceptionHandler
{
  public static class ObjectErrorProblem implements ProblemDTOIF
  {
    private ObjectError error;

    public ObjectErrorProblem(ObjectError error)
    {
      this.error = error;
    }

    @Override
    public String getMessage()
    {
      return this.error.getDefaultMessage();
    }

    @Override
    public String getDeveloperMessage()
    {
      return this.error.getDefaultMessage();
    }

    @Override
    public void setDeveloperMessage(String developerMessage)
    {
    }

  }

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception
  {
    // If the exception is annotated with @ResponseStatus rethrow it and let the
    // framework handle it
    if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
      throw e;

    Throwable t = (Throwable) e;
    if (t instanceof InvocationTargetException)
    {
      t = ( (InvocationTargetException) e ).getTargetException();
    }

    if (t instanceof BindException)
    {
      BindException ex = (BindException) t;
      List<ObjectErrorProblem> problems = ex.getAllErrors().stream().map(er -> new ObjectErrorProblem(er)).collect(Collectors.toList());

      t = new ProblemExceptionDTO("Unable to complete request", problems);
    }

    if (t instanceof ProblemExceptionDTO)
    {
      ProblemExceptionDTOToJSON converter = new ProblemExceptionDTOToJSON((ProblemExceptionDTO) t);
      JSONObject json = converter.populate();

      return new ResponseEntity<String>(json.toString(), HttpStatus.BAD_REQUEST);
    }

    RunwayExceptionDTOToJSON converter = new RunwayExceptionDTOToJSON(t.getClass().getName(), t.getMessage(), t.getLocalizedMessage());
    JSONObject json = converter.populate();

    return new ResponseEntity<String>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}