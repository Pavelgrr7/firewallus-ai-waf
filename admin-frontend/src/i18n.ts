import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import HttpApi from 'i18next-http-backend';

// Retrieve cached language (stored as EN/RU, convert to lowercase for i18next)
const savedLanguage = (localStorage.getItem('lng') || 'EN').toLowerCase();

i18n
  .use(HttpApi)
  .use(initReactI18next)
  .init({
    lng: savedLanguage === 'ru' ? 'ru' : 'en',
    fallbackLng: 'en',
    debug: false,
    interpolation: {
      escapeValue: false, // React already protects against XSS
    },
    backend: {
      loadPath: '/locales/{{lng}}/translation.json',
    },
  });

export default i18n;
