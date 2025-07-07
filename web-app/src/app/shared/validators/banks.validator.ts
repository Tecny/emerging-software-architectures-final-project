import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export function customAccountNumberLengthByBank(bankName: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const accountNumber = control.value;
    let expectedLength: number;

    switch (bankName) {
      case 'BCP':
        expectedLength = 14;
        break;
      case 'BBVA':
        expectedLength = 18;
        break;
      case 'Interbank':
        expectedLength = 13;
        break;
      default:
        expectedLength = 20;
    }

    const isValid = accountNumber?.length === expectedLength;
    return isValid ? null : { invalidAccountNumberLength: { expectedLength } };
  };
}


