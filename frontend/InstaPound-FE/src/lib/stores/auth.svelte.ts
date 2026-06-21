import type { User } from '$lib/types';
import { userApi, authApi, ApiError } from "$lib/api";

function createAuthStore() {
    let user = $state<User | null>(null);
    let loading = $state(true);
    let error = $state<string | null>(null);

    async function checkAuth() {
        loading = true;
        error = null;
        try {
            user = await userApi.getProfile();
        } catch (e) {
            user = null;
            if (e instanceof ApiError && e.status !== 401) {
                error = e.message;
            }
        } finally {
            loading = false;
        }
    }

    async function login(username: string, password: string) {
        loading = true;
        error = null;
        try {
            await authApi.login(username, password);
            user = await userApi.getProfile();
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Login failed';
            throw e;
        } finally {
            loading = false;
        }
    }

    async function register(username: string, email: string, password: string, packageType: 'FREE' | 'PRO' | 'GOLD') {
        loading = true;
        error = null;
        try {
            await authApi.register({username, email, password, packageType});
            await authApi.login(username, password);
            user = await userApi.getProfile();
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Registration failed';
            throw e;
        } finally {
            loading = false;
        }
    }

    async function logout() {
        try {
            await authApi.logout();
        } catch {

        }
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        user = null;
    }

    function refresh() {
        return checkAuth();
    }

    return {
        get user() {
            return user;
        },
        get loading() {
            return loading;
        },
        get error() {
            return error;
        },
        get isAuthenticated() {
            return user !== null;
        },
        get isAdmin() {
            return user?.role === 'ADMIN';
        },
        checkAuth,
        login,
        register,
        logout,
        refresh
    };
}

export const authStore = createAuthStore();