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
        var msg = err.error.localizedMessage || err.error.message;
        
        if (msg.includes("##tferrormsg##"))
        {
          var split = msg.split("##tferrormsg##");
          return split[2];
        }
      }
     
      return unspecified;
    }
}