import type {
    User,
    Photo,
    Page,
    PackageInfo,
    PhotoSearchRequest,
    PhotoEditRequest,
    AdminUserUpdate,
    ActionLog,
    PackageType,
    ImageFormat,
    ImageFilter,
    UserStatistics,
    StorageType, RegisterRequest
} from "$lib/types";

const API_BASE = '/api';

class ApiError extends Error {
    constructor(
        public status: number,
        message: string
    ) {
        super(message);
        this.name = 'ApiError';
    }
}

async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        const text = await response.text();
        throw new ApiError(response.status, text || response.statusText);
    }
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }
    return response.text() as unknown as T;
}

async function get<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        credentials: 'include'
    });
    return handleResponse<T>(response);
}

async function post<T>(endpoint: string, data?: unknown): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: data ? JSON.stringify(data) : undefined,
        credentials: 'include'
    });
    return handleResponse<T>(response);
}

async function put<T>(endpoint: string, data: unknown): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        credentials: 'include'
    });
    return handleResponse<T>(response);
}

async function del<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'DELETE',
        credentials: 'include'
    });
    return handleResponse<T>(response);
}

export const authApi = {
    async register(data: RegisterRequest): Promise<User> {
        return post<User>('/auth/register', data);
    },
    async login(username: string, password: string): Promise<void> {
        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData,
            credentials: 'include'
        });
        if (!response.ok) {
            throw new ApiError(response.status, 'Invalid credentials');
        }
    },
    async logout(): Promise<void> {
        const response = await fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });
        if (!response.ok) {
            throw new ApiError(response.status, 'Logout failed');
        }
    },
    async checkUsername(username: string): Promise<{ available: boolean }> {
        return get<{ available: boolean }>(`/auth/check-username/${encodeURIComponent(username)}`);
    },
    async checkEmail(email: string): Promise<{ available: boolean }> {
        return get<{ available: boolean }>(`/auth/check-email/${encodeURIComponent(email)}`);
    },
    getOAuthUrl(provider: 'google' | 'github'): string {
        return `/oauth2/authorization/${provider}`;
    }
};

export const userApi = {
    async getProfile(): Promise<User> {
        return get<User>('/user/profile');
    },
    async getPackage(): Promise<PackageInfo> {
        return get<PackageInfo>('/user/package');
    },
    async getAllPackages(): Promise<PackageInfo[]> {
        return get<PackageInfo[]>('/user/packages');
    },
    async requestPackageChange(newPackage: PackageType): Promise<string> {
        return post<string>('/user/package/change', { newPackage });
    },
    async cancelPackageChange(): Promise<string> {
        return del<string>('/user/package/change');
    }
};

export const photosApi = {
    async browse(page = 0, size = 15): Promise<Page<Photo>> {
        return get<Page<Photo>>(`/photos/browse?page=${page}&size=${size}`);
    },
    async search(request: PhotoSearchRequest): Promise<Page<Photo>> {
        return post<Page<Photo>>('/photos/search', request);
    },
    async getById(id: number): Promise<Photo> {
        return get<Photo>(`/photos/${id}`);
    },
    async getByUser(userId: number, page = 0, size = 15): Promise<Page<Photo>> {
        return get<Page<Photo>>(`/photos/user/${userId}?page=${page}&size=${size}`);
    },
    async upload(
        file: File,
        options: {
            description?: string;
            hashtags?: string[];
            storageType?: StorageType;
            format?: ImageFormat;
            width?: number;
            height?: number;
        }
    ): Promise<Photo> {
        const formData = new FormData();
        formData.append('file', file);
        if (options.description) formData.append('description', options.description);
        if (options.hashtags && options.hashtags.length > 0) {
            options.hashtags.forEach((tag) => formData.append('hashtags', tag));
        }
        if (options.storageType) formData.append('storageType', options.storageType);
        if (options.format) formData.append('format', options.format);
        if (options.width) formData.append('width', options.width.toString());
        if (options.height) formData.append('height', options.height.toString());
        const response = await fetch(`${API_BASE}/photos/upload`, {
            method: 'POST',
            body: formData,
            credentials: 'include'
        });
        return handleResponse<Photo>(response);
    },
    async edit(id: number, data: PhotoEditRequest): Promise<Photo> {
        return put<Photo>(`/photos/${id}`, data);
    },
    async delete(id: number): Promise<string> {
        return del<string>(`/photos/${id}`);
    },
    async download(
        id: number,
        options: {
            original?: boolean;
            format?: ImageFormat;
            width?: number;
            height?: number;
            filters?: ImageFilter[];
        } = { original: true }
    ): Promise<Blob> {
        const params = new URLSearchParams();
        params.append('original', String(options.original ?? true));
        if (options.format) params.append('format', options.format);
        if (options.width) params.append('width', options.width.toString());
        if (options.height) params.append('height', options.height.toString());
        if (options.filters && options.filters.length > 0) {
            options.filters.forEach((f) => params.append('filters', f));
        }
        const response = await fetch(`${API_BASE}/photos/${id}/download?${params.toString()}`, {
            credentials: 'include'
        });
        if (!response.ok) {
            throw new ApiError(response.status, 'Download failed');
        }
        return response.blob();
    },
    getDownloadUrl(
        id: number,
        options: {
            original?: boolean;
            format?: ImageFormat;
            width?: number;
            height?: number;
            filters?: ImageFilter[];
        } = { original: true }
    ): string {
        const params = new URLSearchParams();
        params.append('original', String(options.original ?? true));
        if (options.format) params.append('format', options.format);
        if (options.width) params.append('width', options.width.toString());
        if (options.height) params.append('height', options.height.toString());
        if (options.filters && options.filters.length > 0) {
            options.filters.forEach((f) => params.append('filters', f));
        }
        return `${API_BASE}/photos/${id}/download?${params.toString()}`;
    }
};

export const adminApi = {
    async getAllUsers(page = 0, size = 10): Promise<Page<User>> {
        return get<Page<User>>(`/admin/users?page=${page}&size=${size}`);
    },
    async getUserById(id: number): Promise<User> {
        return get<User>(`/admin/users/${id}`);
    },
    async updateUser(id: number, data: AdminUserUpdate): Promise<User> {
        return put<User>(`/admin/users/${id}`, data);
    },
    async deleteUser(id: number): Promise<string> {
        return del<string>(`/admin/users/${id}`);
    },
    async getUserStatistics(id: number): Promise<UserStatistics> {
        return get<UserStatistics>(`/admin/users/${id}/statistics`);
    },
    async getAllLogs(page = 0, size = 20): Promise<Page<ActionLog>> {
        return get<Page<ActionLog>>(`/admin/logs?page=${page}&size=${size}`);
    },
    async getLogsForUser(userId: number, page = 0, size = 20): Promise<Page<ActionLog>> {
        return get<Page<ActionLog>>(`/admin/logs/user/${userId}?page=${page}&size=${size}`);
    },
    async getAllPackages(): Promise<PackageInfo[]> {
        return get<PackageInfo[]>('/admin/packages');
    },
    async getAllPhotos(page = 0, size = 15): Promise<Page<Photo>> {
        return get<Page<Photo>>(`/admin/photos?page=${page}&size=${size}`);
    },
    async deletePhoto(id: number): Promise<string> {
        return del<string>(`/admin/photos/${id}`);
    }
};

export { ApiError };