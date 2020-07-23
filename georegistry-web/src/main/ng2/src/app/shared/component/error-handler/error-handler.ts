export class ErrorHandler {
    static getMessageFromError(err: any): string {
    
      var unspecified = "An unspecified error has occurred. Please contact your technical support team.";
    
      if (err == null)
      {
        return unspecified;
      }
      else
      {
        console.log("An error has occurred: ", err);
      }
    
      if (err.error != null)
      {
        return err.error.localizedMessage || err.error.message;
      }
     
      return unspecified;
    }
}