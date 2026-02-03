import type { 
  User, 
  Photo, 
  PhotoFilter, 
  PhotoUpload, 
  PackageUsage,
  PackageType,
  LoginCredentials,
  RegisterData,
  ApiResponse,
  PaginatedResponse,
  ActionLog,
  UserStats,
  DownloadOptions
} from '../types';
import { authStore, usageStore, toastStore } from '../stores/index.svelte';

const API_BASE = '/api';

// Helper function for API calls
async function fetchApi<T>(
  endpoint: string, 
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  try {
    const token = localStorage.getItem('token');
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...options.headers
    };

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'An error occurred' }));
      return { success: false, error: error.message || `HTTP error ${response.status}` };
    }

    const data = await response.json();
    return { success: true, data };
  } catch (error) {
    console.error('API Error:', error);
    return { success: false, error: 'Network error. Please try again.' };
  }
}

// For demo purposes, we'll use mock data since backend may not be ready
// This allows the frontend to be demonstrated independently

// Mock data
let mockUsers: User[] = [
  {
    id: '1',
    email: 'admin@instapound.com',
    username: 'admin',
    role: 'ADMIN',
    currentPackage: 'GOLD',
    createdAt: new Date().toISOString(),
    authProvider: 'LOCAL'
  },
  {
    id: '2',
    email: 'user@example.com',
    username: 'john_doe',
    role: 'REGISTERED',
    currentPackage: 'PRO',
    createdAt: new Date().toISOString(),
    authProvider: 'LOCAL'
  }
];

let mockPhotos: Photo[] = Array.from({ length: 25 }, (_, i) => ({
  id: `photo-${i + 1}`,
  filename: `photo_${i + 1}.jpg`,
  originalFilename: `IMG_${1000 + i}.jpg`,
  description: `Beautiful photo #${i + 1} - A stunning capture of nature's beauty`,
  hashtags: ['nature', 'photography', i % 2 === 0 ? 'landscape' : 'portrait', i % 3 === 0 ? 'sunset' : 'wildlife'],
  authorId: i % 3 === 0 ? '1' : '2',
  authorUsername: i % 3 === 0 ? 'admin' : 'john_doe',
  uploadedAt: new Date(Date.now() - i * 3600000).toISOString(),
  sizeBytes: Math.floor(Math.random() * 5000000) + 500000,
  width: 1920,
  height: 1080,
  format: 'JPG',
  thumbnailUrl: `https://picsum.photos/seed/${i + 1}/300/200`,
  fullUrl: `https://picsum.photos/seed/${i + 1}/1920/1080`
}));

let mockActionLogs: ActionLog[] = Array.from({ length: 50 }, (_, i) => ({
  id: `log-${i + 1}`,
  userId: i % 3 === 0 ? '1' : '2',
  username: i % 3 === 0 ? 'admin' : 'john_doe',
  action: ['PHOTO_UPLOAD', 'PHOTO_VIEW', 'PHOTO_DOWNLOAD', 'LOGIN', 'PROFILE_UPDATE'][i % 5],
  details: `Action details for log entry #${i + 1}`,
  timestamp: new Date(Date.now() - i * 1800000).toISOString()
}));

// Auth Service
export const authService = {
  async login(credentials: LoginCredentials): Promise<ApiResponse<User>> {
    // Mock login
    await new Promise(resolve => setTimeout(resolve, 500));
    
    if (credentials.email === 'admin@instapound.com' && credentials.password === 'admin') {
      const user = mockUsers[0];
      localStorage.setItem('token', 'mock-token-admin');
      authStore.setUser(user);
      toastStore.success('Welcome back, Admin!');
      return { success: true, data: user };
    }
    
    if (credentials.email === 'user@example.com' && credentials.password === 'user') {
      const user = mockUsers[1];
      localStorage.setItem('token', 'mock-token-user');
      authStore.setUser(user);
      toastStore.success('Welcome back!');
      return { success: true, data: user };
    }
    
    return { success: false, error: 'Invalid email or password' };
  },

  async register(data: RegisterData): Promise<ApiResponse<User>> {
    await new Promise(resolve => setTimeout(resolve, 500));
    
    if (mockUsers.some(u => u.email === data.email)) {
      return { success: false, error: 'Email already registered' };
    }
    
    const newUser: User = {
      id: crypto.randomUUID(),
      email: data.email,
      username: data.username,
      role: 'REGISTERED',
      currentPackage: data.selectedPackage,
      createdAt: new Date().toISOString(),
      authProvider: 'LOCAL'
    };
    
    mockUsers.push(newUser);
    localStorage.setItem('token', `mock-token-${newUser.id}`);
    authStore.setUser(newUser);
    toastStore.success('Account created successfully!');
    return { success: true, data: newUser };
  },

  async loginWithGoogle(): Promise<ApiResponse<User>> {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const googleUser: User = {
      id: crypto.randomUUID(),
      email: 'google.user@gmail.com',
      username: 'Google User',
      role: 'REGISTERED',
      currentPackage: 'FREE',
      createdAt: new Date().toISOString(),
      avatarUrl: 'https://ui-avatars.com/api/?name=Google+User&background=EA4335&color=fff',
      authProvider: 'GOOGLE'
    };
    
    localStorage.setItem('token', `mock-token-google`);
    authStore.setUser(googleUser);
    toastStore.success('Signed in with Google!');
    return { success: true, data: googleUser };
  },

  async loginWithGithub(): Promise<ApiResponse<User>> {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const githubUser: User = {
      id: crypto.randomUUID(),
      email: 'github.user@github.com',
      username: 'GitHub User',
      role: 'REGISTERED',
      currentPackage: 'FREE',
      createdAt: new Date().toISOString(),
      avatarUrl: 'https://ui-avatars.com/api/?name=GitHub+User&background=24292E&color=fff',
      authProvider: 'GITHUB'
    };
    
    localStorage.setItem('token', `mock-token-github`);
    authStore.setUser(githubUser);
    toastStore.success('Signed in with GitHub!');
    return { success: true, data: githubUser };
  },

  async logout(): Promise<void> {
    authStore.logout();
    toastStore.info('You have been logged out');
  },

  async getUsage(): Promise<ApiResponse<PackageUsage>> {
    await new Promise(resolve => setTimeout(resolve, 200));
    
    const usage: PackageUsage = {
      uploadedToday: Math.floor(Math.random() * 5),
      totalStoredPhotos: Math.floor(Math.random() * 100) + 10,
      totalStorageMB: Math.floor(Math.random() * 500) + 50
    };
    
    usageStore.setUsage(usage);
    return { success: true, data: usage };
  },

  async changePackage(newPackage: PackageType): Promise<ApiResponse<User>> {
    await new Promise(resolve => setTimeout(resolve, 500));
    
    const user = authStore.user;
    if (!user) {
      return { success: false, error: 'Not authenticated' };
    }
    
    const updatedUser: User = {
      ...user,
      pendingPackage: newPackage,
      packageChangeDate: new Date(Date.now() + 86400000).toISOString()
    };
    
    authStore.setUser(updatedUser);
    toastStore.success(`Package will change to ${newPackage} tomorrow`);
    return { success: true, data: updatedUser };
  }
};

// Photo Service
export const photoService = {
  async getPhotos(
    page: number = 1,
    pageSize: number = 10,
    filter?: PhotoFilter
  ): Promise<ApiResponse<PaginatedResponse<Photo>>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    let filtered = [...mockPhotos];
    
    if (filter) {
      if (filter.hashtags && filter.hashtags.length > 0) {
        filtered = filtered.filter(p => 
          filter.hashtags!.some(h => p.hashtags.includes(h.toLowerCase()))
        );
      }
      if (filter.author) {
        filtered = filtered.filter(p => 
          p.authorUsername.toLowerCase().includes(filter.author!.toLowerCase())
        );
      }
      if (filter.sizeMin) {
        filtered = filtered.filter(p => p.sizeBytes >= filter.sizeMin! * 1024);
      }
      if (filter.sizeMax) {
        filtered = filtered.filter(p => p.sizeBytes <= filter.sizeMax! * 1024);
      }
      if (filter.uploadDateFrom) {
        filtered = filtered.filter(p => new Date(p.uploadedAt) >= new Date(filter.uploadDateFrom!));
      }
      if (filter.uploadDateTo) {
        filtered = filtered.filter(p => new Date(p.uploadedAt) <= new Date(filter.uploadDateTo!));
      }
    }
    
    const total = filtered.length;
    const start = (page - 1) * pageSize;
    const items = filtered.slice(start, start + pageSize);
    
    return {
      success: true,
      data: {
        items,
        total,
        page,
        pageSize,
        totalPages: Math.ceil(total / pageSize)
      }
    };
  },

  async getPhoto(id: string): Promise<ApiResponse<Photo>> {
    await new Promise(resolve => setTimeout(resolve, 200));
    
    const photo = mockPhotos.find(p => p.id === id);
    if (!photo) {
      return { success: false, error: 'Photo not found' };
    }
    
    return { success: true, data: photo };
  },

  async uploadPhoto(upload: PhotoUpload): Promise<ApiResponse<Photo>> {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const user = authStore.user;
    if (!user) {
      return { success: false, error: 'Not authenticated' };
    }
    
    const newPhoto: Photo = {
      id: crypto.randomUUID(),
      filename: `${crypto.randomUUID()}.${upload.processingOptions.format?.toLowerCase() || 'jpg'}`,
      originalFilename: upload.file.name,
      description: upload.description,
      hashtags: upload.hashtags.map(h => h.toLowerCase().replace('#', '')),
      authorId: user.id,
      authorUsername: user.username,
      uploadedAt: new Date().toISOString(),
      sizeBytes: upload.file.size,
      width: upload.processingOptions.resize?.width || 1920,
      height: upload.processingOptions.resize?.height || 1080,
      format: upload.processingOptions.format || 'JPG',
      thumbnailUrl: `https://picsum.photos/seed/${Date.now()}/300/200`,
      fullUrl: `https://picsum.photos/seed/${Date.now()}/1920/1080`
    };
    
    mockPhotos.unshift(newPhoto);
    usageStore.incrementUploads(upload.file.size / (1024 * 1024));
    toastStore.success('Photo uploaded successfully!');
    return { success: true, data: newPhoto };
  },

  async updatePhoto(id: string, description: string, hashtags: string[]): Promise<ApiResponse<Photo>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    const index = mockPhotos.findIndex(p => p.id === id);
    if (index === -1) {
      return { success: false, error: 'Photo not found' };
    }
    
    mockPhotos[index] = {
      ...mockPhotos[index],
      description,
      hashtags: hashtags.map(h => h.toLowerCase().replace('#', ''))
    };
    
    toastStore.success('Photo updated successfully!');
    return { success: true, data: mockPhotos[index] };
  },

  async deletePhoto(id: string): Promise<ApiResponse<void>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    const index = mockPhotos.findIndex(p => p.id === id);
    if (index === -1) {
      return { success: false, error: 'Photo not found' };
    }
    
    mockPhotos.splice(index, 1);
    toastStore.success('Photo deleted successfully!');
    return { success: true };
  },

  async downloadPhoto(id: string, options?: DownloadOptions): Promise<ApiResponse<Blob>> {
    await new Promise(resolve => setTimeout(resolve, 500));
    
    const photo = mockPhotos.find(p => p.id === id);
    if (!photo) {
      return { success: false, error: 'Photo not found' };
    }
    
    // In a real app, this would process and return the actual image
    // For demo, we'll fetch the image from the URL
    try {
      const response = await fetch(photo.fullUrl);
      const blob = await response.blob();
      
      // Trigger download
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${photo.originalFilename}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      
      toastStore.success('Download started!');
      return { success: true, data: blob };
    } catch {
      return { success: false, error: 'Failed to download photo' };
    }
  }
};

// Admin Service
export const adminService = {
  async getUsers(): Promise<ApiResponse<User[]>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    return { success: true, data: mockUsers };
  },

  async updateUser(id: string, updates: Partial<User>): Promise<ApiResponse<User>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    const index = mockUsers.findIndex(u => u.id === id);
    if (index === -1) {
      return { success: false, error: 'User not found' };
    }
    
    mockUsers[index] = { ...mockUsers[index], ...updates };
    toastStore.success('User updated successfully!');
    return { success: true, data: mockUsers[index] };
  },

  async getUserStats(): Promise<ApiResponse<UserStats[]>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    const stats: UserStats[] = mockUsers.map(u => ({
      userId: u.id,
      username: u.username,
      totalPhotos: mockPhotos.filter(p => p.authorId === u.id).length,
      totalActions: mockActionLogs.filter(l => l.userId === u.id).length,
      lastActive: new Date().toISOString(),
      storageUsedMB: Math.floor(Math.random() * 500)
    }));
    
    return { success: true, data: stats };
  },

  async getActionLogs(
    page: number = 1,
    pageSize: number = 20,
    userId?: string
  ): Promise<ApiResponse<PaginatedResponse<ActionLog>>> {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    let filtered = userId 
      ? mockActionLogs.filter(l => l.userId === userId)
      : mockActionLogs;
    
    const total = filtered.length;
    const start = (page - 1) * pageSize;
    const items = filtered.slice(start, start + pageSize);
    
    return {
      success: true,
      data: {
        items,
        total,
        page,
        pageSize,
        totalPages: Math.ceil(total / pageSize)
      }
    };
  },

  async deleteUserPhoto(photoId: string): Promise<ApiResponse<void>> {
    return photoService.deletePhoto(photoId);
  }
};
