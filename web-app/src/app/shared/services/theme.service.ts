import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private darkThemeKey = 'theme';
  isDarkTheme = false;

  constructor() {
    const savedTheme = localStorage.getItem(this.darkThemeKey);
    this.isDarkTheme = savedTheme === 'dark';
    this.applyTheme();
  }

  toggleTheme(isDark: boolean) {
    this.isDarkTheme = isDark;
    localStorage.setItem(this.darkThemeKey, isDark ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme() {
    const body = document.body;
    if (this.isDarkTheme) {
      body.classList.add('dark-theme');
    } else {
      body.classList.remove('dark-theme');
    }
  }
}
