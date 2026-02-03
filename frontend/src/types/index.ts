// User types
export type UserRole = 'ANONYMOUS' | 'REGISTERED' | 'ADMIN';
export type PackageType = 'FREE' | 'PRO' | 'GOLD';

export interface PackageLimits {
  maxUploadSizeMB: number;
  dailyUploadLimit: number;
  maxStoredPhotos: number;
  price: number;
}

export const PACKAGE_LIMITS: Record<PackageType, PackageLimits> = {
  FREE: {
    maxUploadSizeMB: 5,
    dailyUploadLimit: 5,
    maxStoredPhotos: 50,
    price: 0
  },
  PRO: {
    maxUploadSizeMB: 25,
    dailyUploadLimit: 25,
    maxStoredPhotos: 500,
    price: 9.99
  },
  GOLD: {
    maxUploadSizeMB: 100,
    dailyUploadLimit: 100,
    maxStoredPhotos: 5000,
    price: 29.99
  }
};

export interface User {
  id: string;
  email: string;
  username: string;
  role: UserRole;
  currentPackage: PackageType;
  pendingPackage?: PackageType;
  packageChangeDate?: string;
  createdAt: string;
  avatarUrl?: string;
  authProvider: 'LOCAL' | 'GOOGLE' | 'GITHUB';
}

export interface PackageUsage {
  uploadedToday: number;
  totalStoredPhotos: number;
  totalStorageMB: number;
}

// Photo types
export type ImageFormat = 'PNG' | 'JPG' | 'BMP' | 'WEBP';

export interface ProcessingOptions {
  resize?: {
    width: number;
    height: number;
  };
  format?: ImageFormat;
}

export interface DownloadOptions extends ProcessingOptions {
  applySepia?: boolean;
  applyBlur?: number;
  applyGrayscale?: boolean;
}

export interface Photo {
  id: string;
  filename: string;
  originalFilename: string;
  description: string;
  hashtags: string[];
  authorId: string;
  authorUsername: string;
  uploadedAt: string;
  sizeBytes: number;
  width: number;
  height: number;
  format: ImageFormat;
  thumbnailUrl: string;
  fullUrl: string;
}

export interface PhotoUpload {
  file: File;
  description: string;
  hashtags: string[];
  processingOptions: ProcessingOptions;
}

// Search and filtering
export interface PhotoFilter {
  hashtags?: string[];
  sizeMin?: number;
  sizeMax?: number;
  uploadDateFrom?: string;
  uploadDateTo?: string;
  author?: string;
}

// Action logging
export interface ActionLog {
  id: string;
  userId: string;
  username: string;
  action: string;
  details: string;
  timestamp: string;
  ipAddress?: string;
}

// Auth
export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  email: string;
  username: string;
  password: string;
  confirmPassword: string;
  selectedPackage: PackageType;
}

// API Response types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// Admin types
export interface UserStats {
  userId: string;
  username: string;
  totalPhotos: number;
  totalActions: number;
  lastActive: string;
  storageUsedMB: number;
}
