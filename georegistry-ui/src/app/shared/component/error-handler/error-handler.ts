///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///


import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { ErrorModalComponent } from "@shared/component";

export class ErrorHandler {

    static getMessageFromError(err: any): string {
        let unspecified = "An unspecified error has occurred.  Please try your operation again.  If the problem continues, alert your technical support staff.";

        if (err == null) {
            return unspecified;
        } else {
            console.log("An error has occurred: ", err);
        }

        let msg = null;

        if (err.error != null && (typeof err.error === "object")) {
            msg = err.error.localizedMessage || err.error.message;
        }

        if (msg == null) {
            msg = err.message || err.msg || err.localizedMessage;
        }

        if (msg != null && msg.includes("##tferrormsg##")) {
            let split = msg.split("##tferrormsg##");
            return split[2];
        }

        if (msg == null) {
            msg = unspecified;
        }

        return msg;
    }

    static showErrorAsDialog(err: any, modalService: BsModalService): BsModalRef {
        if (err instanceof HttpErrorResponse && err.status === 401) {
            return null;
        }

        let bsModalRef = modalService.show(ErrorModalComponent, { backdrop: true, class: "error-white-space-pre" });

        bsModalRef.content.message = ErrorHandler.getMessageFromError(err);

        return bsModalRef;
    }

}
