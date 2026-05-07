export interface User {
  id: string;
  activationNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: number;
  dateOfBirth: string;
  residentialAddress: string;
  nationality: string;
  roles: string[];
  userStatus: any;
}
