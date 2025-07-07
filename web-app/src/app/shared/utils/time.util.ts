export class TimeUtil {
  static getHoursDifference(startTime: string, endTime: string): number {
    const start = new Date(`1970-01-01T${startTime}:00`);
    const end = new Date(`1970-01-01T${endTime}:00`);
    const differenceInMilliseconds = end.getTime() - start.getTime();
    return differenceInMilliseconds / (1000 * 60 * 60);
  }

  static formatDate(dateStr: string): string {
    const date = new Date(dateStr + 'T00:00:00');
    const formatter = new Intl.DateTimeFormat(this.getLocale(), {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit',
      timeZone: 'America/Lima'
    });

    const parts = formatter.formatToParts(date);
    const weekday = parts.find(p => p.type === 'weekday')?.value;
    const day = parts.find(p => p.type === 'day')?.value;
    const month = parts.find(p => p.type === 'month')?.value;

    return `${this.capitalize(weekday!)} ${day}/${month}`;
  }

  static capitalize(text: string): string {
    return text.charAt(0).toUpperCase() + text.slice(1);
  }

  static getLocale(): string {
    const lang = localStorage.getItem('language');
    if (lang === 'en') return 'en-US';
    return 'es-PE';
  }
}

export class PriceUtil {
  static calculatePrice(type: string, price: number, amount: number, hours: number): number {
    if (type === 'PERSONAL') {
      return (price * hours) / 2;
    } else {
      return amount * hours;
    }
  }
}
