import { useActionState, useRef, useState } from "react";
import type { SubmitEvent } from "react";
import { getReviewFormValues } from "~/utils/review-form-utils";
import type { ReviewFormValues } from "~/utils/review-form-utils";
import { handleFormSubmit } from "~/utils/general-form-utils";

interface UseReviewFormProps {
  initialState: ReviewFormValues;
  submitAction: (
    values: ReviewFormValues
  ) => Promise<void>;
  defaultErrorMessage: string;
}

export function useReviewForm({ initialState, submitAction, defaultErrorMessage }: UseReviewFormProps) {
  const [wasValidated, setWasValidated] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const formRef = useRef<HTMLFormElement>(null);

  async function action(_prevState: ReviewFormValues, formData: FormData): Promise<ReviewFormValues> {
    const values = getReviewFormValues(formData);

    try {
      setErrorMessage("");
      await submitAction(values);
    } catch (error) {
      console.error(error);

      setErrorMessage(
        error instanceof Error
          ? error.message
          : defaultErrorMessage
      );
    }

    return values;
  }

  const [, formAction, isPending] = useActionState(action, initialState);

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    handleFormSubmit(event, formAction, setWasValidated);
  }

  return {
    wasValidated,
    errorMessage,
    formRef,
    isPending,
    handleSubmit,
  };
}
