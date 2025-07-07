import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function customEmailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;

    if (!value) {
      return null;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const valid = emailRegex.test(value);
    return valid ? null : { invalidEmail: true };
  };
}


export function timeRangeValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const openTime = control.get('openTime')?.value;
    const closeTime = control.get('closeTime')?.value;

    if (!openTime || !closeTime) return null;

    const openHour = parseInt(openTime.split(':')[0], 10);
    const closeHour = parseInt(closeTime.split(':')[0], 10);

    if (closeHour <= openHour) {
      return { closeBeforeOpen: true };
    }

    if (closeHour - openHour < 5) {
      return { lessThanFiveHours: true };
    }

    return null;
  };
}

