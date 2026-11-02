export interface ComponentFormValues {
  name: string;
  description: string;
  type: string;
  brand: string;
  price: number;
  stock: number;
}

export function getComponentFormValues(formData: FormData): ComponentFormValues {

  return {
    name: (formData.get("name") as string) ?? "",
    description: (formData.get("description") as string) ?? "",
    type: (formData.get("type") as string) ?? "",
    brand: (formData.get("brand") as string) ?? "",
    price: Number((formData.get("price") as string) ?? 0.00),
    stock: Number((formData.get("stock") as string) ?? 0),
  };

}
