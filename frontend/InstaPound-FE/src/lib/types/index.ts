// User types
export type UserRole = 'ANONYMOUS' | 'REGISTERED' | 'ADMIN';
export type PackageType = 'FREE' | 'PRO' | 'GOLD';
export type AuthProvider = 'LOCAL' | 'GOOGLE' | 'GITHUB';

export interface PackageUsage {
	currentPhotoCount: number;
	todayUploadCount: number;
	lastUploadDate: string | null;
	totalStorageUsed: number;
}

export interface PackageLimits {
	maxUploadSizeInBytes: number;
	dailyUploadLimit: number;
	maxTotalPhotos: number;
	price: number;
}

export interface User {
	id: number;
	username: string;
	email: string;
	role: UserRole;
	packageType: PackageType;
	packageUsage: PackageUsage | null;
	pendingPackageType: PackageType | null;
	packageChangeEffectiveDate: string | null;
	createdAt: string;
	lastLoginAt: string | null;
}

export interface PackageInfo {
	packageType: PackageType;
	name: string;
	description: string;
	limits: PackageLimits;
	currentUsage: PackageUsage | null;
	pendingPackageType: PackageType | null;
	packageChangeEffectiveDate: string | null;
	canChangeToday: boolean;
}

// Photo types
export type ImageFormat = 'JPEG' | 'PNG' | 'BMP';
export type ImageFilter = 'NONE' | 'GRAYSCALE' | 'SEPIA' | 'INVERT' | 'BLUR' | 'SHARPEN' | 'VINTAGE';
export type StorageType = 'LOCAL' | 'CLOUDINARY';

export interface Photo {
	id: number;
	originalFileName: string;
	description: string | null;
	hashtags: string[];
	thumbnailUrl: string;
	imageUrl: string;
	processedUrl: string | null;
	author: string;
	authorId: number;
	fileSizeBytes: number;
	width: number;
	height: number;
	uploadedAt: string;
	editedAt: string | null;
	downloadCount: number;
	viewCount: number;
}

export interface PhotoSearchRequest {
	hashtags?: string[];
	author?: string;
	authorId?: number;
	uploadedAfter?: string;
	uploadedBefore?: string;
	minSizeBytes?: number;
	maxSizeBytes?: number;
	page?: number;
	pageSize?: number;
}

export interface PhotoEditRequest {
	description?: string;
	hashtags?: string[];
}

// Pagination
export interface Page<T> {
	content: T[];
	totalPages: number;
	totalElements: number;
	size: number;
	number: number;
	first: boolean;
	last: boolean;
	empty: boolean;
}

// Action log types
export type ActionType =
	| 'USER_REGISTER'
	| 'USER_LOGIN'
	| 'USER_LOGOUT'
	| 'USER_SEARCH'
	| 'PROFILE_UPDATE'
	| 'PACKAGE_CHANGE'
	| 'PHOTO_UPLOAD'
	| 'PHOTO_EDIT'
	| 'PHOTO_DELETE'
	| 'PHOTO_DOWNLOAD'
	| 'PHOTO_VIEW';

export interface ActionLog {
	id: number;
	userId: number | null;
	username: string | null;
	actionType: ActionType;
	description: string;
	ipAddress: string;
	targetPhotoId: number | null;
	targetUserId: number | null;
	timestamp: string;
}

// Auth request/response
export interface LoginRequest {
	username: string;
	password: string;
}

export interface RegisterRequest {
	username: string;
	email: string;
	password: string;
	packageType: PackageType;
}

// Admin types
export interface AdminUserUpdate {
	username?: string;
	email?: string;
	role?: UserRole;
	packageType?: PackageType;
}

export interface UserStatistics {
	userId: number;
	username: string;
	photoCount: number;
	packageUsage: PackageUsage;
	actionStatistics: Record<ActionType, number>;
	createdAt: string;
	lastLoginAt: string | null;
}
