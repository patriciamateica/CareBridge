export interface User {
  id: string;
  activationNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  dateOfBirth: string;
  residentialAddress: string;
  nationality: string;
  roles: string[];
  userStatus: 'ACTIVE' | 'INACTIVE' | string;
}
