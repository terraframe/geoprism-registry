/* eslint-disable padded-blocks */
export class Profile {
    oid: string;
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    email: string;
    changePassword?: boolean;
    phoneNumber?: string;
    altFirstName?: string;
    altLastName?: string;
    altPhoneNumber?: string;
    position?: string;
    department?: string;
    externalSystemOid?: string;
}