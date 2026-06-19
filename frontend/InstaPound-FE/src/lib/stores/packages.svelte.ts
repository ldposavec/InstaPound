import type { PackageInfo, PackageType } from "$lib/types";
import { userApi, ApiError } from "$lib/api";

function createPackageStore() {
    let packages = $state<PackageInfo[]>([]);
    let currentPackage = $state<PackageInfo | null>(null);
    let loading = $state(true);
    let error = $state<string | null>(null);

    async function loadPackages() {
        loading = true;
        error = null;
        try {
            packages = await userApi.getAllPackages();
        } catch (e)  {
            error = e instanceof ApiError ? e.message : 'Failed to load packages';
        } finally {
            loading = false;
        }
    }

    async function loadCurrentPackage() {
        loading = true;
        error = null;
        try {
            currentPackage = await userApi.getPackage();
        } catch (e)  {
            error = e instanceof ApiError ? e.message : 'Failed to load current package';
        } finally {
            loading = false;
        }
    }

    async function changePackage(newPackage: PackageType) {
        loading = true;
        error = null;
        try {
            await userApi.requestPackageChange(newPackage);
            await loadCurrentPackage();
        } catch (e)  {
            error = e instanceof ApiError ? e.message : 'Failed to change package';
            throw e;
        } finally {
            loading = false;
        }
    }

    async function cancelChange() {
        loading = true;
        error = null;
        try {
            await userApi.cancelPackageChange();
            await loadCurrentPackage();
        } catch (e)  {
            error = e instanceof ApiError ? e.message : 'Failed to cancel package';
            throw e;
        } finally {
            loading = false;
        }
    }

    return {
        get packages() {
            return packages;
        },
        get currentPackage() {
            return currentPackage;
        },
        get loading() {
            return loading;
        },
        get error() {
            return error;
        },
        loadPackages,
        loadCurrentPackage,
        changePackage,
        cancelChange
    };
}

export const packageStore = createPackageStore();