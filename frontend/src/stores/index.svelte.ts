import type { User, PackageUsage, PackageType } from '../types';

// Auth store using Svelte 5 runes pattern
function createAuthStore() {
  let user = $state<User | null>(null);
  let isLoading = $state(true);
  let isAuthenticated = $derived(user !== null);
  let isAdmin = $derived(user?.role === 'ADMIN');

  // Initialize from localStorage
  if (typeof window !== 'undefined') {
    const stored = localStorage.getItem('user');
    if (stored) {
      try {
        user = JSON.parse(stored);
      } catch {
        localStorage.removeItem('user');
      }
    }
    isLoading = false;
  }

  return {
    get user() { return user; },
    get isLoading() { return isLoading; },
    get isAuthenticated() { return isAuthenticated; },
    get isAdmin() { return isAdmin; },
    
    setUser(newUser: User | null) {
      user = newUser;
      if (newUser) {
        localStorage.setItem('user', JSON.stringify(newUser));
      } else {
        localStorage.removeItem('user');
      }
    },
    
    setLoading(loading: boolean) {
      isLoading = loading;
    },
    
    logout() {
      user = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
  };
}

export const authStore = createAuthStore();

// Package usage store
function createUsageStore() {
  let usage = $state<PackageUsage>({
    uploadedToday: 0,
    totalStoredPhotos: 0,
    totalStorageMB: 0
  });
  
  return {
    get usage() { return usage; },
    
    setUsage(newUsage: PackageUsage) {
      usage = newUsage;
    },
    
    incrementUploads(sizeMB: number) {
      usage = {
        ...usage,
        uploadedToday: usage.uploadedToday + 1,
        totalStoredPhotos: usage.totalStoredPhotos + 1,
        totalStorageMB: usage.totalStorageMB + sizeMB
      };
    }
  };
}

export const usageStore = createUsageStore();

// Toast notifications store
export type ToastType = 'success' | 'error' | 'info' | 'warning';

interface Toast {
  id: string;
  message: string;
  type: ToastType;
}

function createToastStore() {
  let toasts = $state<Toast[]>([]);
  
  return {
    get toasts() { return toasts; },
    
    add(message: string, type: ToastType = 'info') {
      const id = crypto.randomUUID();
      toasts = [...toasts, { id, message, type }];
      
      setTimeout(() => {
        toasts = toasts.filter(t => t.id !== id);
      }, 5000);
      
      return id;
    },
    
    remove(id: string) {
      toasts = toasts.filter(t => t.id !== id);
    },
    
    success(message: string) {
      return this.add(message, 'success');
    },
    
    error(message: string) {
      return this.add(message, 'error');
    },
    
    info(message: string) {
      return this.add(message, 'info');
    },
    
    warning(message: string) {
      return this.add(message, 'warning');
    }
  };
}

export const toastStore = createToastStore();

// Theme store
function createThemeStore() {
  let isDark = $state(false);
  
  if (typeof window !== 'undefined') {
    const stored = localStorage.getItem('theme');
    isDark = stored === 'dark' || (!stored && window.matchMedia('(prefers-color-scheme: dark)').matches);
    updateDocumentTheme();
  }
  
  function updateDocumentTheme() {
    if (typeof document !== 'undefined') {
      if (isDark) {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }
    }
  }
  
  return {
    get isDark() { return isDark; },
    
    toggle() {
      isDark = !isDark;
      localStorage.setItem('theme', isDark ? 'dark' : 'light');
      updateDocumentTheme();
    },
    
    setDark(dark: boolean) {
      isDark = dark;
      localStorage.setItem('theme', dark ? 'dark' : 'light');
      updateDocumentTheme();
    }
  };
}

export const themeStore = createThemeStore();
