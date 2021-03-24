
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { ErrorModalComponent } from '@shared/component';

export class ErrorHandler {
    static getMessageFromError(err: any): string {
    
      var unspecified = "An unspecified error has occurred.  Please try your operation again.  If the problem continues, alert your technical support staff.";
    
      if (err == null)
      {
        return unspecified;
      }
      else
      {
        console.log("An error has occurred: ", err);
      }
      
      let msg = null;
      
      if (err.error != null && (typeof err.error === 'object'))
      {
        msg = err.error.localizedMessage || err.error.message;
      }
      
      if (msg == null)
      {
        msg = err.message || err.msg || err.localizedMessage;
      }
      
      if (msg != null && msg.includes("##tferrormsg##"))
      {
        var split = msg.split("##tferrormsg##");
        return split[2];
      }
      
      if (msg == null)
      {
        msg = unspecified;
      }
      
      return msg;
    }
    
    static showErrorAsDialog(err: any, modalService: BsModalService): BsModalRef {
      
      if (err instanceof HttpErrorResponse && err.status == 401)
      {
        return null;
      }
      
      let bsModalRef = modalService.show(ErrorModalComponent, { backdrop: true, class:"error-white-space-pre" });
      
      bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
      
      return bsModalRef;
      
    }
}
