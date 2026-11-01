export interface UserFormValues {
  name: string;
  surname: string;
  username: string;
  address: string;
  email: string;
  password: string;
}

export function getUserFormValues(formData: FormData): UserFormValues {

  return {
    name: (formData.get("name") as string) ?? "",
    surname: (formData.get("surname") as string) ?? "",
    username: (formData.get("username") as string) ?? "",
    address: (formData.get("address") as string) ?? "",
    email: (formData.get("email") as string) ?? "",
    password: (formData.get("password") as string) ?? "",
  };

}